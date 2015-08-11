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

import org.apache.spark.mllib.recommendation.Rating;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.apache.spark.api.java.function.Function;

/**
 * This class converts string array of tokens to {@link org.apache.spark.mllib.recommendation.Rating} object
 */
public class StringArrayToRating implements Function<String[], Rating> {

	private int userIndex;
	private int productIndex;
	private int ratingIndex;

	/**
	 * Constructs a StringArrayToRating transformer object
	 *
	 * @param userIndex     column index containing user_id
	 * @param productIndex  column index containing product_id
	 * @param ratingIndex   column index containing rating value
	 */
	public StringArrayToRating(int userIndex, int productIndex, int ratingIndex) {
		this.userIndex = userIndex;
		this.productIndex = productIndex;
		this.ratingIndex = ratingIndex;
	}

	@Override
	public Rating call(String[] tokens) throws MLModelBuilderException {
		try {
			return new Rating(
				Integer.parseInt(tokens[userIndex]),
			    Integer.parseInt(tokens[productIndex]),
			    Double.parseDouble(tokens[ratingIndex])
			);

		} catch (Exception e) {
			throw new MLModelBuilderException("An error occurred while transforming tokens: " + e.getMessage(), e);
		}
	}
}
