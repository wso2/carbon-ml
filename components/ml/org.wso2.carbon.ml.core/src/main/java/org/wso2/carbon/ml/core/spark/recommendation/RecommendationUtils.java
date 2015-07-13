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
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.recommendation.Rating;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.DatasetPreProcessingException;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.utils.MLConstants;

public class RecommendationUtils {
	
	public static JavaRDD<Rating> preProcess(MLModelConfigurationContext context)
			throws DatasetPreProcessingException {
		if(context == null) {
			throw new DatasetPreProcessingException("ML Configuration Context is empty.");
		}

		JavaRDD<String> lines = context.getLines();
		final String columnSeparator = context.getColumnSeparator();

		JavaRDD<Rating> trainingData = lines.map(new Function<String, Rating>() {
			@Override public Rating call(String line) throws Exception {
				String[] fields = line.split(columnSeparator);
				Rating rating;
				try{
					int userId = Integer.parseInt(fields[0]);
					int productId = Integer.parseInt(fields[1]);
					double ratingValue = getInferredRating(fields[3], fields[4], RecommendationConstants.DEFAULT_WEIGHT);

					rating =  new Rating(userId, productId, ratingValue);
				}catch (ArrayIndexOutOfBoundsException e) {
					throw new DatasetPreProcessingException("Error in parsing the dataset");
				} catch (NumberFormatException e) {
					throw new DatasetPreProcessingException("Error in parsing the dataset");
				}

				return rating;
			}
		});


		return trainingData;
	}

	private static double getInferredRating(String pageViews, String purchases, double weight)
			throws NumberFormatException {

		return (Double.parseDouble(pageViews) * (100 - weight) + Double.parseDouble(purchases) * weight) / 100;

	}
}
