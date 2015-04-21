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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LassoModel;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.mllib.regression.RidgeRegressionModel;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
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
    
    private static final Log logger = LogFactory.getLog(SupervisedModel.class);
    
    /**
     * @param modelID   Model ID
     * @param workflow  Workflow ID
     * @param sparkConf Spark configuration
     * @throws MLModelBuilderException
     */
    public MLModel buildModel(MLModelConfigurationContext context)
            throws MLModelBuilderException {
        JavaSparkContext sparkContext = null;
        DatabaseService databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
        MLModel mlModel = new MLModel();
        try {
            sparkContext = context.getSparkContext();
            Workflow workflow = context.getFacts();
            String headerRow = context.getHeaderRow();
            String columnSeparator = context.getColumnSeparator();
            long modelId = context.getModelId();
            
            // apply pre-processing
            JavaRDD<double[]> features = SparkModelUtils.preProcess(sparkContext, workflow, context.getLines(), headerRow, columnSeparator);
            // generate train and test datasets by converting tokens to labeled points
            int responseIndex = MLUtils.getFeatureIndex(workflow.getResponseVariable(), headerRow, columnSeparator);
            DoubleArrayToLabeledPoint doubleArrayToLabeledPoint = new DoubleArrayToLabeledPoint(responseIndex);
            JavaRDD<LabeledPoint> labeledPoints = features.map(doubleArrayToLabeledPoint);
            JavaRDD<LabeledPoint> trainingData = labeledPoints.sample(false, workflow.getTrainDataFraction(), 
                    MLConstants.RANDOM_SEED);
            JavaRDD<LabeledPoint> testingData = labeledPoints.subtract(trainingData);
            // create a deployable MLModel object
            mlModel.setAlgorithmName(workflow.getAlgorithmName());
            mlModel.setAlgorithmClass(workflow.getAlgorithmClass());
            mlModel.setFeatures(workflow.getFeatures());
            
            ModelSummary summaryModel = null;
            
            // build a machine learning model according to user selected algorithm
            SUPERVISED_ALGORITHM supervisedAlgorithm = SUPERVISED_ALGORITHM.valueOf(workflow.getAlgorithmName());
            switch (supervisedAlgorithm) {
            case LOGISTIC_REGRESSION:
                summaryModel = buildLogisticRegressionModel(modelId, trainingData, testingData, workflow, mlModel, 
                        headerRow, responseIndex, columnSeparator);
                break;
            case DECISION_TREE:
                summaryModel = buildDecisionTreeModel(modelId, trainingData, testingData, workflow, mlModel);
                break;
            case SVM:
                summaryModel = buildSVMModel(modelId, trainingData, testingData, workflow, mlModel, headerRow, 
                        responseIndex, columnSeparator);
                break;
            case NAIVE_BAYES:
                summaryModel = buildNaiveBayesModel(modelId, trainingData, testingData, workflow, mlModel);
                break;
            case LINEAR_REGRESSION:
                summaryModel = buildLinearRegressionModel(modelId, trainingData, testingData, workflow, mlModel,
                        headerRow, responseIndex, columnSeparator);
                break;
            case RIDGE_REGRESSION:
                summaryModel = buildRidgeRegressionModel(modelId, trainingData, testingData, workflow, mlModel,
                        headerRow, responseIndex, columnSeparator);
                break;
            case LASSO_REGRESSION:
                summaryModel = buildLassoRegressionModel(modelId, trainingData, testingData, workflow, mlModel,
                        headerRow, responseIndex, columnSeparator);
                break;
            default:
                throw new AlgorithmNameException("Incorrect algorithm name");
            }
            
            // persist model summary
            databaseService.updateModelSummary(modelId, summaryModel);
            return mlModel;
        }catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building supervised machine learning model: " +
                        e.getMessage(), e);
        }
        finally {
            if (sparkContext != null) {
                sparkContext.stop();
            }
        }
    }

    /**
     * This method builds a logistic regression model
     *
     * @param modelID      Model ID
     * @param trainingData Training data as a JavaRDD of LabeledPoints
     * @param testingData  Testing data as a JavaRDD of LabeledPoints
     * @param workflow     Machine learning workflow
     * @param mlModel      Deployable machine learning model
     * @throws MLModelBuilderException
     */
    private ModelSummary buildLogisticRegressionModel(long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel, String headerRow, int responseIndex,
            String columnSeparator) throws MLModelBuilderException {
        try {
            LogisticRegression logisticRegression = new LogisticRegression();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            LogisticRegressionModel logisticRegressionModel = logisticRegression.trainWithSGD(trainingData,
                    Double.parseDouble(hyperParameters.get(MLConstants.LEARNING_RATE)),
                    Integer.parseInt(hyperParameters.get(MLConstants.ITERATIONS)),
                    hyperParameters.get(MLConstants.REGULARIZATION_TYPE),
                    Double.parseDouble(hyperParameters.get(MLConstants.REGULARIZATION_PARAMETER)),
                    Double.parseDouble(hyperParameters.get(MLConstants.SGD_DATA_FRACTION)));
            // clearing the threshold value to get a probability as the output of the prediction
            logisticRegressionModel.clearThreshold();
            JavaRDD<Tuple2<Object, Object>> scoresAndLabels = logisticRegression.test(logisticRegressionModel,
                    testingData);
            ProbabilisticClassificationModelSummary probabilisticClassificationModelSummary =
                    SparkModelUtils.generateProbabilisticClassificationModelSummary(scoresAndLabels);
            mlModel.setModel(logisticRegressionModel);
            
            List<FeatureImportance> featureWeights = getFeatureWeights(logisticRegressionModel.weights().toArray(), 
                    headerRow.split(columnSeparator), responseIndex);
            probabilisticClassificationModelSummary.setFeatureImportance(featureWeights);
            return probabilisticClassificationModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building logistic regression model: "
                    + e.getMessage(), e);
        }
    }

    /**
     * This method builds a decision tree model
     *
     * @param modelID       Model ID
     * @param trainingData  Training data as a JavaRDD of LabeledPoints
     * @param testingData   Testing data as a JavaRDD of LabeledPoints
     * @param workflow      Machine learning workflow
     * @param mlModel       Deployable machine learning model
     * @throws              MLModelBuilderException
     */
    private ModelSummary buildDecisionTreeModel(long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel) throws MLModelBuilderException {
        try {
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            DecisionTree decisionTree = new DecisionTree();
            // Lochana: passing an empty map since we are not currently handling categorical Features
            DecisionTreeModel decisionTreeModel = decisionTree.train(trainingData,
                    Integer.parseInt(hyperParameters.get(MLConstants.NUM_CLASSES)),
                    new HashMap<Integer, Integer>(), hyperParameters.get(MLConstants.IMPURITY),
                    Integer.parseInt(hyperParameters.get(MLConstants.MAX_DEPTH)),
                    Integer.parseInt(hyperParameters.get(MLConstants.MAX_BINS)));
            JavaPairRDD<Double, Double> predictionsAndLabels = decisionTree.test(decisionTreeModel, testingData);
            ClassClassificationAndRegressionModelSummary classClassificationAndRegressionModelSummary = SparkModelUtils
                    .getClassClassificationModelSummary(predictionsAndLabels);
            mlModel.setModel(decisionTreeModel);
            return classClassificationAndRegressionModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building decision tree model: " + e.getMessage(),
                    e);
        }

    }

    /**
     * This method builds a support vector machine (SVM) model
     *
     * @param modelID       Model ID
     * @param trainingData  Training data as a JavaRDD of LabeledPoints
     * @param testingData   Testing data as a JavaRDD of LabeledPoints
     * @param workflow      Machine learning workflow
     * @param mlModel       Deployable machine learning model
     * @throws              MLModelBuilderException
     */
    private ModelSummary buildSVMModel(long modelID, JavaRDD<LabeledPoint> trainingData, JavaRDD<LabeledPoint> testingData,
            Workflow workflow, MLModel mlModel, String headerRow, int responseIndex,String columnSeparator)
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
                    SparkModelUtils.generateProbabilisticClassificationModelSummary(scoresAndLabels);
            mlModel.setModel(svmModel);
            
            List<FeatureImportance> featureWeights = getFeatureWeights(svmModel.weights().toArray(), 
                headerRow.split(columnSeparator), responseIndex);
            probabilisticClassificationModelSummary.setFeatureImportance(featureWeights);
            return probabilisticClassificationModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building SVM model: " + e.getMessage(), e);
        }
    }

    /**
     * This method builds a linear regression model
     *
     * @param modelID       Model ID
     * @param trainingData  Training data as a JavaRDD of LabeledPoints
     * @param testingData   Testing data as a JavaRDD of LabeledPoints
     * @param workflow      Machine learning workflow
     * @param mlModel       Deployable machine learning model
     * @throws              MLModelBuilderException
     */
    private ModelSummary buildLinearRegressionModel(long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel, String headerRow, int responseIndex,
            String columnSeparator) throws MLModelBuilderException {
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
                    .generateRegressionModelSummary(predictionsAndLabels);
            mlModel.setModel(linearRegressionModel);
            
            List<FeatureImportance> featureWeights = getFeatureWeights(linearRegressionModel.weights().toArray(), 
                headerRow.split(columnSeparator), responseIndex);
            regressionModelSummary.setFeatureImportance(featureWeights);
            return regressionModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building linear regression model: "
                    + e.getMessage(), e);
        }
    }

    /**
     * This method builds a ridge regression model
     *
     * @param modelID       Model ID
     * @param trainingData  Training data as a JavaRDD of LabeledPoints
     * @param testingData   Testing data as a JavaRDD of LabeledPoints
     * @param workflow      Machine learning workflow
     * @param mlModel       Deployable machine learning model
     * @throws              MLModelBuilderException
     */
    private ModelSummary buildRidgeRegressionModel(long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel, String headerRow, int responseIndex,
            String columnSeparator) throws MLModelBuilderException {
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
                    .generateRegressionModelSummary(predictionsAndLabels);
            mlModel.setModel(ridgeRegressionModel);
            
            List<FeatureImportance> featureWeights = getFeatureWeights(ridgeRegressionModel.weights().toArray(), 
                headerRow.split(columnSeparator), responseIndex);
            regressionModelSummary.setFeatureImportance(featureWeights);
            return regressionModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building ridge regression model: "
                    + e.getMessage(), e);
        }
    }

    /**
     * This method builds a lasso regression model
     *
     * @param modelID       Model ID
     * @param trainingData  Training data as a JavaRDD of LabeledPoints
     * @param testingData   Testing data as a JavaRDD of LabeledPoints
     * @param workflow      Machine learning workflow
     * @param mlModel       Deployable machine learning model
     * @throws              MLModelBuilderException
     */
    private ModelSummary buildLassoRegressionModel(long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel, String headerRow, int responseIndex,
            String columnSeparator) throws MLModelBuilderException {
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
                    .generateRegressionModelSummary(predictionsAndLabels);
            mlModel.setModel(lassoModel);
            
            List<FeatureImportance> featureWeights = getFeatureWeights(lassoModel.weights().toArray(), 
                headerRow.split(columnSeparator), responseIndex);
            regressionModelSummary.setFeatureImportance(featureWeights);
            return regressionModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building lasso regression model: "
                    + e.getMessage(), e);
        }
    }

    /**
     * This method builds a naive bayes model
     *
     * @param modelID       Model ID
     * @param trainingData  Training data as a JavaRDD of LabeledPoints
     * @param testingData   Testing data as a JavaRDD of LabeledPoints
     * @param workflow      Machine learning workflow
     * @param mlModel       Deployable machine learning model
     * @throws              MLModelBuilderException
     */
    private ModelSummary buildNaiveBayesModel(long modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel) throws MLModelBuilderException {
        try {
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            NaiveBayesClassifier naiveBayesClassifier = new NaiveBayesClassifier();
            NaiveBayesModel naiveBayesModel = naiveBayesClassifier.train(trainingData, Double.parseDouble(
                    hyperParameters.get(MLConstants.LAMBDA)));
            JavaPairRDD<Double, Double> predictionsAndLabels = naiveBayesClassifier.test(naiveBayesModel, trainingData);
            ClassClassificationAndRegressionModelSummary classClassificationAndRegressionModelSummary = SparkModelUtils
                    .getClassClassificationModelSummary(predictionsAndLabels);
            mlModel.setModel(naiveBayesModel);
            return classClassificationAndRegressionModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building naive bayes model: " + e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param weights   Array of feature weights
     * @param features  Array of Feature names
     * @param Response  Index of response feature
     * @return
     */
    private List<FeatureImportance> getFeatureWeights(double[] weights, String[] features, int Response) {
        List<FeatureImportance> featureWeights = new ArrayList<FeatureImportance>();
        
        // Remove the response variable from the features
        List<String> featureNames = new ArrayList<String>(Arrays.asList(features));
        featureNames.remove(Response);
        
        for(int i = 0 ; i < weights.length ; i++) {
            FeatureImportance featureImportance = new FeatureImportance();
            featureImportance.setLabel(featureNames.get(i));
            featureImportance.setValue(weights[i]);
            featureWeights.add(featureImportance);
        }
        return featureWeights;
    }
}
