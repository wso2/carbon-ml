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

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.wso2.carbon.ml.core.spark.summary.DeeplearningModelSummary;
import org.wso2.carbon.ml.core.spark.summary.PredictedVsActual;
import org.wso2.carbon.ml.core.spark.summary.TestResultDataPoint;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

import scala.Tuple2;
import water.fvec.Frame;
import water.fvec.Vec;

public class DeeplearningModelUtils {

    /**
     * A utility method to generate class classification model summary
     * 
     * @param predictionsAndLabels Predictions and actual labels
     * @return Class classification model summary
     */
    public static DeeplearningModelSummary getDeeplearningModelSummary(JavaSparkContext sparkContext,
            JavaRDD<LabeledPoint> testingData, JavaPairRDD<Double, Double> predictionsAndLabels) {
        DeeplearningModelSummary deeplearningModelSummary = new DeeplearningModelSummary();
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
            if (labeledPoint != null && labeledPoint.features() != null) {
                double[] rowFeatures = labeledPoint.features().toArray();
                features.add(rowFeatures);
            } else {
                continue;
            }
        }
        // create a list of feature values with predicted vs. actuals
        List<TestResultDataPoint> testResultDataPoints = new ArrayList<TestResultDataPoint>();
        for (int i = 0; i < features.size(); i++) {
            TestResultDataPoint testResultDataPoint = new TestResultDataPoint();
            testResultDataPoint.setPredictedVsActual(predictedVsActuals.get(i));
            testResultDataPoint.setFeatureValues(features.get(i));
            testResultDataPoints.add(testResultDataPoint);
        }
        // covert List to JavaRDD
        JavaRDD<TestResultDataPoint> testResultDataPointsJavaRDD = sparkContext.parallelize(testResultDataPoints);
        // collect RDD as a sampled list
        List<TestResultDataPoint> testResultDataPointsSample;
        if (testResultDataPointsJavaRDD.count() > MLCoreServiceValueHolder.getInstance().getSummaryStatSettings()
                .getSampleSize()) {
            testResultDataPointsSample = testResultDataPointsJavaRDD.takeSample(true, MLCoreServiceValueHolder
                    .getInstance().getSummaryStatSettings().getSampleSize());
        } else {
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

    /**
     * Convert a JavaRDD to a H2O Frame
     * 
     * @param data Data to be converted to a Frame
     * @return Frame with training data
     */
    public static Frame JavaRDDtoFrame(JavaRDD<LabeledPoint> data) {
        List<LabeledPoint> list = data.collect();
        Vec[] allVecs = new Vec[list.get(0).features().size() + 1];

        for (int i = 0; i < list.get(0).features().size() + 1; i++) {
            if (i < list.get(0).features().size()) {
                Vec v = Vec.makeZero(list.size());
                for (int j = 0; j < list.size(); j++) {
                    v.set(j, list.get(j).features().toArray()[i]);
                }
                allVecs[i] = v;
            } else {
                Vec v = Vec.makeZero(list.size());
                for (int j = 0; j < list.size(); j++) {
                    v.set(j, (int) list.get(j).label());
                }
                allVecs[i] = v;
            }
        }

        return new Frame(allVecs);
    }

    /**
     * Convert a Double List to a H2O Frame
     * 
     * @param data Data to be converted to a Frame
     * @return Frame with training data
     */
    public static Frame DoubleArrayListtoFrame(List<double[]> data) {
        Vec[] allVecs = new Vec[data.get(0).length];

        for (int i = 0; i < data.get(0).length; i++) {
            Vec v = Vec.makeZero(data.size());
            for (int j = 0; j < data.size(); j++) {
                v.set(j, data.get(j)[i]);
            }
            allVecs[i] = v;
        }

        return new Frame(allVecs);
    }
}
