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

package org.wso2.carbon.ml.model.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.wso2.carbon.ml.commons.domain.ClusterPoint;
import org.wso2.carbon.ml.commons.domain.HyperParameter;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.model.ModelService;
import org.wso2.carbon.ml.model.exceptions.MLAlgorithmParserException;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
import org.wso2.carbon.ml.model.exceptions.SparkConfigurationParserException;
import org.wso2.carbon.ml.model.internal.ds.MLModelServiceValueHolder;
import org.wso2.carbon.ml.model.internal.dto.ConfusionMatrix;
import org.wso2.carbon.ml.model.internal.dto.MLAlgorithm;
import org.wso2.carbon.ml.model.internal.dto.MLAlgorithms;
import org.wso2.carbon.ml.model.internal.dto.ModelSettings;
import org.wso2.carbon.ml.model.spark.algorithms.KMeans;
import org.wso2.carbon.ml.model.spark.algorithms.SupervisedModel;
import org.wso2.carbon.ml.model.spark.algorithms.UnsupervisedModel;
import org.wso2.carbon.ml.model.spark.dto.PredictedVsActual;
import org.wso2.carbon.ml.model.spark.dto.ProbabilisticClassificationModelSummary;
import org.wso2.carbon.ml.model.spark.transformations.HeaderFilter;
import org.wso2.carbon.ml.model.spark.transformations.LineToTokens;
import org.wso2.carbon.ml.model.spark.transformations.MissingValuesFilter;
import org.wso2.carbon.ml.model.spark.transformations.TokensToVectors;
import scala.Tuple2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.CLASSIFICATION;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.CLUSTERING;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.DATASET_SIZE;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.DECIMAL_FORMAT;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.HIGH;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.INTERPRETABILITY;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.LARGE;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.MEDIUM;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.NUMERICAL_PREDICTION;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.SMALL;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.TEXTUAL;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.YES;

public class SparkModelService implements ModelService {
    private static final Log logger = LogFactory.getLog(SparkModelService.class);
    private MLAlgorithms mlAlgorithms;
    private SparkConf sparkConf;

    public SparkModelService(String mlAlgorithmsConfigFilePath, String sparkConfFilePath)
            throws MLAlgorithmParserException, SparkConfigurationParserException {
        mlAlgorithms = MLModelUtils.getMLAlgorithms(mlAlgorithmsConfigFilePath);
        sparkConf = MLModelUtils.getSparkConf(sparkConfFilePath);
    }

    /**
     * @param algorithm Name of the machine learning algorithm
     * @return List containing hyper parameters
     */
    public List<HyperParameter> getHyperParameters(String algorithm) {
        List<HyperParameter> hyperParameters = null;
        for (MLAlgorithm mlAlgorithm : mlAlgorithms.getAlgorithms()) {
            if (algorithm.equals(mlAlgorithm.getName())) {
                hyperParameters = mlAlgorithm.getParameters();
                break;
            }
        }
        return hyperParameters;
    }

    /**
     * @param algorithmType Type of the machine learning algorithm - e.g. Classification
     * @return List of algorithm names
     */
    public List<String> getAlgorithmsByType(String algorithmType) {
        List<String> algorithms = new ArrayList();
        for (MLAlgorithm algorithm : mlAlgorithms.getAlgorithms()) {
            if (algorithmType.equals(algorithm.getType())) {
                algorithms.add(algorithm.getName());
            }
        }
        return algorithms;
    }

