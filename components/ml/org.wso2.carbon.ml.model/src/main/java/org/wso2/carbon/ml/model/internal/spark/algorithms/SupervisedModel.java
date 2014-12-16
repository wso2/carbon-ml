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

package org.wso2.carbon.ml.model.internal.spark.algorithms;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.stat.MultivariateStatisticalSummary;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.wso2.carbon.ml.model.internal.DatabaseHandler;
import org.wso2.carbon.ml.model.internal.MLModelUtils;
import org.wso2.carbon.ml.model.internal.constants.MLModelConstants;
import org.wso2.carbon.ml.model.internal.dto.MLFeature;
import org.wso2.carbon.ml.model.internal.dto.MLWorkflow;
import org.wso2.carbon.ml.model.internal.dto.ProbabilisticClassificationModelSummary;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
import org.wso2.carbon.ml.model.internal.spark.transformations.DiscardedRows;
import org.wso2.carbon.ml.model.internal.spark.transformations.Header;
import org.wso2.carbon.ml.model.internal.spark.transformations.LineToTokens;
import org.wso2.carbon.ml.model.internal.spark.transformations.MeanImputation;
import org.wso2.carbon.ml.model.internal.spark.transformations.MissingValues;
import org.wso2.carbon.ml.model.internal.spark.transformations.TokensToLabeledPoints;
import org.wso2.carbon.ml.model.internal.spark.transformations.TokensToVectors;
import scala.Tuple2;

import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SupervisedModel {

    public void buildModel(String modelID, MLWorkflow workflow, SparkConf sparkConf)
            throws ModelServiceException {
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
            // get column separator
            String columnSeparator = MLModelUtils.getColumnSeparator(datasetURL);
            // set feature indices according to the order in the file
            for (MLFeature feature : workflow.getFeatures()) {
                feature.setIndex(MLModelUtils.getFeatureIndex(feature.getName(), headerRow,
                        columnSeparator));
            }
            Pattern pattern = Pattern.compile(columnSeparator);
            LineToTokens lineToTokens = new LineToTokens(pattern);
            JavaRDD<String[]> tokens = data.map(lineToTokens);
            // apply discard imputation
            DiscardedRows discardedRows = new DiscardedRows(MLModelUtils.getImputeFeatureIndices(
                    workflow, MLModelConstants.DISCARD));
            JavaRDD<String[]> tokensDiscardedRemoved = tokens.filter(discardedRows);
            // get feature indices for mean imputation
            List<Integer> meanImputeIndices = MLModelUtils.getImputeFeatureIndices(workflow,
                    MLModelConstants.MEAN_IMPUTATION);
            Map<Integer, Double> imputeMeans = new HashMap();
            // calculate mean and populate mean imputation hashmap
            if (meanImputeIndices.size() > 0) {
                TokensToVectors tokensToVectors = new TokensToVectors(meanImputeIndices);
                JavaRDD<Vector> sample = sc.parallelize(
                        tokens.filter(new MissingValues()).takeSample(false, 1000,
                                MLModelConstants.RANDOM_SEED)).map(tokensToVectors);
                MultivariateStatisticalSummary summary = Statistics.colStats(sample.rdd());
                double[] means = summary.mean().toArray();
                for (int i = 0; i < means.length; i++) {
                    imputeMeans.put(meanImputeIndices.get(i), means[i]);
                }
            }
            MeanImputation meanImputation = new MeanImputation(imputeMeans);
            JavaRDD<double[]> features = tokensDiscardedRemoved.map(meanImputation);
            // generate train and test datasets by converting tokens to labeled points
            int responseIndex = MLModelUtils.getFeatureIndex(workflow.getResponseVariable(),
                    headerRow, columnSeparator);
            TokensToLabeledPoints tokensToLabeledPoints = new TokensToLabeledPoints(responseIndex);
            JavaRDD<LabeledPoint> labeledPoints = features.map(tokensToLabeledPoints);
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
                    buildLogisticRegressionModel(modelID, trainingData, testingData, workflow);
                    break;
                case DECISION_TREE:
                    // empty hashmap - no categorical features
                    // needs to be populated with categorical column number:no of categories
                    Map<Integer, Integer> categoricalFeatures = new HashMap();
                    buildDecisionTreeModel(modelID, trainingData, testingData, categoricalFeatures,
                            workflow);
                    break;
                default:
                    throw new IllegalStateException();
            }
            // stop spark context
            sc.stop();
        } catch (Exception e) {
            throw new ModelServiceException(
                    "An error occurred while building supervised machine learning model: " +
                    e.getMessage(), e);
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
    private void buildLogisticRegressionModel(String modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData,
            MLWorkflow workflow) throws ModelServiceException {
        try {
            DatabaseHandler databaseHandler = new DatabaseHandler();
            databaseHandler.insertModel(modelID, workflow.getWorkflowID(),
                    new Time(System.currentTimeMillis()));
            LogisticRegression logisticRegression = new LogisticRegression();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
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
            ProbabilisticClassificationModelSummary probabilisticClassificationModelSummary =
                    logisticRegression
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
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            DecisionTree decisionTree = new DecisionTree();
            DecisionTreeModel decisionTreeModel = decisionTree.train(trainingData,
                    Integer.parseInt(hyperParameters.get(MLModelConstants.NUM_CLASSES)),
                    categoricalFeatures,
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
