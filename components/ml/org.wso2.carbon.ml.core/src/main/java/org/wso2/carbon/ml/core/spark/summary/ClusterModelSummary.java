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

import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.ClusterPoint;
import org.wso2.carbon.ml.commons.domain.ModelSummary;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ClusterModelSummary implements ModelSummary, Serializable {

    private static final long serialVersionUID = -2367643384961727119L;
    private double trainDataComputeCost;
    private double testDataComputeCost;
    private String algorithm;
    private String[] features;
    private String datasetVersion;
    private List<ClusterPoint> clusterPoints;

    public List<ClusterPoint> getClusterPoints() {
        return clusterPoints;
    }

    public void setClusterPoints(List<ClusterPoint> clusterPoints) {
        this.clusterPoints = clusterPoints;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override public String getModelSummaryType() {
        return MLConstants.CLUSTER_MODEL_SUMMARY;
    }

    public double getTrainDataComputeCost() {
        return trainDataComputeCost;
    }

    public void setTrainDataComputeCost(double trainDataComputeCost) {
        this.trainDataComputeCost = trainDataComputeCost;
    }

    public double getTestDataComputeCost() {
        return testDataComputeCost;
    }

    public void setTestDataComputeCost(double testDataComputeCost) {
        this.testDataComputeCost = testDataComputeCost;
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

    public String getDatasetVersion() {
        return datasetVersion;
    }

    public void setDatasetVersion(String datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    @Override
    public String[] getFeatures() {
        return features;
    }
}
