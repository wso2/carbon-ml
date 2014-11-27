/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.model.spark.algorithms;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;

import java.io.Serializable;

public class KMeans implements Serializable {

    /**
     * @param data           JavaRDD containing feature vectors
     * @param noOfClusters   No of clusters
     * @param noOfIterations No of iterations
     * @throws org.wso2.carbon.ml.model.exceptions.ModelServiceException
     */
    public KMeansModel train(JavaRDD<Vector> data, int noOfClusters, int noOfIterations)
            throws ModelServiceException {
        try {
            return org.apache.spark.mllib.clustering.KMeans.train(data.rdd(), noOfClusters,
                                                                  noOfIterations);
        } catch (Exception e) {
            throw new ModelServiceException(e.getMessage(), e);
        }
    }

    /**
     * @param kMeansModel KMeans model
     * @param data        JavaRDD containing feature vectors
     * @return JavaRDD containing cluster centers
     * @throws ModelServiceException
     */
    public JavaRDD<Integer> test(KMeansModel kMeansModel,
                          JavaRDD<Vector> data) throws ModelServiceException {
        try {
            return kMeansModel.predict(data);
        } catch (Exception e) {
            throw new ModelServiceException(e.getMessage(), e);
        }
    }
}
