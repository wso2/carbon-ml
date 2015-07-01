/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.carbon.ml.core.spark.algorithms;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.RDD;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.constants.MLConstants.DEEPLEARNING_ALGORITHM;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.MulticlassConfusionMatrix;
import org.wso2.carbon.ml.core.spark.summary.DeeplearningModelSummary;
import org.wso2.carbon.ml.core.spark.transformations.DoubleArrayToLabeledPoint;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.database.DatabaseService;
import scala.Tuple2;

/**
 *
 * @author Thush
 */
public class DeeplearningModel {
    private static final Log log = LogFactory.getLog(DeeplearningModel.class);
    
    public MLModel buildModel(MLModelConfigurationContext context)
 throws MLModelBuilderException {
        
        JavaSparkContext sparkContext = null;
        DatabaseService databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
        MLModel mlModel = new MLModel();
        try {            
            sparkContext = context.getSparkContext();
            Workflow workflow = context.getFacts();
            long modelId = context.getModelId();
            
            // pre-processing
            JavaRDD<double[]> features = SparkModelUtils.preProcess(context);
            // generate train and test datasets by converting tokens to labeled points
            int responseIndex = context.getResponseIndex();
            SortedMap<Integer, String> includedFeatures = MLUtils.getIncludedFeaturesAfterReordering(workflow,
                    context.getNewToOldIndicesList(), responseIndex);

            DoubleArrayToLabeledPoint doubleArrayToLabeledPoint = new DoubleArrayToLabeledPoint();

            JavaRDD<LabeledPoint> labeledPoints = features.map(doubleArrayToLabeledPoint);
            JavaRDD<LabeledPoint>[] dataSplit = labeledPoints.randomSplit(
                    new double[] { workflow.getTrainDataFraction(), 1 - workflow.getTrainDataFraction() },
                    MLConstants.RANDOM_SEED);
            JavaRDD<LabeledPoint> trainingData = dataSplit[0];
            JavaRDD<LabeledPoint> testingData = dataSplit[1];            
            // create a deployable MLModel object
            mlModel.setAlgorithmName(workflow.getAlgorithmName());
            mlModel.setAlgorithmClass(workflow.getAlgorithmClass());
            mlModel.setFeatures(workflow.getIncludedFeatures());
            mlModel.setResponseVariable(workflow.getResponseVariable());
            mlModel.setEncodings(context.getEncodings());
            mlModel.setNewToOldIndicesList(context.getNewToOldIndicesList());
            mlModel.setResponseIndex(responseIndex);
            
            ModelSummary summaryModel = null;
            
            DEEPLEARNING_ALGORITHM deeplearningAlgorithm = DEEPLEARNING_ALGORITHM.valueOf(workflow.getAlgorithmName());
            switch (deeplearningAlgorithm) {
            case STACKED_AUTOENCODERS:
                summaryModel = buildDeepLearningModel(sparkContext, modelId, trainingData, testingData, workflow, mlModel, includedFeatures);
                break;
            default:
                throw new AlgorithmNameException("Incorrect algorithm name");
            }
            
            databaseService.updateModelSummary(modelId, summaryModel);
            return mlModel;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building supervised machine learning model: "
                    + e.getMessage(), e);
        } finally {
            //do something finally
        }
    }
    
    private int[] stringArrToIntArr(String str){
        String[] tokens = str.split(",");
        int[] arr = new int[tokens.length];
        for (int i=0;i<tokens.length;i++){
            arr[i] = Integer.parseInt(tokens[i]);
        }
        return arr;
    }
    
    private ModelSummary buildDeepLearningModel(JavaSparkContext sparkContext, long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel, SortedMap<Integer,String> includedFeatures) throws MLModelBuilderException {
        try {
            StackedAutoencodersClassifier saeClassifier = new StackedAutoencodersClassifier();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            
            File trainFile = new File(workflow.getDatasetURL());
            StackedAutoencodersModel saeModel = saeClassifier.train(trainFile,
                    Integer.parseInt(hyperParameters.get(MLConstants.BATCH_SIZE)),
                    Integer.parseInt(hyperParameters.get(MLConstants.LAYER_COUNT)),
                    stringArrToIntArr(hyperParameters.get(MLConstants.LAYER_SIZES)),
                    Integer.parseInt(hyperParameters.get(MLConstants.EPOCHS)),
                    workflow.getTrainDataFraction(),workflow.getResponseVariable(),modelID);
            
            if (saeModel.getDeepLearningModel() != null){
                log.info("SaeModel DeepLearningModel is not null");
                }
            else {
                log.info("SaeModel DeepLearningModel is null");
            }

            JavaPairRDD<Double, Double> predictionsAndLabels = saeClassifier.test(sparkContext,saeModel, testingData);
            
            DeeplearningModelSummary deeplearningModelSummary = DeeplearningModelUtils
                    .getDeeplearningModelSummary(sparkContext, testingData, predictionsAndLabels);
            
            mlModel.setModel(saeModel);
            if (deeplearningModelSummary != null){
                log.info("DeepLearningModelSummary is not null ...");
            } else {
                log.info("DeepLearningModelSummary is null ...");
            }
            
            //mlModel.setModel(saeModel);
            
            deeplearningModelSummary.setFeatures(includedFeatures.values().toArray(new String[0]));
            deeplearningModelSummary.setAlgorithm(MLConstants.DEEPLEARNING_ALGORITHM.STACKED_AUTOENCODERS.toString());
           
            MulticlassMetrics multiclassMetrics = getMulticlassMetrics(sparkContext, predictionsAndLabels);
            Double modelAccuracy = getModelAccuracy(multiclassMetrics);
            deeplearningModelSummary.setModelAccuracy(modelAccuracy);            

            return deeplearningModelSummary;
            
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building stacked autoencoders model: "
                    + e.getMessage(), e);
        }
        
    }

        /**
     * This method gets multi class metrics for a given set of prediction and label values
     *
     * @param sparkContext           JavaSparkContext
     * @param predictionsAndLabels   Prediction and label values RDD
     */
    private MulticlassMetrics getMulticlassMetrics(JavaSparkContext sparkContext, JavaPairRDD<Double, Double> predictionsAndLabels) {
        List<Tuple2<Double,Double>> predictionsAndLabelsDoubleList = predictionsAndLabels.collect();
        List<Tuple2<Object, Object>> predictionsAndLabelsObjectList = new ArrayList<Tuple2<Object, Object>>();
        for (Tuple2<Double,Double> predictionsAndLabel : predictionsAndLabelsDoubleList) {
            Object prediction = predictionsAndLabel._1;
            Object label = predictionsAndLabel._2;
            Tuple2<Object, Object> tupleElement = new Tuple2<Object, Object>(prediction, label);
            predictionsAndLabelsObjectList.add(tupleElement);
        }
        JavaRDD<Tuple2<Object, Object>> predictionsAndLabelsJavaRDD = sparkContext.parallelize(predictionsAndLabelsObjectList);
        RDD<Tuple2<Object,Object>> scoresAndLabelsRDD = JavaRDD.toRDD(predictionsAndLabelsJavaRDD);
        MulticlassMetrics multiclassMetrics = new MulticlassMetrics(scoresAndLabelsRDD);
        return  multiclassMetrics;
    }
    
    /**
     * This method returns multiclass confusion matrix for a given multiclass metric object
     *
     * @param multiclassMetrics      Multiclass metric object
     */
    private MulticlassConfusionMatrix getMulticlassConfusionMatrix(MulticlassMetrics multiclassMetrics, MLModel mlModel) {
        MulticlassConfusionMatrix multiclassConfusionMatrix = new MulticlassConfusionMatrix();
        if (multiclassMetrics != null) {
            int size = multiclassMetrics.confusionMatrix().numCols();
            double[] matrixArray = multiclassMetrics.confusionMatrix().toArray();
            double[][] matrix = new double[size][size];

            for(int i = 0; i < size; i++) {
                for(int j = 0; j < size; j++) {
                    matrix[i][j] = matrixArray[(j*size) + i];
                }
            }
            multiclassConfusionMatrix.setMatrix(matrix);

            List<Map<String, Integer>> encodings = mlModel.getEncodings();
            // last index is response variable encoding
            Map<String, Integer> encodingMap = encodings.get(encodings.size() - 1);
            List<String> decodedLabels = new ArrayList<String>();
            for(double label : multiclassMetrics.labels()) {
                Integer labelInt = (int) label;
                String decodedLabel = MLUtils.getKeyByValue(encodingMap, labelInt);
                decodedLabels.add(decodedLabel);
            }
            multiclassConfusionMatrix.setLabels(decodedLabels);
            multiclassConfusionMatrix.setSize(size);
        }
        return multiclassConfusionMatrix;
    }
    
    /**
     * This method gets model accuracy from given multi-class metrics
     *
     * @param multiclassMetrics     multi-class metrics object
     */
    private Double getModelAccuracy(MulticlassMetrics multiclassMetrics) {
        Double modelAccuracy = 0.0;
        int confusionMatrixSize = multiclassMetrics.confusionMatrix().numCols();
        int confusionMatrixDiagonal = 0;
        long totalPopulation = arraySum(multiclassMetrics.confusionMatrix().toArray());
        for (int i = 0; i < confusionMatrixSize; i++) {
            int diagonalValueIndex = multiclassMetrics.confusionMatrix().index(i, i);
            confusionMatrixDiagonal += multiclassMetrics.confusionMatrix().toArray()[diagonalValueIndex];
        }
        if(totalPopulation > 0) {
            modelAccuracy = (double) confusionMatrixDiagonal/totalPopulation;
        }
        return modelAccuracy;
    }
    
        /**
     * This summation of a given double array
     *
     * @param array     Double array
     */
    private long arraySum(double[] array) {
        long sum = 0;
        for (double i : array) {
            sum += i;
        }
        return sum;
    }
}
