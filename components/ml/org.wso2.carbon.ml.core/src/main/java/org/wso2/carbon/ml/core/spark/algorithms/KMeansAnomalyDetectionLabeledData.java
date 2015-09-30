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
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.wso2.carbon.ml.core.spark.MulticlassConfusionMatrix;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    public double[][] getDistancesToDataPoints(JavaRDD<Integer> predictedCenters,
                                               Vector[] clusterCenters, JavaRDD<Vector> data) {

        int[] count = new int[clusterCenters.length];
        List<Integer> predictCenters = predictedCenters.collect();

        for (int center : predictCenters) {
            count[center]++;
        }

        double[][] distancesArray = new double[clusterCenters.length][];
        for (int i = 0; i < clusterCenters.length; i++) {
            distancesArray[i] = new double[count[i]];
        }

        List<Vector> dataList = data.collect();
        EuclideanDistance distance = new EuclideanDistance();
        int center;

        for (int i = 0; i < dataList.size(); i++) {
            center = predictCenters.get(i);
            count[center]--;
            distancesArray[center][count[center]] = distance.compute(dataList.get(i).toArray(),
                    clusterCenters[center].toArray());

        }

        return distancesArray;
    }

    public double[][] getDistancesToDataPoints(List<Integer> predictCenters,
                                               Vector[] clusterCenters, List<Vector> dataList) {

        int[] count = new int[clusterCenters.length];


        for (int center : predictCenters) {
            count[center]++;
        }

        double[][] distancesArray = new double[clusterCenters.length][];
        for (int i = 0; i < clusterCenters.length; i++) {
            distancesArray[i] = new double[count[i]];
        }


        EuclideanDistance distance = new EuclideanDistance();
        int center;

        for (int i = 0; i < dataList.size(); i++) {
            center = predictCenters.get(i);
            count[center]--;
            distancesArray[center][count[center]] = distance.compute(dataList.get(i).toArray(),
                    clusterCenters[center].toArray());

        }

        return distancesArray;
    }

/*    public double[] getPercentileDistances(double[][] trainDistances, double percentileValue){

        // Get a DescriptiveStatistics instance
        DescriptiveStatistics stats = new DescriptiveStatistics();
        double[] percentiles = new double[trainDistances.length];

        for (int i = 0; i < percentiles.length; i++) {

            // Add the data from the array
            for (int j = 0; j < trainDistances[i].length; j++) {
                stats.addValue(trainDistances[i][j]);
            }

            percentiles[i] = stats.getPercentile(percentileValue);
            stats.clear();
        }
        return percentiles;
    }*/

    public double[] getPercentileDistances(final double[][] trainDistances, double percentileValue){

        // Get a DescriptiveStatistics instance
        final DescriptiveStatistics stats = new DescriptiveStatistics();
        double[] percentiles = new double[trainDistances.length];

        for (int i = 0; i < percentiles.length; i++) {

            // Add the data from the array
            ExecutorService executor = Executors.newFixedThreadPool(3);
            try {
                //Set<Future<String>> printTaskFutures = new HashSet<Future<String>>();
                for (final double distance : trainDistances[i]) {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                stats.addValue(distance);
                            } catch (Exception ex) {
                                // error management logic
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            } finally {
                if (executor != null) {
                    executor.shutdownNow();
                }
            }

            //stats.addValue(trainDistances[i][j]);

            // wait for all of the executor threads to finish
            executor.shutdown();
            percentiles[i] = stats.getPercentile(percentileValue);
            stats.clear();
        }
        return percentiles;
    }

    public MulticlassConfusionMatrix getEvaluationResults(double[][] testNormalDistances, double[][] testAnomalyDistances, double[] percentiles){

       // ConfusionMatrix confusionMatrix = new ConfusionMatrix();
        MulticlassConfusionMatrix multiclassConfusionMatrix = new MulticlassConfusionMatrix();
        double truePositive = 0;
        double trueNegetive = 0;
        double falsePositive = 0;
        double falseNegetive = 0;

//        Tuple2<Object,Object> t = new Tuple2<Object, Object>(1,2);
//
//        JavaRDD<Tuple2<Object, Object>> results;
//        results = new Tuple2<1,2>()

        //evaluating testNormal data
        for(int i=0; i<percentiles.length; i++){
            for(int j=0; j<testNormalDistances[i].length; j++){
                if(testNormalDistances[i][j] > percentiles[i]){
                    falsePositive++;
                }
                else {
                    trueNegetive++;
                }
            }
        }

        //evaluating testAnomaly data
        for(int i=0; i<percentiles.length; i++){
            for(int j=0; j<testAnomalyDistances[i].length; j++){
                if(testAnomalyDistances[i][j] > percentiles[i]){
                    truePositive++;
                }
                else {
                    falseNegetive++;
                }
            }
        }

/*        confusionMatrix.setTruePositives(truePositive);
        confusionMatrix.setTrueNegatives(trueNegetive);
        confusionMatrix.setFalsePositives(falsePositive);
        confusionMatrix.setFalseNegatives(falseNegetive);*/

        double[][] matrix = new double[2][2];
        matrix[0][0] = truePositive;
        matrix[0][1] = falseNegetive;
        matrix[1][0] = falsePositive;
        matrix[1][1] = trueNegetive;
        multiclassConfusionMatrix.setMatrix(matrix);

        List<String> labels = new ArrayList<String>();
        labels.add(0,"Anomaly");
        labels.add(1,"Normal");
        multiclassConfusionMatrix.setLabels(labels);
        multiclassConfusionMatrix.setSize(2);
        multiclassConfusionMatrix.setAccuracyMeasures();

        return multiclassConfusionMatrix;

    }
}
