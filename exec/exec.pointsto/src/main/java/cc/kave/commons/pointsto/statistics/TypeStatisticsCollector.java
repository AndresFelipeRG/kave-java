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
package cc.kave.commons.pointsto.statistics;

import static com.google.common.io.Files.getNameWithoutExtension;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.pointsto.io.IOHelper;
import cc.recommenders.names.CoReNames;
import cc.recommenders.names.ICoReTypeName;
import cc.recommenders.usages.Usage;

public class TypeStatisticsCollector implements UsageStatisticsCollector {

	private static final char SEPARATOR = ' ';

	private final Predicate<Usage> usageFilter;

	private Map<ICoReTypeName, Statistics> typeStatistics = new HashMap<>();
	private long numPrunedUsages = 0;

	public TypeStatisticsCollector(Predicate<Usage> usageFilter) {
		this.usageFilter = usageFilter;
	}

	@Override
	public UsageStatisticsCollector create() {
		return new TypeStatisticsCollector(usageFilter);
	}

	@Override
	public void merge(UsageStatisticsCollector other) {
		TypeStatisticsCollector otherTypeCollector = (TypeStatisticsCollector) other;

		synchronized (typeStatistics) {
			for (Map.Entry<ICoReTypeName, Statistics> entry : otherTypeCollector.typeStatistics.entrySet()) {
				Statistics otherStats = entry.getValue();
				Statistics myStats = typeStatistics.get(entry.getKey());
				if (myStats == null) {
					myStats = new Statistics();
					typeStatistics.put(entry.getKey(), myStats);
				}

				myStats.numUsages += otherStats.numUsages;
				myStats.numFilteredUsages += otherStats.numFilteredUsages;
				myStats.sumCallsites += otherStats.sumCallsites;
				myStats.sumFilteredCallsites += otherStats.sumFilteredCallsites;
			}

			numPrunedUsages += otherTypeCollector.numPrunedUsages;
		}
	}

	@Override
	public void onProcessContext(Context context) {

	}

	@Override
	public void onEntryPointUsagesExtracted(IMethodDeclaration entryPoint, List<? extends Usage> usages) {
		process(usages);
	}

	@Override
	public void process(List<? extends Usage> usages) {
		for (Usage usage : usages) {
			Statistics stats = typeStatistics.get(usage.getType());
			if (stats == null) {
				stats = new Statistics();
				typeStatistics.put(usage.getType(), stats);
			}

			++stats.numUsages;
			stats.sumCallsites += usage.getAllCallsites().size();

			if (usageFilter.test(usage)) {
				++stats.numFilteredUsages;
				stats.sumFilteredCallsites += usage.getAllCallsites().size();
			}
		}
	}

	@Override
	public void onUsagesPruned(int numPrunedUsages) {
		this.numPrunedUsages += numPrunedUsages;
	}

	@Override
	public void output(Path file) throws IOException {
		List<Map.Entry<ICoReTypeName, Statistics>> entries = new ArrayList<>(typeStatistics.entrySet());
		entries.sort(new Comparator<Map.Entry<ICoReTypeName, Statistics>>() {

			@Override
			public int compare(Entry<ICoReTypeName, Statistics> o1, Entry<ICoReTypeName, Statistics> o2) {
				int diff = o2.getValue().numUsages - o1.getValue().numUsages;

				if (diff == 0) {
					return o1.getKey().getIdentifier().compareTo(o2.getKey().getIdentifier());
				}

				return diff;
			}
		});

		IOHelper.createParentDirs(file);
		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			for (Map.Entry<ICoReTypeName, Statistics> entry : entries) {
				writer.append(CoReNames.vm2srcQualifiedType(entry.getKey()));
				writer.append(SEPARATOR);

				Statistics stats = entry.getValue();
				writer.append(Integer.toString(stats.numUsages));
				writer.append(SEPARATOR);
				writer.append(Integer.toString(stats.numFilteredUsages));
				writer.append(SEPARATOR);
				writer.append(String.format(Locale.US, "%.1f", calcAverage(stats.sumCallsites, stats.numUsages)));
				writer.append(SEPARATOR);
				writer.append(String.format(Locale.US, "%.1f",
						calcAverage(stats.sumFilteredCallsites, stats.numFilteredUsages)));
				writer.newLine();
			}
		}

		Path miscFile = file.getParent().resolve(getNameWithoutExtension(file.getFileName().toString()) + ".misc");
		Files.write(miscFile, Arrays.asList("pruned Usages: " + numPrunedUsages));
	}

	private double calcAverage(long value, int size) {
		if (size == 0) {
			return 0;
		} else {
			return value / (double) size;
		}
	}

	private static class Statistics {
		int numUsages;
		int numFilteredUsages;
		long sumCallsites;
		long sumFilteredCallsites;
	}

}
