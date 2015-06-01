/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.ml.commons.domain;

import java.util.Arrays;

/**
 * DTO class to store k-means cluster point
 */
public class ClusterPoint {
    private double[] features;
    private int cluster;

    /**
     * @return Returns cluster label e.g: 1,2 etc.
     */
    public int getCluster() {
        return cluster;
    }

    /**
     * @param cluster Sets cluster label
     */
    public void setCluster(int cluster) {
        this.cluster = cluster;
    }

    /**
     * @return Returns features of a cluster point
     */
    public double[] getFeatures() {
        return features;
    }

    /**
     * @param features Sets features of a cluster point
     */
    public void setFeatures(double[] features) {
        if (features == null) {
            this.features = new double[0];
        } else {
            this.features = Arrays.copyOf(features, features.length);
        }
    }
}
