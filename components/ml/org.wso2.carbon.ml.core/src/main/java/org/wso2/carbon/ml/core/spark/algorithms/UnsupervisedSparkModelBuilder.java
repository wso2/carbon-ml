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

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.constants.MLConstants.UNSUPERVISED_ALGORITHM;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.interfaces.MLModelBuilder;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.models.MLKMeansModel;
import org.wso2.carbon.ml.core.spark.summary.ClusterModelSummary;
import org.wso2.carbon.ml.core.spark.transformations.DoubleArrayToVector;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.database.DatabaseService;

import java.util.Map;

/**
 * Building unsupervised models supported by Spark.
 */
public class UnsupervisedSparkModelBuilder extends MLModelBuilder {

    public UnsupervisedSparkModelBuilder(MLModelConfigurationContext context) {
        super(context);
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

            // apply pre processing
            JavaRDD<double[]> features = SparkModelUtils.preProcess(context);
            // generate train and test datasets by converting double arrays to vectors
            DoubleArrayToVector doubleArrayToVector = new DoubleArrayToVector();
            JavaRDD<Vector> data = features.map(doubleArrayToVector);
            JavaRDD<Vector> trainingData = data.sample(false, workflow.getTrainDataFraction(), MLConstants.RANDOM_SEED)
                    .cache();
            JavaRDD<Vector> testingData = data.subtract(trainingData);
            // create a deployable MLModel object
            MLModel mlModel = new MLModel();
            mlModel.setAlgorithmName(workflow.getAlgorithmName());
            mlModel.setAlgorithmClass(workflow.getAlgorithmClass());
            mlModel.setFeatures(workflow.getFeatures());
            mlModel.setResponseVariable(workflow.getResponseVariable());
            mlModel.setEncodings(context.getEncodings());
            mlModel.setNewToOldIndicesList(context.getNewToOldIndicesList());
            mlModel.setResponseIndex(-1);

            // build a machine learning model according to user selected algorithm
            UNSUPERVISED_ALGORITHM unsupervised_algorithm = UNSUPERVISED_ALGORITHM.valueOf(workflow.getAlgorithmName());
            switch (unsupervised_algorithm) {
            case K_MEANS:
                summaryModel = buildKMeansModel(modelId, trainingData, testingData, workflow, mlModel);
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
     * @param trainingData Training data as a JavaRDD of LabeledPoints
     * @param testingData Testing data as a JavaRDD of LabeledPoints
     * @param workflow Machine learning workflow
     * @param mlModel Deployable machine learning model
     * @throws ModelServiceException
     */
    private ModelSummary buildKMeansModel(long modelID, JavaRDD<Vector> trainingData, JavaRDD<Vector> testingData,
            Workflow workflow, MLModel mlModel) throws MLModelBuilderException {
        try {
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            KMeans kMeans = new KMeans();
            KMeansModel kMeansModel = kMeans.train(trainingData,
                    Integer.parseInt(hyperParameters.get(MLConstants.NUM_CLUSTERS)),
                    Integer.parseInt(hyperParameters.get(MLConstants.ITERATIONS)));
            ClusterModelSummary clusterModelSummary = new ClusterModelSummary();
//            double trainDataComputeCost = kMeansModel.computeCost(trainingData.rdd());
//            double testDataComputeCost = kMeansModel.computeCost(testingData.rdd());
//            clusterModelSummary.setTrainDataComputeCost(trainDataComputeCost);
//            clusterModelSummary.setTestDataComputeCost(testDataComputeCost);
            mlModel.setModel(new MLKMeansModel(kMeansModel));
            clusterModelSummary.setAlgorithm(UNSUPERVISED_ALGORITHM.K_MEANS.toString());
            return clusterModelSummary;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building k-means model: " + e.getMessage(), e);
        }
    }
}
