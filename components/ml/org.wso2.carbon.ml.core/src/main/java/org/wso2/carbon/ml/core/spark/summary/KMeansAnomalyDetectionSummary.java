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
import org.wso2.carbon.ml.core.spark.ConfusionMatrix;
import org.wso2.carbon.ml.core.spark.MulticlassConfusionMatrix;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class KMeansAnomalyDetectionSummary implements ModelSummary, Serializable{

    private static final long serialVersionUID = -2367643384961727119L;
    private String algorithm;
    private String[] features;
    private Map<Integer, MulticlassConfusionMatrix> multiclassConfusionMatrix;
    private List<ClusterPoint> clusterPoints;
    private int bestPercentile;
    private String datasetVersion;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override public String getModelSummaryType() {
        return MLConstants.KMEANS_ANOMALY_DETECTION_MODEL_SUMMARY;
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

    @Override
    public String[] getFeatures() {
        return features;
    }

    public Map<Integer, MulticlassConfusionMatrix> getMulticlassConfusionMatrix() {
        return multiclassConfusionMatrix;
    }

    public void setMulticlassConfusionMatrix(Map<Integer, MulticlassConfusionMatrix> multiclassConfusionMatrix) {
        this.multiclassConfusionMatrix = multiclassConfusionMatrix;
    }

    public List<ClusterPoint> getClusterPoints() {
        return clusterPoints;
    }

    public void setClusterPoints(List<ClusterPoint> clusterPoints) {
        this.clusterPoints = clusterPoints;
    }

    public int getBestPercentile() {
        return bestPercentile;
    }

    public void setBestPercentile(int bestPercentile) {
        this.bestPercentile = bestPercentile;
    }

    /**
     * @return Returns dataset version
     */
    public String getDatasetVersion() {
        return datasetVersion;
    }

    /**
     * @param datasetVersion Sets dataset version
     */
    public void setDatasetVersion(String datasetVersion) {
        this.datasetVersion = datasetVersion;
    }
}

