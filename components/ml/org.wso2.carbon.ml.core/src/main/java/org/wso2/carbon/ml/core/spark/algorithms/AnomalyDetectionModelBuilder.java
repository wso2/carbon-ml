/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.*;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vector;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.*;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.interfaces.MLModelBuilder;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.MulticlassConfusionMatrix;
import org.wso2.carbon.ml.core.spark.MulticlassMetrics;
import org.wso2.carbon.ml.core.spark.models.MLAnomalyDetectionModel;
import org.wso2.carbon.ml.core.spark.models.ext.AnomalyDetectionModel;
import org.wso2.carbon.ml.core.spark.summary.AnomalyDetectionModelSummary;
import org.wso2.carbon.ml.core.spark.transformations.*;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

import scala.Tuple2;

/**
 * Building K means Anomaly Detection model supported by Spark.
 */
public class AnomalyDetectionModelBuilder extends MLModelBuilder {

    public AnomalyDetectionModelBuilder(MLModelConfigurationContext context) {
        super(context);
    }

    private JavaRDD<Vector> preProcess(MLConstants.ANOMALY_DETECTION_ALGORITHM algorithm,
            MLConstants.ANOMALY_DETECTION_DATA_TYPE dataType) throws MLModelBuilderException {

        MLModelConfigurationContext context = getContext();
        Workflow workflow = context.getFacts();

        HeaderFilter headerFilter = new HeaderFilter.Builder().init(context).build();
        LineToTokens lineToTokens = new LineToTokens.Builder().init(context).build();
        DiscardedRowsFilter discardedRowsFilter = new DiscardedRowsFilter.Builder().init(context).build();
        RemoveDiscardedFeatures removeDiscardedFeatures = new RemoveDiscardedFeatures.Builder().init(context).build();
        MeanImputation meanImputation = new MeanImputation.Builder().init(context).build();
        StringArrayToDoubleArray stringArrayToDoubleArray = new StringArrayToDoubleArray.Builder().build();
        DoubleArrayToVector doubleArrayToVector = new DoubleArrayToVector.Builder().build();
        RemoveResponseColumn removeResponseColumn = new RemoveResponseColumn();

        JavaRDD<String> lines = context.getLines().cache();

        JavaRDD<String[]> tokens = lines.filter(headerFilter).map(lineToTokens).filter(discardedRowsFilter);

        if (dataType != null) {
            switch (dataType) {
            case NORMAL:
                AnomalyRowsFilter anomalyRowsFilter = new AnomalyRowsFilter.Builder().init(context).build();
                tokens = tokens.filter(anomalyRowsFilter);
                break;
            case ANOMALOUS:
                NormalRowsFilter normalRowsFilter = new NormalRowsFilter.Builder().init(context).build();
                tokens = tokens.filter(normalRowsFilter);
                break;
            default:
                throw new AlgorithmNameException("Incorrect data type: " + workflow.getAlgorithmName());
            }
        }

        JavaRDD<String[]> stringArray = tokens.map(removeDiscardedFeatures);

        if (algorithm == MLConstants.ANOMALY_DETECTION_ALGORITHM.K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA) {
            stringArray = stringArray.map(removeResponseColumn);
        }

        JavaRDD<double[]> doubleArray = stringArray.map(meanImputation).map(stringArrayToDoubleArray);

        if (workflow.getNormalization()) {
            Normalization normalization = new Normalization.Builder().init(context).build();
            doubleArray.map(normalization);
        }

        JavaRDD<Vector> vectors = doubleArray.map(doubleArrayToVector);

        return vectors;
    }

