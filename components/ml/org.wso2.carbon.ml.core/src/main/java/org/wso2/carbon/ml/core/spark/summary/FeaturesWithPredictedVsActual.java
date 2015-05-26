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

package org.wso2.carbon.ml.core.spark.summary;

import java.io.Serializable;

/**
 * DTO class to store features with predicted vs. actual
 */
public class FeaturesWithPredictedVsActual implements Serializable {

    private static final long serialVersionUID = 6763495729970627524L;
    private PredictedVsActual predictedVsActual;
    private double[] rowFeatures;

    /**
     * @return Returns predicted vs. actual
     */
    public PredictedVsActual getPredictedVsActual() {
        return predictedVsActual;
    }

    /**
     * @param predictedVsActual Sets predicted vs. actual
     */
    public void setPredictedVsActual(PredictedVsActual predictedVsActual) {
        this.predictedVsActual = predictedVsActual;
    }

    /**
     * @return Returns row features
     */
    public double[] getRowFeatures() {
        return rowFeatures;
    }

    /**
     * @param rowFeatures Sets row features
     */
    public void setRowFeatures(double[] rowFeatures) {
        this.rowFeatures = rowFeatures;
    }
}
