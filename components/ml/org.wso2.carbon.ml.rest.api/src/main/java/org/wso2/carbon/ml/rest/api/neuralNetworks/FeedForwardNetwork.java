/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.ml.rest.api.neuralNetworks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.canova.api.records.reader.RecordReader;
import org.canova.api.records.reader.impl.CSVRecordReader;
import org.canova.api.split.FileSplit;
import org.deeplearning4j.datasets.canova.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.MLDatasetVersion;
import org.wso2.carbon.ml.core.exceptions.MLAnalysisHandlerException;
import org.wso2.carbon.ml.core.exceptions.MLDataProcessingException;
import org.wso2.carbon.ml.core.impl.MLAnalysisHandler;
import org.wso2.carbon.ml.core.impl.MLDatasetProcessor;
import org.wso2.carbon.ml.core.utils.MLUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

/**
 * This class is to build the Feed Forward Neural Network algorithm.
 */
public class FeedForwardNetwork {

    //global variables
    String mlDataSet;
    double analysisFraction;
    String analysisResponceVariable ;
    int responseIndex;
    MLDatasetProcessor datasetProcessor = new MLDatasetProcessor() ;
    MLAnalysisHandler mlAnalysisHandler = new MLAnalysisHandler();
    PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
    int tenantId = carbonContext.getTenantId();
    String userName = carbonContext.getUsername();
    private static final Log log = LogFactory.getLog(FeedForwardNetwork.class);

    /**
     * method to createFeedForwardNetwork.
     * @param seed
     * @param learningRate
     * @param analysisID
     * @param bachSize
     * @param backprop
     * @param hiddenList
     * @param inputLayerNodes
     * @param iterations
     * @param versionID
     * @param momentum
     * @param nepoches
     * @param datasetId
     * @param noHiddenLayers
     * @param optimizationAlgorithms
     * @param outputList
     * @param pretrain
     * @param updater
     * @return an String object with evaluation result.
     */
    public String createFeedForwardNetwork(long seed, double learningRate, int bachSize, double nepoches, int iterations, String optimizationAlgorithms, String updater, double momentum, boolean pretrain, boolean backprop, int noHiddenLayers, int inputLayerNodes, int datasetId, int versionID, int analysisID, List<HiddenLayerDetails> hiddenList, List<OutputLayerDetails> outputList) throws IOException, InterruptedException {

        String evaluationDetails = null;
        int numLinesToSkip = 0;
        String delimiter = ",";
        mlDataSet = getDatasetPath(datasetId,versionID);
        analysisFraction = getAnalysisFraction(analysisID);
        analysisResponceVariable = getAnalysisResponseVariable(analysisID);
        responseIndex = getAnalysisResponseVariableIndex(analysisID);
        SplitTestAndTrain splitTestAndTrain;
        DataSet currentDataset;
        DataSet trainingSet = null;
        DataSet testingSet = null;
        INDArray features = null;
        INDArray labels = null;
        INDArray predicted = null;
        Random rnd = new Random();
        int labelIndex = 0;
        int numClasses = 0;
        int fraction = 0;
        //Initialize RecordReader
        RecordReader rr = new CSVRecordReader(numLinesToSkip,delimiter);
        //read the dataset
        rr.initialize(new FileSplit(new File(mlDataSet)));
        labelIndex = responseIndex;
        numClasses = outputList.get(0).outputNodes;
        //Get the fraction to do the spliting data to training and testing
        FileReader fr = new FileReader(mlDataSet);
        LineNumberReader lineNumberReader=new LineNumberReader(fr);
        //Get the total number of lines
        lineNumberReader.skip(Long.MAX_VALUE);
        int lines = lineNumberReader.getLineNumber();

        //handling multiplication of 0 error
        if(analysisFraction == 0){
            return null;
        }

        //Take floor value to set the numHold of training data
        fraction = ((int) Math.floor(lines * analysisFraction));
        org.nd4j.linalg.dataset.api.iterator.DataSetIterator trainIter = new RecordReaderDataSetIterator(rr,lines,labelIndex,numClasses);

        //Create NeuralNetConfiguration object having basic settings.
        NeuralNetConfiguration.ListBuilder neuralNetConfiguration = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .optimizationAlgo(mapOptimizationAlgorithm(optimizationAlgorithms))
                .learningRate(learningRate)
                .updater(mapUpdater(updater))
                .momentum(momentum)
                .list(noHiddenLayers+1);

        //Add Hidden Layers to the network with unique settings
        for(int i = 0;i< noHiddenLayers;i++){
            int nInput = 0;
            if(i == 0)
                nInput=inputLayerNodes;
            else
                nInput=hiddenList.get(i-1).hiddenNodes;

            neuralNetConfiguration.layer(i,new DenseLayer.Builder().nIn(nInput)
                    .nOut(hiddenList.get(i).hiddenNodes)
                    .weightInit(mapWeightInit(hiddenList.get(i).weightInit))
                    .activation(hiddenList.get(i).activationAlgo)
                    .build());
        }

        //Add Output Layers to the network with unique settings
        neuralNetConfiguration.layer(noHiddenLayers, new OutputLayer.Builder(mapLossFunction(outputList.get(0).lossFunction))
                    .nIn(hiddenList.get(noHiddenLayers-1).hiddenNodes)
                    .nOut(outputList.get(0).outputNodes)
                    .weightInit(mapWeightInit(outputList.get(0).weightInit))
                    .activation(outputList.get(0).activationAlgo)
                    .build());

        //Create MultiLayerConfiguration network
        MultiLayerConfiguration conf = neuralNetConfiguration.pretrain(pretrain)
                .backprop(backprop).build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(Collections.singletonList((IterationListener) new ScoreIterationListener(1)));

        while (trainIter.hasNext()) {
            currentDataset = trainIter.next();
            splitTestAndTrain = currentDataset.splitTestAndTrain(fraction,rnd);
            trainingSet = splitTestAndTrain.getTrain();
            testingSet = splitTestAndTrain.getTest();
            features= testingSet.getFeatureMatrix();
            labels = testingSet.getLabels();
        }

        //Train the model with the training data
        for ( int n = 0; n < nepoches; n++) {
            model.fit( trainingSet);
        }

        //Do the evaluations of the model including the Accuracy, F1 score etc.
        log.info("Evaluate model....");
        Evaluation eval = new Evaluation(outputList.get(0).outputNodes);
        predicted = model.output(features,false);

        eval.eval(labels, predicted);

        evaluationDetails = "{\"Accuracy\":\""+eval.accuracy()+"\", \"Pecision\":\""+eval.precision()+"\",\"Recall\":\""+eval.recall()+"\",\"F1Score\":\""+eval.f1()+"\"}";
        return evaluationDetails;

    }

