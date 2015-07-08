/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.carbon.ml.core.spark.algorithms;

import hex.FrameSplitter;
import hex.deeplearning.DeepLearning;
import hex.deeplearning.DeepLearningModel;
import hex.deeplearning.DeepLearningParameters;
import java.io.File;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import water.DKV;
import water.H2O;
import water.H2OApp;
import water.Key;
import water.Scope;
import water.api.ShutdownHandler;
import water.fvec.Frame;
import water.fvec.NFSFileVec;
import water.parser.ParseDataset;
import static water.util.FrameUtils.generateNumKeys;

/**
 *
 * @author Thush
 */
public class StackedAutoencodersClassifier implements Serializable {

    private static final Log log = LogFactory.getLog(StackedAutoencodersClassifier.class);
    
    private transient DeepLearning dl;
    private transient DeepLearningModel model;

    //Start the H2O server and enter the H2O Scope
    public StackedAutoencodersClassifier() {        
        
    }    
    

    /**
     *
     *
     * @param trainingData Training dataset as a JavaRDD of labeled points
     * @param lambda Lambda parameter
     * @return Naive bayes model
     */
    /**
     * This method trains a stacked autoencoder
     *
     * @param trainFile Training dataset as a File
     * @param batchSize Size of a training mini-batch
     * @param layerCount Number of layers
     * @param layerSizes Number of neurons for each layer
     * @param epochs Number of epochs to train
     * @param trainFraction The fraction considered for training
     * @param responseColumn Name of the response column
     * @param modelID Id of the model
     * @return
     */
    public StackedAutoencodersModel train(File trainFile, int batchSize,
            int layerCount, int[] layerSizes, int epochs, double trainFraction, String responseColumn, long modelID) {
        //build stacked autoencoder by training the model with training data                               
        Scope.enter();
        StackedAutoencodersModel saeModel = new StackedAutoencodersModel();
        
        try {

            if (trainFile != null) {

                NFSFileVec trainfv = NFSFileVec.make(trainFile);
                Frame frame = ParseDataset.parse(Key.make(), trainfv._key);
                
                //H2O uses default C<x> for column header
                String classifColName = "C" + frame.numCols();
                
                //splitting train file to train, validation and test
                double[] ratios = new double[]{trainFraction, 0.2f};
                FrameSplitter fs = new FrameSplitter(frame, ratios, generateNumKeys(frame._key, ratios.length + 1), null);
                H2O.submitTask(fs).join();
                Frame[] splits = fs.getResult();

                Frame trainFrame = splits[0];
                Frame vframe = splits[1];
                Frame tframe = splits[2];

                log.info("Creating Deeplearning parameters");
                DeepLearningParameters p = new DeepLearningParameters();
                
                // populate model parameters
                p._model_id = Key.make("dl_" + modelID + "_model");
                p._train = trainFrame._key;
                p._valid = vframe._key;
                p._response_column = classifColName; // last column is the response
                p._autoencoder = true;
                p._activation = DeepLearningParameters.Activation.RectifierWithDropout;
                p._hidden = new int[]{500, 500, 500};
                p._train_samples_per_iteration = batchSize;
                p._input_dropout_ratio = 0.2;
                p._l1 = 1e-5;
                p._max_w2 = 10;
                p._epochs = epochs;
                
                // Convert response to categorical (digits 1 to <num of columns>)
                int ci = trainFrame.find(classifColName);
                Scope.track(trainFrame.replace(ci, trainFrame.vecs()[ci].toEnum())._key);
                Scope.track(vframe.replace(ci, vframe.vecs()[ci].toEnum())._key);
                DKV.put(trainFrame);
                DKV.put(vframe);

                // speed up training
                p._adaptive_rate = true; //disable adaptive per-weight learning rate -> default settings for learning rate and momentum are probably not ideal (slow convergence)
                p._replicate_training_data = true; //avoid extra communication cost upfront, got enough data on each node for load balancing
                p._overwrite_with_best_model = true; //no need to keep the best model around
                p._diagnostics = false; //no need to compute statistics during training
                p._classification_stop = -1;
                p._score_interval = 60; //score and print progress report (only) every 20 seconds
                p._score_training_samples = 10000; //only score on a small sample of the training set -> don't want to spend too much time scoring (note: there will be at least 1 row per chunk)

                dl = new DeepLearning(p);
                log.info("Start training deeplearning model ....");
                try {
                    model = dl.trainModel().get();
                    saeModel.setDeepLearningModel(model);
                                        
                    log.info("Successfully finished Training deeplearning model ....");
                } catch (RuntimeException ex) {
                    log.info("Error in training the model");
                    log.info(ex.getMessage());
                } 
            } else {
                log.error("Train file not found!");
            }
        } catch (RuntimeException ex) {
            log.info("Failed to train the deeplearning model [id] "+ modelID + ". " + ex.getMessage());
        } finally {
            Scope.exit();            
        }

        return saeModel;
    }

    /**
     * This method applies a stacked autoencoders model to a given dataset and make predictions
     * @param ctxt JavaSparkContext 
     * @param saeModel Stacked Autoencoders model
     * @param test Testing dataset as a JavaRDD of labeled points
     * @return
     */
    public JavaPairRDD<Double, Double> test(JavaSparkContext ctxt, final StackedAutoencodersModel saeModel, JavaRDD<LabeledPoint> test) {
        log.info("Start testing");     

        Scope.enter();
        
        List<LabeledPoint> labeledPointList= test.collect();
        ArrayList<Tuple2<Double, Double>> tupleList = new ArrayList<Tuple2<Double, Double>>();        
        for (LabeledPoint lp : labeledPointList){
            tupleList.add(new Tuple2<Double, Double>(saeModel.predict(lp.features()),
                        lp.label()));
        }
        
        Scope.exit();

        log.info("Done Testing");
        
        return ctxt.parallelizePairs(tupleList);   

    }

}
