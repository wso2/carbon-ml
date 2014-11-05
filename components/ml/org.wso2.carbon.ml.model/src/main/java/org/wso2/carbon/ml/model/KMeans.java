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
