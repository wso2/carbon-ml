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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.constants.MLConstants.DEEPLEARNING_ALGORITHM;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.models.MLDeeplearningModel;
import org.wso2.carbon.ml.core.spark.summary.DeeplearningModelSummary;
import org.wso2.carbon.ml.core.utils.DeeplearningModelUtils;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.database.DatabaseService;

import hex.deeplearning.DeepLearningModel;

/**
 * Build deep learning models
 */
public class DeeplearningModelBuilder extends SupervisedSparkModelBuilder {
    private static final Log log = LogFactory.getLog(DeeplearningModelBuilder.class);

    public DeeplearningModelBuilder(MLModelConfigurationContext context) {
        super(context);
    }

    @Override
    public MLModel build() throws MLModelBuilderException {

        if (log.isDebugEnabled()) {
            log.debug("Start building the Stacked Autoencoders...");
        }       
        MLModelConfigurationContext context = getContext();
        JavaSparkContext sparkContext = null;
        DatabaseService databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
        MLModel mlModel = new MLModel();

        try {
            sparkContext = context.getSparkContext();
            Workflow workflow = context.getFacts();
            long modelId = context.getModelId();

            // generate train and test datasets by converting tokens to labeled points
            int responseIndex = context.getResponseIndex();
            SortedMap<Integer, String> includedFeatures = MLUtils.getIncludedFeaturesAfterReordering(workflow,
                    context.getNewToOldIndicesList(), responseIndex);

            // gets the pre-processed dataset
            JavaRDD<LabeledPoint> labeledPoints = preProcess().cache();

            JavaRDD<LabeledPoint>[] dataSplit = labeledPoints.randomSplit(
                    new double[] { workflow.getTrainDataFraction(), 1 - workflow.getTrainDataFraction() },
                    MLConstants.RANDOM_SEED);
            JavaRDD<LabeledPoint> trainingData = dataSplit[0];
            JavaRDD<LabeledPoint> testingData = dataSplit[1];

            List<LabeledPoint> trpoints = trainingData.collect();
            List<LabeledPoint> tepoints = testingData.collect();

            // create a deployable MLModel object
            mlModel.setAlgorithmName(workflow.getAlgorithmName());
            mlModel.setAlgorithmClass(workflow.getAlgorithmClass());
            mlModel.setFeatures(workflow.getIncludedFeatures());
            mlModel.setResponseVariable(workflow.getResponseVariable());
            mlModel.setEncodings(context.getEncodings());
            mlModel.setNewToOldIndicesList(context.getNewToOldIndicesList());
            mlModel.setResponseIndex(responseIndex);

            ModelSummary summaryModel = null;

            DEEPLEARNING_ALGORITHM deeplearningAlgorithm = DEEPLEARNING_ALGORITHM.valueOf(workflow.getAlgorithmName());
            switch (deeplearningAlgorithm) {
            case STACKED_AUTOENCODERS:
                log.info("Building summary model for SAE");
                summaryModel = buildStackedAutoencodersModel(sparkContext, modelId, trainingData, testingData, workflow,
                        mlModel, includedFeatures);
                log.info("Successful building summary model for SAE");
                break;
            default:
                throw new AlgorithmNameException("Incorrect algorithm name");
            }

            databaseService.updateModelSummary(modelId, summaryModel);
            return mlModel;
        } catch (Exception e) {
            throw new MLModelBuilderException(
                    "An error occurred while building supervised machine learning model: " + e.getMessage(), e);
        } finally {
            // do something finally
        }
    }

    private int[] stringArrToIntArr(String str) {
        String[] tokens = str.split(",");
        int[] arr = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            arr[i] = Integer.parseInt(tokens[i]);
        }
        return arr;
    }

    /**
     * Build the stacked autoencoder model
     * 
     * @param sparkContext
     * @param modelID model ID
     * @param trainingData training data to train the classifier
     * @param testingData testing data to test the classifier and get metrics
     * @param workflow workflow
     * @param mlModel MLModel to be updated with calcualted values
     * @param includedFeatures Included features
     * @return
     * @throws MLModelBuilderException
     */
    private ModelSummary buildStackedAutoencodersModel(JavaSparkContext sparkContext, long modelID,
            JavaRDD<LabeledPoint> trainingData, JavaRDD<LabeledPoint> testingData, Workflow workflow, MLModel mlModel,
            SortedMap<Integer, String> includedFeatures) throws MLModelBuilderException {
        try {
            StackedAutoencodersClassifier saeClassifier = new StackedAutoencodersClassifier();
            Map<String, String> hyperParameters = workflow.getHyperParameters();

            // train the stacked autoencoder
            DeepLearningModel deeplearningModel = saeClassifier.train(trainingData,
                    Integer.parseInt(hyperParameters.get(MLConstants.BATCH_SIZE)),
                    stringArrToIntArr(hyperParameters.get(MLConstants.LAYER_SIZES)),
                    hyperParameters.get(MLConstants.ACTIVATION_TYPE),
                    Integer.parseInt(hyperParameters.get(MLConstants.EPOCHS)), workflow.getResponseVariable(), modelID);

            if (deeplearningModel == null) {
                throw new MLModelBuilderException("DeeplearningModel is Null.");
            }

            // remove from cache
            trainingData.unpersist();
            // add to cache
            testingData.cache();

            // make predictions with the trained model
            JavaPairRDD<Double, Double> predictionsAndLabels = saeClassifier.test(sparkContext, deeplearningModel,
                    testingData).cache();

            // get model summary
            DeeplearningModelSummary deeplearningModelSummary = DeeplearningModelUtils
                    .getDeeplearningModelSummary(sparkContext, testingData, predictionsAndLabels);

            // remove from cache
            testingData.unpersist();

            mlModel.setModel(new MLDeeplearningModel(deeplearningModel));

            deeplearningModelSummary.setFeatures(includedFeatures.values().toArray(new String[0]));
            deeplearningModelSummary.setAlgorithm(MLConstants.DEEPLEARNING_ALGORITHM.STACKED_AUTOENCODERS.toString());

            // set accuracy values
            MulticlassMetrics multiclassMetrics = getMulticlassMetrics(sparkContext, predictionsAndLabels);

            // remove from cache
            predictionsAndLabels.unpersist();

            deeplearningModelSummary
                    .setMulticlassConfusionMatrix(getMulticlassConfusionMatrix(multiclassMetrics, mlModel));
            Double modelAccuracy = getModelAccuracy(multiclassMetrics);
            deeplearningModelSummary.setModelAccuracy(modelAccuracy);

            return deeplearningModelSummary;

        } catch (Exception e) {
            throw new MLModelBuilderException(
                    "An error occurred while building stacked autoencoders model: " + e.getMessage(), e);
        }

    }
}
