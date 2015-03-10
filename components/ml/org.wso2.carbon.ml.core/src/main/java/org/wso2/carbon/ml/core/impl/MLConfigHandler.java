/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.core.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.config.HyperParameter;
import org.wso2.carbon.ml.commons.domain.config.MLAlgorithm;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

/**
 * {@link MLConfigHandler} is responsible for handling/delegating all the ml config related requests.
 */
public class MLConfigHandler {
    private static final Log log = LogFactory.getLog(MLConfigHandler.class);

    /**
     * Retrieve hyper parameters of an algorithm
     * 
     * @param algorithmName name of the algorithm
     * @return list of {@link HyperParameter}
     */
    public List<HyperParameter> getHyperParameters(String algorithmName) {
        List<MLAlgorithm> algos = MLCoreServiceValueHolder.getInstance().getAlgorithms();
        if (algos != null && algorithmName != null) {
            for (MLAlgorithm mlAlgorithm : algos) {
                if (algorithmName.equals(mlAlgorithm.getName())) {
                    return mlAlgorithm.getParameters();
                }
            }
        }
        return new ArrayList<HyperParameter>();
    }

    /**
     * Retrieve ML algorithms belongs to a particular Category (i.e. Classification, Numerical Prediction, Clustering).
     * 
     * @param algorithmType Type of the machine learning algorithm - e.g. Classification
     * @return List of algorithm names
     * @throws ModelServiceException
     */
    public List<String> getAlgorithmsByType(String algorithmType) {
        List<String> algorithms = new ArrayList<String>();
        List<MLAlgorithm> algos = MLCoreServiceValueHolder.getInstance().getAlgorithms();

        if (algos != null && algorithmType != null) {
            for (MLAlgorithm algorithm : algos) {
                if (algorithmType.equals(algorithm.getType())) {
                    algorithms.add(algorithm.getName());
                }
            }
        }
        return algorithms;
    }
    
    /**
     * TODO we need to improve this.
     * Retrieve recommended algorithms.
     * 
     * @param algorithmType     Type of the machine learning algorithm - e.g. Classification
     * @param userResponse      User's response to a questionnaire about machine learning task
     * @return                  Map containing names of recommended machine learning algorithms and
     *                          recommendation scores (out of 5) for each algorithm
     * @throws                  ModelServiceException
     */
    public Map<String, Double> getRecommendedAlgorithms(String algorithmType,
            Map<String, String> userResponse) {
        if (algorithmType == null || algorithmType == "") {
            throw new IllegalArgumentException("Argument: algorithmType is either null or empty");
        }
        if (userResponse == null || userResponse.size() == 0) {
            throw new IllegalArgumentException("Argument: userResponse is either null or empty");
        }
        
        List<MLAlgorithm> algos = MLCoreServiceValueHolder.getInstance().getAlgorithms();
        List<MLAlgorithm> algorithms = new ArrayList<MLAlgorithm>();
        for (MLAlgorithm mlAlgorithm : algos) {
            if (algorithmType.equals(mlAlgorithm.getType())) {
                algorithms.add(mlAlgorithm);
            }
        }
        Map<String, Double> recommendations = calculateAlgorithmWeigths(algorithms, userResponse);
        Double max = Collections.max(recommendations.values());
        DecimalFormat ratingNumberFormat = new DecimalFormat(MLConstants.DECIMAL_FORMAT);
        Double scaledRating;
        for (Map.Entry<String, Double> recommendation : recommendations.entrySet()) {
            scaledRating = ((recommendation.getValue()) / max) * MLConstants.ML_ALGORITHM_WEIGHT_LEVEL_1;
            scaledRating = Double.valueOf(ratingNumberFormat.format(scaledRating));
            recommendations.put(recommendation.getKey(), scaledRating);
        }
        return recommendations;
    }
    
    /**
     * Calculate weights for each algorithm based on the user reponses.
     * 
     * @param algorithms    List of ML Algorithms.
     * @param userResponse  Responses of the user for the set of questions.
     * @return              A Map containing a weights calculated for each algorithm 
     */
    private Map<String, Double> calculateAlgorithmWeigths(List<MLAlgorithm> algorithms, Map<String, String> userResponse) {
        Map<String, Double> recommendations = new HashMap<String, Double>();
        for (MLAlgorithm mlAlgorithm : algorithms) {
            // Set Interpretability
            if (MLConstants.HIGH.equals(userResponse.get(MLConstants.INTERPRETABILITY))) {
                mlAlgorithm.setInterpretability(mlAlgorithm.getInterpretability() * MLConstants
                        .ML_ALGORITHM_WEIGHT_LEVEL_1);
            } else if (MLConstants.MEDIUM.equals(userResponse.get(MLConstants.INTERPRETABILITY))) {
                mlAlgorithm.setInterpretability(mlAlgorithm.getInterpretability() * MLConstants
                        .ML_ALGORITHM_WEIGHT_LEVEL_2);
            } else {
                mlAlgorithm.setInterpretability(5);
            }
            // Set Scalability
            if (MLConstants.LARGE.equals(userResponse.get(MLConstants.DATASET_SIZE))) {
                mlAlgorithm.setScalability(mlAlgorithm.getScalability() * MLConstants.ML_ALGORITHM_WEIGHT_LEVEL_1);
            } else if (MLConstants.MEDIUM.equals(userResponse.get(MLConstants.DATASET_SIZE))) {
                mlAlgorithm.setScalability(mlAlgorithm.getScalability() * MLConstants.ML_ALGORITHM_WEIGHT_LEVEL_2);
            } else if (MLConstants.SMALL.equals(userResponse.get(MLConstants.DATASET_SIZE))) {
                mlAlgorithm.setScalability(5);
            }
            // Set Dimentiionality
            if (MLConstants.YES.equals(userResponse.get(MLConstants.TEXTUAL))) {
                mlAlgorithm.setDimensionality(mlAlgorithm.getDimensionality() * MLConstants.ML_ALGORITHM_WEIGHT_LEVEL_2);
            } else {
                mlAlgorithm.setDimensionality(5);
            }
            recommendations.put(mlAlgorithm.getName(), (double) (mlAlgorithm.getDimensionality() + mlAlgorithm
                    .getInterpretability() + mlAlgorithm.getScalability()));
        }
        return recommendations;
    }

}
