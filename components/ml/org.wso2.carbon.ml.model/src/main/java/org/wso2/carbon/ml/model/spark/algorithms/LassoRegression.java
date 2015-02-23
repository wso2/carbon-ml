package org.wso2.carbon.ml.model.spark.algorithms;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LassoModel;
import org.apache.spark.mllib.regression.LassoWithSGD;
import scala.Tuple2;

import java.io.Serializable;

public class LassoRegression implements Serializable {
    /**
     * This method uses stochastic gradient descent (SGD) algorithm to train a lasso regression model
     *
     * @param trainingDataset           Training dataset as a JavaRDD of LabeledPoints
     * @param noOfIterations            Number of iterarations
     * @param initialLearningRate       Initial learning rate (SGD step size)
     * @param regularizationParameter   Regularization parameter
     * @param miniBatchFraction         SGD minibatch fraction
     * @return                          Lasso regression model
     */
    public LassoModel train(JavaRDD<LabeledPoint> trainingDataset, int noOfIterations, double initialLearningRate, 
            double regularizationParameter, double miniBatchFraction) {
        return LassoWithSGD.train(trainingDataset.rdd(), noOfIterations, initialLearningRate, regularizationParameter,
                miniBatchFraction);
    }

    /**
     * This method applies lasso regression using a given model and a dataset
     *
     * @param lassoModel        Lasso regression model
     * @param testingDataset    Testing dataset as a JavaRDD of LabeledPoints
     * @return                  Tuple2 containing predicted values and labels
     */
    public JavaRDD<Tuple2<Double, Double>> test(final LassoModel lassoModel,
            JavaRDD<LabeledPoint> testingDataset) {
        return testingDataset.map(
                new Function<LabeledPoint, Tuple2<Double, Double>>() {
                    public Tuple2<Double, Double> call(LabeledPoint labeledPoint) {
                        Double predicted = lassoModel.predict(labeledPoint.features());
                        return new Tuple2<Double, Double>(predicted, labeledPoint.label());
                    }
                }
        );
    }
}
