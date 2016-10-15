package org.wso2.carbon.ml.core.spark.algorithms;

import java.util.ArrayList;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.SparkConf;
import org.apache.spark.mllib.linalg.Vectors;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;

import org.apache.spark.mllib.regression.LabeledPoint;

public class SequencialDataSplitTest {

    SparkConf sparkConf = new SparkConf().setAppName("test").setMaster("local");
    JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);
    ArrayList<LabeledPoint> labeledPoints = new ArrayList<>();

    public SequencialDataSplitTest() {
        labeledPoints.add(new LabeledPoint(1.0, Vectors.dense(1.0, 0.0, 3.0)));
        labeledPoints.add(new LabeledPoint(2.0, Vectors.dense(1.0, 0.0, 3.0)));
        labeledPoints.add(new LabeledPoint(3.0, Vectors.dense(1.0, 0.0, 3.0)));
        labeledPoints.add(new LabeledPoint(4.0, Vectors.dense(1.0, 0.0, 3.0)));
        labeledPoints.add(new LabeledPoint(5.0, Vectors.dense(1.0, 0.0, 3.0)));
        labeledPoints.add(new LabeledPoint(6.0, Vectors.dense(1.0, 0.0, 3.0)));
        labeledPoints.add(new LabeledPoint(7.0, Vectors.dense(1.0, 0.0, 3.0)));
        labeledPoints.add(new LabeledPoint(8.0, Vectors.dense(1.0, 0.0, 3.0)));
        labeledPoints.add(new LabeledPoint(9.0, Vectors.dense(1.0, 0.0, 3.0)));
        labeledPoints.add(new LabeledPoint(10.0, Vectors.dense(1.0, 0.0, 3.0)));
    }

    @Test
    public void testSequencialDataSplit() {
        JavaRDD<LabeledPoint> distData = javaSparkContext.parallelize(labeledPoints);
        MLModelConfigurationContext mlModelConfigurationContext = new MLModelConfigurationContext();
        SupervisedSparkModelBuilder supervisedSparkModelBuilder = new SupervisedSparkModelBuilder(mlModelConfigurationContext);
        ArrayList<JavaRDD<LabeledPoint>> dataSplit = supervisedSparkModelBuilder.getSplittedData(true, distData, 0.6);
        JavaRDD<LabeledPoint> testingData = dataSplit.get(1);
        Assert.assertEquals(testingData.first(), new LabeledPoint(7.0, Vectors.dense(1.0, 0.0, 3.0)));
    }
}
