/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.ml.core.spark.recommendation;

import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.constants.MLConstants.RECOMMENDATION_ALGORITHM;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.interfaces.MLModelBuilder;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.models.MLMatrixFactorizationModel;
import org.wso2.carbon.ml.core.spark.summary.RecommendationModelSummary;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.database.DatabaseService;

import scala.Tuple2;

import java.util.Map;

/**
 * Builds a Matrix Factorization Model using a collaborative filtering algorithm (ALS) to generate personalized
 * recommendations.
 */

public class RecommendationModelBuilder extends MLModelBuilder {

	public RecommendationModelBuilder(MLModelConfigurationContext context) {
		super(context);
	}

	/**
	 * Build a model using the context.
	 *
	 * @return build {@link MLModel}
	 * @throws MLModelBuilderException if failed to build the model.
	 */
	@Override public MLModel build() throws MLModelBuilderException {
		MLModelConfigurationContext context = getContext();
		DatabaseService databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
		try {
			Workflow workflow = context.getFacts();
			long modelId = context.getModelId();
			MLModel mlModel = new MLModel();
			ModelSummary summaryModel;
			JavaRDD<Rating> trainingData;

			mlModel.setAlgorithmName(workflow.getAlgorithmName());
			mlModel.setAlgorithmClass(workflow.getAlgorithmClass());
			mlModel.setFeatures(workflow.getFeatures());

			// build a recommendation model according to user selected algorithm
			RECOMMENDATION_ALGORITHM recommendation_algorithm =
					RECOMMENDATION_ALGORITHM.valueOf(workflow.getAlgorithmName());
			switch (recommendation_algorithm) {
				case COLLABORATIVE_FILTERING:
					trainingData = RecommendationUtils.preProcess(context, false);
					summaryModel = buildCollaborativeFilteringModel(trainingData, workflow, mlModel, false);
					break;
				case COLLABORATIVE_FILTERING_IMPLICIT:
					trainingData = RecommendationUtils.preProcess(context, true);
					summaryModel = buildCollaborativeFilteringModel(trainingData, workflow, mlModel, true);
					break;
				default:
					throw new AlgorithmNameException(
							"Incorrect algorithm name: " + workflow.getAlgorithmName() + " for model id: " + modelId);
			}
			//persist model summary
			databaseService.updateModelSummary(modelId, summaryModel);
			return mlModel;
		} catch (Exception e) {
			throw new MLModelBuilderException(
					"An error occurred while building recommendation model: " + e.getMessage(), e);
		}
	}

	/**
	 * Builds a matrix factorization model using a collaborative filtering algorithm
	 * @param trainingData              training data
	 * @param workflow                  {@link Workflow}
	 * @param mlModel                   {@link MLModel}
	 * @param trainImplicit             train using implicit data
	 * @return                          {@link ModelSummary}
	 * @throws MLModelBuilderException  If failed to build the model
	 */
	private ModelSummary buildCollaborativeFilteringModel(JavaRDD<Rating> trainingData, Workflow workflow,
	                                                                  MLModel mlModel, boolean trainImplicit) throws MLModelBuilderException {

		try {
			Map<String, String> parameters = workflow.getHyperParameters();
			CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering();
			RecommendationModelSummary recommendationModelSummary = new RecommendationModelSummary();
			MatrixFactorizationModel model;
			if (trainImplicit) {
				model = collaborativeFiltering
						.trainImplicit(trainingData, Integer.parseInt(parameters.get(MLConstants.RANK)),
						               Integer.parseInt(parameters.get(MLConstants.ITERATIONS)),
						               Double.parseDouble(parameters.get(MLConstants.LAMBDA)),
						               Double.parseDouble(parameters.get(MLConstants.ALPHA)),
						               Integer.parseInt(parameters.get(MLConstants.BLOCKS)));
				recommendationModelSummary
						.setAlgorithm(RECOMMENDATION_ALGORITHM.COLLABORATIVE_FILTERING_IMPLICIT.toString());
			} else {
				model = collaborativeFiltering
						.trainExplicit(trainingData, Integer.parseInt(parameters.get(MLConstants.RANK)),
						               Integer.parseInt(parameters.get(MLConstants.ITERATIONS)),
						               Double.parseDouble(parameters.get(MLConstants.LAMBDA)),
						               Integer.parseInt(parameters.get(MLConstants.BLOCKS)));
				recommendationModelSummary.setAlgorithm(RECOMMENDATION_ALGORITHM.COLLABORATIVE_FILTERING.toString());
			}
			mlModel.setModel(new MLMatrixFactorizationModel(model));
			
			// Evaluate the model on rating data
			double meanSquaredError = collaborativeFiltering.test(model,
					trainingData).mean();
			recommendationModelSummary.setMeanSquaredError(meanSquaredError);

			return recommendationModelSummary;
		} catch (Exception e) {
			throw new MLModelBuilderException(
					"An error occurred while building recommendation model: " + e.getMessage(), e);
		}
	}
}
