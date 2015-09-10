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

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.io.Serializable;

public class NaiveBayesClassifier implements Serializable {

    private static final long serialVersionUID = 9159185970866124812L;

    /**
     * This method trains a naive bayes model
     *
     * @param trainingData Training dataset as a JavaRDD of labeled points
     * @param lambda Lambda parameter
     * @return Naive bayes model
     */
    public NaiveBayesModel train(JavaRDD<LabeledPoint> trainingData, double lambda) {
        return NaiveBayes.train(trainingData.rdd(), lambda);
    }

    /**
     * This method applies a naive bayes model to a given dataset
     *
     * @param naiveBayesModel Naive bayes model
     * @param test Testing dataset as a JavaRDD of labeled points
     * @return JavaPairRDD of predicted labels and actual labels
     */
    public JavaPairRDD<Double, Double> test(final NaiveBayesModel naiveBayesModel, JavaRDD<LabeledPoint>
            test) {
        return test.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {
            private static final long serialVersionUID = -8165253833889387018L;

            @Override
            public Tuple2<Double, Double> call(LabeledPoint labeledPoint) {
                return new Tuple2<Double, Double>(naiveBayesModel.predict(labeledPoint.features()),
                        labeledPoint.label());
            }
        });

    }

}
