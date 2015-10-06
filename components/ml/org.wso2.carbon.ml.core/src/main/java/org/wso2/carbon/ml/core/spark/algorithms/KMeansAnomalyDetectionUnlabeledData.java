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

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;

public class KMeansAnomalyDetectionUnlabeledData implements Serializable {

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

    public double[][] getDistancesToDataPoints(JavaRDD<Integer> predictedCenters, Vector[] clusterCenters,
            JavaRDD<Vector> data) {

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
}
