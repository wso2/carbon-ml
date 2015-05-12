package org.wso2.carbon.ml.core.spark.algorithms;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.RidgeRegressionModel;
import org.apache.spark.mllib.regression.RidgeRegressionWithSGD;
import scala.Tuple2;

import java.io.Serializable;

public class RidgeRegression implements Serializable {
    private static final long serialVersionUID = -4652546556448809559L;

    /**
     * This method uses stochastic gradient descent (SGD) algorithm to train a ridge regression model
     *
     * @param trainingDataset           Training dataset as a JavaRDD of LabeledPoints
     * @param noOfIterations            Number of iterarations
     * @param initialLearningRate       Initial learning rate (SGD step size)
     * @param regularizationParameter   Regularization parameter
     * @param miniBatchFraction         SGD minibatch fraction
     * @return                          Ridge regression model
     */
    public RidgeRegressionModel train(JavaRDD<LabeledPoint> trainingDataset, int noOfIterations,
            double initialLearningRate, double regularizationParameter, double miniBatchFraction) {
        return RidgeRegressionWithSGD.train(trainingDataset.rdd(), noOfIterations, initialLearningRate,
                regularizationParameter, miniBatchFraction);
    }

    /**
     * This method applies ridge regression using a given model and a dataset
     *
     * @param ridgeRegressionModel  Ridge regression model
     * @param testingDataset        Testing dataset as a JavaRDD of LabeledPoints
     * @return                      Tuple2 containing predicted values and labels
     */
    public JavaRDD<Tuple2<Double, Double>> test(final RidgeRegressionModel ridgeRegressionModel,
            JavaRDD<LabeledPoint> testingDataset) {
        return testingDataset.map(
                new Function<LabeledPoint, Tuple2<Double, Double>>() {
                    private static final long serialVersionUID = -1894306961652577838L;

                    public Tuple2<Double, Double> call(LabeledPoint labeledPoint) {
                        Double predicted = ridgeRegressionModel.predict(labeledPoint.features());
                        return new Tuple2<Double, Double>(predicted, labeledPoint.label());
                    }
                }
        );
    }
}