    /**
     * @param algorithmType Type of the machine learning algorithm - e.g. Classification
     * @param userResponse  User's response to a questionnaire about machine learning task
     * @return Map containing names of recommended machine learning algorithms and
     * recommendation scores (out of 5) for each algorithm
     */
    public Map<String, Double> getRecommendedAlgorithms(String algorithmType,
            Map<String, String> userResponse) {
        Map<String, Double> recommendations = new HashMap<String, Double>();
        List<MLAlgorithm> algorithms = new ArrayList();
        for (MLAlgorithm mlAlgorithm : mlAlgorithms.getAlgorithms()) {
            if (algorithmType.equals(mlAlgorithm.getType())) {
                algorithms.add(mlAlgorithm);
            }
        }
        for (MLAlgorithm mlAlgorithm : algorithms) {
            if (HIGH.equals(userResponse.get(INTERPRETABILITY))) {
                mlAlgorithm.setInterpretability(mlAlgorithm.getInterpretability() * 5);
            } else if (MEDIUM.equals(userResponse.get(INTERPRETABILITY))) {
                mlAlgorithm.setInterpretability(mlAlgorithm.getInterpretability() * 3);
            } else {
                mlAlgorithm.setInterpretability(5);
            }
            if (LARGE.equals(userResponse.get(DATASET_SIZE))) {
                mlAlgorithm.setScalability(mlAlgorithm.getScalability() * 5);
            } else if (MEDIUM.equals(userResponse.get(DATASET_SIZE))) {
                mlAlgorithm.setScalability(mlAlgorithm.getScalability() * 3);
            } else if (SMALL.equals(userResponse.get(DATASET_SIZE))) {
                mlAlgorithm.setScalability(5);
            }
            if (YES.equals(userResponse.get(TEXTUAL))) {
                mlAlgorithm.setDimensionality(mlAlgorithm.getDimensionality() * 3);
            } else {
                mlAlgorithm.setDimensionality(5);
            }
            recommendations.put(mlAlgorithm.getName(), (double) (mlAlgorithm.getDimensionality()
                                                                         + mlAlgorithm
                    .getInterpretability() + mlAlgorithm.getScalability()));
        }
        Double max = Collections.max(recommendations.values());
        DecimalFormat ratingNumberFormat = new DecimalFormat(DECIMAL_FORMAT);
        Double scaledRating;
        for (Map.Entry<String, Double> recommendation : recommendations.entrySet()) {
            scaledRating = ((recommendation.getValue()) / max) * 5;
            scaledRating = Double.valueOf(ratingNumberFormat.format(scaledRating));
            recommendations.put(recommendation.getKey(), scaledRating);
        }
        return recommendations;
    }

