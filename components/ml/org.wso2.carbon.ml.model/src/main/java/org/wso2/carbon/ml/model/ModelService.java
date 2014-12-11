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

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;

import java.util.List;
import java.util.Map;

public interface ModelService {

    /**
     * @param algorithm Name of the machine learning algorithm
     * @return Json array containing hyper parameters
     * @throws org.wso2.carbon.ml.model.exceptions.ModelServiceException
     */
    public JSONArray getHyperParameters(String algorithm) throws ModelServiceException;

    /**
     * @param algorithmType Type of the machine learning algorithm - e.g. Classification
     * @return List of algorithm names
     * @throws ModelServiceException
     */
    public List<String> getAlgorithmsByType(String algorithmType) throws ModelServiceException;

    /**
     * @param algorithmType    Type of the machine learning algorithm - e.g. Classification
     * @param userResponseJson User's response to a questionnaire about machine learning task
     * @return Map containing names of recommended machine learning algorithms and
     * recommendation scores (out of 5) for each algorithm
     * @throws ModelServiceException
     */
    public Map<String, Double> getRecommendedAlgorithms(String algorithmType,
            String userResponseJson)
            throws ModelServiceException;

    /**
     * @param workflow JSON object containing machine learning work flow information
     * @throws ModelServiceException
     */
    public void buildModel(String workflowJSON) throws ModelServiceException;

    /**
     * This method checks whether model execution is completed or not
     * @param modelId
     * @return indicates whether model execution is completed or not
     * @throws ModelServiceException
     */
    public boolean isExecutionCompleted(String modelId) throws ModelServiceException;

    /**
     * This method checks whether model execution is started or not
     * @param modelId
     * @return indicates whether model execution is started or not
     * @throws ModelServiceException
     */
    public boolean isExecutionStarted(String modelId) throws ModelServiceException;

     /** @param modelID Model ID
     * @param <T>     Model summary type
     * @return Model summary object
     * @throws ModelServiceException
     */
    public <T> T getModelSummary(String modelID) throws ModelServiceException;

}