    /**
     * Build an KMeans Anomaly Detection model.
     */
    public MLModel build() throws MLModelBuilderException {
        MLModelConfigurationContext context = getContext();
        DatabaseService databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
        try {
            Workflow workflow = context.getFacts();
            long modelId = context.getModelId();
            ModelSummary summaryModel;

            // create a deployable MLModel object
            MLModel mlModel = new MLModel();
            mlModel.setNormalization(workflow.getNormalization());
            mlModel.setNormalLabels(workflow.getNormalLabels());
            mlModel.setAlgorithmName(workflow.getAlgorithmName());
            mlModel.setAlgorithmClass(workflow.getAlgorithmClass());
            mlModel.setFeatures(workflow.getIncludedFeatures());
            mlModel.setResponseVariable(workflow.getResponseVariable());
            mlModel.setEncodings(context.getEncodings());
            mlModel.setNewToOldIndicesList(context.getNewToOldIndicesList());
            mlModel.setSummaryStatsOfFeatures(context.getSummaryStatsOfFeatures());

            SortedMap<Integer, String> includedFeatures = MLUtils.getIncludedFeaturesAfterReordering(workflow,
                    context.getNewToOldIndicesList(), context.getResponseIndex());
            // build a machine learning model according to user selected algorithm
            MLConstants.ANOMALY_DETECTION_ALGORITHM anomaly_detection_algorithm = MLConstants.ANOMALY_DETECTION_ALGORITHM
                    .valueOf(workflow.getAlgorithmName());

            MLConstants.ANOMALY_DETECTION_DATA_TYPE anomaly_detection_data_type;

            switch (anomaly_detection_algorithm) {
            case K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA:
                mlModel.setResponseIndex(-1);
                // gets the pre-processed dataset for unlabeled data
                JavaRDD<Vector> data = preProcess(anomaly_detection_algorithm, null).cache();

                summaryModel = buildUnlabeledDataAnomalyDetectionModel(modelId, data, workflow, mlModel,
                        includedFeatures);
                break;

            case K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA:
                mlModel.setResponseIndex(context.getResponseIndex());
                // gets the pre-processed dataset for labeled data
                anomaly_detection_data_type = MLConstants.ANOMALY_DETECTION_DATA_TYPE.NORMAL;
                JavaRDD<Vector> normalData = preProcess(anomaly_detection_algorithm, anomaly_detection_data_type)
                        .cache();
                JavaRDD<Vector> normalTrainData = normalData
                        .sample(false, workflow.getTrainDataFraction(), MLConstants.RANDOM_SEED).cache();
                JavaRDD<Vector> normalTestData = normalData.subtract(normalTrainData).cache();
                // remove from cache
                normalData.unpersist();

                anomaly_detection_data_type = MLConstants.ANOMALY_DETECTION_DATA_TYPE.ANOMALOUS;
                JavaRDD<Vector> anomalyData = preProcess(anomaly_detection_algorithm, anomaly_detection_data_type)
                        .cache();
                double testDataFraction = (1 - workflow.getTrainDataFraction());
                JavaRDD<Vector> anomalyTestData = anomalyData.sample(false, testDataFraction, MLConstants.RANDOM_SEED)
                        .cache();
                // remove from cache
                anomalyData.unpersist();

                summaryModel = buildLabeledDataAnomalyDetectionModel(modelId, normalTrainData, normalTestData,
                        anomalyTestData, workflow, mlModel, includedFeatures);
                break;

            default:
                throw new AlgorithmNameException(
                        "Incorrect algorithm name: " + workflow.getAlgorithmName() + " for model id: " + modelId);
            }
            // persist model summary
            databaseService.updateModelSummary(modelId, summaryModel);
            return mlModel;

        } catch (DatabaseHandlerException e) {
            throw new MLModelBuilderException(
                    "An error occurred while building anomaly detection machine learning model: " + e.getMessage(), e);
        }
    }

    /**
     * This method builds a Anomaly detection model.
     *
     * @param modelID Model ID
     * @param data Training data as a JavaRDD of Vectors
     * @param workflow Machine learning workflow
     * @param mlModel Deployable machine learning model
     * @param includedFeatures included features Map
     * @throws MLModelBuilderException
     * @return ModelSummary containing the AnomalyDetectionModelSummary
     */
    private ModelSummary buildUnlabeledDataAnomalyDetectionModel(long modelID, JavaRDD<Vector> data, Workflow workflow,
            MLModel mlModel, SortedMap<Integer, String> includedFeatures) throws MLModelBuilderException {

        try {

            Map<String, String> hyperParameters = workflow.getHyperParameters();
            String newNormalLabel = workflow.getNewNormalLabel();
            String newAnomalyLabel = workflow.getNewAnomalyLabel();

            // creating the AnomalyDetection object
            AnomalyDetection anomalyDetection = new AnomalyDetection();
            // building the kmeans model
            AnomalyDetectionModel anomalyDetectionModel = anomalyDetection.train(data,
                    Integer.parseInt(hyperParameters.get(MLConstants.NUM_OF_NORMAL_CLUSTERS)),
                    Integer.parseInt(hyperParameters.get(MLConstants.MAX_ITERATIONS)), newNormalLabel, newAnomalyLabel);

            // remove from cache
            data.unpersist();

            // creating the model summary object
            AnomalyDetectionModelSummary anomalyDetectionModelSummary = new AnomalyDetectionModelSummary();
            // creating the model object
            MLAnomalyDetectionModel MLAnomalyDetectionModel = new MLAnomalyDetectionModel(anomalyDetectionModel);

            mlModel.setModel(MLAnomalyDetectionModel);

            anomalyDetectionModelSummary.setAlgorithm(
                    MLConstants.ANOMALY_DETECTION_ALGORITHM.K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA.toString());
            anomalyDetectionModelSummary.setDatasetVersion(workflow.getDatasetVersion());
            anomalyDetectionModelSummary.setFeatures(includedFeatures.values().toArray(new String[0]));

            return anomalyDetectionModelSummary;

        } catch (Exception e) {
            throw new MLModelBuilderException(
                    "An error occurred while building k-means anomaly detection with unlabeled data model: "
                            + e.getMessage(),
                    e);
        }
    }

