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

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD;
import org.apache.spark.mllib.optimization.L1Updater;
import org.apache.spark.mllib.optimization.SquaredL2Updater;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.wso2.carbon.ml.commons.constants.MLConstants;

import scala.Tuple2;

import java.io.Serializable;

public class LogisticRegression implements Serializable {

    private static final long serialVersionUID = -8590453832704525074L;

    /**
     * TODO add another overloaded method to avoid Regularization.
     * This method uses stochastic gradient descent (SGD) algorithm to train a logistic regression model
     *
     * @param trainingDataset               Training dataset as a JavaRDD of labeled points
     * @param noOfIterations                No of iterations
     * @param initialLearningRate           Initial learning rate
     * @param regularizationType            Regularization type : L1 or L2
     * @param regularizationParameter       Regularization parameter
     * @param dataFractionPerSGDIteration   Data fraction per SGD iteration
     * @return                              Logistic regression model
     */
    public LogisticRegressionModel trainWithSGD(JavaRDD<LabeledPoint> trainingDataset, double initialLearningRate,
            int noOfIterations, String regularizationType, double regularizationParameter,
            double dataFractionPerSGDIteration) {
        LogisticRegressionWithSGD lrSGD = new LogisticRegressionWithSGD(initialLearningRate, noOfIterations, 
                regularizationParameter, dataFractionPerSGDIteration);
        if (MLConstants.L1.equals(regularizationType)) {
            lrSGD.optimizer().setUpdater(new L1Updater());
        } else if (MLConstants.L2.equals(regularizationType)) {
            lrSGD.optimizer().setUpdater(new SquaredL2Updater());
        }
        lrSGD.setIntercept(true);
        return lrSGD.run(trainingDataset.rdd());
    }

    /**
     * This method uses LBFGS optimizer to train a logistic regression model for a given dataset
     *
     * @param trainingDataset           Training dataset as a JavaRDD of labeled points
     * @param noOfCorrections           No of corrections : Default 10
     * @param convergenceTolerance      Convergence tolerance
     * @param noOfIterations            No of iterations
     * @param regularizationParameter   Regularization parameter
     * @return                          Logistic regression model
     */
    public LogisticRegressionModel trainWithLBFGS(JavaRDD<LabeledPoint> trainingDataset, String regularizationType,
            int noOfClasses) {
        LogisticRegressionWithLBFGS lbfgs = new LogisticRegressionWithLBFGS();
        if (MLConstants.L1.equals(regularizationType)) {
            lbfgs.optimizer().setUpdater(new L1Updater());
        } else if (MLConstants.L2.equals(regularizationType)) {
            lbfgs.optimizer().setUpdater(new SquaredL2Updater());
        }
        lbfgs.setIntercept(true);
        return lbfgs.setNumClasses(noOfClasses < 2 ? 2 : noOfClasses).run(trainingDataset.rdd());
    }

    /**
     * This method performs a binary classification using a given model and a dataset
     *
     * @param logisticRegressionModel   Logistic regression model
     * @param testingDataset            Testing dataset as a JavaRDD of LabeledPoints
     * @return                          Tuple2 containing scores and labels
     */
    public JavaRDD<Tuple2<Object, Object>> test(final LogisticRegressionModel logisticRegressionModel,
            JavaRDD<LabeledPoint> testingDataset) {
        return testingDataset.map(
                new Function<LabeledPoint, Tuple2<Object, Object>>() {
                    private static final long serialVersionUID = 910861043765821669L;

                    public Tuple2<Object, Object> call(LabeledPoint labeledPoint) {
                        Double score = logisticRegressionModel.predict(labeledPoint.features());
                        return new Tuple2<Object, Object>(score, labeledPoint.label());
                    }
                }
        );
    }
}
