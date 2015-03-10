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

package org.wso2.carbon.ml.model.spark.algorithms;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.optimization.L1Updater;
import org.apache.spark.mllib.optimization.SquaredL2Updater;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.io.Serializable;

import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.L1;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.L2;

public class SVM implements Serializable {
    /**
     * This method uses stochastic gradient descent (SGD) algorithm to train a support vector machine (SVM) model.
     *
     * @param trainingDataset         Training dataset as a JavaRDD of LabeledPoints
     * @param noOfIterations          Number of iterarations
     * @param regularizationType      Regularization type: L1 or L2
     * @param regularizationParameter Regularization parameter
     * @param initialLearningRate     Initial learning rate (SGD step size)
     * @param miniBatchFraction       SGD minibatch fraction
     * @return                        SVM model
     */
    public SVMModel train(JavaRDD<LabeledPoint> trainingDataset, int noOfIterations, String regularizationType,
            double regularizationParameter, double initialLearningRate, double miniBatchFraction) {
        SVMWithSGD svmWithSGD = new SVMWithSGD();
        if (regularizationType.equals(L1)) {
            svmWithSGD.optimizer().setUpdater(new L1Updater()).setRegParam(regularizationParameter);
        } else if (regularizationType.equals((L2))) {
            svmWithSGD.optimizer().setUpdater(new SquaredL2Updater()).setRegParam(regularizationParameter);
        }
        svmWithSGD.optimizer().setNumIterations(noOfIterations).setStepSize(initialLearningRate)
                .setMiniBatchFraction(miniBatchFraction);
        return svmWithSGD.run(trainingDataset.rdd());
    }

    /**
     * SVM train - overload method with 2 parameters.
     * 
     * @param trainingDataset           Training dataset as a JavaRDD of LabeledPoints
     * @param noOfIterations            Number of iterarations
     * @param regularizationParameter   Regularization parameter
     * @return                          SVM model
     */
    public SVMModel train(JavaRDD<LabeledPoint> trainingDataset, int noOfIterations, double regularizationParameter) {
        SVMWithSGD svmWithSGD = new SVMWithSGD();
        svmWithSGD.optimizer().setNumIterations(noOfIterations).setRegParam(regularizationParameter);
        return svmWithSGD.run(trainingDataset.rdd());
    }

    /**
     * This method performs a binary classification using a given SVM model and a dataset.
     *
     * @param svmModel          SVM model
     * @param testingDataset    Testing dataset as a JavaRDD of LabeledPoints
     * @return                  Tuple2 containing scores and labels
     */
    public JavaRDD<Tuple2<Object, Object>> test(final SVMModel svmModel, JavaRDD<LabeledPoint> testingDataset) {
        return testingDataset.map(
                new Function<LabeledPoint, Tuple2<Object, Object>>() {
                    public Tuple2<Object, Object> call(LabeledPoint labeledPoint) {
                        Double score = svmModel.predict(labeledPoint.features());
                        return new Tuple2<Object, Object>(score, labeledPoint.label());
                    }
                }
        );
    }
}
