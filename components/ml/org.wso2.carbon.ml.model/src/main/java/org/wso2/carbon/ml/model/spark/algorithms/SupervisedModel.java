/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.model.spark.algorithms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.wso2.carbon.ml.model.DatabaseHandler;
import org.wso2.carbon.ml.model.MLModelUtils;
import org.wso2.carbon.ml.model.constants.MLModelConstants;
import org.wso2.carbon.ml.model.dto.ProbabilisticClassificationModelSummary;
import org.wso2.carbon.ml.model.dto.MLWorkflow;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
import org.wso2.carbon.ml.model.spark.transformations.Header;
import org.wso2.carbon.ml.model.spark.transformations.LineToTokens;
import org.wso2.carbon.ml.model.spark.transformations.TokensToLabeledPoints;
import scala.Tuple2;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SupervisedModel {
    private static final Log logger = LogFactory.getLog(SupervisedModel.class);

    public void buildModel(String modelID,MLWorkflow workflow, SparkConf sparkConf) {
        try {
            sparkConf.setAppName(modelID);
            // create a new java spark context
            JavaSparkContext sc = new JavaSparkContext(sparkConf);
            // parse lines in the dataset
            String datasetURL = workflow.getDatasetURL();
            JavaRDD<String> lines = sc.textFile(datasetURL);
            // filter out header line
            String headerRow = lines.take(1).get(0);
            Header header = new Header(headerRow);
            JavaRDD<String> data = lines.filter(header);
            // convert lines to tokens
            String columnSeparator = MLModelUtils.getColumnSeparator(datasetURL);
            Pattern pattern = Pattern.compile(columnSeparator);
            LineToTokens lineToTokens = new LineToTokens(pattern);
            JavaRDD<String[]> tokens = data.map(lineToTokens);
            // generate train and test datasets by converting tokens to labeled points
            int responseIndex = MLModelUtils.getResponseIndex(workflow.getResponseVariable(), headerRow, columnSeparator);
            TokensToLabeledPoints tokensToLabeledPoints = new TokensToLabeledPoints(responseIndex);
            JavaRDD<LabeledPoint> labeledPoints = tokens.map(tokensToLabeledPoints);
            JavaRDD<LabeledPoint> trainingData = labeledPoints.sample(false,
                    workflow.getTrainDataFraction(),
                    MLModelConstants.RANDOM_SEED);
            JavaRDD<LabeledPoint> testingData = labeledPoints.subtract(trainingData);
            // build a machine learning model according to user selected algorithm
            MLModelConstants.SUPERVISED_ALGORITHM supervisedAlgorithm = MLModelConstants
                    .SUPERVISED_ALGORITHM.valueOf(
                            workflow.getAlgorithmName());
            switch (supervisedAlgorithm) {
                case LOGISTIC_REGRESSION:
                    buildLogisticRegressionModel(modelID,trainingData, testingData, workflow);
                    break;
                case DECISION_TREE:
                    // empty hashmap - no categorical features
                    // needs to be populated with categorical column number:no of categories
                    Map<Integer, Integer> categoricalFeatures = new HashMap();
                    buildDecisionTreeModel(modelID,trainingData, testingData, categoricalFeatures,
                            workflow);
                    break;
                default:
                    throw new IllegalStateException();
            }
            // stop spark context
            sc.stop();
        } catch (Exception e) {
            logger.error("An error occurred while building supervised machine learning " +
                         "model: " + e.getMessage(), e);
        }
    }

    /**
     * This method builds a logistic regression model
     *
     * @param trainingData Training data
     * @param testingData  Testing data
     * @param workflow     Machine learning workflow
     * @throws org.wso2.carbon.ml.model.exceptions.ModelServiceException
     */
    private void buildLogisticRegressionModel(String modelID,JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData,
            MLWorkflow workflow) throws ModelServiceException {
        try {
            DatabaseHandler databaseHandler = new DatabaseHandler();
            databaseHandler.insertModel(modelID, workflow.getWorkflowID(),
                    new Time(System.currentTimeMillis()));
            LogisticRegression logisticRegression = new LogisticRegression();
            Map<String,String> hyperParameters = workflow.getHyperParameters();
            LogisticRegressionModel model = logisticRegression.trainWithSGD(trainingData,
                    Double.parseDouble(hyperParameters.get(MLModelConstants.LEARNING_RATE)),
                    Integer.parseInt(hyperParameters.get(MLModelConstants.ITERATIONS)),
                    hyperParameters.get(MLModelConstants.REGULARIZATION_TYPE),
                    Double.parseDouble(
                            hyperParameters.get(MLModelConstants.REGULARIZATION_PARAMETER)),
                    Double.parseDouble(hyperParameters.get(MLModelConstants.SGD_DATA_FRACTION)));
            model.clearThreshold();
            JavaRDD<Tuple2<Object, Object>> scoresAndLabels = logisticRegression.test(model,
                    testingData);
            ProbabilisticClassificationModelSummary probabilisticClassificationModelSummary = logisticRegression
                    .getModelSummary(scoresAndLabels);
            databaseHandler.updateModel(modelID, model, probabilisticClassificationModelSummary,
                    new Time(System.currentTimeMillis()));
        } catch (Exception e) {
            throw new ModelServiceException(
                    "An error occured while building logistic regression model: " + e.getMessage(),
                    e);
        }
    }

    /**
     * This method builds a decision tree model
     *
     * @param trainingData        Training data
     * @param testingData         Testing data
     * @param categoricalFeatures Map of categorical features - categorical column index : no of
     *                            categories
     * @param workflow            Machine learning workflow
     * @throws ModelServiceException
     */
    private void buildDecisionTreeModel(String modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData,
            Map<Integer, Integer> categoricalFeatures,
            MLWorkflow workflow) throws ModelServiceException {
        try {
            Map<String,String> hyperParameters = workflow.getHyperParameters();
            DecisionTree decisionTree = new DecisionTree();
            DecisionTreeModel decisionTreeModel = decisionTree.train(trainingData,
                    Integer.parseInt(hyperParameters.get(MLModelConstants.NUM_CLASSES)), categoricalFeatures,
                    hyperParameters.get(MLModelConstants.IMPURITY),
                    Integer.parseInt(hyperParameters.get(MLModelConstants.MAX_DEPTH)),
                    Integer.parseInt(hyperParameters.get(MLModelConstants.MAX_BINS)));
            JavaPairRDD<Double, Double> predictionsAnsLabels = decisionTree.test(decisionTreeModel,
                    trainingData);
            double testError = decisionTree.getTestError(predictionsAnsLabels);
        } catch (Exception e) {
            throw new ModelServiceException(
                    "An error occured while building decision tree model: " + e.getMessage(), e);
        }

    }
}
