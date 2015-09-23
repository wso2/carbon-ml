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

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.Feature;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.interfaces.MLModelBuilder;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.MulticlassConfusionMatrix;
import org.wso2.carbon.ml.core.spark.models.MLKMeansAnomalyDetectionModel;
import org.wso2.carbon.ml.core.spark.summary.KMeansAnomalyDetectionSummary;
import org.wso2.carbon.ml.core.spark.transformations.*;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.database.DatabaseService;

/**
 * Building K means Anomaly Detection model supported by Spark.
 */
public class KmeansAnomalyDetectionModelBuilder extends MLModelBuilder {

    public KmeansAnomalyDetectionModelBuilder(MLModelConfigurationContext context) {
        super(context);
    }

    private JavaRDD<Vector> preProcess() throws MLModelBuilderException {
        MLModelConfigurationContext context = getContext();
        Workflow workflow = context.getFacts();
        // MLModel mlModel = new MLModel();
        // mlModel.setNormalization(workflow.getNormalization());
        // mlModel.setNormalLabels(workflow.getNormalLabels());

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

        if (workflow.getNormalization().equals("true")) {

            Normalization normalization = new Normalization.Builder().init(context).build();
            return lines.filter(headerFilter).map(lineToTokens).filter(discardedRowsFilter)
                    .map(removeDiscardedFeatures).map(removeResponseColumn).map(basicEncoder).map(meanImputation).map(stringArrayToDoubleArray)
                    .map(normalization).map(doubleArrayToVector);

        } else {
            return lines.filter(headerFilter).map(lineToTokens).filter(discardedRowsFilter)
                    .map(removeDiscardedFeatures).map(removeResponseColumn).map(basicEncoder).map(meanImputation).map(stringArrayToDoubleArray)
                    .map(doubleArrayToVector);
        }

    }

    private JavaRDD<Vector> preProcessNormal() throws MLModelBuilderException {
        MLModelConfigurationContext context = getContext();
        Workflow workflow = context.getFacts();
        // MLModel mlModel = new MLModel();
        // mlModel.setNormalization(workflow.getNormalization());
        // mlModel.setNormalLabels(workflow.getNormalLabels());

        HeaderFilter headerFilter = new HeaderFilter.Builder().init(context).build();
        LineToTokens lineToTokens = new LineToTokens.Builder().init(context).build();
        DiscardedRowsFilter discardedRowsFilter = new DiscardedRowsFilter.Builder().init(context).build();
        RemoveDiscardedFeatures removeDiscardedFeatures = new RemoveDiscardedFeatures.Builder().init(context).build();
        BasicEncoder basicEncoder = new BasicEncoder.Builder().init(context).build();
        MeanImputation meanImputation = new MeanImputation.Builder().init(context).build();
        StringArrayToDoubleArray stringArrayToDoubleArray = new StringArrayToDoubleArray.Builder().build();
        DoubleArrayToVector doubleArrayToVector = new DoubleArrayToVector.Builder().build();
        NormalRowsFilter normalRowsFilter = new NormalRowsFilter.Builder().init(context).build();
        RemoveResponseColumn removeResponseColumn = new RemoveResponseColumn();

        JavaRDD<String> lines = context.getLines().cache();

        if (workflow.getNormalization().equals("true")) {

            Normalization normalization = new Normalization.Builder().init(context).build();
            return lines.filter(headerFilter).map(lineToTokens).filter(discardedRowsFilter).filter(normalRowsFilter)
                    .map(removeDiscardedFeatures).map(removeResponseColumn).map(basicEncoder).map(meanImputation).map(stringArrayToDoubleArray)
                    .map(normalization).map(doubleArrayToVector);

        } else {
            return lines.filter(headerFilter).map(lineToTokens).filter(discardedRowsFilter).filter(normalRowsFilter)
                    .map(removeDiscardedFeatures).map(removeResponseColumn).map(basicEncoder).map(meanImputation).map(stringArrayToDoubleArray)
                    .map(doubleArrayToVector);
        }

    }

