/*
 * Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.evaluation.RegressionMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LassoModel;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.mllib.regression.RidgeRegressionModel;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.rdd.RDD;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.constants.MLConstants.SUPERVISED_ALGORITHM;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.summary.ClassClassificationAndRegressionModelSummary;
import org.wso2.carbon.ml.core.spark.summary.FeatureImportance;
import org.wso2.carbon.ml.core.spark.summary.ProbabilisticClassificationModelSummary;
import org.wso2.carbon.ml.core.spark.transformations.DoubleArrayToLabeledPoint;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.database.DatabaseService;

import scala.Tuple2;

public class SupervisedModel {
    
    /**
     * @param modelID       Model ID
     * @param workflow      Workflow ID
     * @param sparkConf     Spark configuration
     * @throws              MLModelBuilderException
     */
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
            mlModel.setResponseIndex(responseIndex);

            ModelSummary summaryModel = null;

            // build a machine learning model according to user selected algorithm
            SUPERVISED_ALGORITHM supervisedAlgorithm = SUPERVISED_ALGORITHM.valueOf(workflow.getAlgorithmName());
            switch (supervisedAlgorithm) {
            case LOGISTIC_REGRESSION:
                summaryModel = buildLogisticRegressionModel(sparkContext, modelId, trainingData, testingData, workflow,
                        mlModel, includedFeatures, true);
                break;
            case LOGISTIC_REGRESSION_LBFGS:
                summaryModel = buildLogisticRegressionModel(sparkContext, modelId, trainingData, testingData, workflow,
                        mlModel, includedFeatures, false);
                break;
            case DECISION_TREE:
                Map<Integer, Integer> categoricalFeatureInfo = getCategoricalFeatureInfo(context.getEncodings());
                summaryModel = buildDecisionTreeModel(sparkContext, modelId, trainingData, testingData, workflow,
                        mlModel, includedFeatures, categoricalFeatureInfo);
                break;
            case SVM:
                summaryModel = buildSVMModel(sparkContext, modelId, trainingData, testingData, workflow, mlModel,
                        includedFeatures);
                break;
            case NAIVE_BAYES:
                summaryModel = buildNaiveBayesModel(sparkContext, modelId, trainingData, testingData, workflow,
                        mlModel, includedFeatures);
                break;
            case LINEAR_REGRESSION:
                summaryModel = buildLinearRegressionModel(sparkContext, modelId, trainingData, testingData, workflow,
                        mlModel, includedFeatures);
                break;
            case RIDGE_REGRESSION:
                summaryModel = buildRidgeRegressionModel(sparkContext, modelId, trainingData, testingData, workflow,
                        mlModel, includedFeatures);
                break;
            case LASSO_REGRESSION:
                summaryModel = buildLassoRegressionModel(sparkContext, modelId, trainingData, testingData, workflow,
                        mlModel, includedFeatures);
                break;
            default:
                throw new AlgorithmNameException("Incorrect algorithm name");
            }

            // persist model summary
            databaseService.updateModelSummary(modelId, summaryModel);
            return mlModel;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building supervised machine learning model: "
                    + e.getMessage(), e);
        } finally {
            if (sparkContext != null) {
                sparkContext.stop();
            }
        }
    }

    private Map<Integer, Integer> getCategoricalFeatureInfo(List<Map<String, Integer>> encodings) {
        Map<Integer, Integer> info = new HashMap<Integer, Integer>();
        // skip the response variable which is at last
        for (int i = 0; i < encodings.size() - 1; i++) {
            if (encodings.get(i).size() > 0) {
                info.put(i, encodings.get(i).size());
            }
        }
        return info;
    }

    /**
     * This method builds a logistic regression model
     *
     * @param modelID           Model ID
     * @param trainingData      Training data as a JavaRDD of LabeledPoints
     * @param testingData       Testing data as a JavaRDD of LabeledPoints
     * @param workflow          Machine learning workflow
     * @param mlModel           Deployable machine learning model
     * @param headerRow         Header row of the dataset
     * @param responseIndex     Index of the response variable in the dataset
     * @param columnSeparator   Column separator of dataset
     * @param isSGD             Whether the algorithm is Logistic regression with SGD
     * @throws                  MLModelBuilderException
     */
    private ModelSummary buildLogisticRegressionModel(JavaSparkContext sparkContext, long modelID,
            JavaRDD<LabeledPoint> trainingData, JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel,
            SortedMap<Integer, String> includedFeatures, boolean isSGD) throws MLModelBuilderException {
        try {
            LogisticRegression logisticRegression = new LogisticRegression();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            LogisticRegressionModel logisticRegressionModel;
            String algorithmName;

            if (isSGD) {
                algorithmName = SUPERVISED_ALGORITHM.LOGISTIC_REGRESSION.toString();
                logisticRegressionModel = logisticRegression.trainWithSGD(trainingData,
                        Double.parseDouble(hyperParameters.get(MLConstants.LEARNING_RATE)),
                        Integer.parseInt(hyperParameters.get(MLConstants.ITERATIONS)),
                        hyperParameters.get(MLConstants.REGULARIZATION_TYPE),
                        Double.parseDouble(hyperParameters.get(MLConstants.REGULARIZATION_PARAMETER)),
                        Double.parseDouble(hyperParameters.get(MLConstants.SGD_DATA_FRACTION)));
            } else {

                algorithmName = SUPERVISED_ALGORITHM.LOGISTIC_REGRESSION_LBFGS.toString();
                int noOfClasses = getNoOfClasses(mlModel);
                logisticRegressionModel = logisticRegression.trainWithLBFGS(trainingData,
                        hyperParameters.get(MLConstants.REGULARIZATION_TYPE), noOfClasses);
            }

            // clearing the threshold value to get a probability as the output of the prediction
            logisticRegressionModel.clearThreshold();
            JavaRDD<Tuple2<Object, Object>> scoresAndLabels = logisticRegression.test(logisticRegressionModel,
                    testingData);
            ProbabilisticClassificationModelSummary probabilisticClassificationModelSummary = SparkModelUtils
                    .generateProbabilisticClassificationModelSummary(sparkContext, testingData, scoresAndLabels);
            mlModel.setModel(logisticRegressionModel);

            List<FeatureImportance> featureWeights = getFeatureWeights(includedFeatures, logisticRegressionModel
                    .weights().toArray());
            probabilisticClassificationModelSummary.setFeatures(includedFeatures.values().toArray(new String[0]));
            probabilisticClassificationModelSummary.setFeatureImportance(featureWeights);
            probabilisticClassificationModelSummary.setAlgorithm(algorithmName);

            Double modelAccuracy = getModelAccuracy(scoresAndLabels, MLConstants.DEFAULT_THRESHOLD, testingData);
            probabilisticClassificationModelSummary.setModelAccuracy(modelAccuracy);

            return probabilisticClassificationModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building logistic regression model: "
                    + e.getMessage(), e);
        }
    }

    private int getNoOfClasses(MLModel mlModel) {
        if (mlModel.getEncodings() == null) {
            return -1;
        }
        int responseIndex = mlModel.getEncodings().size() - 1;
        return mlModel.getEncodings().get(responseIndex) != null ? mlModel.getEncodings().get(responseIndex).size()
                : -1;
    }

    /**
     * This method builds a decision tree model
     *
     * @param sparkContext      JavaSparkContext
     * @param modelID           Model ID
     * @param trainingData      Training data as a JavaRDD of LabeledPoints
     * @param testingData       Testing data as a JavaRDD of LabeledPoints
     * @param workflow          Machine learning workflow
     * @param mlModel           Deployable machine learning model
     * @param headerRow         Header row of the dataset
     * @param responseIndex     Index of the response variable in the dataset
     * @param columnSeparator   Column separator of dataset
     * @throws                  MLModelBuilderException
     */
    private ModelSummary buildDecisionTreeModel(JavaSparkContext sparkContext, long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel, SortedMap<Integer,String> includedFeatures, Map<Integer,Integer> categoricalFeatureInfo) throws MLModelBuilderException {
        try {
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            DecisionTree decisionTree = new DecisionTree();
            DecisionTreeModel decisionTreeModel = decisionTree.train(trainingData, getNoOfClasses(mlModel),
                    categoricalFeatureInfo, hyperParameters.get(MLConstants.IMPURITY),
                    Integer.parseInt(hyperParameters.get(MLConstants.MAX_DEPTH)),
                    Integer.parseInt(hyperParameters.get(MLConstants.MAX_BINS)));
            JavaPairRDD<Double, Double> predictionsAndLabels = decisionTree.test(decisionTreeModel, testingData);
            ClassClassificationAndRegressionModelSummary classClassificationAndRegressionModelSummary = SparkModelUtils
                    .getClassClassificationModelSummary(sparkContext, testingData, predictionsAndLabels);
            mlModel.setModel(decisionTreeModel);
            
            classClassificationAndRegressionModelSummary.setFeatures(includedFeatures.values().toArray(new String[0]));
            classClassificationAndRegressionModelSummary.setAlgorithm(SUPERVISED_ALGORITHM.DECISION_TREE.toString());

            MulticlassMetrics multiclassMetrics = getMulticlassMetrics(sparkContext, predictionsAndLabels);
            Double modelAccuracy = getModelAccuracy(multiclassMetrics, testingData);
            classClassificationAndRegressionModelSummary.setModelAccuracy(modelAccuracy);

            return classClassificationAndRegressionModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building decision tree model: " + e.getMessage(),
                    e);
        }

    }

    /**
     * This method builds a support vector machine (SVM) model
     *
     * @param modelID           Model ID
     * @param trainingData      Training data as a JavaRDD of LabeledPoints
     * @param testingData       Testing data as a JavaRDD of LabeledPoints
     * @param workflow          Machine learning workflow
     * @param mlModel           Deployable machine learning model
     * @param headerRow         Header row of the dataset
     * @param responseIndex     Index of the response variable in the dataset
     * @param columnSeparator   Column separator of dataset
     * @throws                  MLModelBuilderException
     */
    private ModelSummary buildSVMModel(JavaSparkContext sparkContext, long modelID, JavaRDD<LabeledPoint> trainingData, JavaRDD<LabeledPoint> testingData,
            Workflow workflow, MLModel mlModel, SortedMap<Integer,String> includedFeatures)
            throws MLModelBuilderException {
        try {
            SVM svm = new SVM();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            SVMModel svmModel = svm.train(trainingData, Integer.parseInt(hyperParameters.get(MLConstants.ITERATIONS)),
                    hyperParameters.get(MLConstants.REGULARIZATION_TYPE),
                    Double.parseDouble(hyperParameters.get(MLConstants.REGULARIZATION_PARAMETER)),
                    Double.parseDouble(hyperParameters.get(MLConstants.LEARNING_RATE)),
                    Double.parseDouble(hyperParameters.get(MLConstants.SGD_DATA_FRACTION)));
            svmModel.clearThreshold();
            JavaRDD<Tuple2<Object, Object>> scoresAndLabels = svm.test(svmModel, testingData);
            ProbabilisticClassificationModelSummary probabilisticClassificationModelSummary =
                    SparkModelUtils.generateProbabilisticClassificationModelSummary(sparkContext, testingData, scoresAndLabels);
            mlModel.setModel(svmModel);
            
            List<FeatureImportance> featureWeights = getFeatureWeights(includedFeatures, svmModel.weights().toArray());
            probabilisticClassificationModelSummary.setFeatures(includedFeatures.values().toArray(new String[0]));
            probabilisticClassificationModelSummary.setFeatureImportance(featureWeights);
            probabilisticClassificationModelSummary.setAlgorithm(SUPERVISED_ALGORITHM.SVM.toString());

            Double modelAccuracy = getModelAccuracy(scoresAndLabels, MLConstants.DEFAULT_THRESHOLD, testingData);
            probabilisticClassificationModelSummary.setModelAccuracy(modelAccuracy);

            return probabilisticClassificationModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building SVM model: " + e.getMessage(), e);
        }
    }

    /**
     * This method builds a linear regression model
     *
     * @param sparkContext      JavaSparkContext
     * @param modelID           Model ID
     * @param trainingData      Training data as a JavaRDD of LabeledPoints
     * @param testingData       Testing data as a JavaRDD of LabeledPoints
     * @param workflow          Machine learning workflow
     * @param mlModel           Deployable machine learning model
     * @param headerRow         Header row of the dataset
     * @param responseIndex     Index of the response variable in the dataset
     * @param columnSeparator   Column separator of dataset
     * @throws                  MLModelBuilderException
     */
    private ModelSummary buildLinearRegressionModel(JavaSparkContext sparkContext, long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel, SortedMap<Integer,String> includedFeatures) throws MLModelBuilderException {
        try {
            LinearRegression linearRegression = new LinearRegression();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            LinearRegressionModel linearRegressionModel = linearRegression.train(trainingData,
                    Integer.parseInt(hyperParameters.get(MLConstants.ITERATIONS)),
                    Double.parseDouble(hyperParameters.get(MLConstants.LEARNING_RATE)),
                    Double.parseDouble(hyperParameters.get(MLConstants.SGD_DATA_FRACTION)));
            JavaRDD<Tuple2<Double, Double>> predictionsAndLabels = linearRegression.test(linearRegressionModel,
                    testingData);
            ClassClassificationAndRegressionModelSummary regressionModelSummary = SparkModelUtils
                    .generateRegressionModelSummary(sparkContext, testingData, predictionsAndLabels);
            mlModel.setModel(linearRegressionModel);
            
            List<FeatureImportance> featureWeights = getFeatureWeights(includedFeatures, linearRegressionModel
                    .weights().toArray());
            regressionModelSummary.setFeatures(includedFeatures.values().toArray(new String[0]));
            regressionModelSummary.setFeatureImportance(featureWeights);
            regressionModelSummary.setAlgorithm(SUPERVISED_ALGORITHM.LINEAR_REGRESSION.toString());

            RegressionMetrics regressionMetrics = getRegressionMetrics(sparkContext, predictionsAndLabels);
            Double meanSquaredError = regressionMetrics.meanSquaredError();
            regressionModelSummary.setMeanSquaredError(meanSquaredError);

            return regressionModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building linear regression model: "
                    + e.getMessage(), e);
        }
    }

    /**
     * This method builds a ridge regression model
     *
     * @param sparkContext      JavaSparkContext
     * @param modelID           Model ID
     * @param trainingData      Training data as a JavaRDD of LabeledPoints
     * @param testingData       Testing data as a JavaRDD of LabeledPoints
     * @param workflow          Machine learning workflow
     * @param mlModel           Deployable machine learning model
     * @param headerRow         Header row of the dataset
     * @param responseIndex     Index of the response variable in the dataset
     * @param columnSeparator   Column separator of dataset
     * @throws                  MLModelBuilderException
     */
    private ModelSummary buildRidgeRegressionModel(JavaSparkContext sparkContext, long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel, SortedMap<Integer,String> includedFeatures) throws MLModelBuilderException {
        try {
            RidgeRegression ridgeRegression = new RidgeRegression();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            RidgeRegressionModel ridgeRegressionModel = ridgeRegression.train(trainingData,
                    Integer.parseInt(hyperParameters.get(MLConstants.ITERATIONS)),
                    Double.parseDouble(hyperParameters.get(MLConstants.LEARNING_RATE)),
                    Double.parseDouble(hyperParameters.get(MLConstants.REGULARIZATION_PARAMETER)),
                    Double.parseDouble(hyperParameters.get(MLConstants.SGD_DATA_FRACTION)));
            JavaRDD<Tuple2<Double, Double>> predictionsAndLabels = ridgeRegression.test(ridgeRegressionModel,
                    testingData);
            ClassClassificationAndRegressionModelSummary regressionModelSummary = SparkModelUtils
                    .generateRegressionModelSummary(sparkContext, testingData, predictionsAndLabels);
            mlModel.setModel(ridgeRegressionModel);
            
            List<FeatureImportance> featureWeights = getFeatureWeights(includedFeatures, ridgeRegressionModel
                    .weights().toArray());
            regressionModelSummary.setFeatures(includedFeatures.values().toArray(new String[0]));
            regressionModelSummary.setAlgorithm(SUPERVISED_ALGORITHM.RIDGE_REGRESSION.toString());
            regressionModelSummary.setFeatureImportance(featureWeights);

            RegressionMetrics regressionMetrics = getRegressionMetrics(sparkContext, predictionsAndLabels);
            Double meanSquaredError = regressionMetrics.meanSquaredError();
            regressionModelSummary.setMeanSquaredError(meanSquaredError);

            return regressionModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building ridge regression model: "
                    + e.getMessage(), e);
        }
    }

    /**
     * This method builds a lasso regression model
     *
     * @param sparkContext      JavaSparkContext
     * @param modelID           Model ID
     * @param trainingData      Training data as a JavaRDD of LabeledPoints
     * @param testingData       Testing data as a JavaRDD of LabeledPoints
     * @param workflow          Machine learning workflow
     * @param mlModel           Deployable machine learning model
     * @param headerRow         Header row of the dataset
     * @param responseIndex     Index of the response variable in the dataset
     * @param columnSeparator   Column separator of dataset
     * @throws                  MLModelBuilderException
     */
    private ModelSummary buildLassoRegressionModel(JavaSparkContext sparkContext, long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel, SortedMap<Integer,String> includedFeatures) throws MLModelBuilderException {
        try {
            LassoRegression lassoRegression = new LassoRegression();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            LassoModel lassoModel = lassoRegression.train(trainingData,
                    Integer.parseInt(hyperParameters.get(MLConstants.ITERATIONS)),
                    Double.parseDouble(hyperParameters.get(MLConstants.LEARNING_RATE)),
                    Double.parseDouble(hyperParameters.get(MLConstants.REGULARIZATION_PARAMETER)),
                    Double.parseDouble(hyperParameters.get(MLConstants.SGD_DATA_FRACTION)));
            JavaRDD<Tuple2<Double, Double>> predictionsAndLabels = lassoRegression.test(lassoModel, testingData);
            ClassClassificationAndRegressionModelSummary regressionModelSummary = SparkModelUtils
                    .generateRegressionModelSummary(sparkContext, testingData, predictionsAndLabels);
            mlModel.setModel(lassoModel);
            
            List<FeatureImportance> featureWeights = getFeatureWeights(includedFeatures, lassoModel.weights()
                    .toArray());
            regressionModelSummary.setFeatures(includedFeatures.values().toArray(new String[0]));
            regressionModelSummary.setAlgorithm(SUPERVISED_ALGORITHM.LASSO_REGRESSION.toString());
            regressionModelSummary.setFeatureImportance(featureWeights);

            RegressionMetrics regressionMetrics = getRegressionMetrics(sparkContext, predictionsAndLabels);
            Double meanSquaredError = regressionMetrics.meanSquaredError();
            regressionModelSummary.setMeanSquaredError(meanSquaredError);

            return regressionModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building lasso regression model: "
                    + e.getMessage(), e);
        }
    }

    /**
     * This method builds a naive bayes model
     *
     * @param sparkContext      JavaSparkContext
     * @param modelID           Model ID
     * @param trainingData      Training data as a JavaRDD of LabeledPoints
     * @param testingData       Testing data as a JavaRDD of LabeledPoints
     * @param workflow          Machine learning workflow
     * @param mlModel           Deployable machine learning model
     * @param headerRow         Header row of the dataset
     * @param responseIndex     Index of the response variable in the dataset
     * @param columnSeparator   Column separator of dataset
     * @throws                  MLModelBuilderException
     */
    private ModelSummary buildNaiveBayesModel(JavaSparkContext sparkContext, long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel, SortedMap<Integer,String> includedFeatures) throws MLModelBuilderException {
        try {
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            NaiveBayesClassifier naiveBayesClassifier = new NaiveBayesClassifier();
            NaiveBayesModel naiveBayesModel = naiveBayesClassifier.train(trainingData, Double.parseDouble(
                    hyperParameters.get(MLConstants.LAMBDA)));
            JavaPairRDD<Double, Double> predictionsAndLabels = naiveBayesClassifier.test(naiveBayesModel, testingData);
            ClassClassificationAndRegressionModelSummary classClassificationAndRegressionModelSummary = SparkModelUtils
                    .getClassClassificationModelSummary(sparkContext, testingData, predictionsAndLabels);
            mlModel.setModel(naiveBayesModel);
            
            classClassificationAndRegressionModelSummary.setFeatures(includedFeatures.values().toArray(new String[0]));
            classClassificationAndRegressionModelSummary.setAlgorithm(SUPERVISED_ALGORITHM.NAIVE_BAYES.toString());

            MulticlassMetrics multiclassMetrics = getMulticlassMetrics(sparkContext, predictionsAndLabels);
            Double modelAccuracy = getModelAccuracy(multiclassMetrics, testingData);
            classClassificationAndRegressionModelSummary.setModelAccuracy(modelAccuracy);

            return classClassificationAndRegressionModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building naive bayes model: " + e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param features  Array of names of features
     * @param weights   Array of weights of features
     * @return
     */
    private List<FeatureImportance> getFeatureWeights(SortedMap<Integer,String> features, double[] weights) {
        List<FeatureImportance> featureWeights = new ArrayList<FeatureImportance>();
        int i = 0 ;
        for(String featureName : features.values()) {
            FeatureImportance featureImportance = new FeatureImportance();
            featureImportance.setLabel(featureName);
            featureImportance.setValue(weights[i]);
            featureWeights.add(featureImportance);
            i++;
        }
        return featureWeights;
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
     * This method gets regression metrics for a given set of prediction and label values
     *
     * @param sparkContext           JavaSparkContext
     * @param predictionsAndLabels   Prediction and label values RDD
     */
    private RegressionMetrics getRegressionMetrics(JavaSparkContext sparkContext, JavaRDD<Tuple2<Double, Double>> predictionsAndLabels) {
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
        RegressionMetrics regressionMetrics = new RegressionMetrics(scoresAndLabelsRDD);
        return  regressionMetrics;
    }

    /**
     * This method gets model accuracy for a given testing dataset
     *
     * @param scoresAndLabels   score and label values
     * @param threshold         Threshold value
     * @param testingData       Testing data RDD
     */
    private Double getModelAccuracy(JavaRDD<Tuple2<Object, Object>> scoresAndLabels, Double threshold, JavaRDD<LabeledPoint> testingData) {
        Double modelAccuracy = 0.0;
        long correctlyClassified = 0;
        long totalPopulation = testingData.count();
        List<Tuple2<Object,Object>> scoresAndLabelsList = scoresAndLabels.collect();
        for (Tuple2<Object, Object> scoresAndLabel : scoresAndLabelsList) {
            Double scores = (Double) scoresAndLabel._1;
            Double label = (Double) scoresAndLabel._2;
            if (getLabel(scores, threshold).equals(label)) {
                correctlyClassified++;
            }
        }
        if(totalPopulation > 0) {
            modelAccuracy = (double) correctlyClassified/totalPopulation;
        }
        return modelAccuracy;
    }

    /**
     * This method gets model accuracy from given multi-class metrics
     *
     * @param multiclassMetrics     multi-class metrics object
     * @param testingData           Testing data RDD
     */
    private Double getModelAccuracy(MulticlassMetrics multiclassMetrics, JavaRDD<LabeledPoint> testingData) {
        Double modelAccuracy = 0.0;
        int confusionMatrixSize = multiclassMetrics.confusionMatrix().numCols();
        int confusionMatrixDiagonal = 0;
        long totalPopulation = testingData.count();
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
     * This method gets label of a given score
     *
     * @param score        Predicted score
     * @param threshold    Threshold value
     */
    private Double getLabel(Double score, Double threshold) {
        Double label = 0.0;
        if(score >= threshold) {
            label = 1.0;
        }
        return label;
    }
}
