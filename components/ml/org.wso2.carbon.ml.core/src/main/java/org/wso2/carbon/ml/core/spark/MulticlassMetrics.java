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

package org.wso2.carbon.ml.core.spark;

import java.io.Serializable;

/**
 * class to store multiclass metrics
 */
public class MulticlassMetrics implements Serializable {

    private static final long serialVersionUID = -9036951933788806073L;
    private MulticlassConfusionMatrix multiclassConfusionMatrix;

    private double f1Score;
    private double accuracy;
    private double precision;
    private double recall;

    public MulticlassMetrics (MulticlassConfusionMatrix multiclassConfusionMatrix) {
        this.multiclassConfusionMatrix = multiclassConfusionMatrix;

        double[][] matrix = multiclassConfusionMatrix.getMatrix();
        double truePositive = matrix[0][0];
        double falseNegative = matrix[0][1];
        double falsePositive = matrix[1][0];
        double trueNegative = matrix[1][1];

        this.f1Score = (2 * truePositive / (2 * truePositive + falsePositive + falseNegative));
        this.accuracy = ((truePositive + trueNegative) / (truePositive + trueNegative + falsePositive + falseNegative))
                * 100;
        this.precision = (truePositive / (truePositive + falsePositive)) * 100;
        this.recall = (truePositive / (truePositive + falseNegative)) * 100;

    }

    public MulticlassConfusionMatrix getMulticlassConfusionMatrix() {
        return multiclassConfusionMatrix;
    }

    public double getF1Score() {
        return f1Score;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }
}
