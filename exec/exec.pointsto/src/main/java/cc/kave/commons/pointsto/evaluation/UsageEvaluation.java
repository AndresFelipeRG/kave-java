/**
 * Copyright 2016 Simon Reuß
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package cc.kave.commons.pointsto.evaluation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import cc.kave.commons.pointsto.evaluation.annotations.NumberOfCVFolds;
import cc.kave.commons.pointsto.evaluation.annotations.UsageFilter;
import cc.kave.commons.pointsto.evaluation.cv.CVEvaluator;
import cc.kave.commons.pointsto.evaluation.cv.CrossValidationFoldBuilder;
import cc.kave.commons.pointsto.evaluation.cv.SetProvider;
import cc.kave.commons.pointsto.evaluation.cv.UsageSetProvider;
import cc.kave.commons.pointsto.stores.ProjectIdentifier;
import cc.kave.commons.pointsto.stores.ProjectUsageStore;
import cc.recommenders.names.CoReNames;
import cc.recommenders.names.ICoReTypeName;
import cc.recommenders.usages.Usage;

public class UsageEvaluation extends AbstractEvaluation {

	private static final boolean HIDE_UNIQUE_TYPES = true;

	private int numFolds;

	private Predicate<Usage> usageFilter;
	private CrossValidationFoldBuilder foldBuilder;
	private CVEvaluator cvEvaluator;

	private long skippedNumProjects;
	private long skippedUsageFilter;
	private Map<ICoReTypeName, Double> results = new HashMap<>();

	@Inject
	public UsageEvaluation(@NumberOfCVFolds int numFolds, @UsageFilter Predicate<Usage> usageFilter,
			CrossValidationFoldBuilder foldBuilder, CVEvaluator cvEvaluator) {
		this.numFolds = numFolds;
		this.usageFilter = usageFilter;
		this.foldBuilder = foldBuilder;
		this.cvEvaluator = cvEvaluator;
	}

	private void reset() {
		skippedNumProjects = 0;
		skippedUsageFilter = 0;
		results.clear();
	}

	public Map<ICoReTypeName, Double> getResults() {
		return Collections.unmodifiableMap(results);
	}

	public void run(Path storePath) throws IOException {
		reset();

		try (ProjectUsageStore usageStore = new ProjectUsageStore(storePath)) {
			// store types in a list and sort it alphabetically to get a consistent ordering in which types are
			// evaluated
			List<ICoReTypeName> types = new ArrayList<>(usageStore.getAllTypes());
			types.sort(new TypeNameComparator());

			int numProjects = usageStore.getNumberOfProjects();
			log("Loaded usage store containing %d projects and %d types\n", numProjects, types.size());

			for (ICoReTypeName type : types) {
				evaluateType(usageStore, type);
				usageStore.flush();
			}

			long totalTypesSkipped = skippedNumProjects + skippedUsageFilter;
			log("Skipped %d/%d (%.2f%%) types\n", totalTypesSkipped, types.size(),
					((double) totalTypesSkipped) / types.size() * 100);
			log("\t%d types due to an insufficient number of projects\n", skippedNumProjects);
			log("\t%d types due to an insufficient number of projects after filtering\n", skippedUsageFilter);
		}

	}

	private void evaluateType(ProjectUsageStore usageStore, ICoReTypeName type) throws IOException {
		int numProjects = usageStore.getNumberOfProjects();
		Set<ProjectIdentifier> projects = usageStore.getProjects(type);
		int numProjectsWithType = projects.size();

		if (numProjectsWithType <= 1 && HIDE_UNIQUE_TYPES) {
			++skippedNumProjects;
			return;
		}

		log("%s:\n", CoReNames.vm2srcQualifiedType(type));
		if (numProjectsWithType < numFolds) {
			log("\tSkipping because type is only used in %d projects\n", numProjectsWithType);
			++skippedNumProjects;
			return;
		}
		log("\tType is used in %d/%d (%.2f%%) projects\n", numProjectsWithType, numProjects,
				((double) numProjectsWithType) / numProjects * 100);

		Map<ProjectIdentifier, List<Usage>> projectUsages = usageStore.loadUsagesPerProject(type, usageFilter);
		// re-check whether enough projects with usages are available after filtering
		projectUsages.values().removeIf(usages -> usages.isEmpty());
		numProjectsWithType = projectUsages.size();
		if (numProjectsWithType < numFolds) {
			log("\tSkipping because type is only used in %d projects after filtering\n", numProjectsWithType);
			skippedUsageFilter++;
			return;
		}

		int numUsages = pruneUsages(projectUsages);
		log("\t%d usages in total\n", numUsages);

		List<List<Usage>> folds = foldBuilder.createFolds(projectUsages);
		SetProvider setProvider = new UsageSetProvider(folds);
		double score = cvEvaluator.evaluate(setProvider);
		results.put(type, score);
		log("\tF1: %.3f\n", score);
		log("\tFold size deviation: %.1f\n", setProvider.getAbsoluteFoldSizeDeviation());
	}

	private int pruneUsages(Map<ProjectIdentifier, List<Usage>> projectUsages) {
		int numUsages = projectUsages.values().stream().mapToInt(usages -> usages.size()).sum();
		if (numUsages <= MAX_USAGES) {
			return numUsages;
		}

		LinkedList<Pair<ProjectIdentifier, Integer>> projects = projectUsages.entrySet().stream()
				.map(entry -> ImmutablePair.of(entry.getKey(), entry.getValue().size()))
				.sorted(Comparator.comparingInt(Pair::getValue)).collect(Collectors.toCollection(Lists::newLinkedList));
		int numPrunedUsages = 0;
		while (numUsages > MAX_USAGES) {
			Pair<ProjectIdentifier, Integer> largestProject = projects.removeLast();
			projectUsages.remove(largestProject.getKey());
			numPrunedUsages += largestProject.getValue();
			numUsages -= largestProject.getValue();
		}
		log("\tPruned %d usages\n", numPrunedUsages);

		return numUsages;
	}

	private static final Injector INJECTOR = Guice.createInjector(new Module());

	public static UsageEvaluation run(Path storePath, Path exportFile) throws IOException {
		UsageEvaluation evaluation = INJECTOR.getInstance(UsageEvaluation.class);
		evaluation.run(storePath);

		if (exportFile != null) {
			INJECTOR.getInstance(ResultExporter.class).export(exportFile, evaluation.getResults());
		}

		return evaluation;
	}

	public static void shutdown() {
		INJECTOR.getInstance(ExecutorService.class).shutdown();
	}

	public static void main(String[] args) throws IOException {
		Locale.setDefault(Locale.US);
		Path storePath = Paths.get("E:\\Coding\\MT\\Usages\\UnificationAnalysis_FULL");
		Path exportFile = Paths.get("E:\\Coding\\MT\\EvaluationResults\\UnificationAnalysis_FULL.txt");
		run(storePath, exportFile);
		shutdown();
	}

}
