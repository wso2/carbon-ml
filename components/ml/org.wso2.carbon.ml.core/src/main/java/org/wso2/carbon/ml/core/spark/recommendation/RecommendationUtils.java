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
import org.apache.spark.mllib.recommendation.Rating;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.DatasetPreProcessingException;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.transformations.ImplicitDataToRating;
import org.wso2.carbon.ml.core.spark.transformations.StringArrayToRating;
import org.wso2.carbon.ml.core.utils.MLUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendationUtils {

	private static final String SEPARATOR = ",";

	/**
	 * Pre processes the data set.
	 *
	 * @param context                           {@link MLModelConfigurationContext}
	 * @param containsImplicitData              the data set contains implicit feedback data.
	 * @return                                  Processed RDD of the data set.
	 * @throws DatasetPreProcessingException    If error occurred when pre processing the data set.
	 */
	public static JavaRDD<Rating> preProcess(MLModelConfigurationContext context, boolean containsImplicitData)
			throws DatasetPreProcessingException {
		Workflow workflow = context.getFacts();
		JavaRDD<String> lines = context.getLines();
		String headerRow = context.getHeaderRow();
		String columnSeparator = context.getColumnSeparator();

		int userIndex = MLUtils.getFeatureIndex(workflow.getUserVariable(), headerRow, columnSeparator);
		int productIndex = MLUtils.getFeatureIndex(workflow.getProductVariable(), headerRow, columnSeparator);

		JavaRDD<String[]> tokens = MLUtils.filterRows(columnSeparator, headerRow, lines,
		                                              MLUtils.getImputeFeatureIndices(workflow,
		                                                                              new ArrayList<Integer>(),
		                                                                              MLConstants.DISCARD));

		if (containsImplicitData) {
			List<Integer> observationList = getObservationList(workflow.getObservations());
			Map<String, String> parameters = workflow.getHyperParameters();
			List<Double> weightList = getWeightList(parameters.get(MLConstants.WEIGHTS));
			tokens = tokens.map(new ImplicitDataToRating(userIndex, productIndex, observationList, weightList));
			return tokens.map(new StringArrayToRating(0,1,2));
		} else {
			int ratingIndex = MLUtils.getFeatureIndex(workflow.getRatingVariable(), headerRow, columnSeparator);
			return tokens.map(new StringArrayToRating(userIndex, productIndex, ratingIndex));
		}
	}

	/**
	 * Converts the comma separated weights to double list.
	 *
	 * @param weights   comma separated weights
	 * @return          List of doubles containing weights
	 */
	private static List<Double> getWeightList(String weights) {
		List<Double> weightList = new ArrayList<Double>();
		String[] weightArray = weights.trim().split(SEPARATOR);

		for(String weight : weightArray) {
			weightList.add(Double.parseDouble(weight));
		}

		return weightList;
	}

	/**
	 * Converts the comma separated observations to integer list.
	 *
	 * @param observations  comma separated observations
	 * @return              List of integer containing observation indexes
	 */
	private static List<Integer> getObservationList(String observations) {
		List<Integer> observationList = new ArrayList<Integer>();
		String[] observationArray = observations.trim().split(SEPARATOR);

		for (String observation : observationArray) {
			observationList.add(Integer.parseInt(observation));
		}

		return  observationList;
	}
}
