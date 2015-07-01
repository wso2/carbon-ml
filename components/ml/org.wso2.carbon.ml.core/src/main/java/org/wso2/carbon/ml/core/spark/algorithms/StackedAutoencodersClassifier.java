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
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import water.DKV;
import water.H2O;
import water.H2OApp;
import water.Key;
import water.Scope;
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

    public StackedAutoencodersClassifier() {
        H2OApp.main(new String[0]);
        H2O.waitForCloudSize(1, 10 * 1000 /* ms */);
        Scope.enter();
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
     * @return
     */
    public StackedAutoencodersModel train(File trainFile, int batchSize,
            int layerCount, int[] layerSizes, int epochs, double trainFraction, String responseColumn, long modelID) {
        //build stacked autoencoder by training the model with training data                       

        StackedAutoencodersModel saeModel = new StackedAutoencodersModel();

        try {

            if (trainFile != null) {

                NFSFileVec trainfv = NFSFileVec.make(trainFile);
                Frame frame = ParseDataset.parse(Key.make(), trainfv._key);
                String classifColName = "C" + frame.numCols();
                log.info(String.format("Response Variable: %s ", classifColName));
                double[] ratios = new double[]{0.5f, 0.2f};
                FrameSplitter fs = new FrameSplitter(frame, ratios, generateNumKeys(frame._key, ratios.length + 1), null);
                H2O.submitTask(fs).join();
                Frame[] splits = fs.getResult();

                Frame trainFrame = splits[0];
                Frame vframe = splits[1];
                Frame tframe = splits[2];

                log.info("Creating Deeplearning parameters");
                DeepLearningParameters p = new DeepLearningParameters();

                // populate model parameters
                p._model_id = Key.make("dl_" + modelID + "model");
                p._train = trainFrame._key;
                p._valid = vframe._key;
                p._response_column = classifColName; // last column is the response
                p._activation = DeepLearningParameters.Activation.RectifierWithDropout;
                p._hidden = new int[]{500, 500, 500};
                p._train_samples_per_iteration = -2;
                p._input_dropout_ratio = 0.2;
                p._l1 = 1e-5;
                p._max_w2 = 10;
                p._epochs = 5;

                log.info("Converting last column to Enum");
                // Convert response 'C785' to categorical (digits 1 to 10)
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
                p._score_training_samples = tframe.numRows(); //only score on a small sample of the training set -> don't want to spend too much time scoring (note: there will be at least 1 row per chunk)

                dl = new DeepLearning(p);
                log.info("Start training ....");
                try {
                    model = dl.trainModel().get();
                    saeModel.setDeepLearningModel(model);
                    log.info("Set saeModel");
                    List<Key> keysToExport = new LinkedList<Key>();
                    keysToExport.add(model._key);
                    //keysToExport.addAll(model.getPublishedKeys());
                    saeModel.setDeepLearningModelKeys(keysToExport);
                    log.info("Finished Training ....");
                } catch (RuntimeException ex) {
                    log.info("Error in training the model");
                    log.info(ex.getMessage());
                } finally {
                    //dl.remove();
                    if (model != null) {
                        //model.delete();
                    }
                }
            } else {
                //TODO: Error
            }
        } catch (RuntimeException ex) {
            log.info(ex.getMessage());
        } finally {

        }

        return saeModel;
    }

    /**
     * This method applies a stacked autoencoders model to a given dataset
     *
     * @param saeModel Stacked Autoencoders model
     * @param test Testing dataset as a JavaRDD of labeled points
     * @return
     */
    public JavaPairRDD<Double, Double> test(JavaSparkContext ctxt, final StackedAutoencodersModel saeModel, JavaRDD<LabeledPoint> test) {
        log.info("Start testing");     
        log.info("Test features count: " + test.count());
        
        List<LabeledPoint> labeledPointList= test.collect();
        ArrayList<Tuple2<Double, Double>> tupleList = new ArrayList<Tuple2<Double, Double>>();        
        for (LabeledPoint lp : labeledPointList){
            tupleList.add(new Tuple2<Double, Double>(saeModel.predict(lp.features()),
                        lp.label()));
        }
             
        log.info("Done Testing");
        
        return ctxt.parallelizePairs(tupleList);
        /*
        return test.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {
            private static final long serialVersionUID = -8165253833889387018L;

            
            @Override
            public Tuple2<Double, Double> call(LabeledPoint labeledPoint) {
                if (labeledPoint == null) {
                    log.info("Labeledpoint is null");
                } else {
                    log.info("Labeledpoint is not null");
                }
                if (saeModel == null) {
                    log.info("Sae model is null 2");
                } else {
                    log.info("Sae model is not null 2");
                }
                if (saeModel.getDeepLearningModel() == null) {
                    log.info("Sae model deeplearningModel is null 2");
                } else {
                    log.info("Sae model deeplearningModel is not null 2");
                }
                if (saeModel.getDeepLearningModelKeys()== null) {
                    log.info("Sae model keys is null 2");
                } else {
                    log.info("Sae model keys is not null 2");
                }
                
                return new Tuple2<Double, Double>(saeModel.predict(labeledPoint.features()),
                        labeledPoint.label());
            }
        });*/

    }

}