    /**
     * method to map user selected Optimazation Algorithm to OptimizationAlgorithm object.
     * @param optimizationAlgorithms
     * @return an OptimizationAlgorithm object.
     */
    OptimizationAlgorithm mapOptimizationAlgorithm(String optimizationAlgorithms){

        OptimizationAlgorithm optimizationAlgo = null;

        switch (optimizationAlgorithms){
            case "Line_Gradient_Descent":
                optimizationAlgo = OptimizationAlgorithm.LINE_GRADIENT_DESCENT;
                break;
            case "Conjugate_Gradient":
                optimizationAlgo = OptimizationAlgorithm.CONJUGATE_GRADIENT;
                break;
            case "Hessian_Free":
                optimizationAlgo = OptimizationAlgorithm.HESSIAN_FREE;
                break;
            case "LBFGS":
                optimizationAlgo = OptimizationAlgorithm.LBFGS;
                break;
            case "Stochastic_Gradient_Descent":
                optimizationAlgo = OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT;
                break;
            default:
                optimizationAlgo = null;
                break;
        }
        return optimizationAlgo;
    }

    /**
     * method to map user selected Updater Algorithm to Updater object.
     * @param updater
     * @return an Updater object .
     */
    Updater mapUpdater(String updater) {

        Updater updaterAlgo = null;

        switch (updater) {
            case "sgd":
                updaterAlgo = Updater.SGD;
                break;
            case "adam":
                updaterAlgo = Updater.ADAM;
                break;
            case "adadelta":
                updaterAlgo = Updater.ADADELTA;
                break;
            case "nesterovs":
                updaterAlgo = Updater.NESTEROVS;
                break;
            case "adagrad":
                updaterAlgo = Updater.ADAGRAD;
                break;
            case "rmsprop":
                updaterAlgo = Updater.RMSPROP;
                break;
            case "none":
                updaterAlgo = Updater.NONE;
                break;
            case "custom":
                updaterAlgo = Updater.CUSTOM;
                break;
            default:
                updaterAlgo = null;
                break;
        }
        return updaterAlgo;
    }

    /**
     * method to map user selected Loss Function Algorithm to LossFunction object.
     * @param lossFunction
     * @return an LossFunction object .
     */
    LossFunction mapLossFunction(String lossFunction){

        LossFunction lossfunctionAlgo = null;

        switch (lossFunction){
            case "mse":
                lossfunctionAlgo = LossFunction.MSE;
                break;
            case "expll":
                lossfunctionAlgo = LossFunction.EXPLL;
                break;
            case "xent":
                lossfunctionAlgo = LossFunction.XENT;
                break;
            case "mcxent":
                lossfunctionAlgo = LossFunction.MCXENT;
                break;
            case "rmsexent":
                lossfunctionAlgo = LossFunction.RMSE_XENT;
                break;
            case "sqauredloss":
                lossfunctionAlgo = LossFunction.SQUARED_LOSS;
                break;
            case "reconstructioncrossentropy":
                lossfunctionAlgo = LossFunction.RECONSTRUCTION_CROSSENTROPY;
                break;
            case "negetiveloglilelihood":
                lossfunctionAlgo = LossFunction.NEGATIVELOGLIKELIHOOD;
                break;
            case "custom":
                lossfunctionAlgo = LossFunction.CUSTOM;
                break;
            default:
                lossfunctionAlgo = null;
        }
        return lossfunctionAlgo;
    }