    private JavaRDD<Vector> preProcessAnomaly() throws MLModelBuilderException {
        MLModelConfigurationContext context = getContext();
        Workflow workflow = context.getFacts();
        // MLModel mlModel = new MLModel();
        // mlModel.setNormalization(workflow.getNormalization());
        // mlModel.setNormalLabels(workflow.getNormalLabels());

        HeaderFilter headerFilter = new HeaderFilter.Builder().init(context).build();
        LineToTokens lineToTokens = new LineToTokens.Builder().init(context).build();
        DiscardedRowsFilter discardedRowsFilter = new DiscardedRowsFilter.Builder().init(context).build();
        RemoveDiscardedFeatures removeDiscardedFeatures = new RemoveDiscardedFeatures.Builder().init(context).build();
        BasicEncoder basicEncoder = new BasicEncoder.Builder().init(context).build();
        MeanImputation meanImputation = new MeanImputation.Builder().init(context).build();
        StringArrayToDoubleArray stringArrayToDoubleArray = new StringArrayToDoubleArray.Builder().build();
        DoubleArrayToVector doubleArrayToVector = new DoubleArrayToVector.Builder().build();
        AnomalyRowsFilter anomalyRowsFilter = new AnomalyRowsFilter.Builder().init(context).build();
        RemoveResponseColumn removeResponseColumn = new RemoveResponseColumn();

        JavaRDD<String> lines = context.getLines().cache();

        if (workflow.getNormalization().equals("true")) {

            Normalization normalization = new Normalization.Builder().init(context).build();
            return lines.filter(headerFilter).map(lineToTokens).filter(discardedRowsFilter).filter(anomalyRowsFilter)
                    .map(removeDiscardedFeatures).map(removeResponseColumn).map(basicEncoder).map(meanImputation).map(stringArrayToDoubleArray)
                    .map(normalization).map(doubleArrayToVector);

        } else {
            return lines.filter(headerFilter).map(lineToTokens).filter(discardedRowsFilter).filter(anomalyRowsFilter)
                    .map(removeDiscardedFeatures).map(removeResponseColumn).map(basicEncoder).map(meanImputation).map(stringArrayToDoubleArray)
                    .map(doubleArrayToVector);
        }

    }

