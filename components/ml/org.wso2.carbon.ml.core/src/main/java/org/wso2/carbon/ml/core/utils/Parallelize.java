package org.wso2.carbon.ml.core.utils;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;

import java.util.List;

/**
 * Created by pekasa on 09.06.16.
 */
public class Parallelize {
    /**
     * This Class makes sure that no Multiple Sparkcontext run Simulatneously
     */
    static public JavaSparkContext cxt = null;

    // Intialize JavaSparkContext if not set yet.
    static public JavaSparkContext parallelizeList(){
        if (cxt == null) {
            // Set up context
            SparkConf conf = new SparkConf().setAppName("Ensemble").setMaster("local");
            cxt = new JavaSparkContext(conf);
        }

        return cxt;
    }


    // Helper method to convert data to JavaRDD

   static public JavaRDD<LabeledPoint> convertToJavaRDD(List<LabeledPoint> labeledList){
       if (cxt == null) {
           parallelizeList();
       }

        JavaRDD<LabeledPoint> javaRDDLabeledList = cxt.parallelize(labeledList);

        return javaRDDLabeledList;

    }
}
