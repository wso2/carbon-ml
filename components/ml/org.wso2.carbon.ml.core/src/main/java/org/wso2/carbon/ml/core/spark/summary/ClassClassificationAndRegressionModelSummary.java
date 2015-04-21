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
 * A utility class to store class classification model summary
 */
public class ClassClassificationAndRegressionModelSummary implements ModelSummary, Serializable {

    private static final long serialVersionUID = 5035275752959356840L;
    private double error;
    private List<PredictedVsActual> predictedVsActuals;
    private List<FeatureImportance> featureImportance;

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

    @Override
    public String getModelSummaryType() {
        return MLConstants.CLASS_CLASSIFICATION_AND_REGRESSION_MODEL_SUMMARY;
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
}
