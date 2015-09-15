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

import java.util.Map;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.interfaces.MLModelBuilder;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.models.MLKMeansAnomalyDetectionModel;
import org.wso2.carbon.ml.core.spark.summary.KMeansAnomalyDetectionSummary;
import org.wso2.carbon.ml.core.spark.transformations.*;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
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


        JavaRDD<String> lines = context.getLines().cache();

        if (workflow.getNormalization() == "true") {

            Normalization normalization = new Normalization.Builder().init(context).build();
            return lines.filter(headerFilter).map(lineToTokens).filter(discardedRowsFilter)
                    .map(removeDiscardedFeatures).map(basicEncoder).map(meanImputation).map(stringArrayToDoubleArray)
                    .map(normalization).map(doubleArrayToVector);

        } else {
            return lines.filter(headerFilter).map(lineToTokens).filter(discardedRowsFilter)
                    .map(removeDiscardedFeatures).map(basicEncoder).map(meanImputation).map(stringArrayToDoubleArray)
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

            // gets the pre-processed dataset
            JavaRDD<Vector> data = preProcess().cache();
            JavaRDD<Vector> trainingData = data.sample(false, workflow.getTrainDataFraction(), MLConstants.RANDOM_SEED)
                    .cache();
            JavaRDD<Vector> testingData = data.subtract(trainingData);
            // create a deployable MLModel object
            MLModel mlModel = new MLModel();
            mlModel.setNormalization(workflow.getNormalization());
            mlModel.setAlgorithmName(workflow.getAlgorithmName());
            mlModel.setAlgorithmClass(workflow.getAlgorithmClass());
            mlModel.setFeatures(workflow.getFeatures());
            mlModel.setResponseVariable(workflow.getResponseVariable());
            mlModel.setEncodings(context.getEncodings());
            mlModel.setNewToOldIndicesList(context.getNewToOldIndicesList());
            mlModel.setResponseIndex(-1);

            // build a machine learning model according to user selected algorithm
            MLConstants.ANOMALY_DETECTION_ALGORITHM anomaly_detection_algorithm = MLConstants.ANOMALY_DETECTION_ALGORITHM
                    .valueOf(workflow.getAlgorithmName());
            switch (anomaly_detection_algorithm) {
            case K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA:
                summaryModel = buildKMeansUnlabeledDataModel(modelId, data, workflow, mlModel);
                break;
            default:
                throw new AlgorithmNameException("Incorrect algorithm name: " + workflow.getAlgorithmName()
                        + " for model id: " + modelId);
            }
            // persist model summary
            databaseService.updateModelSummary(modelId, summaryModel);
            return mlModel;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building unsupervised machine learning model: "
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
            MLModel mlModel) throws MLModelBuilderException {
        try {
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            KMeansUnlabeledData kMeansUnlabeledData = new KMeansUnlabeledData();
            KMeansModel kMeansModel = kMeansUnlabeledData.train(data,
                    Integer.parseInt(hyperParameters.get(MLConstants.NUM_CLUSTERS)),
                    Integer.parseInt(hyperParameters.get(MLConstants.ITERATIONS)));

            JavaRDD<Integer> predict = kMeansUnlabeledData.test(kMeansModel, data);
            Vector[] clusterCenters = kMeansUnlabeledData.getClusterCenters(kMeansModel);

            double[][] distancesArray = kMeansUnlabeledData.getDistancesToDataPoints(predict, clusterCenters, data);

            // remove from cache
            data.unpersist();

            KMeansAnomalyDetectionSummary kMeansAnomalyDetectionSummary = new KMeansAnomalyDetectionSummary();
            MLKMeansAnomalyDetectionModel mlkMeansAnomalyDetectionModel = new MLKMeansAnomalyDetectionModel(kMeansModel);
            mlkMeansAnomalyDetectionModel.setDistancesArray(distancesArray);

            mlModel.setModel(mlkMeansAnomalyDetectionModel);

            kMeansAnomalyDetectionSummary
                    .setAlgorithm(MLConstants.ANOMALY_DETECTION_ALGORITHM.K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA
                            .toString());

            return kMeansAnomalyDetectionSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building k-means model: " + e.getMessage(), e);
        }
    }
}