    /**
     * This method builds a Anomaly detection model.
     *
     * @param modelID Model ID
     * @param trainData Training data as a JavaRDD of Vectors
     * @param normalTestData Normal test data as a JavaRDD of Vectors
     * @param anomalyTestData Anomaly test data as a JavaRDD of Vectors
     * @param workflow Machine learning workflow
     * @param mlModel Deployable machine learning model
     * @param includedFeatures included features Map
     * @throws MLModelBuilderException
     * @return ModelSummary containing the AnomalyDetectionModelSummary
     */
    private ModelSummary buildLabeledDataAnomalyDetectionModel(long modelID, JavaRDD<Vector> trainData,
            JavaRDD<Vector> normalTestData, JavaRDD<Vector> anomalyTestData, Workflow workflow, MLModel mlModel,
            SortedMap<Integer, String> includedFeatures) throws MLModelBuilderException {

        try {

            Map<String, String> hyperParameters = workflow.getHyperParameters();
            String newNormalLabel = workflow.getNewNormalLabel();
            String newAnomalyLabel = workflow.getNewAnomalyLabel();

            // creating the anomalyDetection object
            AnomalyDetection anomalyDetection = new AnomalyDetection();
            // building the kmeans model
            AnomalyDetectionModel anomalyDetectionModel = anomalyDetection.train(trainData,
                    Integer.parseInt(hyperParameters.get(MLConstants.NUM_OF_NORMAL_CLUSTERS)),
                    Integer.parseInt(hyperParameters.get(MLConstants.MAX_ITERATIONS)), newNormalLabel, newAnomalyLabel);

            // remove from cache
            trainData.unpersist();

            // creating the model summary object
            AnomalyDetectionModelSummary anomalyDetectionModelSummary = new AnomalyDetectionModelSummary();
            // creating the model object
            MLAnomalyDetectionModel MLAnomalyDetectionModel = new MLAnomalyDetectionModel(anomalyDetectionModel);
            mlModel.setModel(MLAnomalyDetectionModel);

            // evaluating the model using test data
            // calculating the evaluation results for each percentile of defined range
            int maxRange = System.getProperty(MLConstants.MAX_PERCENTILE_CONF) == null ? MLConstants.MAX_PERCENTILE
                    : Integer.parseInt(System.getProperty(MLConstants.MAX_PERCENTILE_CONF));
            int minRange = System.getProperty(MLConstants.MIN_PERCENTILE_CONF) == null ? MLConstants.MIN_PERCENTILE
                    : Integer.parseInt(System.getProperty(MLConstants.MIN_PERCENTILE_CONF));

            Map<Integer, MulticlassMetrics> percentileToMulticlassMetricsMap = getEvaluationResults(
                    anomalyDetectionModel, normalTestData, anomalyTestData, minRange, maxRange, newNormalLabel,
                    newAnomalyLabel);

            normalTestData.unpersist();
            anomalyTestData.unpersist();

            // finding the best percentile value based on the F1 score
            double maxF1Score = 0;
            int bestPercentile = minRange;

            for (int percentile = minRange; percentile <= maxRange; percentile++) {

                MulticlassMetrics multiclassMetrics = percentileToMulticlassMetricsMap.get(percentile);
                if (multiclassMetrics.getF1Score() > maxF1Score) {
                    maxF1Score = multiclassMetrics.getF1Score();
                    bestPercentile = percentile;
                }
            }

            // generating data for summary clusters
            double sampleSize = (double) MLCoreServiceValueHolder.getInstance().getSummaryStatSettings()
                    .getSampleSize();
            double sampleFraction = sampleSize / (trainData.count() - 1);
            JavaRDD<Vector> sampleData = null;
            if (sampleFraction >= 1.0) {
                sampleData = trainData;
            }
            // Use ramdomly selected sample fraction of rows if number of records is > sample fraction
            else {
                sampleData = trainData.sample(false, sampleFraction);
            }

            // Populate cluster points list with predicted clusters and features
            List<Tuple2<Integer, Vector>> kMeansPredictions = anomalyDetectionModel.getkMeansModel().predict(sampleData)
                    .zip(sampleData).collect();
            List<ClusterPoint> clusterPoints = new ArrayList<ClusterPoint>();

            for (Tuple2<Integer, org.apache.spark.mllib.linalg.Vector> kMeansPrediction : kMeansPredictions) {

                ClusterPoint clusterPoint = new ClusterPoint();
                clusterPoint.setCluster(kMeansPrediction._1());

                double[] features = new double[includedFeatures.size()];

                for (int i = 0; i < includedFeatures.size(); i++) {
                    double point = (kMeansPrediction._2().toArray())[i];
                    features[i] = point;
                }
                clusterPoint.setFeatures(features);
                clusterPoints.add(clusterPoint);
            }

            anomalyDetectionModelSummary.setAlgorithm(
                    MLConstants.ANOMALY_DETECTION_ALGORITHM.K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA.toString());
            anomalyDetectionModelSummary.setPercentileToMulticlassMetricsMap(percentileToMulticlassMetricsMap);
            anomalyDetectionModelSummary.setClusterPoints(clusterPoints);
            anomalyDetectionModelSummary.setMinPercentile(minRange);
            anomalyDetectionModelSummary.setMaxPercentile(maxRange);
            anomalyDetectionModelSummary.setBestPercentile(bestPercentile);
            anomalyDetectionModelSummary.setDatasetVersion(workflow.getDatasetVersion());
            anomalyDetectionModelSummary.setFeatures(includedFeatures.values().toArray(new String[0]));

            return anomalyDetectionModelSummary;

        } catch (Exception e) {
            throw new MLModelBuilderException(
                    "An error occurred while building k-means anomaly detection with labeled data model: "
                            + e.getMessage(),
                    e);
        }
    }

