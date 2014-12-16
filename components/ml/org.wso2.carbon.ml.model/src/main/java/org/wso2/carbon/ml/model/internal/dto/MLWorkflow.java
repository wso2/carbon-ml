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

package org.wso2.carbon.ml.model.internal.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class MLWorkflow implements Serializable {
    private String workflowID;
    private String datasetURL;
    private String algorithmName;
    private String algorithmClass;
    private String responseVariable;
    private double trainDataFraction;
    private List<MLFeature> features;
    private Map<String, String> hyperParameters;

    public String getWorkflowID() {
        return workflowID;
    }

    public void setWorkflowID(String workflowID) {
        this.workflowID = workflowID;
    }

    public String getDatasetURL() {
        return datasetURL;
    }

    public void setDatasetURL(String datasetURL) {
        this.datasetURL = datasetURL;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getResponseVariable() {
        return responseVariable;
    }

    public void setResponseVariable(String responseVariable) {
        this.responseVariable = responseVariable;
    }

    public double getTrainDataFraction() {
        return trainDataFraction;
    }

    public void setTrainDataFraction(double trainDataFraction) {
        this.trainDataFraction = trainDataFraction;
    }

    public List<MLFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<MLFeature> features) {
        this.features = features;
    }

    public String getAlgorithmClass() {
        return algorithmClass;
    }

    public void setAlgorithmClass(String algorithmClass) {
        this.algorithmClass = algorithmClass;
    }

    public Map<String, String> getHyperParameters() {
        return hyperParameters;
    }

    public void setHyperParameters(Map<String, String> hyperParameters) {
        this.hyperParameters = hyperParameters;
    }
}
