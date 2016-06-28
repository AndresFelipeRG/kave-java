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
package cc.kave.commons.pointsto.evaluation.cv;

import static cc.kave.commons.pointsto.evaluation.Logger.log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cc.kave.commons.pointsto.evaluation.annotations.NumberOfCVFolds;
import cc.kave.commons.pointsto.evaluation.measures.AbstractMeasure;
import cc.recommenders.evaluation.queries.QueryBuilder;
import cc.recommenders.mining.calls.ICallsRecommender;
import cc.recommenders.mining.calls.pbn.PBNMiner;
import cc.recommenders.names.ICoReMethodName;
import cc.recommenders.usages.CallSite;
import cc.recommenders.usages.Query;
import cc.recommenders.usages.Usage;

public class CVEvaluator {

	private final int numFolds;
	private final Provider<PBNMiner> pbnMinerProvider;
	private final Provider<QueryBuilder<Usage, Query>> queryBuilderProvider;
	private final AbstractMeasure measure;
	private final ExecutorService executorService;

	@Inject
	public CVEvaluator(@NumberOfCVFolds int numFolds, Provider<PBNMiner> pbnMinerProvider,
			Provider<QueryBuilder<Usage, Query>> queryBuilderProvider, AbstractMeasure measure,
			ExecutorService executorService) {
		this.numFolds = numFolds;
		this.pbnMinerProvider = pbnMinerProvider;
		this.queryBuilderProvider = queryBuilderProvider;
		this.measure = measure;
		this.executorService = executorService;
	}

	public AbstractMeasure getMeasure() {
		return measure;
	}

	public double evaluate(SetProvider setProvider) {
		List<Future<Pair<Integer, Double>>> futures = new ArrayList<>(numFolds);
		double[] evaluationResults = new double[numFolds];

		for (int i = 0; i < numFolds; ++i) {
			futures.add(executorService.submit(new FoldEvaluation(i, setProvider)));
		}

		for (int i = 0; i < evaluationResults.length; ++i) {
			try {
				Pair<Integer, Double> result = futures.get(i).get();
				evaluationResults[i] = result.getValue();
				log("\tFold %d: %.3f\n", i + 1, evaluationResults[i]);
			} catch (ExecutionException e) {
				throw new RuntimeException(e.getCause());
			} catch (InterruptedException e) {
				e.printStackTrace();
				return Double.NaN;
			}

		}

		return StatUtils.mean(evaluationResults);
	}

	private static Set<ICoReMethodName> getExpectation(Usage validationUsage, Query q) {
		Set<CallSite> missingCallsites = new HashSet<>(validationUsage.getReceiverCallsites());
		missingCallsites.removeAll(q.getAllCallsites());

		Set<ICoReMethodName> expectation = new HashSet<>(missingCallsites.size());
		for (CallSite callsite : missingCallsites) {
			expectation.add(callsite.getMethod());
		}
		return expectation;
	}

	private class FoldEvaluation implements Callable<Pair<Integer, Double>> {

		private final int validationFoldIndex;
		private final SetProvider setProvider;

		public FoldEvaluation(int validationFoldIndex, SetProvider setProvider) {
			this.validationFoldIndex = validationFoldIndex;
			this.setProvider = setProvider;
		}

		@Override
		public Pair<Integer, Double> call() throws Exception {
			List<Usage> training = setProvider.getTrainingSet(validationFoldIndex);
			List<Usage> validation = setProvider.getValidationSet(validationFoldIndex);

			if (training.isEmpty() || validation.isEmpty()) {
				throw new EmptySetException();
			}

			PBNMiner pbnMiner = pbnMinerProvider.get();
			ICallsRecommender<Query> recommender = pbnMiner.createRecommender(training);
			DescriptiveStatistics statistics = new DescriptiveStatistics();

			for (Usage validationUsage : validation) {
				QueryBuilder<Usage, Query> queryBuilder = queryBuilderProvider.get();
				List<Query> queries;
				// QueryBuilder may not be thread safe due to their reliance on random number generators
				// (PartialUsageQueryBuilder uses the static Random in Collections.shuffle)
				synchronized (queryBuilder) {
					queries = queryBuilder.createQueries(validationUsage);
				}

				for (Query q : queries) {
					Set<ICoReMethodName> expectation = getExpectation(validationUsage, q);
					double score = measure.calculate(recommender, q, expectation);
					statistics.addValue(score);
				}
			}

			return ImmutablePair.of(validationFoldIndex, statistics.getMean());
		}

	}

}
