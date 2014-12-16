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

package org.wso2.carbon.ml.model.spark.dto;

import java.io.Serializable;
import java.util.List;

/**
 * A utility class to store probabilistic classification model summary
 */
public class ProbabilisticClassificationModelSummary implements Serializable {
    private String roc;
    private double auc;
    private List<PredictedVsActual> predictedVsActuals;

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
}
