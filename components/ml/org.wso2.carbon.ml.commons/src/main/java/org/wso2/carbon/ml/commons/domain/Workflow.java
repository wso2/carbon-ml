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

package org.wso2.carbon.ml.commons.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Workflow implements Serializable {
    private static final long serialVersionUID = -6804510317746660339L;
    private long workflowID;
    private String datasetURL;
    private String algorithmName;
    private String algorithmClass;
    private String responseVariable;
    private double trainDataFraction;
    private List<Feature> features;
    private Map<String, String> hyperParameters;

    /**
     * @return Returns machine learning workflow ID
     */
    public long getWorkflowID() {
        return workflowID;
    }

    /**
     * @param workflowID Sets machine learning workflow ID
     */
    public void setWorkflowID(long workflowID) {
        this.workflowID = workflowID;
    }

    /**
     * @return Returns dataset URL
     */
    public String getDatasetURL() {
        return datasetURL;
    }

    /**
     * @param datasetURL Sets dataset URL
     */
    public void setDatasetURL(String datasetURL) {
        this.datasetURL = datasetURL;
    }

    /**
     * @return Returns machine learning algorithm name
     */
    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * @param algorithmName Sets machine learning algorithm name
     */
    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    /**
     * @return Returns response variable
     */
    public String getResponseVariable() {
        return responseVariable;
    }

    /**
     * @param responseVariable Sets response variable
     */
    public void setResponseVariable(String responseVariable) {
        this.responseVariable = responseVariable;
    }

    /**
     * @return Returns training data fraction
     */
    public double getTrainDataFraction() {
        return trainDataFraction;
    }

    /**
     * @param trainDataFraction Sets training data fraction
     */
    public void setTrainDataFraction(double trainDataFraction) {
        this.trainDataFraction = trainDataFraction;
    }

    /**
     * @return Returns a list of machine learning features
     */
    public List<Feature> getFeatures() {
        return features;
    }

    /**
     * @param features Sets machine learning features
     */
    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    /**
     * @return Returns machine learning algorithm class e.g. Classification
     */
    public String getAlgorithmClass() {
        return algorithmClass;
    }

    /**
     * @param algorithmClass Sets machine learning algorithm class
     */
    public void setAlgorithmClass(String algorithmClass) {
        this.algorithmClass = algorithmClass;
    }

    /**
     * @return Returns hyper parameters
     */
    public Map<String, String> getHyperParameters() {
        return hyperParameters;
    }

    /**
     * @param hyperParameters Sets hyper parameters
     */
    public void setHyperParameters(Map<String, String> hyperParameters) {
        this.hyperParameters = hyperParameters;
    }
    
    public List<Feature> getIncludedFeatures(){
        List<Feature> includedFeatures = new ArrayList<Feature>();
        for (Feature feature : features) {
            if (feature.isInclude() == true && !responseVariable.equals(feature.getName())) {
                includedFeatures.add(feature);
            }
        }
        return includedFeatures;
    }

    @Override
    public String toString() {
        return "Model Configuration [algorithmName=" + algorithmName + ", algorithmClass="
                + algorithmClass + ", responseVariable=" + responseVariable + ", trainDataFraction="
                + trainDataFraction + ", hyperParameters=" + hyperParameters + ", features=" + features + "]";
    }
}
