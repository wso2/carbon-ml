/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for machine learning model building related tasks
 */
public class ModelService {

    private static final Log logger = LogFactory.getLog(ModelService.class);

    /**
     * @param algorithm Name of the machine learning algorithm
     * @return Json object containing hyper parameters
     * @throws ModelServiceException
     */
    public JSONObject getHyperParameters(String algorithm) throws ModelServiceException {
        try {
            DatabaseHandler handler = DatabaseHandler.getDatabaseHandler();
            return handler.getHyperParameters(algorithm);
        } catch (DatabaseHandlerException ex) {
            String msg = "Error has occurred while retrieving hyper parameters";
            logger.error(msg, ex);
            throw new ModelServiceException(msg);
        }
    }

    /**
     * @param algorithmType Type of the machine learning algorithm - e.g. Classification
     * @return Array of algorithm names
     * @throws ModelServiceException
     */
    public String[] getAlgorithmsByType(String algorithmType) throws ModelServiceException {
        try {
            DatabaseHandler handler = DatabaseHandler.getDatabaseHandler();
            return handler.getAlgorithms(algorithmType);
        } catch (DatabaseHandlerException ex) {
            String msg = "Error has occurred while retrieving algorithm names";
            logger.error(msg, ex);
            throw new ModelServiceException(msg);
        }
    }

    /**
     * @param algorithmType    Type of the machine learning algorithm - e.g. Classification
     * @param userResponseJson User's response to a questionnaire about machine learning task
     * @return Map containing names of recommended machine learning algorithms and
     * recommendation scores (out of 5) for each algorithm
     * @throws ModelServiceException
     */
    public Map<String, Double> getRecommendedAlgorithms(String algorithmType,
                                                        String userResponseJson)
            throws ModelServiceException {
        Map<String, Double> recommendations = new HashMap<String, Double>();
        try {
            JSONObject userResponse = new JSONObject(userResponseJson);
            DatabaseHandler handler = DatabaseHandler.getDatabaseHandler();
            Map<String, List<Integer>> algorithmRatings = handler.getAlgorithmRatings(algorithmType);
            for (Map.Entry<String, List<Integer>> pair : algorithmRatings.entrySet()) {
                if ("high".equals(userResponse.get("interpretability"))) {
                    pair.getValue().set(0, (pair.getValue().get(0) * 5));
                } else if ("medium".equals(userResponse.get("interpretability"))) {
                    pair.getValue().set(0, (pair.getValue().get(0) * 3));
                } else {
                    pair.getValue().set(0, 5);
                }
                if ("large".equals(userResponse.get("datasetSize"))) {
                    pair.getValue().set(1, (pair.getValue().get(1) * 5));
                } else if ("medium".equals(userResponse.get("datasetSize"))) {
                    pair.getValue().set(1, (pair.getValue().get(1) * 3));
                } else if ("small".equals(userResponse.get("datasetSize"))) {
                    pair.getValue().set(1, 5);
                }
                if ("Yes".equals(userResponse.get("textual"))) {
                    pair.getValue().set(2, (pair.getValue().get(2) * 3));
                } else {
                    pair.getValue().set(2, 5);
                }
            }

            for (Map.Entry<String, List<Integer>> pair : algorithmRatings.entrySet()) {
                recommendations.put(pair.getKey(), sum(pair.getValue()));
            }
            Double max = Collections.max(recommendations.values());
            Double scaledRating;
            for (Map.Entry<String, Double> pair : recommendations.entrySet()) {
                scaledRating = ((pair.getValue()) / max) * 5;
                recommendations.put(pair.getKey(), scaledRating);
            }
        } catch (Exception e) {
            String msg = "An error occurred while retrieving recommended algorithms";
            logger.error(msg, e);
            throw new ModelServiceException(msg);
        }
        return recommendations;
    }

    /**
     * @param ratings List of integer scores
     * @return Sum of the list of scores as a double
     */
    private Double sum(List<Integer> ratings) {
        Double sum = 0.0;
        for (Integer rating : ratings) {
            sum = sum + rating;
        }
        return sum;
    }


}
