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
import org.apache.spark.mllib.recommendation.ALS;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;

import java.io.Serializable;

public class CollaborativeFiltering implements Serializable{

	private static final long serialVersionUID = 5273514743795162923L;

	/**
	 * This method uses alternating least squares (ALS) algorithm to train a matrix factorization model given an JavaRDD
	 * of ratings given by users to some products.
	 *
	 * @param trainingDataset         Training dataset as a JavaRDD of Ratings
	 * @param rank                    Number of latent factors
	 * @param noOfIterations          Number of iterations
	 * @param regularizationParameter Regularization parameter
	 * @param noOfBlocks              Level of parallelism (auto configure = -1)
	 * @return Matrix factorization model
	 */
	public MatrixFactorizationModel trainExplicit(JavaRDD<Rating> trainingDataset, int rank, int noOfIterations,
	                                              double regularizationParameter, int noOfBlocks) {

		return ALS.train(trainingDataset.rdd(), rank, noOfIterations, regularizationParameter, noOfBlocks);
	}

	/**
	 * This method uses alternating least squares (ALS) algorithm to train a matrix factorization model given an JavaRDD
	 * of 'implicit preferences' given by users to some products.
	 *
	 * @param trainingDataset         Training dataset as a JavaRDD of Ratings
	 * @param rank                    Number of latent factors
	 * @param noOfIterations          Number of iterations
	 * @param regularizationParameter Regularization parameter
	 * @param confidenceParameter     Confidence parameter
	 * @param noOfBlocks              Level of parallelism (auto configure = -1)
	 * @return Matrix factorization model
	 */
	public MatrixFactorizationModel trainImplicit(JavaRDD<Rating> trainingDataset, int rank, int noOfIterations,
	                                              double regularizationParameter, double confidenceParameter,
	                                              int noOfBlocks) {

		return ALS.trainImplicit(trainingDataset.rdd(), rank, noOfIterations, regularizationParameter, noOfBlocks,
		                         confidenceParameter);
	}

	/**
	 * This method recommends products for a given user.
	 *
	 * @param model            Matrix factorization model
	 * @param userId           The user to recommend products to
	 * @param numberOfProducts Number of products to return
	 * @return Array of Rating objects sorted according to the predicted score
	 * @see org.apache.spark.mllib.recommendation.Rating
	 */
	public Rating[] recommendProducts(final MatrixFactorizationModel model, int userId, int numberOfProducts) {
		return model.recommendProducts(userId, numberOfProducts);
	}

	/**
	 * This method recommends users for a given product. (i.e. the users who are most likely to be interested in the given product.
	 *
	 * @param model         Matrix factorization model
	 * @param productId     The product to recommend users to
	 * @param numberOfUsers Number of users to return
	 * @return Array of Rating objects sorted according to the predicted score
	 * @see org.apache.spark.mllib.recommendation.Rating
	 */
	public Rating[] recommendUsers(final MatrixFactorizationModel model, int productId, int numberOfUsers) {
		return model.recommendUsers(productId, numberOfUsers);
	}
}
