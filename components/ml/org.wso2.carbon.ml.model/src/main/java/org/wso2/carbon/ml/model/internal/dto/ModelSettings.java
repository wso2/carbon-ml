/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ml.model.internal.dto;

import java.util.List;

/**
 * DTO class to store model settings
 */
public class ModelSettings {
    private String modelSettingsID;
    private String workflowID;
    private String algorithmType;
    private String algorithmName;
    private String response;
    private String datasetURL;
    private double trainDataFraction;
    private List<HyperParameter> hyperParameters;

    /**
     * @return Returns model settings ID
     */
    public String getModelSettingsID() {
        return modelSettingsID;
    }

    /**
     * @param modelSettingsID Sets model settings ID
     */
    public void setModelSettingsID(String modelSettingsID) {
        this.modelSettingsID = modelSettingsID;
    }

    /**
     * @return Returns workflow ID
     */
    public String getWorkflowID() {
        return workflowID;
    }

    /**
     * @param workflowID Sets workflow ID
     */
    public void setWorkflowID(String workflowID) {
        this.workflowID = workflowID;
    }

    /**
     * @return Returns algorithm type e.g. classification
     */
    public String getAlgorithmType() {
        return algorithmType;
    }

    /**
     * @param algorithmType Sets algorithm type
     */
    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    /**
     * @return Returns algorithm name
     */
    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * @param algorithmName Sets algorithm name
     */
    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    /**
     * @return Returns response variable name
     */
    public String getResponse() {
        return response;
    }

    /**
     * @param response Sets response variable name
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * @return Returns train data fraction
     */
    public double getTrainDataFraction() {
        return trainDataFraction;
    }

    /**
     * @param trainDataFraction Sets train data fraction
     */
    public void setTrainDataFraction(double trainDataFraction) {
        this.trainDataFraction = trainDataFraction;
    }

    /**
     * @return Returns hyper parameters
     */
    public List<HyperParameter> getHyperParameters() {
        return hyperParameters;
    }

    /**
     * @param hyperParameters Sets hyper parameters
     */
    public void setHyperParameters(List<HyperParameter> hyperParameters) {
        this.hyperParameters = hyperParameters;
    }

    public String getDatasetURL() {
        return datasetURL;
    }

    public void setDatasetURL(String datasetURL) {
        this.datasetURL = datasetURL;
    }
}