    /**
     * @param modelID    Model ID
     * @param workflowID Workflow ID
     * @throws ModelServiceException
     */
    public void buildModel(String modelID, String workflowID) throws ModelServiceException {
        /**
         * Spark looks for various configuration files using thread context class loader. Therefore, the
         * class loader needs to be switched temporarily.
         */
        // assign current thread context class loader to a variable
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            // class loader is switched to JavaSparkContext.class's class loader
            Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
            if (modelID == null || modelID.length() == 0) {
                throw new ModelServiceException("Argument: modelID is either null or empty");
            }
            if (workflowID == null || workflowID.length() == 0) {
                throw new ModelServiceException("Argument: workflowID is either null or empty");
            }
            DatabaseService dbService = MLModelServiceValueHolder.getDatabaseService();
            Workflow workflow = dbService.getWorkflow(workflowID);
            String algorithmType = workflow.getAlgorithmClass();
            if (CLASSIFICATION.equals(algorithmType) || NUMERICAL_PREDICTION.equals(
                    algorithmType)) {
                SupervisedModel supervisedModel = new SupervisedModel();
                supervisedModel.buildModel(modelID, workflow, sparkConf);
            } else if (CLUSTERING.equals((algorithmType))) {
                UnsupervisedModel unsupervisedModel = new UnsupervisedModel();
                unsupervisedModel.buildModel(modelID, workflow, sparkConf);
            }
        } catch (DatabaseHandlerException e) {
            throw new ModelServiceException("An error occurred while saving model to database: " + e.getMessage(), e);
        } finally {
            // switch class loader back to thread context class loader
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    /**
     * @param modelID Model ID
     * @return Model summary object
     * @throws ModelServiceException
     */
    public ModelSummary getModelSummary(String modelID) throws ModelServiceException {
        ModelSummary modelSummary = null;
        try {
            if (modelID == null || modelID.length() == 0) {
                throw new ModelServiceException("Argument: modelID is either null or empty");
            }
            DatabaseService dbService = MLModelServiceValueHolder.getDatabaseService();
            modelSummary = dbService.getModelSummary(modelID);
        } catch (DatabaseHandlerException e) {
            throw new ModelServiceException("An error occurred while retrieving model summary: " + e.getMessage(), e);
        }
        return modelSummary;
    }

    /**
     * @param modelSettings Model settings
     * @throws ModelServiceException
     */
    public void insertModelSettings(ModelSettings modelSettings) throws ModelServiceException {
        try {
            DatabaseService dbService = MLModelServiceValueHolder.getDatabaseService();
            dbService.insertModelSettings(modelSettings.getModelSettingsID(),
                    modelSettings.getWorkflowID(), modelSettings.getAlgorithmType(),
                    modelSettings.getAlgorithmName(), modelSettings.getResponse(),
                    modelSettings.getTrainDataFraction(), modelSettings.getHyperParameters());
        } catch (DatabaseHandlerException e) {
            throw new ModelServiceException("An error occurred while inserting model settings for " +
                    modelSettings.getModelSettingsID() + " : " + e.getMessage(), e);
        }
    }

    /**
     * This method checks whether model execution is completed or not
     *
     * @param modelID Model ID
     * @return Indicates whether model execution is completed or not
     * @throws ModelServiceException
     */
    public boolean isExecutionCompleted(String modelID) throws ModelServiceException {
        try {
            if (modelID == null || modelID.length() == 0) {
                throw new ModelServiceException("Argument: modelID is either null or empty");
            }
            DatabaseService dbService = MLModelServiceValueHolder.getDatabaseService();
            return dbService.getModelExecutionEndTime(modelID) > 0;
        } catch (DatabaseHandlerException e) {
            throw new ModelServiceException("An error occurred while querying model: " + modelID +
                    " for execution end time: " + e.getMessage(), e);
        }
    }

    /**
     * This method checks whether model execution is started or not
     *
     * @param modelID Model ID
     * @return Indicates whether model execution is started or not
     * @throws ModelServiceException
     */
    public boolean isExecutionStarted(String modelID) throws ModelServiceException {
        try {
            if (modelID == null || modelID.length() == 0) {
                throw new ModelServiceException("Argument: modelID is either null or empty");
            }
            DatabaseService dbService = MLModelServiceValueHolder.getDatabaseService();
            return dbService.getModelExecutionStartTime(modelID) > 0;
        } catch (DatabaseHandlerException e) {
            throw new ModelServiceException("An error occurred while querying model: " + modelID +
                    " for execution start time: " + e.getMessage(), e);
        }
    }

    /**
     * This method returns a confusion matrix for a given threshold
     *
     * @param modelID   Model ID
     * @param threshold Probability threshold
     * @return Returns a confusion matrix object
     * @throws ModelServiceException
     */
    public ConfusionMatrix getConfusionMatrix(String modelID, double threshold)
            throws ModelServiceException {
        try {
            if (modelID == null || modelID.length() == 0) {
                throw new ModelServiceException("Argument: modelID is either null or empty");
            }
            if (threshold < 0 || threshold > 1.0) {
                throw new ModelServiceException("Invalid threshold");
            }
            long truePositives, falsePositives, trueNegatives, falseNegatives;
            trueNegatives = truePositives = falsePositives = falseNegatives = 0;
            List<PredictedVsActual> predictedVsActuals = ((ProbabilisticClassificationModelSummary)
                                                                  getModelSummary(modelID)).getPredictedVsActuals();
            double predicted, actual;
            for (PredictedVsActual predictedVsActual : predictedVsActuals) {
                predicted = predictedVsActual.getPredicted();
                actual = predictedVsActual.getActual();
                if (predicted > threshold) {
                    if (actual == 1.0) {
                        truePositives += 1;
                    } else {
                        falsePositives += 1;
                    }
                } else {
                    if (actual == 0.0) {
                        trueNegatives += 1;
                    } else {
                        falseNegatives += 1;
                    }
                }
            }
            ConfusionMatrix confusionMatrix = new ConfusionMatrix();
            confusionMatrix.setTruePositives(truePositives);
            confusionMatrix.setFalsePositives(falsePositives);
            confusionMatrix.setTrueNegatives(trueNegatives);
            confusionMatrix.setFalseNegatives(falseNegatives);
            return confusionMatrix;
        } catch (ModelServiceException e) {
            throw new ModelServiceException("An error occured while generating confusion matrix: " + e.getMessage(), e);
        }
    }

    /**
     * This method retuns a list of k-means cluster points
     *
     * @param datasetURL   Dataset URL
     * @param features     List containing feature names
     * @param noOfClusters Number of clusters
     * @return Returns a list of cluster points
     * @throws ModelServiceException
     */
    public List<ClusterPoint> getClusterPoints(String datasetURL, List<String> features, int noOfClusters)
            throws ModelServiceException {
        // assign current thread context class loader to a variable
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            final List<ClusterPoint> clusterPoints = new ArrayList<ClusterPoint>();
            if (datasetURL == null || datasetURL.equals("")) {
                throw new ModelServiceException("Argument: datasetURL is either null or empty");
            }
            if (features == null) {
                throw new ModelServiceException("Argument: features is null");
            }
            if (noOfClusters < 1) {
                throw new ModelServiceException("Number of clusters can't be less than 1");
            }
            // class loader is switched to JavaSparkContext.class's class loader
            Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
            // create a new spark configuration
            sparkConf.setAppName(datasetURL);
            // create a new java spark context
            JavaSparkContext sc = new JavaSparkContext(sparkConf);
            // parse lines in the dataset
            JavaRDD<String> lines = sc.textFile(datasetURL);
            // get header line
            String headerRow = lines.take(1).get(0);
            // get column separator
            String columnSeparator = MLModelUtils.getColumnSeparator(datasetURL);
            Pattern pattern = Pattern.compile(columnSeparator);
            // get selected feature indices
            final List<Integer> featureIndices = new ArrayList<Integer>();
            for (String feature : features) {
                featureIndices.add(MLModelUtils.getFeatureIndex(feature, headerRow, columnSeparator));
            }
            // Convert ramdomly selected 10000 rows to feature vectors
            JavaRDD<Vector> featureVectors = lines.filter(new HeaderFilter(headerRow))
                    .sample(false, 10000 / lines.count()).map(new LineToTokens(pattern))
                    .filter(new MissingValuesFilter())
                    .map(new TokensToVectors(featureIndices));
            KMeans kMeans = new KMeans();
            KMeansModel kMeansModel = kMeans.train(featureVectors, noOfClusters, 100);
            // Populate cluster points list with predicted clusters and features
            List<Tuple2<Integer, Vector>> kMeansPredictions = kMeansModel.predict(featureVectors).zip(featureVectors)
                    .collect();
            for (Tuple2<Integer, Vector> kMeansPrediction : kMeansPredictions) {
                ClusterPoint clusterPoint = new ClusterPoint();
                clusterPoint.setCluster(kMeansPrediction._1());
                clusterPoint.setFeatures(kMeansPrediction._2().toArray());
                clusterPoints.add(clusterPoint);
            }
            sc.stop();
            return clusterPoints;
        } catch (ModelServiceException e) {
            throw new ModelServiceException("An error occured while generating cluster points: " + e.getMessage(), e);
        } finally {
            // switch class loader back to thread context class loader
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }
}
