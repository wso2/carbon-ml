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

package org.wso2.carbon.ml.model.spark.algorithms;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.optimization.L1Updater;
import org.apache.spark.mllib.optimization.LBFGS;
import org.apache.spark.mllib.optimization.LogisticGradient;
import org.apache.spark.mllib.optimization.SquaredL2Updater;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import org.json.JSONArray;
import org.wso2.carbon.ml.model.spark.dto.PredictedVsActual;
import org.wso2.carbon.ml.model.spark.dto.ProbabilisticClassificationModelSummary;
import scala.Tuple2;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.DECIMAL_FORMAT;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.L1;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.L2;

public class LogisticRegression implements Serializable {

    /**
     * TODO add another overloaded method to avoid Regularization.
     * This method uses stochastic gradient descent (SGD) algorithm to train a logistic regression model
     *
     * @param trainingDataset             Training dataset as a JavaRDD of labeled points
     * @param noOfIterations              No of iterations
     * @param initialLearningRate         Initial learning rate
     * @param regularizationType          Regularization type : L1 or L2
     * @param regularizationParameter     Regularization parameter
     * @param dataFractionPerSGDIteration Data fraction per SGD iteration
     * @return Logistic regression model
     */
    public LogisticRegressionModel trainWithSGD(JavaRDD<LabeledPoint> trainingDataset, double initialLearningRate,
            int noOfIterations, String regularizationType, double regularizationParameter,
            double dataFractionPerSGDIteration) {
        LogisticRegressionWithSGD lrSGD = new LogisticRegressionWithSGD(initialLearningRate,
                noOfIterations, regularizationParameter, dataFractionPerSGDIteration);
        if (L1.equals(regularizationType)) {
            lrSGD.optimizer().setUpdater(new L1Updater());
        } else if (L2.equals(regularizationType)) {
            lrSGD.optimizer().setUpdater(new SquaredL2Updater());
        }
        lrSGD.setIntercept(true);
        return lrSGD.run(trainingDataset.rdd());
    }

    /**
     * This method uses LBFGS optimizer to train a logistic regression model for a given dataset
     *
     * @param trainingDataset         Training dataset as a JavaRDD of labeled points
     * @param noOfCorrections         No of corrections : Default 10
     * @param convergenceTolerance    Convergence tolerance
     * @param noOfIterations          No of iterations
     * @param regularizationParameter Regularization parameter
     * @return Logistic regression model
     */
    public LogisticRegressionModel trainWithLBFGS(JavaRDD<LabeledPoint> trainingDataset, int noOfCorrections,
            double convergenceTolerance, int noOfIterations, double regularizationParameter) {
        int numFeatures = trainingDataset.take(1).get(0).features().size();
        JavaRDD<Tuple2<Object, Vector>> training = trainingDataset.map(
                new Function<LabeledPoint, Tuple2<Object, Vector>>() {
                    public Tuple2<Object, Vector> call(LabeledPoint p) {
                        return new Tuple2<Object, Vector>(p.label(),
                                MLUtils.appendBias(p.features()));
                    }
                });
        training.cache();
        Vector initialWeightsWithIntercept = Vectors.dense(new double[numFeatures + 1]);
        Tuple2<Vector, double[]> result = LBFGS.runLBFGS(training.rdd(), new LogisticGradient(),
                new SquaredL2Updater(), noOfCorrections, convergenceTolerance, noOfIterations,
                regularizationParameter, initialWeightsWithIntercept);
        Vector weightsWithIntercept = result._1();
        return new LogisticRegressionModel(
                Vectors.dense(Arrays.copyOf(weightsWithIntercept.toArray(),
                        weightsWithIntercept.size() - 1)), (weightsWithIntercept.toArray())
                [weightsWithIntercept.size() - 1]);
    }

    /**
     * This method performs a binary classification using a given model and a dataset
     *
     * @param logisticRegressionModel Logistic regression model
     * @param testingDataset          Testing dataset as a JavaRDD of LabeledPoints
     * @return Tuple2 containing scores and labels
     */
    public JavaRDD<Tuple2<Object, Object>> test(final LogisticRegressionModel logisticRegressionModel,
            JavaRDD<LabeledPoint> testingDataset) {
        return testingDataset.map(
                new Function<LabeledPoint, Tuple2<Object, Object>>() {
                    public Tuple2<Object, Object> call(LabeledPoint labeledPoint) {
                        Double score = logisticRegressionModel.predict(labeledPoint.features());
                        return new Tuple2<Object, Object>(score, labeledPoint.label());
                    }
                }
        );
    }

}
