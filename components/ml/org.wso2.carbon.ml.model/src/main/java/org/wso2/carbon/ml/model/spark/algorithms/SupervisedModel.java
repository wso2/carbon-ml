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

package org.wso2.carbon.ml.model.spark.algorithms;

import org.apache.commons.math3.stat.regression.ModelSpecificationException;
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
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.model.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
//import org.wso2.carbon.ml.model.internal.DatabaseHandler;
import org.wso2.carbon.ml.model.internal.MLModelUtils;
import org.wso2.carbon.ml.database.dto.Workflow;
import org.wso2.carbon.ml.model.internal.ds.MLModelServiceValueHolder;
import org.wso2.carbon.ml.model.spark.dto.ClassClassificationModelSummary;
import org.wso2.carbon.ml.model.spark.dto.ProbabilisticClassificationModelSummary;
import org.wso2.carbon.ml.model.spark.transformations.DiscardedRowsFilter;
import org.wso2.carbon.ml.model.spark.transformations.HeaderFilter;
import org.wso2.carbon.ml.model.spark.transformations.LineToTokens;
import org.wso2.carbon.ml.model.spark.transformations.MeanImputation;
import org.wso2.carbon.ml.model.spark.transformations.MissingValuesFilter;
import org.wso2.carbon.ml.model.spark.transformations.StringArrayToDoubleArray;
import org.wso2.carbon.ml.model.spark.transformations.TokensToLabeledPoints;
import org.wso2.carbon.ml.model.spark.transformations.TokensToVectors;
import scala.Tuple2;

import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.DISCARD;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.IMPURITY;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.ITERATIONS;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.LEARNING_RATE;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.MAX_BINS;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.MAX_DEPTH;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.MEAN_IMPUTATION;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.NUM_CLASSES;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.RANDOM_SEED;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.REGULARIZATION_PARAMETER;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.REGULARIZATION_TYPE;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.SGD_DATA_FRACTION;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.SUPERVISED_ALGORITHM;

public class SupervisedModel {
    /**
     * @param modelID   Model ID
     * @param workflow  Workflow ID
     * @param sparkConf Spark configuration
     * @throws ModelServiceException
     */
    public void buildModel(String modelID, Workflow workflow, SparkConf sparkConf)
            throws ModelServiceException {
        try {
            sparkConf.setAppName(modelID);
            // create a new java spark context
            JavaSparkContext sc = new JavaSparkContext(sparkConf);
            // parse lines in the dataset
            String datasetURL = workflow.getDatasetURL();
            JavaRDD<String> lines = sc.textFile(datasetURL);
            // get header line
            String headerRow = lines.take(1).get(0);
            // get column separator
            String columnSeparator = MLModelUtils.getColumnSeparator(datasetURL);
            // apply pre processing
            JavaRDD<double[]> features = preProcess(sc, workflow, lines, headerRow,
                    columnSeparator);
            // generate train and test datasets by converting tokens to labeled points
            int responseIndex = MLModelUtils.getFeatureIndex(workflow.getResponseVariable(),
                    headerRow, columnSeparator);
            TokensToLabeledPoints tokensToLabeledPoints = new TokensToLabeledPoints(responseIndex);
            JavaRDD<LabeledPoint> labeledPoints = features.map(tokensToLabeledPoints);
            JavaRDD<LabeledPoint> trainingData = labeledPoints.sample(false,
                    workflow.getTrainDataFraction(), RANDOM_SEED);
            JavaRDD<LabeledPoint> testingData = labeledPoints.subtract(trainingData);
            // build a machine learning model according to user selected algorithm
            SUPERVISED_ALGORITHM supervisedAlgorithm = SUPERVISED_ALGORITHM.valueOf(
                    workflow.getAlgorithmName());
            switch (supervisedAlgorithm) {
                case LOGISTIC_REGRESSION:
                    buildLogisticRegressionModel(modelID, trainingData, testingData, workflow);
                    break;
                case DECISION_TREE:
                    buildDecisionTreeModel(modelID, trainingData, testingData, workflow);
                    break;
                default:
                    throw new AlgorithmNameException("Incorrect algorithm name");
            }
            // stop spark context
            sc.stop();
        } catch (ModelSpecificationException e) {
            throw new ModelServiceException(
                    "An error occurred while building supervised machine learning model: " +
                    e.getMessage(), e);
        }
    }

    /**
     * @param sc              JavaSparkContext
     * @param workflow        Machine learning workflow
     * @param lines           JavaRDD of strings
     * @param headerRow       HeaderFilter row
     * @param columnSeparator Column separator
     * @return Returns a JavaRDD of doubles
     * @throws ModelServiceException
     */
    private JavaRDD<double[]> preProcess(JavaSparkContext sc, Workflow workflow, JavaRDD<String>
            lines, String headerRow, String columnSeparator) throws ModelServiceException {
        try {
            HeaderFilter headerFilter = new HeaderFilter(headerRow);
            JavaRDD<String> data = lines.filter(headerFilter);
            Pattern pattern = Pattern.compile(columnSeparator);
            LineToTokens lineToTokens = new LineToTokens(pattern);
            JavaRDD<String[]> tokens = data.map(lineToTokens);
            // get feature indices for discard imputation
            DiscardedRowsFilter discardedRowsFilter = new DiscardedRowsFilter(
                    MLModelUtils.getImputeFeatureIndices(
                            workflow, DISCARD));
            // Discard the row if any of the impute indices content have a missing or NA value
            JavaRDD<String[]> tokensDiscardedRemoved = tokens.filter(discardedRowsFilter);
            JavaRDD<double[]> features = null;
            // get feature indices for mean imputation
            List<Integer> meanImputeIndices = MLModelUtils.getImputeFeatureIndices(workflow,
                    MEAN_IMPUTATION);
            if (meanImputeIndices.size() > 0) {
                // calculate means for the whole dataset (sampleFraction = 1.0) or a sample
                Map<Integer, Double> means = getMeans(sc, tokensDiscardedRemoved, meanImputeIndices,
                        0.01);
                // Replace missing values in impute indices with the mean for that column
                MeanImputation meanImputation = new MeanImputation(means);
                features = tokensDiscardedRemoved.map(meanImputation);
            } else {
                /**
                 * Mean imputation mapper will convert string tokens to doubles as a part of the
                 * operation. If there is no mean imputation for any columns, tokens has to be
                 * converted into doubles.
                 */
                features = tokensDiscardedRemoved.map(new StringArrayToDoubleArray());
            }
            return features;
        } catch (ModelServiceException e) {
            throw new ModelServiceException("An error occured while preprocessing data: " +
                                            e.getMessage(), e);
        }
    }

