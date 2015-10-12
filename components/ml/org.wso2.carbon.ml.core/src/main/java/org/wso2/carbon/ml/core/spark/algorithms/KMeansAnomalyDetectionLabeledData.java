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

public class KMeansAnomalyDetectionLabeledData implements Serializable {

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
    public Map<Integer, List<Double>> getDistancesToDataPoints(JavaRDD<Integer> predictedClustersOfEachDataPoints,
            Vector[] clusterCenters, JavaRDD<Vector> data) {

        return SparkModelUtils.getDistancesToDataPoints(predictedClustersOfEachDataPoints, clusterCenters, data);
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

    /**
     * This method returns cluster centers of a given kmeans model
     *
     * @param normalTestDataDistanceMap distance Map of normal test data points
     * @param anomalyTestDataDistanceMap distance Map of anomaly test data points
     * @param percentilesMap percentile values Map
     * @param newNormalLabel new normal label value
     * @param newAnomalyLabel new anomaly label value
     * @return MulticlassConfusionMatrix containing multiclassconfusionmatrix of test results
     */
    public MulticlassConfusionMatrix getEvaluationResults(Map<Integer, List<Double>> normalTestDataDistanceMap,
            Map<Integer, List<Double>> anomalyTestDataDistanceMap, Map<Integer, Double> percentilesMap,
            String newNormalLabel, String newAnomalyLabel) {

        MulticlassConfusionMatrix multiclassConfusionMatrix = new MulticlassConfusionMatrix();
        double truePositive = 0;
        double trueNegetive = 0;
        double falsePositive = 0;
        double falseNegetive = 0;

        // evaluating testNormal data
        for (int clusterIndex = 0; clusterIndex < percentilesMap.size(); clusterIndex++) {

            for (double distance : normalTestDataDistanceMap.get(clusterIndex)) {

                if (distance > percentilesMap.get(clusterIndex)) {
                    falsePositive++;
                } else {
                    trueNegetive++;
                }
            }
        }

        // evaluating testAnomaly data
        for (int clusterIndex = 0; clusterIndex < percentilesMap.size(); clusterIndex++) {

            for (double distance : anomalyTestDataDistanceMap.get(clusterIndex)) {

                if (distance > percentilesMap.get(clusterIndex)) {
                    truePositive++;
                } else {
                    falseNegetive++;
                }
            }
        }

        double[][] matrix = new double[2][2];
        matrix[0][0] = truePositive;
        matrix[0][1] = falseNegetive;
        matrix[1][0] = falsePositive;
        matrix[1][1] = trueNegetive;
        multiclassConfusionMatrix.setMatrix(matrix);

        List<String> labels = new ArrayList<String>();
        labels.add(0, newNormalLabel);
        labels.add(1, newAnomalyLabel);
        multiclassConfusionMatrix.setLabels(labels);
        multiclassConfusionMatrix.setSize(2);
        multiclassConfusionMatrix.setAccuracyMeasures();

        return multiclassConfusionMatrix;

    }
}
