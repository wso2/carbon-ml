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

import hex.FrameSplitter;
import hex.deeplearning.DeepLearning;
import hex.deeplearning.DeepLearningModel;
import hex.deeplearning.DeepLearningParameters;
import hex.splitframe.ShuffleSplitFrame;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.regression.LabeledPoint;

import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;

import water.DKV;
import water.H2O;
import water.Key;
import water.Scope;
import water.fvec.Frame;
import static water.util.FrameUtils.generateNumKeys;
import water.util.MRUtils;

public class StackedAutoencodersClassifier implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -3518369175759608115L;

    private static final Log log = LogFactory.getLog(StackedAutoencodersClassifier.class);

    private transient DeepLearning deeplearning;
    private transient DeepLearningModel dlModel;

    /**
     * @param trainingData Training dataset as a JavaRDD of labeled points
     * @param lambda Lambda parameter
     * @return Naive bayes model
     */
    /**
     * This method trains a stacked autoencoder
     * 
     * @param trainData Training dataset as a JavaRDD
     * @param batchSize Size of a training mini-batch
     * @param layerSizes Number of neurons for each layer
     * @param epochs Number of epochs to train
     * @param responseColumn Name of the response column
     * @param modelID Id of the model
     * @return
     */
    public DeepLearningModel train(JavaRDD<LabeledPoint> trainData, int batchSize, int[] layerSizes,
            String activationType, int epochs, String responseColumn, long modelID) {
        // build stacked autoencoder by training the model with training data
        
        double trainingFraction = 0.8;
        try {
            Scope.enter();
            if (trainData != null) {

                Frame frame = DeeplearningModelUtils.JavaRDDtoFrame(trainData);

                // H2O uses default C<x> for column header
                String classifColName = "C" + frame.numCols();

                // Convert response to categorical (digits 1 to <num of columns>)
                int ci = frame.find(classifColName);
                Scope.track(frame.replace(ci, frame.vecs()[ci].toEnum())._key);

                // Splitting train file to train, validation and test
                // Using FrameSplitter (instead of SuffleSplitFrame) gives a weird exception
                // barrier onExCompletion for hex.deeplearning.DeepLearning$DeepLearningDriver@78ec854
                double[] ratios = new double[] { trainingFraction, 1 - trainingFraction };
                Frame[] splits = ShuffleSplitFrame.shuffleSplitFrame(frame, generateNumKeys(frame._key, ratios.length),
                        ratios, 123456789);

                Frame trainFrame = splits[0];
                Frame vframe = splits[1];

                double[] trainArr = trainFrame.vec(classifColName).toDoubleArray();
                double[] validArr = vframe.vec(classifColName).toDoubleArray();
                for (int i = 0; i < trainFrame.numRows(); i++) {
                    System.out.print(trainArr[i] + "\t");
                }
                for (int i = 0; i < vframe.numRows(); i++) {
                    System.out.print(validArr[i] + "\t");
                }

                log.info("Creating Deeplearning parameters");
                DeepLearningParameters deeplearningParameters = new DeepLearningParameters();

                // populate model parameters
                deeplearningParameters._model_id = Key.make("dl_" + modelID + "_model");
                deeplearningParameters._train = trainFrame._key;
                deeplearningParameters._valid = vframe._key;
                deeplearningParameters._response_column = classifColName; // last column is the response
                // This is causin all the predictions to be 0.0
                // p._autoencoder = true;
                deeplearningParameters._activation = getActivationType(activationType);
                deeplearningParameters._hidden = layerSizes;
                deeplearningParameters._train_samples_per_iteration = batchSize;
                deeplearningParameters._input_dropout_ratio = 0.2;
                deeplearningParameters._l1 = 1e-5;
                deeplearningParameters._max_w2 = 10;
                deeplearningParameters._epochs = epochs;

                // speed up training
                deeplearningParameters._adaptive_rate = true; // disable adaptive per-weight learning rate -> default
                                                              // settings for learning rate and momentum are probably
                                                              // not ideal (slow convergence)
                deeplearningParameters._replicate_training_data = true; // avoid extra communication cost upfront, got
                                                                        // enough data on each node for load balancing
                deeplearningParameters._overwrite_with_best_model = true; // no need to keep the best model around
                deeplearningParameters._diagnostics = false; // no need to compute statistics during training
                deeplearningParameters._classification_stop = -1;
                deeplearningParameters._score_interval = 60; // score and print progress report (only) every 20 seconds
                deeplearningParameters._score_training_samples = batchSize / 10; // only score on a small sample of the
                                                                                 // training set -> don't want to spend
                                                                                 // too much time scoring (note: there
                                                                                 // will be at least 1 row per chunk)

                DKV.put(trainFrame);
                DKV.put(vframe);

                deeplearning = new DeepLearning(deeplearningParameters);

                log.info("Start training deeplearning model ....");
                try {
                    dlModel = deeplearning.trainModel().get();
                    log.info("Successfully finished Training deeplearning model ....");
                } catch (RuntimeException ex) {
                    log.info("Error in training the model");
                    log.info(ex.getMessage());
                }
            } else {
                log.error("Train file not found!");
            }
        } catch (RuntimeException ex) {
            log.info("Failed to train the deeplearning model [id] " + modelID + ". " + ex.getMessage());
        } finally {
            Scope.exit();
            log.info("Exit scope");
        }

        return dlModel;
    }

    private DeepLearningParameters.Activation getActivationType(String activation){
        String[] activationTypes = {"Rectifier", "RectifierWithDropout", "Tanh", "TanhWithDropout", "Maxout", "MaxoutWithDropout"};
        if (activation.equalsIgnoreCase(activationTypes[0])){
            return DeepLearningParameters.Activation.Rectifier;
        } else if (activation.equalsIgnoreCase(activationTypes[1])){
            return DeepLearningParameters.Activation.RectifierWithDropout;
        } else if (activation.equalsIgnoreCase(activationTypes[2])){
            return DeepLearningParameters.Activation.Tanh;
        } else if (activation.equalsIgnoreCase(activationTypes[3])){
            return DeepLearningParameters.Activation.TanhWithDropout;
        } else if (activation.equalsIgnoreCase(activationTypes[4])){
            return DeepLearningParameters.Activation.Maxout;
        } else if (activation.equalsIgnoreCase(activationTypes[5])){
            return DeepLearningParameters.Activation.MaxoutWithDropout;
        } else {
            return DeepLearningParameters.Activation.RectifierWithDropout;
        }
    }
    /**
     * This method applies a stacked autoencoders model to a given dataset and make predictions
     * 
     * @param ctxt JavaSparkContext
     * @param deeplearningModel Stacked Autoencoders model
     * @param test Testing dataset as a JavaRDD of labeled points
     * @return
     */
    public JavaPairRDD<Double, Double> test(JavaSparkContext ctxt, final DeepLearningModel deeplearningModel,
            JavaRDD<LabeledPoint> test) {

        Scope.enter();

        if (deeplearningModel == null) {
            log.info("DeeplearningModel is Null");
        }

        Frame testData = DeeplearningModelUtils.JavaRDDtoFrame(test);
        Frame testDataWithoutLabels = testData.subframe(0, testData.numCols() - 1);
        double[] predVales = deeplearningModel.score(testDataWithoutLabels).vec(0).toDoubleArray();
        double[] labels = testData.vec(testData.numCols() - 1).toDoubleArray();

        Scope.exit();

        ArrayList<Tuple2<Double, Double>> tupleList = new ArrayList<Tuple2<Double, Double>>();
        for (int i = 0; i < labels.length; i++) {
            tupleList.add(new Tuple2<Double, Double>(predVales[i], labels[i]));
        }

        return ctxt.parallelizePairs(tupleList);

    }

}
