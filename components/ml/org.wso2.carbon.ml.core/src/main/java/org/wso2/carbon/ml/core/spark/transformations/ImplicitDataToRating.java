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

package org.wso2.carbon.ml.core.spark.transformations;

import org.apache.spark.api.java.function.Function;
import org.wso2.carbon.ml.core.exceptions.DatasetPreProcessingException;

import java.util.List;

/**
 * This class infers a rating score using the given array of data and given weights.
 */
public class ImplicitDataToRating implements Function<String[], String[]> {
	private int userIndex;
	private int productIndex;
	private List<Integer> observations;
	private List<Double> weights;

	/**
	 * Constructs the ImplicitDataToRating transformation object.
	 *
	 * @param userIndex     column index containing user_id
	 * @param productIndex  column index containing product_id
	 * @param observations  list of observation columns
	 * @param weights       list of weights to calculate rating score
	 */
	public ImplicitDataToRating(int userIndex, int productIndex, List<Integer> observations, List<Double> weights) {
		this.userIndex = userIndex;
		this.productIndex = productIndex;
		this.observations = observations;
		this.weights = weights;
	}

	@Override
	public String[] call(String[] tokens) throws DatasetPreProcessingException {

		//there should be at least 4 columns (two implicit feedback data columns) to infer a rating score.
		if (tokens.length <= 3) {
			throw new DatasetPreProcessingException("There is no, required number of fields to infer rating score");
		}
		//each implicit data field should have exactly one weight value.
		if (observations.size() != weights.size()) {
			throw new DatasetPreProcessingException("Number of implicit fields and number of weights does not match");
		}

		double rating = 0.0d;
		double weightSum = 0.0d;

		for(int i = 0; i < observations.size(); ++i) {
			rating += Double.parseDouble(tokens[observations.get(i)]) * weights.get(i);
			weightSum += weights.get(observations.get(i));
		}

		rating = rating / weightSum;
		return new String[] { tokens[userIndex], tokens[productIndex], Double.toString(rating) };
	}
}
