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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.DatasetPreProcessingException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.interfaces.MLModelBuilder;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.algorithms.SparkModelUtils;
import org.wso2.carbon.ml.core.spark.models.MLMatrixFactorizationModel;
import org.wso2.carbon.ml.core.spark.transformations.DoubleArrayToVector;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.database.DatabaseService;

import java.util.Map;

public class RecommendationModelBuilder extends MLModelBuilder{
	private static final Log log = LogFactory.getLog(RecommendationModelBuilder.class);

	public RecommendationModelBuilder(MLModelConfigurationContext context) {
		super(context);
	}

	@Override
	public MLModel build() throws MLModelBuilderException {
		MLModelConfigurationContext context = getContext();
		DatabaseService databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
		try {
			Workflow workflow = context.getFacts();
			long modelId = context.getModelId();

			// apply pre processing
			JavaRDD<Rating> trainingData = RecommendationUtils.preProcess(context);

			MLModel mlModel = new MLModel();
			mlModel.setAlgorithmName(workflow.getAlgorithmName());
			mlModel.setAlgorithmClass(workflow.getAlgorithmClass());
			mlModel.setFeatures(workflow.getFeatures());
			mlModel.setResponseVariable(workflow.getResponseVariable());
			mlModel.setEncodings(context.getEncodings());
			mlModel.setNewToOldIndicesList(context.getNewToOldIndicesList());
			mlModel.setResponseIndex(-1);

			MatrixFactorizationModel model;

			// build a recommendation model according to user selected algorithm
			MLConstants.RECOMMENDATION_ALGORITHM
					recommendation_algorithm = MLConstants.RECOMMENDATION_ALGORITHM.valueOf(workflow.getAlgorithmName());
			switch (recommendation_algorithm) {
				case COLLABORATIVE_FILTERING:
					model = buildCollaborativeFilteringModel(trainingData, workflow, mlModel);
					break;
				default:
					throw new AlgorithmNameException("Incorrect algorithm name: " + workflow.getAlgorithmName()
					                                 + " for model id: " + modelId);
			}
			Rating[] recommendedProducts = new CollaborativeFiltering().recommendProducts(model,1,RecommendationConstants.DEFAULT_NUMBER_OF_ITEMS);
			for (Rating recommendedProduct : recommendedProducts) {
				System.out.println(recommendedProduct.user() + " " + recommendedProduct.product());
			}
			return mlModel;
		} catch (Exception e) {
			throw new MLModelBuilderException("An error occurred while building unsupervised machine learning model: "
			                                  + e.getMessage(), e);
		}
	}

	private MatrixFactorizationModel buildCollaborativeFilteringModel(JavaRDD<Rating> trainingData, Workflow workflow,
	                                                                  MLModel mlModel) throws MLModelBuilderException {

		try {
			Map<String, String> parameters = workflow.getHyperParameters();
			CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering();
			MatrixFactorizationModel model = collaborativeFiltering
					.trainExplicit(trainingData, Integer.parseInt(parameters.get(MLConstants.RANK)),
					               Integer.parseInt(parameters.get(MLConstants.NUM_ITERATIONS)),
					               Double.parseDouble(parameters.get(MLConstants.LAMBDA)),
					               Integer.parseInt(parameters.get(MLConstants.NUM_BLOCKS)));

			mlModel.setModel(new MLMatrixFactorizationModel(model));
			return model;
		} catch(Exception e) {
			throw new MLModelBuilderException("An error occurred while building recommendation model: " + e.getMessage(), e);
		}
	}
}
