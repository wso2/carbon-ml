/*
 *
 *  * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.carbon.ml.model;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;

public class KMeans {

    /**
     * @param data           JavaRDD containing feature vectors
     * @param noOfClusters   No of clusters
     * @param noOfIterations No of iterations
     * @throws ModelServiceException
     */
    public KMeansModel train(JavaRDD<Vector> data, int noOfClusters, int noOfIterations)
            throws ModelServiceException {
        return org.apache.spark.mllib.clustering.KMeans.train(data.rdd(), noOfClusters, noOfIterations);
    }

    /**
     * @param kMeansModel KMeans model
     * @param data        JavaRDD containing feature vectors
     * @return JavaRDD containing cluster centers
     * @throws ModelServiceException
     */
    public JavaRDD<Integer> test(KMeansModel kMeansModel,
                                 JavaRDD<Vector> data) throws ModelServiceException {
        return kMeansModel.predict(data);
    }
}