    /**
     * @param sc                JavaSparkContext
     * @param tokens            JavaRDD of String[]
     * @param meanImputeIndices Indices of columns to impute
     * @param sampleFraction    Sample fraction used to calculate mean
     * @return Returns a map of impute indices and means
     * @throws ModelServiceException
     */
    private Map<Integer, Double> getMeans(JavaSparkContext sc, JavaRDD<String[]> tokens,
            List<Integer> meanImputeIndices, double sampleFraction) throws ModelServiceException {
        Map<Integer, Double> imputeMeans = new HashMap();
        JavaRDD<String[]> missingValuesRemoved = tokens.filter(new MissingValuesFilter());
        JavaRDD<Vector> features = null;
        // calculate mean and populate mean imputation hashmap
        TokensToVectors tokensToVectors = new TokensToVectors(meanImputeIndices);
        if (sampleFraction < 1.0) {
            features = tokens.sample(false, sampleFraction).map(tokensToVectors);
        } else {
            features = tokens.map(tokensToVectors);
        }
        MultivariateStatisticalSummary summary = Statistics.colStats(features.rdd());
        double[] means = summary.mean().toArray();
        for (int i = 0; i < means.length; i++) {
            imputeMeans.put(meanImputeIndices.get(i), means[i]);
        }
        return imputeMeans;
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
            Workflow workflow) throws ModelServiceException {
        try {
            //DatabaseHandler databaseHandler = new DatabaseHandler();
            DatabaseService dbService =  MLModelServiceValueHolder.getDatabaseService(); //TODO: Upul
            dbService.insertModel(modelID, workflow.getWorkflowID(),
                    new Time(System.currentTimeMillis()));
            LogisticRegression logisticRegression = new LogisticRegression();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            LogisticRegressionModel model = logisticRegression.trainWithSGD(trainingData,
                    Double.parseDouble(hyperParameters.get(LEARNING_RATE)),
                    Integer.parseInt(hyperParameters.get(ITERATIONS)),
                    hyperParameters.get(REGULARIZATION_TYPE),
                    Double.parseDouble(hyperParameters.get(REGULARIZATION_PARAMETER)),
                    Double.parseDouble(hyperParameters.get(SGD_DATA_FRACTION)));
            model.clearThreshold();
            JavaRDD<Tuple2<Object, Object>> scoresAndLabels = logisticRegression.test(model,
                    testingData);
            ProbabilisticClassificationModelSummary probabilisticClassificationModelSummary =
                    logisticRegression.getModelSummary(scoresAndLabels);
            dbService.updateModel(modelID, model, probabilisticClassificationModelSummary,
                    new Time(System.currentTimeMillis()));
        } catch (DatabaseHandlerException e) {
            throw new ModelServiceException("An error occured while building logistic regression " +
                                            "model: " + e.getMessage(), e);
        }
    }

    /**
     * This method builds a decision tree model
     *
     * @param trainingData Training data
     * @param testingData  Testing data
     * @param workflow     Machine learning workflow
     * @throws ModelServiceException
     */
    private void buildDecisionTreeModel(String modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow) throws ModelServiceException {
        try {
            //DatabaseHandler databaseHandler = new DatabaseHandler();
            DatabaseService dbService =  MLModelServiceValueHolder.getDatabaseService(); //TODO: Upul
            dbService.insertModel(modelID, workflow.getWorkflowID(),
                    new Time(System.currentTimeMillis()));
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            DecisionTree decisionTree = new DecisionTree();
            DecisionTreeModel decisionTreeModel = decisionTree.train(trainingData,
                    Integer.parseInt(hyperParameters.get(NUM_CLASSES)),
                    new HashMap<Integer, Integer>(), hyperParameters.get(IMPURITY),
                    Integer.parseInt(hyperParameters.get(MAX_DEPTH)),
                    Integer.parseInt(hyperParameters.get(MAX_BINS)));
            JavaPairRDD<Double, Double> predictionsAnsLabels = decisionTree.test(decisionTreeModel,
                    trainingData);
            ClassClassificationModelSummary classClassificationModelSummary = decisionTree
                    .getClassClassificationModelSummary(predictionsAnsLabels);
            dbService.updateModel(modelID, decisionTreeModel, classClassificationModelSummary,
                    new Time(System.currentTimeMillis()));
        } catch (DatabaseHandlerException e) {
            throw new ModelServiceException("An error occured while building decision tree model: "
                                            + e.getMessage(), e);
        }

    }
}
