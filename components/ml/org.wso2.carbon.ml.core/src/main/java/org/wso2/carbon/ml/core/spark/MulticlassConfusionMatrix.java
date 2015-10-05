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
import java.util.Arrays;
import java.util.List;

/**
 * DTO class to store multiclass confusion matrix
 */
public class MulticlassConfusionMatrix implements Serializable {
    private static final long serialVersionUID = -3812754594966583187L;
    /**
     * 2D array containing values of the matrix.
     */
    private double[][] matrix;
    /**
     * Labels of the confusion matrix.
     */
    List<String> labels;
    /**
     * Size of the matrix (for a 3x3 matrix this value will be 3 since the confusion matrix is always a square matrix).
     */
    private int size;

    private double f1Score;
    private double accuracy;
    private double precision;
    private double recall;

    private double truePositive;
    private double falsePositive;
    private double trueNegetive;
    private double falseNegetive;

    /**
     *
     * @return Returns the confusion matrix
     */
    public double[][] getMatrix() {
        return matrix;
    }

    /**
     *
     * @param matrix Matrix containing the values of confusion matrix
     */
    public void setMatrix(double[][] matrix) {
        if (matrix == null) {
            this.matrix = new double[0][0];
        }
        else {
            this.matrix = Arrays.copyOf(matrix, matrix.length);
        }

    }

    /**
     *
     * @return Returns the sequence of labels in ascending order
     */
    public List<String> getLabels() {
        return labels;
    }

    /**
     *
     * @param labels Set labels
     */
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    /**
     *
     * @return Returns size of the confusion matrix
     */
    public int getSize() {
        return size;
    }

    /**
     *
     * @param size Size of the matrix
     */
    public void setSize(int size) {
        this.size = size;
    }

    public void setAccuracyMeasures() {

        if (size == 2) {
            truePositive = matrix[0][0];
            falsePositive = matrix[1][0];
            trueNegetive = matrix[1][1];
            falseNegetive = matrix[0][1];

            f1Score = (2 * truePositive / (2 * truePositive + falsePositive + falseNegetive)) * 100;
            accuracy = ((truePositive + trueNegetive) / (truePositive + trueNegetive + falsePositive + falseNegetive)) * 100;
            precision = (truePositive / (truePositive + falsePositive)) * 100;
            recall = (truePositive / (truePositive + falseNegetive)) * 100;
        }
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

    @Override
    public String toString() {
        return "Confusion Matrix " + Arrays.deepToString(matrix);
    }
    
}
