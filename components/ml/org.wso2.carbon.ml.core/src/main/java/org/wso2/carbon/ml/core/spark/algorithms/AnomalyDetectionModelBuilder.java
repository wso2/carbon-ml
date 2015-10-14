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
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.*;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.interfaces.MLModelBuilder;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.MulticlassConfusionMatrix;
import org.wso2.carbon.ml.core.spark.models.MLAnomalyDetectionModel;
import org.wso2.carbon.ml.core.spark.summary.KMeansAnomalyDetectionSummary;
import org.wso2.carbon.ml.core.spark.transformations.*;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.database.DatabaseService;

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
        BasicEncoder basicEncoder = new BasicEncoder.Builder().init(context).build();
        MeanImputation meanImputation = new MeanImputation.Builder().init(context).build();
        StringArrayToDoubleArray stringArrayToDoubleArray = new StringArrayToDoubleArray.Builder().build();
        DoubleArrayToVector doubleArrayToVector = new DoubleArrayToVector.Builder().build();
        RemoveResponseColumn removeResponseColumn = new RemoveResponseColumn();

        JavaRDD<String> lines = context.getLines().cache();

        JavaRDD<String[]> tokens = lines.filter(headerFilter).map(lineToTokens).filter(discardedRowsFilter);

        if (dataType != null) {
            switch (dataType) {
            case NORMAL:
                NormalRowsFilter normalRowsFilter = new NormalRowsFilter.Builder().init(context).build();
                tokens = tokens.filter(normalRowsFilter);
                break;
            case ANOMALOUS:
                AnomalyRowsFilter anomalyRowsFilter = new AnomalyRowsFilter.Builder().init(context).build();
                tokens = tokens.filter(anomalyRowsFilter);
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
            mlModel.setNewNormalLabel(workflow.getNewNormalLabel());
            mlModel.setNewAnomalyLabel(workflow.getNewAnomalyLabel());
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
                summaryModel = buildKMeansUnlabeledDataModel(modelId, data, workflow, mlModel, includedFeatures);
                break;

            case K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA:
                mlModel.setResponseIndex(context.getResponseIndex());
                // gets the pre-processed dataset for labeled data
                anomaly_detection_data_type = MLConstants.ANOMALY_DETECTION_DATA_TYPE.NORMAL;
                JavaRDD<Vector> normalData = preProcess(anomaly_detection_algorithm, anomaly_detection_data_type).cache();
                JavaRDD<Vector> normalTrainData = normalData.sample(false, workflow.getTrainDataFraction(),
                        MLConstants.RANDOM_SEED).cache();
                JavaRDD<Vector> normalTestData = normalData.subtract(normalTrainData).cache();
                // remove from cache
                normalData.unpersist();

                anomaly_detection_data_type = MLConstants.ANOMALY_DETECTION_DATA_TYPE.ANOMALOUS;
                JavaRDD<Vector> anomalyData = preProcess(anomaly_detection_algorithm, anomaly_detection_data_type).cache();
                double testDataFraction = (1 - workflow.getTrainDataFraction());
                JavaRDD<Vector> anomalyTestData = anomalyData.sample(false, testDataFraction,
                        MLConstants.RANDOM_SEED).cache();
                // remove from cache
                anomalyData.unpersist();

                summaryModel = buildKMeansLabeledDataModel(modelId, normalTrainData, normalTestData,
                        anomalyTestData, workflow, mlModel, includedFeatures);
                break;

            default:
                throw new AlgorithmNameException("Incorrect algorithm name: " + workflow.getAlgorithmName()
                        + " for model id: " + modelId);
            }
            // persist model summary
            databaseService.updateModelSummary(modelId, summaryModel);
            return mlModel;

        } catch (Exception e) {
            throw new MLModelBuilderException(
                    "An error occurred while building anomaly detection machine learning model: " + e.getMessage(), e);
        }
    }

    /**
     * This method builds a k-means model.
     *
     * @param modelID Model ID
     * @param data Training data as a JavaRDD of Vectors
     * @param workflow Machine learning workflow
     * @param mlModel Deployable machine learning model
     * @param includedFeatures included features Map
     * @throws MLModelBuilderException
     * @return ModelSummary containing the KMeansAnomalyDetectionSummary
     */
    private ModelSummary buildKMeansUnlabeledDataModel(long modelID, JavaRDD<Vector> data, Workflow workflow,
            MLModel mlModel, SortedMap<Integer, String> includedFeatures) throws MLModelBuilderException {

        try {
            Map<String, String> hyperParameters = workflow.getHyperParameters();

            // creating the AnomalyDetection object
            AnomalyDetection anomalyDetection = new AnomalyDetection();
            // building the kmeans model
            KMeansModel kMeansModel = anomalyDetection.train(data,
                    Integer.parseInt(hyperParameters.get(MLConstants.NUM_OF_NORMAL_CLUSTERS)),
                    Integer.parseInt(hyperParameters.get(MLConstants.MAX_ITERATIONS)));

            // get the cluster indexes of each data points of the train dataset
            JavaRDD<Integer> predictedClusters = anomalyDetection.test(kMeansModel, data);
            // get the cluster centers array from the model
            Vector[] clusterCenters = anomalyDetection.getClusterCenters(kMeansModel);
            // get the distance Map of training data
            Map<Integer, List<Double>> distancesMap = anomalyDetection.getDistancesToDataPoints(
                    predictedClusters, clusterCenters, data);

            // remove from cache
            data.unpersist();

            // creating the model summary object
            KMeansAnomalyDetectionSummary kMeansAnomalyDetectionSummary = new KMeansAnomalyDetectionSummary();
            // creating the model object
            MLAnomalyDetectionModel MLAnomalyDetectionModel = new MLAnomalyDetectionModel(kMeansModel);
            MLAnomalyDetectionModel.setDistancesMap(distancesMap);

            mlModel.setModel(MLAnomalyDetectionModel);

            kMeansAnomalyDetectionSummary
                    .setAlgorithm(MLConstants.ANOMALY_DETECTION_ALGORITHM.K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA
                            .toString());
            kMeansAnomalyDetectionSummary.setDatasetVersion(workflow.getDatasetVersion());
            kMeansAnomalyDetectionSummary.setFeatures(includedFeatures.values().toArray(new String[0]));

            return kMeansAnomalyDetectionSummary;

        } catch (Exception e) {
            throw new MLModelBuilderException(
                    "An error occurred while building k-means anomaly detection with unlabeled data model: "
                            + e.getMessage(), e);
        }
    }

    /**
     * This method builds a k-means model.
     *
     * @param modelID Model ID
     * @param trainData Training data as a JavaRDD of Vectors
     * @param normalTestData Normal test data as a JavaRDD of Vectors
     * @param anomalyTestData Anomaly test data as a JavaRDD of Vectors
     * @param workflow Machine learning workflow
     * @param mlModel Deployable machine learning model
     * @param includedFeatures included features Map
     * @throws MLModelBuilderException
     * @return ModelSummary containing the KMeansAnomalyDetectionSummary
     */
    private ModelSummary buildKMeansLabeledDataModel(long modelID, JavaRDD<Vector> trainData,
            JavaRDD<Vector> normalTestData, JavaRDD<Vector> anomalyTestData, Workflow workflow, MLModel mlModel,
            SortedMap<Integer, String> includedFeatures) throws MLModelBuilderException {

        try {
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            String newNormalLabel = workflow.getNewNormalLabel();
            String newAnomalyLabel = workflow.getNewAnomalyLabel();

            // creating the anomalyDetection object
            AnomalyDetection anomalyDetection = new AnomalyDetection();
            // building the kmeans model
            KMeansModel kMeansModel = anomalyDetection.train(trainData,
                    Integer.parseInt(hyperParameters.get(MLConstants.NUM_OF_NORMAL_CLUSTERS)),
                    Integer.parseInt(hyperParameters.get(MLConstants.MAX_ITERATIONS)));

            // get the cluster indexes of each data points of the train dataset
            JavaRDD<Integer> predictedClustersOfTrainData = anomalyDetection.test(kMeansModel, trainData);
            // get the cluster centers array from the model
            Vector[] clusterCenters = anomalyDetection.getClusterCenters(kMeansModel);

            // get the distance Map of training data
            Map<Integer, List<Double>> distancesMapOfTrainData = anomalyDetection.getDistancesToDataPoints(
                    predictedClustersOfTrainData, clusterCenters, trainData);

            // remove from cache
            trainData.unpersist();

            // creating the model summary object
            KMeansAnomalyDetectionSummary kMeansAnomalyDetectionSummary = new KMeansAnomalyDetectionSummary();
            // creating the model object
            MLAnomalyDetectionModel MLAnomalyDetectionModel = new MLAnomalyDetectionModel(kMeansModel);
            MLAnomalyDetectionModel.setDistancesMap(distancesMapOfTrainData);

            // evaluating the model using test data
            // get the cluster indexes of each data points of the normal test dataset
            JavaRDD<Integer> predictedClustesOfNormalTestData = anomalyDetection.test(kMeansModel, normalTestData);
            // get the distance Map of normal test data
            Map<Integer, List<Double>> distanceMapOfNormalTestData = anomalyDetection.getDistancesToDataPoints(
                    predictedClustesOfNormalTestData, clusterCenters, normalTestData);
            // remove from cache
            normalTestData.unpersist();

            // get the cluster indexes of each data points of the anomaly test dataset
            JavaRDD<Integer> predictedClustesOfAnomalyTestData = anomalyDetection.test(kMeansModel,
                    anomalyTestData);
            // get the distance Map of anomaly test data
            Map<Integer, List<Double>> distanceMapOfAnomalyTestData = anomalyDetection
                    .getDistancesToDataPoints(predictedClustesOfAnomalyTestData, clusterCenters, anomalyTestData);
            // remove from cache
            anomalyTestData.unpersist();

            Map<Integer, MulticlassConfusionMatrix> multiclassConfusionMatrixMap = new HashMap<Integer, MulticlassConfusionMatrix>();
            int maxRange = 100;
            int minRange = 80;
            double maxF1Score = 0;
            int bestPercentile = minRange;

            // calculating the evaluation results for each percentile of defined range
            for (int percentileValue = minRange; percentileValue <= maxRange; percentileValue++) {

                // get the percentile Map of each cluster
                Map<Integer, Double> percentilesMap = anomalyDetection.getPercentileDistances(
                        distancesMapOfTrainData, percentileValue);

                // get the multiclassconfusionmatrix of test data
                MulticlassConfusionMatrix evaluationResults = getEvaluationResults(
                        distanceMapOfNormalTestData, distanceMapOfAnomalyTestData, percentilesMap, newNormalLabel,
                        newAnomalyLabel);

                // finding the best percentile value based on the F1 score
                if (evaluationResults.getF1Score() > maxF1Score) {
                    maxF1Score = evaluationResults.getF1Score();
                    bestPercentile = percentileValue;
                }

                // storing the evaluation results mapped with their respective percentile values
                multiclassConfusionMatrixMap.put(percentileValue, evaluationResults);

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
            List<Tuple2<Integer, Vector>> kMeansPredictions = anomalyDetection
                    .test(kMeansModel, sampleData).zip(sampleData).collect();
            List<ClusterPoint> clusterPoints = new ArrayList<ClusterPoint>();
            List<Feature> featuresList = mlModel.getFeatures();

            for (Tuple2<Integer, org.apache.spark.mllib.linalg.Vector> kMeansPrediction : kMeansPredictions) {

                ClusterPoint clusterPoint = new ClusterPoint();
                clusterPoint.setCluster(kMeansPrediction._1());
                Map<String, Double> featureMap = new HashMap<String, Double>();

                for (int i = 0; i < featuresList.size(); i++) {
                    String featureName = featuresList.get(i).getName();
                    double point = (kMeansPrediction._2().toArray())[i];
                    featureMap.put(featureName, point);
                }
                clusterPoint.setFeatureMap(featureMap);
                clusterPoints.add(clusterPoint);
            }


            MLAnomalyDetectionModel.setBestPercentile(bestPercentile);
            mlModel.setModel(MLAnomalyDetectionModel);

            kMeansAnomalyDetectionSummary
                    .setAlgorithm(MLConstants.ANOMALY_DETECTION_ALGORITHM.K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA
                            .toString());
            kMeansAnomalyDetectionSummary.setPercentileToMulticlassConfusionMatrixMap(multiclassConfusionMatrixMap);
            kMeansAnomalyDetectionSummary.setClusterPoints(clusterPoints);
            kMeansAnomalyDetectionSummary.setBestPercentile(bestPercentile);
            kMeansAnomalyDetectionSummary.setDatasetVersion(workflow.getDatasetVersion());
            kMeansAnomalyDetectionSummary.setFeatures(includedFeatures.values().toArray(new String[0]));

            return kMeansAnomalyDetectionSummary;

        } catch (Exception e) {
            throw new MLModelBuilderException(
                    "An error occurred while building k-means anomaly detection with labeled data model: "
                            + e.getMessage(), e);
        }
    }

    /**
     * This method returns the evaluation results
     *
     * @param normalTestDataDistanceMap distance Map of normal test data points
     * @param anomalyTestDataDistanceMap distance Map of anomaly test data points
     * @param percentilesMap percentile values Map
     * @param newNormalLabel new normal label value
     * @param newAnomalyLabel new anomaly label value
     * @return MulticlassConfusionMatrix containing multiclassconfusionmatrix of test results
     */
    public MulticlassConfusionMatrix getEvaluationResults(Map<Integer, List<Double>> normalTestDataDistanceMap,
                                                          Map<Integer, List<Double>> anomalyTestDataDistanceMap, Map<Integer, Double> percentilesMap,
                                                          String newNormalLabel, String newAnomalyLabel) {

        MulticlassConfusionMatrix multiclassConfusionMatrix = new MulticlassConfusionMatrix();
        double truePositive = 0;
        double trueNegetive = 0;
        double falsePositive = 0;
        double falseNegetive = 0;

        // evaluating testNormal and testAnomaly data
        for (int clusterIndex = 0; clusterIndex < percentilesMap.size(); clusterIndex++) {

            for (double distance : normalTestDataDistanceMap.get(clusterIndex)) {

                if (distance > percentilesMap.get(clusterIndex)) {
                    falsePositive++;
                } else {
                    trueNegetive++;
                }
            }

            for (double distance : anomalyTestDataDistanceMap.get(clusterIndex)) {

                if (distance > percentilesMap.get(clusterIndex)) {
                    truePositive++;
                } else {
                    falseNegetive++;
                }
            }
        }

        double[][] matrix = new double[2][2];
        matrix[0][0] = truePositive;
        matrix[0][1] = falseNegetive;
        matrix[1][0] = falsePositive;
        matrix[1][1] = trueNegetive;
        multiclassConfusionMatrix.setMatrix(matrix);

        List<String> labels = new ArrayList<String>();
        labels.add(0, newNormalLabel);
        labels.add(1, newAnomalyLabel);
        multiclassConfusionMatrix.setLabels(labels);
        multiclassConfusionMatrix.setSize(2);
        multiclassConfusionMatrix.setAccuracyMeasures();

        return multiclassConfusionMatrix;

    }

}
