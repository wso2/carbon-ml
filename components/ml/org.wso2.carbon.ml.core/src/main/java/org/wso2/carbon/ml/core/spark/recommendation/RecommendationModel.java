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

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.core.exceptions.DatasetPreProcessingException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.database.DatabaseService;

public class RecommendationModel {

	public MLModel buildModel(MLModelConfigurationContext context) throws MLModelBuilderException {
		JavaSparkContext sparkContext = null;
		DatabaseService databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
		MLModel mlModel = new MLModel();

		sparkContext = context.getSparkContext();

		//pre-processing dataset
		JavaRDD<Rating> trainingData = null;
		try {
			trainingData = RecommendationUtils.preProcess(context);
		} catch (DatasetPreProcessingException e) {
			e.printStackTrace();
		}

		CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering();
		MatrixFactorizationModel model = collaborativeFiltering.trainExplicit(trainingData, 10, 20,RecommendationConstants.DEFAULT_LAMBDA, 10);
		Rating[] recommendedProducts = collaborativeFiltering.recommendProducts(model,1,RecommendationConstants.DEFAULT_NUMBER_OF_ITEMS);
		for (Rating recommendedProduct : recommendedProducts) {
			System.out.println(recommendedProduct.user() + " " + recommendedProduct.product());
		}
		return null;
	}
}