    /**
     * method to map user selected WeightInit Algorithm to WeightInit object.
     * @param weightinit
     * @return an WeightInit object .
     */
    WeightInit mapWeightInit(String weightinit){

        WeightInit weightInitAlgo = null;

        switch (weightinit){

            case "Distribution":
                weightInitAlgo = WeightInit.DISTRIBUTION;
                break;
            case "Normalized":
                weightInitAlgo = WeightInit.NORMALIZED;
                break;
            case "Size":
                weightInitAlgo = WeightInit.SIZE;
                break;
            case "Uniform":
                weightInitAlgo = WeightInit.UNIFORM;
                break;
            case "Vi":
                weightInitAlgo = WeightInit.VI;
                break;
            case "Zero":
                weightInitAlgo = WeightInit.ZERO;
                break;
            case "Xavier":
                weightInitAlgo = WeightInit.XAVIER;
                break;
            case "RELU":
                weightInitAlgo = WeightInit.RELU;
                break;
            default:
                weightInitAlgo = null;
                break;
        }
        return weightInitAlgo;
    }

    /**
     * method to analysis fraction from mlAnalysisHandler.
     * @param analysisId
     * @return analysis fraction .
     */
    double getAnalysisFraction(long analysisId) {
        try {
            double trainDataFraction = mlAnalysisHandler.getTrainDataFraction(analysisId);
            return  trainDataFraction;

        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                  "Error occurred while retrieving train data fraction for the analysis [id] %s of tenant [id] %s and [user] %s .",
                   analysisId, tenantId, userName), e);
            log.info(msg, e);
            return 0.0;
        }
    }

    /**
     * method to get analysis Responsible Variable from mlAnalysisHandler.
     * @param analysisId
     * @return Response Variable .
     */
    String getAnalysisResponseVariable(long analysisId) {
        try {
            String responseVariable = mlAnalysisHandler.getResponseVariable(analysisId);
            return  responseVariable;
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                   "Error occurred while retrieving train data fraction for the analysis [id] %s of tenant [id] %s and [user] %s .",
                    analysisId, tenantId, userName), e);
            log.error(msg, e);
            return null;
        }
    }

    /**
     * method to get analysis Responsible Variable index.
     * @param analysisId
     * @return Response Variable index.
     */
    int getAnalysisResponseVariableIndex(long analysisId) {
        try {
            List<String> features = mlAnalysisHandler.getFeatureNames(Long.toString(analysisId));
            int index= features.indexOf(analysisResponceVariable);
            return  index;
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                   "Error occurred while retrieving index of the current response feature for the analysis [id] %s of tenant [id] %s and [user] %s .",
                   analysisId, tenantId, userName), e);
            log.error(msg, e);
            return -1;
        }
    }

    /** =
     * method to get dataset version path.
     * @param datasetId
     * @param versionId
     * @return Dataset version stored path-target path.
     */
    String getDatasetPath(int datasetId, int versionId) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        String targetPath=null;
        String version = null;
        long versionSetId=0;

        //to get the version
        try {
            List<MLDatasetVersion> versionSets = datasetProcessor.getAllDatasetVersions(tenantId, userName, datasetId);
            Iterator<MLDatasetVersion> versionsetIterator = versionSets.iterator();

            while (versionsetIterator.hasNext()) {
                MLDatasetVersion mlDatasetVersion= versionsetIterator.next();

                if (mlDatasetVersion.getId()== versionId) {
                   version = mlDatasetVersion.getVersion();
                }
                else{
                    version = null;
                }
            }

        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving all versions of a dataset with the [id] %s of tenant [id] %s and [user] %s .",
                                    datasetId, tenantId, userName), e);
            log.error(msg, e);
            version = null;
        }

        //get target path of the Version
        try {
            versionSetId = datasetProcessor.getVersionSetWithVersion(tenantId, userName, datasetId, version).getId();
            targetPath = datasetProcessor.getVersionset(tenantId, userName,versionSetId).getTargetPath();
            return targetPath;

        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving the version set with [version] %s of a dataset with the [id] %s of tenant [id] %s and [user] %s .",
                                    version, datasetId, tenantId, userName), e);
            log.error(msg, e);
            targetPath = null;
        }

        return targetPath;
    }
}
