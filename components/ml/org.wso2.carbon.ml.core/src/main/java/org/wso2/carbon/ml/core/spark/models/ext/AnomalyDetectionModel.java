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
package org.wso2.carbon.ml.core.spark.models.ext;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnomalyDetectionModel model.
 */
public class AnomalyDetectionModel implements Serializable {

    private static final long serialVersionUID = 7012024887487309471L;

    private KMeansModel kMeansModel;
    private Map<Integer, List<Double>> clusterIndexTodistancesListMap;
    private String normalLabel;
    private String anomalyLabel;

    public KMeansModel getkMeansModel() {
        return kMeansModel;
    }

    public void setkMeansModel(KMeansModel kMeansModel) {
        this.kMeansModel = kMeansModel;
    }

    public Map<Integer, List<Double>> getClusterIndexTodistancesListMap() {
        return clusterIndexTodistancesListMap;
    }

    public void setClusterIndexTodistancesListMap(Map<Integer, List<Double>> clusterIndexTodistancesListMap) {
        this.clusterIndexTodistancesListMap = clusterIndexTodistancesListMap;
    }

    public String getNormalLabel() {
        return normalLabel;
    }

    public void setNormalLabel(String normalLabel) {
        this.normalLabel = normalLabel;
    }

    public String getAnomalyLabel() {
        return anomalyLabel;
    }

    public void setAnomalyLabel(String anomalyLabel) {
        this.anomalyLabel = anomalyLabel;
    }

    /**
     * This method applies a anomaly detection model to a given dataset
     *
     * @param data a single data point as a Vector
     * @param percentile percentile value to identify the cluster boundaries
     * @return prediction label as a String
     */
    public String predict(Vector data, double percentile) {

        String predictions;
        int predictedCluster = kMeansModel.predict(data);

        int clusterIndex = predictedCluster;
        double clusterBoundary = getPercentileDistance(percentile, clusterIndex);

        predictions = getPredictedValue(data, clusterIndex, clusterBoundary);

        return predictions;
    }

    /**
     * This method applies a anomaly detection model to a given dataset
     *
     * @param data JavaRDD containing feature vectors
     * @param percentile percentile value to identify the cluster boundaries
     * @return prediction labels as a List of Strings
     */
    public List<String> predict(JavaRDD<Vector> data, double percentile) {

        List<String> predictions = new ArrayList<String>();
        // convert data JAVARDD into a List
        List<Vector> dataList = data.collect();
        List<Integer> predictedClusters = kMeansModel.predict(data).collect();
        Map<Integer, Double> percentilesMap = getPercentileDistancesMap(percentile);

        for (int i = 0; i < dataList.size(); i++) {

            int clusterIndex = predictedClusters.get(i);
            double clusterBoundary = percentilesMap.get(clusterIndex);

            String prediction = getPredictedValue(dataList.get(i), clusterIndex, clusterBoundary);
            predictions.add(prediction);
        }

        return predictions;
    }

    /**
     * This method applies a anomaly detection model to a given dataset for a range of percentile values
     *
     * @param data a single data point as a Vector
     * @param minPercentile min percentile value of the range
     * @param maxPercentile max percentile value of the range
     * @return Map<Integer, String> key:percentile value:prediction label
     */
    public Map<Integer, String> predict(Vector data, int minPercentile, int maxPercentile) {

        /*
         * key : percentile value
         * value : prediction label
         */
        Map<Integer, String> percentileTopredictionMap = new HashMap<Integer, String>();

        int predictedCluster = kMeansModel.predict(data);

        for (int percentile = minPercentile; percentile <= maxPercentile; percentile++) {

            int clusterIndex = predictedCluster;
            double clusterBoundary = getPercentileDistance(percentile, clusterIndex);

            String prediction = getPredictedValue(data, clusterIndex, clusterBoundary);
            percentileTopredictionMap.put(percentile, prediction);
        }

        return percentileTopredictionMap;
    }

    /**
     * This method applies a anomaly detection model to a given dataset for a range of percentile values
     *
     * @param data JavaRDD containing feature vectors
     * @param minPercentile min percentile value of the range
     * @param maxPercentile max percentile value of the range
     * @return Map<Integer, List<String>> key:percentile value:prediction labels as a List of Strings
     */
    public Map<Integer, List<String>> predict(JavaRDD<Vector> data, int minPercentile, int maxPercentile) {

        /*
         * key : percentile value
         * value : predictions List
         */
        Map<Integer, List<String>> percentileTopredictionsListMap = new HashMap<Integer, List<String>>();
        // convert data JAVARDD into a List
        List<Vector> dataList = data.collect();
        List<Integer> predictedClusters = kMeansModel.predict(data).collect();

        for (int percentile = minPercentile; percentile <= maxPercentile; percentile++) {

            List<String> predictionsList = new ArrayList<String>();
            Map<Integer, Double> percentilesMap = getPercentileDistancesMap(percentile);

            for (int i = 0; i < dataList.size(); i++) {

                int clusterIndex = predictedClusters.get(i);
                double clusterBoundary = percentilesMap.get(clusterIndex);

                String prediction = getPredictedValue(dataList.get(i), clusterIndex, clusterBoundary);
                predictionsList.add(prediction);
            }
            percentileTopredictionsListMap.put(percentile, predictionsList);
        }

        return percentileTopredictionsListMap;
    }

    /**
     * This method is to predict the label of a given data point
     */
    private String getPredictedValue(Vector dataPointVector, int clusterIndex, double clusterBoundary) {

        String prediction;
        EuclideanDistance euclideanDistance = new EuclideanDistance();
        Vector[] clusterCenters = kMeansModel.clusterCenters();

        double[] dataPoint = dataPointVector.toArray();
        double[] clusterCenter = clusterCenters[clusterIndex].toArray();
        double distance = euclideanDistance.compute(clusterCenter, dataPoint);

        if (distance > clusterBoundary) {
            prediction = anomalyLabel;
        } else {
            prediction = normalLabel;
        }

        return prediction;
    }

    /**
     * This method is to get the percentile distances map
     */
    private Map<Integer, Double> getPercentileDistancesMap(double percentileValue) {

        // Get a DescriptiveStatistics instance
        DescriptiveStatistics stats = new DescriptiveStatistics();
        /*
         * key : percentile value
         * value : distance value
         */
        Map<Integer, Double> percentilesMap = new HashMap<Integer, Double>();

        // calculating percentile distance of each cluster
        for (int clusterIndex = 0; clusterIndex < clusterIndexTodistancesListMap.size(); clusterIndex++) {

            for (double distance : clusterIndexTodistancesListMap.get(clusterIndex)) {
                stats.addValue(distance);
            }

            double percentileDistance = stats.getPercentile(percentileValue);
            percentilesMap.put(clusterIndex, percentileDistance);
            stats.clear();
        }

        return percentilesMap;
    }

    /**
     * This method is to get the percentile distance to a given cluster
     */
    private double getPercentileDistance(double percentileValue, int clusterIndex) {

        // Get a DescriptiveStatistics instance
        DescriptiveStatistics stats = new DescriptiveStatistics();

        // calculating percentile distance
        for (double distance : clusterIndexTodistancesListMap.get(clusterIndex)) {
            stats.addValue(distance);
        }
        double percentileDistance = stats.getPercentile(percentileValue);
        stats.clear();

        return percentileDistance;
    }
}
