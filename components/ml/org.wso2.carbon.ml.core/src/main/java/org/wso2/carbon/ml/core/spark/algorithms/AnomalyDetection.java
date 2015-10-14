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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.wso2.carbon.ml.core.spark.MulticlassConfusionMatrix;

public class AnomalyDetection implements Serializable {

    private static final long serialVersionUID = 7012024887487309471L;

    /**
     * This method trains a k-means clustering model
     *
     * @param data Training data as a JavaRDD of Vectors
     * @param noOfClusters Number of clusters
     * @param noOfIterations Number of iterations to run
     * @param noOfRuns Number of runs of the algorithm to execute in parallel
     * @param initializationMode Initialization algorithm: random or k-means||
     * @return
     */
    public KMeansModel train(JavaRDD<Vector> data, int noOfClusters, int noOfIterations, int noOfRuns,
            String initializationMode) {
        return org.apache.spark.mllib.clustering.KMeans.train(data.rdd(), noOfClusters, noOfIterations, noOfRuns,
                initializationMode);
    }

    /**
     * This method trains a k-means clustering model - overload method with 3 parameters
     *
     * @param data Training data as a JavaRDD of Vectors
     * @param noOfClusters Number of clusters
     * @param noOfIterations Number of iterations to run
     * @return
     */
    public KMeansModel train(JavaRDD<Vector> data, int noOfClusters, int noOfIterations) {
        return org.apache.spark.mllib.clustering.KMeans.train(data.rdd(), noOfClusters, noOfIterations);
    }

    /**
     * This method applies a kmeans model to a given dataset
     *
     * @param kMeansModel KMeans model
     * @param data JavaRDD containing feature vectors
     * @return JavaRDD containing cluster centers
     */
    public JavaRDD<Integer> test(KMeansModel kMeansModel, JavaRDD<Vector> data) {
        return kMeansModel.predict(data);
    }

    /**
     * This method returns cluster centers of a given kmeans model
     *
     * @param kMeansModel KMeans model
     * @return Vector[] containing cluster centers
     */
    public Vector[] getClusterCenters(KMeansModel kMeansModel) {
        return kMeansModel.clusterCenters();
    }

    /**
     * This method is to calculate the euclidean distances of each data point to it's cluster centers
     *
     * @param predictedClustersOfEachDataPoints predicted clusters from the model for data points
     * @param clusterCenters vector array of cluster centers
     * @param data data points
     * @return Map<Integer, List<Double>> containing double Lists of distances of each cluster mapped with their cluster
     *         Indexes
     */
    public static Map<Integer, List<Double>> getDistancesToDataPoints(JavaRDD<Integer> predictedClustersOfEachDataPoints,
                                                                      org.apache.spark.mllib.linalg.Vector[] clusterCenters, JavaRDD<Vector> data) {

        // convert predicted clusters JAVARDD into a List
        List<Integer> predictedClusters = predictedClustersOfEachDataPoints.collect();

        // creating the distance Map to store the distances of each points with their cluster centers
        Map<Integer, List<Double>> distancesMap = new HashMap<Integer, List<Double>>();

        // creating the map with respect to each cluster
        for (int clusterIndex = 0; clusterIndex < clusterCenters.length; clusterIndex++) {

            List<Double> distancesList = new ArrayList<Double>();
            distancesMap.put(clusterIndex, distancesList);
        }

        // convert data JAVARDD into a List
        List<Vector> dataList = data.collect();
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

        return distancesMap;
    }

    /**
     * This method returns cluster centers of a given kmeans model
     *
     * @param distanceMap distance Map of each cluster
     * @param percentileValue percentile value to identify the cluster boundaries
     * @return Map<Integer, Double> containing percentile distance values mapped with their respective cluster Indexes
     */
    public Map<Integer, Double> getPercentileDistances(Map<Integer, List<Double>> distanceMap, double percentileValue) {

        // Get a DescriptiveStatistics instance
        DescriptiveStatistics stats = new DescriptiveStatistics();
        Map<Integer, Double> percentilesMap = new HashMap<Integer, Double>();

        // calculating percentile distance of each cluster
        for (int clusterIndex = 0; clusterIndex < distanceMap.size(); clusterIndex++) {

            for (double distance : distanceMap.get(clusterIndex)) {
                stats.addValue(distance);
            }

            double percentileDistance = stats.getPercentile(percentileValue);
            percentilesMap.put(clusterIndex, percentileDistance);
            stats.clear();
        }
        return percentilesMap;
    }

}
