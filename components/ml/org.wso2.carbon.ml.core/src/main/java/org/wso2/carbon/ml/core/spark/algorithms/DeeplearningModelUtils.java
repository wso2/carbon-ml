/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.carbon.ml.core.spark.algorithms;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.core.spark.summary.ClassClassificationAndRegressionModelSummary;
import org.wso2.carbon.ml.core.spark.summary.DeeplearningModelSummary;
import org.wso2.carbon.ml.core.spark.summary.PredictedVsActual;
import org.wso2.carbon.ml.core.spark.summary.TestResultDataPoint;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import scala.Tuple2;

/**
 *
 * @author Thush
 */
public class DeeplearningModelUtils {
    
    /**
     * A utility method to generate class classification model summary
     *
     * @param predictionsAndLabels Predictions and actual labels
     * @return Class classification model summary
     */
    public static DeeplearningModelSummary getDeeplearningModelSummary(JavaSparkContext sparkContext,
                                                                                                  JavaRDD<LabeledPoint> testingData,
                                                                                                  JavaPairRDD<Double, Double> predictionsAndLabels) {
        DeeplearningModelSummary deeplearningModelSummary = new
                DeeplearningModelSummary();
        // store predictions and actuals
        List<PredictedVsActual> predictedVsActuals = new ArrayList<PredictedVsActual>();
        for (Tuple2<Double, Double> scoreAndLabel : predictionsAndLabels.collect()) {
            PredictedVsActual predictedVsActual = new PredictedVsActual();
            predictedVsActual.setPredicted(scoreAndLabel._1());
            predictedVsActual.setActual(scoreAndLabel._2());
            predictedVsActuals.add(predictedVsActual);
        }
        // create a list of feature values
        List<double[]> features = new ArrayList<double[]>();
        for (LabeledPoint labeledPoint : testingData.collect()) {
            if(labeledPoint != null && labeledPoint.features() != null) {
                double[] rowFeatures = labeledPoint.features().toArray();
                features.add(rowFeatures);
            }
            else {
                continue;
            }
        }
        // create a list of feature values with predicted vs. actuals
        List<TestResultDataPoint> testResultDataPoints = new ArrayList<TestResultDataPoint>();
        for(int i = 0; i < features.size(); i++) {
            TestResultDataPoint testResultDataPoint = new TestResultDataPoint();
            testResultDataPoint.setPredictedVsActual(predictedVsActuals.get(i));
            testResultDataPoint.setFeatureValues(features.get(i));
            testResultDataPoints.add(testResultDataPoint);
        }
        // covert List to JavaRDD
        JavaRDD<TestResultDataPoint> testResultDataPointsJavaRDD = sparkContext.parallelize(testResultDataPoints);
        // collect RDD as a sampled list
        List<TestResultDataPoint> testResultDataPointsSample;
        if(testResultDataPointsJavaRDD.count() > MLCoreServiceValueHolder.getInstance().getSummaryStatSettings().getSampleSize()) {
            testResultDataPointsSample = testResultDataPointsJavaRDD.takeSample(true, MLCoreServiceValueHolder.getInstance().getSummaryStatSettings().getSampleSize());
        }
        else {
            testResultDataPointsSample = testResultDataPointsJavaRDD.collect();
        }
        deeplearningModelSummary.setTestResultDataPointsSample(testResultDataPointsSample);
        deeplearningModelSummary.setPredictedVsActuals(predictedVsActuals);
        // calculate test error
        double error = 1.0 * predictionsAndLabels.filter(new Function<Tuple2<Double, Double>, Boolean>() {
            private static final long serialVersionUID = -3063364114286182333L;

            @Override
            public Boolean call(Tuple2<Double, Double> pl) {
                return !pl._1().equals(pl._2());
            }
        }).count() / predictionsAndLabels.count();
        deeplearningModelSummary.setError(error);
        return deeplearningModelSummary;
    }
}
