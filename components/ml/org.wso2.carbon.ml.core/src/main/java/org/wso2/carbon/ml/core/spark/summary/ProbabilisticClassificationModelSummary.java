/*
 * Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.ModelSummary;

import java.io.Serializable;
import java.util.List;


/**
 * A utility class to store probabilistic classification model summary
 */
public class ProbabilisticClassificationModelSummary implements ModelSummary,Serializable {

    private static final long serialVersionUID = -3725591755536859086L;
    private String roc;
    private double auc;
    private  List<FeatureImportance> featureImportance;
    private List<PredictedVsActual> predictedVsActuals;
    private List<FeaturesWithPredictedVsActual> featuresWithPredictedVsActualSample;
    private String algorithm;
    private String[] features;
    private double modelAccuracy;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    

    /**
     * @return Returns area under curve (AUC)
     */
    public double getAuc() {
        return auc;
    }

    /**
     * @param auc Sets area under curve (AUC)
     */
    public void setAuc(double auc) {
        this.auc = auc;
    }

    /**
     * @return Returns a list of predicted vs. actual values
     */
    public List<PredictedVsActual> getPredictedVsActuals() {
        return predictedVsActuals;
    }

    /**
     * @param predictedVsActuals Sets predicted vs. actual values
     */
    public void setPredictedVsActuals(List<PredictedVsActual> predictedVsActuals) {
        this.predictedVsActuals = predictedVsActuals;
    }

    /**
     * @return Returns a list of features with predicted vs. actual values
     */
    public List<FeaturesWithPredictedVsActual> getFeaturesWithPredictedVsActualSample() {
        return featuresWithPredictedVsActualSample;
    }

    /**
     * @param featuresWithPredictedVsActualSample Sets features with predicted vs. actual values
     */
    public void setFeaturesWithPredictedVsActualSample(List<FeaturesWithPredictedVsActual> featuresWithPredictedVsActualSample) {
        this.featuresWithPredictedVsActualSample = featuresWithPredictedVsActualSample;
    }

    /**
     * @return Returns receiver operating characteristic (ROC) curve as a JSON string
     */
    public String getRoc() {
        return roc;
    }

    /**
     * @param roc Sets receiver operating characteristic (ROC) curve
     */
    public void setRoc(String roc) {
        this.roc = roc;
    }

    @Override
    public String getModelSummaryType() {
        return MLConstants.PROBABILISTIC_CLASSIFICATION_MODEL_SUMMARY;
    }
    
    /**
     * @return Weights of each of the feature
     */
    public  List<FeatureImportance> getFeatureImportance() {
        return featureImportance;
    }

    /**
     * @param featureImportance Weights of each of the feature
     */
    public void setFeatureImportance( List<FeatureImportance> featureImportance) {
        this.featureImportance = featureImportance;
    }
    
    /**
     * @param features Array of names of the features
     */
    public void setFeatures(String[] features) {
        this.features = features;
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

    @Override
    public String[] getFeatures() {
        return features;
    }
}
