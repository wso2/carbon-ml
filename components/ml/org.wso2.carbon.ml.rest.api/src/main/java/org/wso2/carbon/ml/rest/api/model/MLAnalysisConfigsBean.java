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
package org.wso2.carbon.ml.rest.api.model;

import org.wso2.carbon.ml.commons.domain.MLCustomizedFeature;
import org.wso2.carbon.ml.commons.domain.MLHyperParameter;

import java.util.List;

/**
 * Represents configurations of an analysis in ML.
 */
public class MLAnalysisConfigsBean {

    private long id;
    private List<MLCustomizedFeature> customizedFeatures;
    String algorithmName;
    String responseVariable;
    double trainDataFraction;
    List<MLHyperParameter> hyperParameters;
    private String normalLabels;
    private boolean normalization;
    // user defined normal label
    private String newNormalLabel;
    // user defined anomaly label
    private String newAnomalyLabel;
    private String userVariable;
    private String productVariable;
    private String ratingVariable;
    private String observations;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<MLCustomizedFeature> getCustomizedFeatures() {
        return customizedFeatures;
    }

    public void setCustomizedFeatures(List<MLCustomizedFeature> customizedFeatures) {
        this.customizedFeatures = customizedFeatures;
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

    public String getNormalLabels() {
        return normalLabels;
    }

    public void setNormalLabels(String normalLabels) {
        this.normalLabels = normalLabels;
    }

    public boolean isNormalization() {
        return normalization;
    }

    public void setNormalization(boolean normalization) {
        this.normalization = normalization;
    }

    public String getNewNormalLabel() {
        return newNormalLabel;
    }

    public void setNewNormalLabel(String newNormalLabel) {
        this.newNormalLabel = newNormalLabel;
    }

    public String getNewAnomalyLabel() {
        return newAnomalyLabel;
    }

    public void setNewAnomalyLabel(String newAnomalyLabel) {
        this.newAnomalyLabel = newAnomalyLabel;
    }

    public String getUserVariable() {
        return userVariable;
    }

    public void setUserVariable(String userVariable) {
        this.userVariable = userVariable;
    }

    public String getProductVariable() {
        return productVariable;
    }

    public void setProductVariable(String productVariable) {
        this.productVariable = productVariable;
    }

    public String getRatingVariable() {
        return ratingVariable;
    }

    public void setRatingVariable(String ratingVariable) {
        this.ratingVariable = ratingVariable;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public List<MLHyperParameter> getHyperParameters() {
        return hyperParameters;
    }

    public void setHyperParameters(List<MLHyperParameter> hyperParameters) {
        this.hyperParameters = hyperParameters;
    }
}
