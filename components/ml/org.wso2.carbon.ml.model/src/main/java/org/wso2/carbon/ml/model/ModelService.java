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
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.ComponentContext;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @scr.component name="modelService" immediate="true"
 * Service class for machine learning model building related tasks
 */

public class ModelService {

    private static final Log logger = LogFactory.getLog(ModelService.class);

    /**
     * ModelService activator
     *
     * @param context ComponentContext
     */
    protected void activate(ComponentContext context) {
        try {
            ModelService modelService = new ModelService();
            context.getBundleContext().registerService(ModelService.class.getName(),
                                                       modelService, null);
            logger.info("ML Model Service Started");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * ModelService de-activator
     *
     * @param context ComponentContext
     */
    protected void deactivate(ComponentContext context) {
        logger.info("ML Model Service Stopped");
    }

    /**
     * @param algorithm Name of the machine learning algorithm
     * @return Json array containing hyper parameters
     * @throws ModelServiceException
     */
    public JSONArray getHyperParameters(String algorithm) throws ModelServiceException {
        try {
            MLAlgorithmConfigurationParser mlAlgorithmConfigurationParser = new
                    MLAlgorithmConfigurationParser();
            return mlAlgorithmConfigurationParser.getHyperParameters(algorithm);
        } catch (MLAlgorithmConfigurationParserException ex) {
            String msg = "An error occurred while retrieving hyper parameters";
            logger.error(msg, ex);
            throw new ModelServiceException(msg);
        }
    }

    /**
     * @param algorithmType Type of the machine learning algorithm - e.g. Classification
     * @return List of algorithm names
     * @throws ModelServiceException
     */
    public List<String> getAlgorithmsByType(String algorithmType) throws ModelServiceException {
        try {
            MLAlgorithmConfigurationParser mlAlgorithmConfigurationParser = new
                    MLAlgorithmConfigurationParser();
            return mlAlgorithmConfigurationParser.getAlgorithms(algorithmType);
        } catch (MLAlgorithmConfigurationParserException ex) {
            String msg = "An error occurred while retrieving algorithm names";
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
            MLAlgorithmConfigurationParser mlAlgorithmConfigurationParser = new
                    MLAlgorithmConfigurationParser();
            Map<String, List<Integer>> algorithmRatings = mlAlgorithmConfigurationParser.getAlgorithmRatings(algorithmType);
            for (Map.Entry<String, List<Integer>> rating : algorithmRatings.entrySet()) {
                if (MLModelConstants.HIGH.equals(userResponse.get(MLModelConstants.INTERPRETABILITY))) {
                    rating.getValue().set(0, rating.getValue().get(0) * 5);
                } else if (MLModelConstants.MEDIUM.equals(userResponse.get(MLModelConstants.INTERPRETABILITY))) {
                    rating.getValue().set(0, rating.getValue().get(0) * 3);
                } else {
                    rating.getValue().set(0, 5);
                }
                if (MLModelConstants.LARGE.equals(userResponse.get(MLModelConstants.DATASET_SIZE))) {
                    rating.getValue().set(1, rating.getValue().get(1) * 5);
                } else if (MLModelConstants.MEDIUM.equals(userResponse.get(MLModelConstants.DATASET_SIZE))) {
                    rating.getValue().set(1, rating.getValue().get(1) * 3);
                } else if (MLModelConstants.SMALL.equals(userResponse.get(MLModelConstants.DATASET_SIZE))) {
                    rating.getValue().set(1, 5);
                }
                if (MLModelConstants.YES.equals(userResponse.get(MLModelConstants.TEXTUAL))) {
                    rating.getValue().set(2, rating.getValue().get(2) * 3);
                } else {
                    rating.getValue().set(2, 5);
                }
            }

            for (Map.Entry<String, List<Integer>> pair : algorithmRatings.entrySet()) {
                recommendations.put(pair.getKey(), sum(pair.getValue()));
            }
            Double max = Collections.max(recommendations.values());
            DecimalFormat ratingNumberFormat = new DecimalFormat(MLModelConstants.DECIMAL_FORMAT);
            Double scaledRating;
            for (Map.Entry<String, Double> recommendation : recommendations.entrySet()) {
                scaledRating = ((recommendation.getValue()) / max) * 5;
                scaledRating = Double.valueOf(ratingNumberFormat.format(scaledRating));
                recommendations.put(recommendation.getKey(), scaledRating);
            }
        } catch (MLAlgorithmConfigurationParserException e) {
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
