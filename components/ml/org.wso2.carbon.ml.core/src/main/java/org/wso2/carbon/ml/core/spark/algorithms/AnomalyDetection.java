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

package org.wso2.carbon.ml.core.spark.algorithms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.*;
import org.apache.spark.mllib.linalg.Vector;
import org.wso2.carbon.ml.core.spark.models.ext.AnomalyDetectionModel;

public class AnomalyDetection implements Serializable {

    private static final long serialVersionUID = -8547369844618293845L;

    /**
     * This methods trains Anomaly detection model
     *
     * @param trainData Training data as a JavaRDD of Vectors
     * @param noOfClusters Number of clusters
     * @param noOfIterations Number of iterations to run
     * @return
     */
    public AnomalyDetectionModel train(JavaRDD<Vector> trainData, int noOfClusters, int noOfIterations,
            String newNormalLabel, String newAnomalyLabel) {

        AnomalyDetectionModel anomalyDetectionModel = new AnomalyDetectionModel();
        KMeansModel kMeansModel = org.apache.spark.mllib.clustering.KMeans.train(trainData.rdd(), noOfClusters,
                noOfIterations);

        // convert predicted clusters JAVARDD into a List
        List<Integer> predictedClusters = kMeansModel.predict(trainData).collect();
        Vector[] clusterCenters = kMeansModel.clusterCenters();

        // creating the distance Map to store the distances of each points with their cluster centers
        Map<Integer, List<Double>> distancesMap = new HashMap<Integer, List<Double>>();

        // creating the map with respect to each cluster
        for (int clusterIndex = 0; clusterIndex < clusterCenters.length; clusterIndex++) {

            List<Double> distancesList = new ArrayList<Double>();
            distancesMap.put(clusterIndex, distancesList);
        }

        // convert data JAVARDD into a List
        List<Vector> dataList = trainData.collect();
        // creating the EuclideanDistance Object
        EuclideanDistance distance = new EuclideanDistance();

        // calculating and storing the distances of each data point to it's cluster center
        for (int i = 0; i < dataList.size(); i++) {

            int clusterIndex = predictedClusters.get(i);
            double[] dataPoint = dataList.get(i).toArray();
            double[] clusterCenter = clusterCenters[clusterIndex].toArray();
            List<Double> distanceList = distancesMap.get(clusterIndex);
            double distanceBetweenDataPointAndItsClusterCenter = distance.compute(dataPoint, clusterCenter);
            distanceList.add(distanceBetweenDataPointAndItsClusterCenter);
        }

        anomalyDetectionModel.setkMeansModel(kMeansModel);
        anomalyDetectionModel.setClusterIndexToDistancesListMap(distancesMap);
        anomalyDetectionModel.setNormalLabel(newNormalLabel);
        anomalyDetectionModel.setAnomalyLabel(newAnomalyLabel);

        return anomalyDetectionModel;
    }

    /**
     * This method applies a anomaly detection model to a given dataset
     *
     * @param anomalyDetectionModel anomaly detection model
     * @param data a single data point as a Vector
     * @param percentile percentile value to identify the cluster boundaries
     * @return prediction label as a String
     */
    public String test(AnomalyDetectionModel anomalyDetectionModel, Vector data, int percentile) {

        return anomalyDetectionModel.predict(data, percentile);
    }

    /**
     * This method applies a anomaly detection model to a given dataset
     *
     * @param anomalyDetectionModel anomaly detection model
     * @param data JavaRDD containing feature vectors
     * @param percentile percentile value to identify the cluster boundaries
     * @return prediction labels as a List of Strings
     */
    public List<String> test(AnomalyDetectionModel anomalyDetectionModel, JavaRDD<Vector> data, int percentile) {

        return anomalyDetectionModel.predict(data, percentile);
    }

    /**
     * This method applies a anomaly detection model to a given dataset for a range of percentile values
     *
     * @param anomalyDetectionModel anomaly detection model
     * @param data a single data point as a Vector
     * @param minPercentile min percentile value of the range
     * @param maxPercentile max percentile value of the range
     * @return Map<Integer, String> key:percentile value:prediction label
     */
    public Map<Integer, String> test(AnomalyDetectionModel anomalyDetectionModel, Vector data, int minPercentile,
            int maxPercentile) {

        return anomalyDetectionModel.predict(data, minPercentile, maxPercentile);
    }

    /**
     * This method applies a anomaly detection model to a given dataset for a range of percentile values
     *
     * @param anomalyDetectionModel anomaly detection model
     * @param data JavaRDD containing feature vectors
     * @param minPercentile min percentile value of the range
     * @param maxPercentile max percentile value of the range
     * @return Map<Integer, List<String>> key:percentile value:prediction labels as a List of Strings
     */
    public Map<Integer, List<String>> test(AnomalyDetectionModel anomalyDetectionModel, JavaRDD<Vector> data,
            int minPercentile, int maxPercentile) {

        return anomalyDetectionModel.predict(data, minPercentile, maxPercentile);
    }

}