    /**
     * Build an unsupervised model.
     */
    public MLModel build() throws MLModelBuilderException {
        MLModelConfigurationContext context = getContext();
        DatabaseService databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
        try {
            Workflow workflow = context.getFacts();
            long modelId = context.getModelId();
            ModelSummary summaryModel = null;

            // // gets the pre-processed dataset for unlabeled data
            // JavaRDD<Vector> data = preProcess().cache();

            // // gets the pre-processed dataset for labeled data
            // JavaRDD<Vector> dataNormal = preProcessNormal().cache();
            // JavaRDD<Vector> trainingDataNormal = dataNormal.sample(false, workflow.getTrainDataFraction(),
            // MLConstants.RANDOM_SEED).cache();
            // JavaRDD<Vector> testingDataNormal = dataNormal.subtract(trainingDataNormal);
            //
            // JavaRDD<Vector> dataAnomaly = preProcessAnomaly().cache();
            // double testDataFraction = (1 - workflow.getTrainDataFraction());
            // JavaRDD<Vector> testingDataAnomaly = dataAnomaly.sample(false, testDataFraction, MLConstants.RANDOM_SEED)
            // .cache();
            // JavaRDD<Vector> testingDataNormal = data.subtract(trainingDataNormal);

            // create a deployable MLModel object
            MLModel mlModel = new MLModel();
            mlModel.setNormalization(workflow.getNormalization());
            mlModel.setNormalLabels(workflow.getNormalLabels());
            mlModel.setAlgorithmName(workflow.getAlgorithmName());
            mlModel.setAlgorithmClass(workflow.getAlgorithmClass());
           // mlModel.setFeatures(workflow.getFeatures());
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
            switch (anomaly_detection_algorithm) {
            case K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA:
                mlModel.setResponseIndex(-1);
                // gets the pre-processed dataset for unlabeled data
                JavaRDD<Vector> data = preProcess().cache();
                summaryModel = buildKMeansUnlabeledDataModel(modelId, data, workflow, mlModel, includedFeatures);
                break;
            case K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA:
                mlModel.setResponseIndex(context.getResponseIndex());
                // gets the pre-processed dataset for labeled data
                JavaRDD<Vector> dataNormal = preProcessNormal().cache();
                JavaRDD<Vector> trainingDataNormal = dataNormal.sample(false, workflow.getTrainDataFraction(),
                        MLConstants.RANDOM_SEED).cache();
                JavaRDD<Vector> testingDataNormal = dataNormal.subtract(trainingDataNormal).cache();

                // remove from cache
                dataNormal.unpersist();

                JavaRDD<Vector> dataAnomaly = preProcessAnomaly().cache();
                double testDataFraction = (1 - workflow.getTrainDataFraction());
                JavaRDD<Vector> testingDataAnomaly = dataAnomaly.sample(false, testDataFraction,
                        MLConstants.RANDOM_SEED).cache();
                // remove from cache
                dataAnomaly.unpersist();

                summaryModel = buildKMeansLabeledDataModel(modelId, trainingDataNormal, testingDataNormal, testingDataAnomaly, workflow, mlModel, includedFeatures);
                break;
            default:
                throw new AlgorithmNameException("Incorrect algorithm name: " + workflow.getAlgorithmName()
                        + " for model id: " + modelId);
            }
            // persist model summary
            databaseService.updateModelSummary(modelId, summaryModel);
            return mlModel;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building anomaly detection machine learning model: "
                    + e.getMessage(), e);
        }
    }

    /**
     * This method builds a k-means model.
     *
     * @param modelID Model ID
     * @param data Training data as a JavaRDD of LabeledPoints
     * @param workflow Machine learning workflow
     * @param mlModel Deployable machine learning model
     * @throws MLModelBuilderException
     */
    private ModelSummary buildKMeansUnlabeledDataModel(long modelID, JavaRDD<Vector> data, Workflow workflow,
            MLModel mlModel, SortedMap<Integer, String> includedFeatures) throws MLModelBuilderException {
        try {
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            KMeansAnomalyDetectionUnlabeledData kMeansAnomalyDetectionUnlabeledData = new KMeansAnomalyDetectionUnlabeledData();
            KMeansModel kMeansModel = kMeansAnomalyDetectionUnlabeledData.train(data,
                    Integer.parseInt(hyperParameters.get(MLConstants.NUM_OF_NORMAL_CLUSTERS)),
                    Integer.parseInt(hyperParameters.get(MLConstants.MAX_ITERATIONS)));

            JavaRDD<Integer> predict = kMeansAnomalyDetectionUnlabeledData.test(kMeansModel, data);
            Vector[] clusterCenters = kMeansAnomalyDetectionUnlabeledData.getClusterCenters(kMeansModel);

            double[][] distancesArray = kMeansAnomalyDetectionUnlabeledData.getDistancesToDataPoints(predict,
                    clusterCenters, data);

            // remove from cache
            data.unpersist();

            KMeansAnomalyDetectionSummary kMeansAnomalyDetectionSummary = new KMeansAnomalyDetectionSummary();
            MLKMeansAnomalyDetectionModel mlkMeansAnomalyDetectionModel = new MLKMeansAnomalyDetectionModel(kMeansModel);
            mlkMeansAnomalyDetectionModel.setDistancesArray(distancesArray);

            mlModel.setModel(mlkMeansAnomalyDetectionModel);

            kMeansAnomalyDetectionSummary
                    .setAlgorithm(MLConstants.ANOMALY_DETECTION_ALGORITHM.K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA
                            .toString());
            kMeansAnomalyDetectionSummary.setDatasetVersion(workflow.getDatasetVersion());
            kMeansAnomalyDetectionSummary.setFeatures(includedFeatures.values().toArray(new String[0]));
            //kMeansAnomalyDetectionSummary.setModelAccuracy(99);

            return kMeansAnomalyDetectionSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building k-means anomaly detection with unlabeled data model: " + e.getMessage(), e);
        }
    }

    /**
     * This method builds a k-means model.
     *
     * @param modelID Model ID
     * @param trainData Training data as a JavaRDD of LabeledPoints
     * @param workflow Machine learning workflow
     * @param mlModel Deployable machine learning model
     * @throws MLModelBuilderException
     */
    private ModelSummary buildKMeansLabeledDataModel(long modelID, JavaRDD<Vector> trainData,
            JavaRDD<Vector> testDataNormal, JavaRDD<Vector> testDataAnomaly, Workflow workflow, MLModel mlModel, SortedMap<Integer, String> includedFeatures)
            throws MLModelBuilderException {
        try {
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            KMeansAnomalyDetectionLabeledData kMeansAnomalyDetectionLabeledData = new KMeansAnomalyDetectionLabeledData();
            KMeansModel kMeansModel = kMeansAnomalyDetectionLabeledData.train(trainData,
                    Integer.parseInt(hyperParameters.get(MLConstants.NUM_OF_NORMAL_CLUSTERS)),
                    Integer.parseInt(hyperParameters.get(MLConstants.MAX_ITERATIONS)));

            //KMeansModel kMeansModel = kMeansAnomalyDetectionLabeledData.train(trainData, 5, 100);

            JavaRDD<Integer> prediction = kMeansAnomalyDetectionLabeledData.test(kMeansModel, trainData);
            Vector[] clusterCenters = kMeansAnomalyDetectionLabeledData.getClusterCenters(kMeansModel);

            double[][] distancesArray = kMeansAnomalyDetectionLabeledData.getDistancesToDataPoints(prediction,
                    clusterCenters, trainData);

            // remove from cache
            trainData.unpersist();

            KMeansAnomalyDetectionSummary kMeansAnomalyDetectionSummary = new KMeansAnomalyDetectionSummary();
            MLKMeansAnomalyDetectionModel mlkMeansAnomalyDetectionModel = new MLKMeansAnomalyDetectionModel(kMeansModel);
            mlkMeansAnomalyDetectionModel.setDistancesArray(distancesArray);
            mlModel.setModel(mlkMeansAnomalyDetectionModel);

            // evaluating the model
            double[] percentiles = kMeansAnomalyDetectionLabeledData.getPercentileDistances(distancesArray,
                    Double.parseDouble(hyperParameters.get(MLConstants.PERCENTILE_VALUE)));
            JavaRDD<Integer> predictionTestNormal = kMeansAnomalyDetectionLabeledData.test(kMeansModel, testDataNormal);
            double[][] distancesArrayTetsNormal = kMeansAnomalyDetectionLabeledData.getDistancesToDataPoints(
                    predictionTestNormal, clusterCenters, testDataNormal);
            // remove from cache
            testDataNormal.unpersist();

            JavaRDD<Integer> predictionTestAnomaly = kMeansAnomalyDetectionLabeledData.test(kMeansModel,
                    testDataAnomaly);
            double[][] distancesArrayTetsAnomaly = kMeansAnomalyDetectionLabeledData.getDistancesToDataPoints(
                    predictionTestAnomaly, clusterCenters, testDataAnomaly);
            // remove from cache
            testDataAnomaly.unpersist();

            MulticlassConfusionMatrix predictionResults = kMeansAnomalyDetectionLabeledData.getEvaluationResults(
                    distancesArrayTetsNormal, distancesArrayTetsAnomaly, percentiles);

            kMeansAnomalyDetectionSummary
                    .setAlgorithm(MLConstants.ANOMALY_DETECTION_ALGORITHM.K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA
                            .toString());

            kMeansAnomalyDetectionSummary.setMulticlassConfusionMatrix(predictionResults);
            kMeansAnomalyDetectionSummary.setModelAccuracy(getModelAccuracy(predictionResults));
            kMeansAnomalyDetectionSummary.setDatasetVersion(workflow.getDatasetVersion());
            kMeansAnomalyDetectionSummary.setFeatures(includedFeatures.values().toArray(new String[0]));
            return kMeansAnomalyDetectionSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building k-means anomaly detection with labeled data model: " + e.getMessage(), e);
        }
    }

    private Double getModelAccuracy(MulticlassConfusionMatrix multiclassConfusionMatrix){
        double f1Score;
        double truePositive;
        //double trueNegetive = 0;
        double falsePositive;
        double falseNegetive;

        double[][] matrix = multiclassConfusionMatrix.getMatrix();
        truePositive = matrix[0][0];
        falseNegetive = matrix[0][1];
        falsePositive = matrix[1][0];
       // trueNegetive =  matrix[1][1];

        f1Score = 2*truePositive / (2*truePositive + falsePositive + falseNegetive);

        return f1Score * 100;
    }
}
