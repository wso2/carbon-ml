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

package org.wso2.carbon.ml.core.spark.summary;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.core.spark.MulticlassConfusionMatrix;

/**
 * A utility class to store summary of information of DeeplearningModel
 * 
 */
public class DeeplearningModelSummary implements ModelSummary, Serializable{
    
    private double error;
    private List<PredictedVsActual> predictedVsActuals;
    private List<TestResultDataPoint> testResultDataPointsSample;
    private List<FeatureImportance> featureImportance;
    private String algorithm;
    private String[] features;
    private double modelAccuracy;
    private MulticlassConfusionMatrix multiclassConfusionMatrix;
    private double meanSquaredError;

        public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * @return Returns classification error
     */
    public double getError() {
        return error;
    }

    /**
     * @param error Sets classification error
     */
    public void setError(double error) {
        this.error = error;
    }

    /**
     * @return Returns predicted vs. actual labels
     */
    public List<PredictedVsActual> getPredictedVsActuals() {
        return predictedVsActuals;
    }

    /**
     * @param predictedVsActuals Sets predicted vs. actual labels
     */
    public void setPredictedVsActuals(List<PredictedVsActual> predictedVsActuals) {
        this.predictedVsActuals = predictedVsActuals;
    }

    /**
     * @return Returns a list of features with predicted vs. actual values
     */
    public List<TestResultDataPoint> getTestResultDataPointsSample() {
        return testResultDataPointsSample;
    }

    /**
     * @param testResultDataPointsSample Sets features with predicted vs. actual values
     */
    public void setTestResultDataPointsSample(List<TestResultDataPoint> testResultDataPointsSample) {
        this.testResultDataPointsSample = testResultDataPointsSample;
    }

    @Override
    public String getModelSummaryType() {
        return MLConstants.DEEPLEARNING_MODEL_SUMMARY;
    }

    
    /**
     * @return Weights of each of the feature
     */
    public List<FeatureImportance> getFeatureImportance() {
        return featureImportance;
    }

    /**
     * @param featureImportance Weights of each of the feature
     */
    public void setFeatureImportance(List<FeatureImportance> featureImportance) {
        this.featureImportance = featureImportance;
    }

    /**
     * @param features Array of names of the features
     */
    public void setFeatures(String[] features) {
        if (features == null) {
            this.features = new String[0];
        } else {
            this.features = Arrays.copyOf(features, features.length);
        }
    }

    /**
     * @return Returns model accuracy
     */
    public double getModelAccuracy() {
        return modelAccuracy;
    }

    /**
     * @param modelAccuracy Sets model accuracy
     */
    public void setModelAccuracy(double modelAccuracy) {
        this.modelAccuracy = modelAccuracy;
    }

    /**
     * @return Returns mean squared error
     */
    public double getMeanSquaredError() {
        return meanSquaredError;
    }

    /**
     * @param meanSquaredError Sets mean squared error
     */
    public void setMeanSquaredError(double meanSquaredError) {
        this.meanSquaredError = meanSquaredError;
    }

    @Override
    public String[] getFeatures() {
        return features;
    }
    
    /**
     * @return Returns multiclass confusion matrix
     */
    public MulticlassConfusionMatrix getMulticlassConfusionMatrix() {
        return multiclassConfusionMatrix;
    }

    /**
     * @param multiclassConfusionMatrix multiclass confusion matrix
     */
    public void setMulticlassConfusionMatrix(MulticlassConfusionMatrix multiclassConfusionMatrix) {
        this.multiclassConfusionMatrix = multiclassConfusionMatrix;
    }

}
