/*
 *
 *  * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.carbon.ml.model.spark.algorithms;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Map;

public class DecisionTree implements Serializable {
// make errors more descriptive
    /**
     * @param train               Training dataset as a JavaRDD of labeled points
     * @param noOfClasses         No of classes
     * @param categoricalFeatures Categorical features
     * @param impurityCriteria    Impurity criteria
     * @param maxTreeDepth        Maximum tree depth
     * @param maxBins             Maximum no of bins
     * @return Decision tree model
     * @throws org.wso2.carbon.ml.model.exceptions.ModelServiceException
     */
    public DecisionTreeModel train(JavaRDD<LabeledPoint> train,
                            int noOfClasses,
                            Map<Integer, Integer> categoricalFeatures,
                            String impurityCriteria,
                            int maxTreeDepth,
                            int maxBins
    ) throws ModelServiceException {
        try {
            return org.apache.spark.mllib.tree.DecisionTree.trainClassifier(train,
                                                                            noOfClasses,
                                                                            categoricalFeatures,
                                                                            impurityCriteria,
                                                                            maxTreeDepth,
                                                                            maxBins);
        } catch (Exception e) {
            throw new ModelServiceException(e.getMessage(), e);
        }
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

        try {
            return test.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {
                @Override
                public Tuple2<Double, Double> call(LabeledPoint p) {
                    return new Tuple2<Double, Double>(model.predict(p.features()), p.label());
                }
            });
        } catch (Exception e) {
            throw new ModelServiceException(e.getMessage(), e);
        }

    }

    /**
     * @param predictionsAndLabels JavaPairRDD containing predictions and labels
     * @return Test error
     * @throws ModelServiceException
     */
    public double getTestError(JavaPairRDD<Double, Double> predictionsAndLabels)
            throws ModelServiceException {
        try {
            return 1.0 * predictionsAndLabels.filter(new Function<Tuple2<Double, Double>,
                    Boolean>() {
                @Override
                public Boolean call(Tuple2<Double, Double> pl) {
                    return !pl._1().equals(pl._2());
                }
            }).count() / predictionsAndLabels.count();
        } catch (Exception e) {
            throw new ModelServiceException(e.getMessage(), e);
        }
    }
}
