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

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.mllib.regression.LinearRegressionWithSGD;
import scala.Tuple2;

import java.io.Serializable;

public class LinearRegression implements Serializable {

    private static final long serialVersionUID = -5137378340857656687L;

    /**
     * This method uses stochastic gradient descent (SGD) algorithm to train a linear regression model
     *
     * @param trainingDataset       Training dataset as a JavaRDD of LabeledPoints
     * @param noOfIterations        Number of iterarations
     * @param initialLearningRate   Initial learning rate (SGD step size)
     * @param miniBatchFraction     SGD minibatch fraction
     * @return                      Linear regression model
     */
    public LinearRegressionModel train(JavaRDD<LabeledPoint> trainingDataset, int noOfIterations,
            double initialLearningRate, double miniBatchFraction) {
        return LinearRegressionWithSGD.train(trainingDataset.rdd(), noOfIterations, initialLearningRate,
                miniBatchFraction);
    }

    /**
     * Linear regression train - overload method with 1 parameter
     *
     * @param trainingDataset   Training dataset as a JavaRDD of LabeledPoints
     * @param noOfIterations    Number of iterarations
     * @return                  Linear regression model
     */
    public LinearRegressionModel train(JavaRDD<LabeledPoint> trainingDataset, int noOfIterations) {
        return LinearRegressionWithSGD.train(trainingDataset.rdd(), noOfIterations);
    }

    /**
     * This method applies linear regression using a given model and a dataset
     *
     * @param linearRegressionModel Linear regression model
     * @param testingDataset        Testing dataset as a JavaRDD of LabeledPoints
     * @return                      Tuple2 containing predicted values and labels
     */
    public JavaRDD<Tuple2<Double, Double>> test(final LinearRegressionModel linearRegressionModel,
            JavaRDD<LabeledPoint> testingDataset) {
        return testingDataset.map(
                new Function<LabeledPoint, Tuple2<Double, Double>>() {
                    private static final long serialVersionUID = 2027559237268104710L;

                    public Tuple2<Double, Double> call(LabeledPoint labeledPoint) {
                        Double predicted = linearRegressionModel.predict(labeledPoint.features());
                        return new Tuple2<Double, Double>(predicted, labeledPoint.label());
                    }
                }
        );
    }
}
