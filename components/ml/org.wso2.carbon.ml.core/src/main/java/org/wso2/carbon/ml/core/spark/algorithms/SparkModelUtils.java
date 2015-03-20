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

package org.wso2.carbon.ml.core.spark.algorithms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.stat.MultivariateStatisticalSummary;
import org.apache.spark.mllib.stat.Statistics;
import org.json.JSONArray;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.DatasetPreProcessingException;
import org.wso2.carbon.ml.core.spark.summary.ClassClassificationAndRegressionModelSummary;
import org.wso2.carbon.ml.core.spark.summary.PredictedVsActual;
import org.wso2.carbon.ml.core.spark.summary.ProbabilisticClassificationModelSummary;
import org.wso2.carbon.ml.core.spark.transformations.DiscardedRowsFilter;
import org.wso2.carbon.ml.core.spark.transformations.HeaderFilter;
import org.wso2.carbon.ml.core.spark.transformations.LineToTokens;
import org.wso2.carbon.ml.core.spark.transformations.MeanImputation;
import org.wso2.carbon.ml.core.spark.transformations.MissingValuesFilter;
import org.wso2.carbon.ml.core.spark.transformations.StringArrayToDoubleArray;
import org.wso2.carbon.ml.core.spark.transformations.TokensToVectors;
import org.wso2.carbon.ml.core.utils.MLUtils;

