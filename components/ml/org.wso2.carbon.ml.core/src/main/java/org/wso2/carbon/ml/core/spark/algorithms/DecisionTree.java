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

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Map;

public class DecisionTree implements Serializable {

    private static final long serialVersionUID = 5725487832599918979L;

    /**
     * This method trains a decision tree model
     *
     * @param train               Training dataset as a JavaRDD of labeled points
     * @param noOfClasses         Number of classes
     * @param categoricalFeatures Map containing categorical feature indices and number of categories for each feature
     * @param impurityCriteria    Impurity criteria - "gini" or "entropy" for classification, "variance" for regression
     * @param maxTreeDepth        Maximum tree depth
     * @param maxBins             Maximum number of bins
     * @return Decision tree model
     */
    public DecisionTreeModel train(JavaRDD<LabeledPoint> train, int noOfClasses,
            Map<Integer, Integer> categoricalFeatures, String impurityCriteria, int maxTreeDepth,
            int maxBins) {
        return org.apache.spark.mllib.tree.DecisionTree.trainClassifier(train, noOfClasses,
                categoricalFeatures, impurityCriteria, maxTreeDepth, maxBins);
    }

    /**
     * This method applies a decision tree model to a given dataset
     *
     * @param model     Decision tree model
     * @param test      Test dataset as a JavaRDD of labeled points
     * @return          JavaPairRDD containing predictions and labels
     */
    public JavaPairRDD<Double, Double> test(final DecisionTreeModel model, JavaRDD<LabeledPoint>
            test) {
        return test.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {
            private static final long serialVersionUID = -7078534438332774197L;

            @Override
            public Tuple2<Double, Double> call(LabeledPoint labeledPoint) {
                return new Tuple2<Double, Double>(model.predict(labeledPoint.features()), labeledPoint.label());
            }
        });

    }
}