    /**
     * This method is to evaluate the anomaly detection model
     *
     * @param anomalyDetectionModel anomaly detection model
     * @param normalTestData JavaRDD of normal test data
     * @param anomalyTestData JavaRDD of anomalous test data
     * @param minPercentile min percentile of the range
     * @param maxPercentile max percentile of the range
     * @param newNormalLabel normal label
     * @param newAnomalyLabel anomaly label
     * @return Map<Integer, MulticlassMetrics> key:percentile value value:MulticlassMetrics
     */
    public Map<Integer, MulticlassMetrics> getEvaluationResults(AnomalyDetectionModel anomalyDetectionModel,
            JavaRDD<Vector> normalTestData, JavaRDD<Vector> anomalyTestData, int minPercentile, int maxPercentile,
            String newNormalLabel, String newAnomalyLabel) {

        /*
         * key : percentile value
         * value : MulticlassMetrics
         */
        Map<Integer, MulticlassMetrics> multiclassMetricsMap = new HashMap<Integer, MulticlassMetrics>();

        /*
         * key : percentile value
         * value : predictions List for normal test data
         */
        Map<Integer, List<String>> percentileToNormalTestDataPredictionsListMap = anomalyDetectionModel
                .predict(normalTestData, minPercentile, maxPercentile);
        /*
         * key : percentile value
         * value : predictions List for anomaly test data
         */
        Map<Integer, List<String>> percentileToAomalyTestDataPredictionsListMap = anomalyDetectionModel
                .predict(anomalyTestData, minPercentile, maxPercentile);

        // evaluating test data for a range of percentile distances
        for (int percentile = minPercentile; percentile <= maxPercentile; percentile++) {

            double truePositive = 0;
            double trueNegative = 0;
            double falsePositive = 0;
            double falseNegative = 0;

            List<String> normalTestDataPredictions = percentileToNormalTestDataPredictionsListMap.get(percentile);

            for (String normalDataPrediction : normalTestDataPredictions) {
                if (normalDataPrediction.equals(newNormalLabel)) {
                    truePositive++;
                } else {
                    falseNegative++;
                }
            }

            List<String> anomalyTestDataPredictions = percentileToAomalyTestDataPredictionsListMap.get(percentile);

            for (String anomalyDataPrediction : anomalyTestDataPredictions) {
                if (anomalyDataPrediction.equals(newAnomalyLabel)) {
                    trueNegative++;
                } else {
                    falsePositive++;
                }
            }

            double[][] matrix = new double[2][2];
            matrix[0][0] = truePositive;
            matrix[0][1] = falseNegative;
            matrix[1][0] = falsePositive;
            matrix[1][1] = trueNegative;

            MulticlassConfusionMatrix multiclassConfusionMatrix = new MulticlassConfusionMatrix();
            multiclassConfusionMatrix.setMatrix(matrix);
            List<String> labels = new ArrayList<String>();
            labels.add(0, newAnomalyLabel);
            labels.add(1, newNormalLabel);
            multiclassConfusionMatrix.setLabels(labels);
            multiclassConfusionMatrix.setSize(2);

            MulticlassMetrics multiclassMetrics = new MulticlassMetrics(multiclassConfusionMatrix);
            multiclassMetricsMap.put(percentile, multiclassMetrics);

        }

        return multiclassMetricsMap;
    }

}
