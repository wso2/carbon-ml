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

package org.wso2.carbon.ml.model.internal.dto;

/**
 * DTO class to store confusion matrix
 */
public class ConfusionMatrix {
    private long truePositives;
    private long trueNegatives;
    private long falsePositives;
    private long falseNegatives;

    /**
     *
     * @return Returns number of true positives
     */
    public long getTruePositives() {
        return truePositives;
    }

    /**
     *
     * @param truePositives Set number of true positives
     */
    public void setTruePositives(long truePositives) {
        this.truePositives = truePositives;
    }

    /**
     *
     * @return Returns number of true negatives
     */
    public long getTrueNegatives() {
        return trueNegatives;
    }

    /**
     *
     * @param trueNegatives Set number of true negatives
     */
    public void setTrueNegatives(long trueNegatives) {
        this.trueNegatives = trueNegatives;
    }

    /**
     *
     * @return Returns number of false positives
     */
    public long getFalsePositives() {
        return falsePositives;
    }

    /**
     *
     * @param falsePositives Sets number of false positives
     */
    public void setFalsePositives(long falsePositives) {
        this.falsePositives = falsePositives;
    }

    /**
     *
     * @return Returns number of false negatives
     */
    public long getFalseNegatives() {
        return falseNegatives;
    }

    /**
     *
     * @param falseNegatives Sets number of false negatives
     */
    public void setFalseNegatives(long falseNegatives) {
        this.falseNegatives = falseNegatives;
    }

    @Override
    public String toString() {
        return "ConfusionMatrix [truePositives=" + truePositives + ", trueNegatives=" + trueNegatives
                + ", falsePositives=" + falsePositives + ", falseNegatives=" + falseNegatives + "]";
    }
    
}
