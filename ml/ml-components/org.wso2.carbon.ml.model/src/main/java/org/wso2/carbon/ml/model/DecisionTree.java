package org.wso2.carbon.ml.model;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import scala.Tuple2;

import java.util.HashMap;

public class DecisionTree {
    /**
     * @param train               Training dataset as a JavaRDD of labeled points
     * @param noOfClasses         No of classes
     * @param categoricalFeatures Categorical features
     * @param impurityCriteria    Impurity criteria
     * @param maxTreeDepth        Maximum tree depth
     * @param maxBins             Maximum no of bins
     * @return Decision tree model
     * @throws ModelServiceException
     */
    public DecisionTreeModel train(JavaRDD<LabeledPoint> train,
                                   int noOfClasses,
                                   HashMap<Integer, Integer> categoricalFeatures,
                                   String impurityCriteria,
                                   int maxTreeDepth,
                                   int maxBins
    ) throws ModelServiceException {
        return org.apache.spark.mllib.tree.DecisionTree.trainClassifier(train,
                                                                        noOfClasses,
                                                                        categoricalFeatures,
                                                                        impurityCriteria,
                                                                        maxTreeDepth,
                                                                        maxBins);
    }

    /**
     * @param model Decision tree model
     * @param test  Test dataset as a JavaRDD of labeled points
     * @return JavaPairRDD containing predictions and labels
     * @throws ModelServiceException
     */
    public JavaPairRDD<Double, Double> test(final DecisionTreeModel model,
                                            JavaRDD<LabeledPoint> test)
            throws ModelServiceException {

        return test.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {
            @Override
            public Tuple2<Double, Double> call(LabeledPoint p) {
                return new Tuple2<Double, Double>(model.predict(p.features()), p.label());
            }
        });

    }

    /**
     * @param predictionsAndLabels JavaPairRDD containing predictions and labels
     * @return Test error
     * @throws ModelServiceException
     */
    public double getTestError(JavaPairRDD<Double, Double> predictionsAndLabels)
            throws ModelServiceException {
        Double testError =
                1.0 * predictionsAndLabels.filter(new Function<Tuple2<Double, Double>, Boolean>() {
                    @Override
                    public Boolean call(Tuple2<Double, Double> pl) {
                        return !pl._1().equals(pl._2());
                    }
                }).count() / predictionsAndLabels.count();
        return testError;
    }
}
