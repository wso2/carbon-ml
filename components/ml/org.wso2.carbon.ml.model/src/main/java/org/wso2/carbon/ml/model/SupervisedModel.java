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

package org.wso2.carbon.ml.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.json.JSONObject;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
import scala.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SupervisedModel implements Runnable {
    private JSONObject workflow;
    private static final Log logger = LogFactory.getLog(SupervisedModel.class);

    /**
     * @param workflow Machine learning workflow
     */
    SupervisedModel(JSONObject workflow) {
        this.workflow = workflow;
    }

    @Override
    public void run() {
        try {
            // create a new spark configuration
            SparkConfigurationParser sparkConfigurationParser = new SparkConfigurationParser();
            SparkConf sparkConf = sparkConfigurationParser.getSparkConfiguration();
            sparkConf.setAppName(workflow.getString(MLModelConstants.MODEL_ID));
            // create a new java spark context
            JavaSparkContext sc = new JavaSparkContext(sparkConf);
            // parse lines in the dataset
            String datasetURL = workflow.getString(MLModelConstants.DATASET_URL);
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
            int responseIndex = MLModelUtils.getResponseIndex(workflow.getString(MLModelConstants
                                                                                         .RESPONSE), headerRow, columnSeparator);
            TokensToLabeledPoints tokensToLabeledPoints = new TokensToLabeledPoints(responseIndex);
            JavaRDD<LabeledPoint> labeledPoints = tokens.map(tokensToLabeledPoints);
            JavaRDD<LabeledPoint> trainingData = labeledPoints.sample(false,
                                                                      workflow.getDouble(MLModelConstants.TRAIN_DATA_FRACTION),
                                                                      MLModelConstants.RANDOM_SEED);
            JavaRDD<LabeledPoint> testingData = labeledPoints.subtract(trainingData);
            // build a machine learning model according to user selected algorithm
            MLModelConstants.SUPERVISED_ALGORITHM supervisedAlgorithm = MLModelConstants
                    .SUPERVISED_ALGORITHM.valueOf(workflow.getString(MLModelConstants.ALGORITHM));
            switch (supervisedAlgorithm) {
                case LOGISTIC_REGRESSION:
                    buildLogisticRegressionModel(trainingData, testingData, workflow);
                    break;
                case DECISION_TREE:
                    // empty hashmap - no categorical features
                    // needs to be populated with categorical column number:no of categories
                    Map<Integer, Integer> categoricalFeatures = new HashMap();
                    buildDecisionTreeModel(trainingData, testingData, categoricalFeatures, workflow);
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
    private void buildLogisticRegressionModel(JavaRDD<LabeledPoint> trainingData,
                                              JavaRDD<LabeledPoint> testingData,
                                              JSONObject workflow) throws ModelServiceException {
        try {
            LogisticRegression logisticRegression = new LogisticRegression();
            LogisticRegressionModel model = logisticRegression.trainWithSGD(trainingData,
                                                                            workflow.getDouble
                                                                                    (MLModelConstants.LEARNING_RATE),
                                                                            workflow.getInt
                                                                                    (MLModelConstants.ITERATIONS),
                                                                            workflow.getString
                                                                                    (MLModelConstants.REGULARIZATION_TYPE),
                                                                            workflow.getDouble
                                                                                    (MLModelConstants.REGULARIZATION_PARAMETER),
                                                                            workflow.getDouble
                                                                                    (MLModelConstants.SGD_DATA_FRACTION));
            model.clearThreshold();
            JavaRDD<Tuple2<Object, Object>> scoresAndLabels = logisticRegression.test(model,
                                                                                      testingData);
            LogisticRegressionModelSummary logisticRegressionModelSummary = logisticRegression
                    .getModelSummary(scoresAndLabels);
        } catch (Exception e) {
            throw new ModelServiceException(e.getMessage(), e);
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
    private void buildDecisionTreeModel(JavaRDD<LabeledPoint> trainingData,
                                        JavaRDD<LabeledPoint> testingData,
                                        Map<Integer, Integer> categoricalFeatures,
                                        JSONObject workflow) throws ModelServiceException {
        try {
            DecisionTree decisionTree = new DecisionTree();
            DecisionTreeModel decisionTreeModel = decisionTree.train(trainingData,
                                                                     workflow.getInt(MLModelConstants.NUM_CLASSES),
                                                                     categoricalFeatures,
                                                                     workflow.getString(MLModelConstants.IMPURITY),
                                                                     workflow.getInt(MLModelConstants.MAX_DEPTH),
                                                                     workflow.getInt(MLModelConstants.MAX_BINS));
            JavaPairRDD<Double, Double> predictionsAnsLabels = decisionTree.test(decisionTreeModel, trainingData);
            double testError = decisionTree.getTestError(predictionsAnsLabels);
        } catch (Exception e) {
            throw new ModelServiceException(e.getMessage(), e);
        }

    }
}