import akka.event.slf4j.Logger;
import scala.Tuple2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SparkModelUtils {
    private static final Log log = LogFactory.getLog(SparkModelUtils.class);

    /**
     * Private constructor to prevent any other class from instantiating.
     */
    private SparkModelUtils() {
    }

    /**
     * A utility method to generate probabilistic classification model summary
     *
     * @param scoresAndLabels   Tuple2 containing scores and labels
     * @return                  Probabilistic classification model summary
     */
    public static ProbabilisticClassificationModelSummary generateProbabilisticClassificationModelSummary(
            JavaRDD<Tuple2<Object, Object>> scoresAndLabels) {
        ProbabilisticClassificationModelSummary probabilisticClassificationModelSummary =
            new ProbabilisticClassificationModelSummary();
        // store predictions and actuals
        List<PredictedVsActual> predictedVsActuals = new ArrayList<PredictedVsActual>();
        DecimalFormat decimalFormat = new DecimalFormat(MLConstants.DECIMAL_FORMAT);
        for (Tuple2<Object, Object> scoreAndLabel : scoresAndLabels.collect()) {
            PredictedVsActual predictedVsActual = new PredictedVsActual();
            predictedVsActual.setPredicted(Double.parseDouble(decimalFormat.format(scoreAndLabel._1())));
            predictedVsActual.setActual(Double.parseDouble(decimalFormat.format(scoreAndLabel._2())));
            predictedVsActuals.add(predictedVsActual);
            if (log.isTraceEnabled()) {
                log.trace("Predicted: "+predictedVsActual.getPredicted() + " ------ Actual: "+predictedVsActual.getActual());
            }
        }
        probabilisticClassificationModelSummary.setPredictedVsActuals(predictedVsActuals);
        // generate binary classification metrics
        BinaryClassificationMetrics metrics = new BinaryClassificationMetrics(JavaRDD.toRDD(scoresAndLabels));
        // store AUC
        probabilisticClassificationModelSummary.setAuc(metrics.areaUnderROC());
        // store ROC data points
        List<Tuple2<Object, Object>> rocData = metrics.roc().toJavaRDD().collect();
        JSONArray rocPoints = new JSONArray();
        for (int i = 0; i < rocData.size(); i += 1) {
            JSONArray point = new JSONArray();
            point.put(decimalFormat.format(rocData.get(i)._1()));
            point.put(decimalFormat.format(rocData.get(i)._2()));
            rocPoints.put(point);
        }
        probabilisticClassificationModelSummary.setRoc(rocPoints.toString());
        return probabilisticClassificationModelSummary;
    }

    /**
     * A utility method to generate regression model summary
     *
     * @param predictionsAndLabels  Tuple2 containing predicted and actual values
     * @return                      Regression model summary
     */
    public static ClassClassificationAndRegressionModelSummary generateRegressionModelSummary(
            JavaRDD<Tuple2<Double, Double>> predictionsAndLabels) {
        ClassClassificationAndRegressionModelSummary regressionModelSummary =
                new ClassClassificationAndRegressionModelSummary();
        // store predictions and actuals
        List<PredictedVsActual> predictedVsActuals = new ArrayList<PredictedVsActual>();
        DecimalFormat decimalFormat = new DecimalFormat(MLConstants.DECIMAL_FORMAT);
        for (Tuple2<Double, Double> scoreAndLabel : predictionsAndLabels.collect()) {
            PredictedVsActual predictedVsActual = new PredictedVsActual();
            predictedVsActual.setPredicted(Double.parseDouble(decimalFormat.format(scoreAndLabel._1())));
            predictedVsActual.setActual(Double.parseDouble(decimalFormat.format(scoreAndLabel._2())));
            predictedVsActuals.add(predictedVsActual);
        }
        regressionModelSummary.setPredictedVsActuals(predictedVsActuals);
        // calculate mean squared error (MSE)
        double meanSquaredError = new JavaDoubleRDD(predictionsAndLabels.map(
                new Function<Tuple2<Double, Double>, Object>() {
                    public Object call(Tuple2<Double, Double> pair) {
                        return Math.pow(pair._1() - pair._2(), 2.0);
                    }
                }
        ).rdd()).mean();
        regressionModelSummary.setError(meanSquaredError);
        return regressionModelSummary;
    }

    /**
     * A utility method to generate class classification model summary
     *
     * @param predictionsAndLabels Predictions and actual labels
     * @return Class classification model summary
     */
    public static ClassClassificationAndRegressionModelSummary getClassClassificationModelSummary(
            JavaPairRDD<Double, Double> predictionsAndLabels) {
        ClassClassificationAndRegressionModelSummary classClassificationModelSummary = new
                ClassClassificationAndRegressionModelSummary();
        // store predictions and actuals
        List<PredictedVsActual> predictedVsActuals = new ArrayList<PredictedVsActual>();
        for (Tuple2<Double, Double> scoreAndLabel : predictionsAndLabels.collect()) {
            PredictedVsActual predictedVsActual = new PredictedVsActual();
            predictedVsActual.setPredicted(scoreAndLabel._1());
            predictedVsActual.setActual(scoreAndLabel._2());
            predictedVsActuals.add(predictedVsActual);
        }
        classClassificationModelSummary.setPredictedVsActuals(predictedVsActuals);
        // calculate test error
        double error = 1.0 * predictionsAndLabels.filter(new Function<Tuple2<Double, Double>, Boolean>() {
            @Override
            public Boolean call(Tuple2<Double, Double> pl) {
                return !pl._1().equals(pl._2());
            }
        }).count() / predictionsAndLabels.count();
        classClassificationModelSummary.setError(error);
        return classClassificationModelSummary;
    }

    /**
     * A utility method to pre-process data
     *
     * @param sc              JavaSparkContext
     * @param workflow        Machine learning workflow
     * @param lines           JavaRDD of strings
     * @param headerRow       HeaderFilter row
     * @param columnSeparator Column separator
     * @return Returns a JavaRDD of doubles
     * @throws org.wso2.carbon.ml.model.exceptions.ModelServiceException
     */
    public static JavaRDD<double[]> preProcess(JavaSparkContext sc, Workflow workflow, JavaRDD<String>
            lines, String headerRow, String columnSeparator) throws DatasetPreProcessingException {
            HeaderFilter headerFilter = new HeaderFilter(headerRow);
            JavaRDD<String> data = lines.filter(headerFilter);
            Pattern pattern = Pattern.compile(columnSeparator);
            LineToTokens lineToTokens = new LineToTokens(pattern);
            JavaRDD<String[]> tokens = data.map(lineToTokens);
            // get feature indices for discard imputation
            DiscardedRowsFilter discardedRowsFilter = new DiscardedRowsFilter(
                    MLUtils.getImputeFeatureIndices(workflow, MLConstants.DISCARD));
            // Discard the row if any of the impute indices content have a missing or NA value
            JavaRDD<String[]> tokensDiscardedRemoved = tokens.filter(discardedRowsFilter);
            JavaRDD<double[]> features = null;
            // get feature indices for mean imputation
            List<Integer> meanImputeIndices = MLUtils.getImputeFeatureIndices(workflow, MLConstants
                    .MEAN_IMPUTATION);
            if (meanImputeIndices.size() > 0) {
                // calculate means for the whole dataset (sampleFraction = 1.0) or a sample
                Map<Integer, Double> means = getMeans(sc, tokensDiscardedRemoved, meanImputeIndices, 0.01);
                // Replace missing values in impute indices with the mean for that column
                MeanImputation meanImputation = new MeanImputation(means);
                features = tokensDiscardedRemoved.map(meanImputation);
            } else {
                /**
                 * Mean imputation mapper will convert string tokens to doubles as a part of the
                 * operation. If there is no mean imputation for any columns, tokens has to be
                 * converted into doubles.
                 */
                features = tokensDiscardedRemoved.map(new StringArrayToDoubleArray());
            }
            return features;
       
    }

    /**
     * A utility method to perform mean imputation
     *
     * @param sparkContext                JavaSparkContext
     * @param tokens            JavaRDD of String[]
     * @param meanImputeIndices Indices of columns to impute
     * @param sampleFraction    Sample fraction used to calculate mean
     * @return Returns a map of impute indices and means
     * @throws                  ModelServiceException
     */
    private static Map<Integer, Double> getMeans(JavaSparkContext sparkContext, JavaRDD<String[]> tokens,
            List<Integer> meanImputeIndices, double sampleFraction) throws DatasetPreProcessingException {
        Map<Integer, Double> imputeMeans = new HashMap<Integer, Double>();
        JavaRDD<String[]> missingValuesRemoved = tokens.filter(new MissingValuesFilter());
        JavaRDD<Vector> features = null;
        // calculate mean and populate mean imputation hashmap
        TokensToVectors tokensToVectors = new TokensToVectors(meanImputeIndices);
        if (sampleFraction < 1.0) {
            features = tokens.sample(false, sampleFraction).map(tokensToVectors);
        } else {
            features = tokens.map(tokensToVectors);
        }
        MultivariateStatisticalSummary summary = Statistics.colStats(features.rdd());
        double[] means = summary.mean().toArray();
        for (int i = 0; i < means.length; i++) {
            imputeMeans.put(meanImputeIndices.get(i), means[i]);
        }
        return imputeMeans;
    }
}
