package org.wso2.carbon.ml.model;

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
import scala.Tuple2;

import java.util.Arrays;


public class LogisticRegression {
    /**
     * This method uses SGD to train a logistic regression model for a given dataset
     *
     * @param trainingDataset             Training dataset as a JavaRDD of labeled points
     * @param noOfIterations              No of iterations
     * @param initialLearningRate         Initial learning rate
     * @param regularizationType          Regularization type : L1 or L2
     * @param regularizationParameter     Regularization parameter
     * @param dataFractionPerSGDIteration Data fraction per SGD iteration
     * @return Logistic regression model
     * @throws ModelServiceException
     */
    public LogisticRegressionModel trainWithSGD(JavaRDD<LabeledPoint> trainingDataset,
                                                double initialLearningRate,
                                                int noOfIterations,
                                                String regularizationType,
                                                double regularizationParameter,
                                                double dataFractionPerSGDIteration) throws
                                                                                    ModelServiceException {
        LogisticRegressionWithSGD lrSGD = new LogisticRegressionWithSGD(initialLearningRate,
                                                                        noOfIterations,
                                                                        regularizationParameter, dataFractionPerSGDIteration);
        if ("L1".equals(regularizationType)) {
            lrSGD.optimizer().setUpdater(new L1Updater());
        } else if ("L2".equals(regularizationType)) {
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
     * @throws ModelServiceException
     */
    public LogisticRegressionModel trainWithLBFGS(JavaRDD<LabeledPoint> trainingDataset,
                                                  int noOfCorrections,
                                                  double convergenceTolerance,
                                                  int noOfIterations,
                                                  double regularizationParameter)
            throws ModelServiceException {
        int numFeatures = trainingDataset.take(1).get(0).features().size();
        JavaRDD<Tuple2<Object, Vector>> training = trainingDataset.map(
                new Function<LabeledPoint, Tuple2<Object, Vector>>() {
                    public Tuple2<Object, Vector> call(LabeledPoint p) {
                        return new Tuple2<Object, Vector>(p.label(), MLUtils.appendBias(p.features()));
                    }
                });
        training.cache();
        Vector initialWeightsWithIntercept = Vectors.dense(new double[numFeatures + 1]);
        Tuple2<Vector, double[]> result = LBFGS.runLBFGS(
                training.rdd(),
                new LogisticGradient(),
                new SquaredL2Updater(),
                noOfCorrections,
                convergenceTolerance,
                noOfIterations,
                regularizationParameter,
                initialWeightsWithIntercept);
        Vector weightsWithIntercept = result._1();
        return new LogisticRegressionModel(
                Vectors.dense(Arrays.copyOf(weightsWithIntercept.toArray(), weightsWithIntercept.size() - 1)),
                (weightsWithIntercept.toArray())[weightsWithIntercept.size() - 1]);
    }

    /**
     * This method performs a binary classification using a given model and a dataset
     *
     * @param model          Logistic regression model
     * @param testingDataset Testing dataset as a LabeledPoint JavaRDD
     * @return Tuple2 containing scores and labels
     * @throws ModelServiceException
     */
    public JavaRDD<Tuple2<Object, Object>> test(final LogisticRegressionModel model,
                                                JavaRDD<LabeledPoint> testingDataset)
            throws ModelServiceException {
        return testingDataset.map(
                new Function<LabeledPoint, Tuple2<Object, Object>>() {
                    public Tuple2<Object, Object> call(LabeledPoint p) {
                        Double score = model.predict(p.features());
                        return new Tuple2<Object, Object>(score, p.label());
                    }
                }
        );
    }

    /**
     * This method calculates performance metrics for a given set of test scores and labels
     *
     * @param scoreAndLabels Scores and labels
     * @return Binary classification metrics
     * @throws ModelServiceException
     */
    public BinaryClassificationMetrics getEvaluationMetrics(
            JavaRDD<Tuple2<Object, Object>> scoreAndLabels) throws ModelServiceException {
        return new BinaryClassificationMetrics(JavaRDD.toRDD(scoreAndLabels));
    }
}
