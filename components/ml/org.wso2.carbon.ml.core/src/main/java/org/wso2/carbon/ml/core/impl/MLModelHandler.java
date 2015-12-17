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
package org.wso2.carbon.ml.core.impl;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.InvalidRequestException;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.pmml.PMMLExportable;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer.Context;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.*;
import org.wso2.carbon.ml.commons.domain.config.Storage;
import org.wso2.carbon.ml.core.exceptions.*;
import org.wso2.carbon.ml.core.factories.DatasetType;
import org.wso2.carbon.ml.core.factories.ModelBuilderFactory;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLModelBuilder;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;
import org.wso2.carbon.ml.core.interfaces.PMMLModelContainer;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.algorithms.KMeans;
import org.wso2.carbon.ml.core.spark.algorithms.SparkModelUtils;
import org.wso2.carbon.ml.core.spark.models.MLDeeplearningModel;
import org.wso2.carbon.ml.core.spark.models.MLMatrixFactorizationModel;
import org.wso2.carbon.ml.core.spark.recommendation.CollaborativeFiltering;
import org.wso2.carbon.ml.core.spark.transformations.HeaderFilter;
import org.wso2.carbon.ml.core.spark.transformations.LineToTokens;
import org.wso2.carbon.ml.core.spark.transformations.MissingValuesFilter;
import org.wso2.carbon.ml.core.spark.transformations.TokensToVectors;
import org.wso2.carbon.ml.core.utils.BlockingExecutor;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.core.utils.MLUtils.ColumnSeparatorFactory;
import org.wso2.carbon.ml.core.utils.MLUtils.DataTypeFactory;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.xml.sax.InputSource;

import scala.Tuple2;
import hex.deeplearning.DeepLearningModel;


/**
 * {@link MLModelHandler} is responsible for handling/delegating all the model related requests.
 */
public class MLModelHandler {
    private static final Log log = LogFactory.getLog(MLModelHandler.class);
    private DatabaseService databaseService;
    private Properties mlProperties;
    private BlockingExecutor threadExecutor;

    public enum Format {SERIALIZED, PMML}

    public MLModelHandler() {
        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        databaseService = valueHolder.getDatabaseService();
        mlProperties = valueHolder.getMlProperties();
        threadExecutor = valueHolder.getThreadExecutor();
    }

    /**
     * Create a new model.
     *
     * @param model model to be created.
     * @throws MLModelHandlerException
     */
    public MLModelData createModel(MLModelData model) throws MLModelHandlerException {
        try {
            // set the model storage configurations
            Storage modelStorage = MLCoreServiceValueHolder.getInstance().getModelStorage();
            model.setStorageType(modelStorage.getStorageType());
            model.setStorageDirectory(modelStorage.getStorageDirectory());

            int tenantId = model.getTenantId();
            String userName = model.getUserName();
            MLAnalysis analysis = databaseService.getAnalysis(tenantId, userName, model.getAnalysisId());
            if (analysis == null) {
                throw new MLModelHandlerException("Invalid analysis [id] " + model.getAnalysisId());
            }

            MLDatasetVersion versionSet = databaseService.getVersionset(tenantId, userName, model.getVersionSetId());
            if (versionSet == null) {
                throw new MLModelHandlerException("Invalid version set [id] " + model.getVersionSetId());

            }
            // set model name
            String modelName = analysis.getName();
            modelName = modelName + "." + MLConstants.MODEL_NAME + "." + MLUtils.getDate();
            model.setName(modelName);
            model.setStatus(MLConstants.MODEL_STATUS_NOT_STARTED);

            databaseService.insertModel(model);
            log.info(String.format("[Created] %s", model));
            return model;
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e.getMessage(), e);
        }
    }

    public void deleteModel(int tenantId, String userName, long modelId) throws MLModelHandlerException {
        try {
            databaseService.deleteModel(tenantId, userName, modelId);
            log.info(String.format("[Deleted] Model [id] %s", modelId));
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e.getMessage(), e);
        }
    }

    public MLModelData getModel(int tenantId, String userName, String modelName) throws MLModelHandlerException {
        try {
            return databaseService.getModel(tenantId, userName, modelName);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e.getMessage(), e);
        }
    }

    public MLModelData getModel(int tenantId, String userName, long modelId) throws MLModelHandlerException {
        try {
            return databaseService.getModel(tenantId, userName, modelId);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e.getMessage(), e);
        }
    }

    public List<MLModelData> getAllModels(int tenantId, String userName) throws MLModelHandlerException {
        try {
            return databaseService.getAllModels(tenantId, userName);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e.getMessage(), e);
        }
    }

    public boolean isValidModelId(int tenantId, String userName, long modelId) throws MLModelHandlerException {
        try {
            return databaseService.isValidModelId(tenantId, userName, modelId);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e.getMessage(), e);
        }
    }

    public boolean isValidModelStatus(long modelId, int tenantId, String userName) throws MLModelHandlerException {
        try {
            return databaseService.isValidModelStatus(modelId, tenantId, userName);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e.getMessage(), e);
        }
    }


    /**
     * @param modelId unique id of the model
     * @param storage MLStorage to be updated
     * @throws MLModelHandlerException
     */
    public void addStorage(long modelId, MLStorage storage) throws MLModelHandlerException {
        try {
            databaseService.updateModelStorage(modelId, storage.getType(), storage.getLocation());
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e.getMessage(), e);
        }
    }

    /**
     * Get the summary of a model
     *
     * @param modelId ID of the model
     * @return Model Summary
     * @throws MLModelHandlerException
     */
    public ModelSummary getModelSummary(long modelId) throws MLModelHandlerException {
        try {
            return databaseService.getModelSummary(modelId);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e.getMessage(), e);
        }
    }

    /**
     * Build a ML model asynchronously and persist the built model in a given storage.
     *
     * @param modelId id of the model to be built.
     * @param tenantId tenant id
     * @param userName tenant user name
     * @throws MLModelHandlerException
     * @throws MLModelBuilderException
     */
    public Workflow buildModel(int tenantId, String userName, long modelId) throws MLModelHandlerException,
            MLModelBuilderException {

        if (!isValidModelId(tenantId, userName, modelId)) {
            String msg = String.format("Failed to build the model. Invalid model id: %s for tenant: %s and user: %s",
                    modelId, tenantId, userName);
            throw new MLModelHandlerException(msg);
        }

        try {
            long datasetVersionId = databaseService.getDatasetVersionIdOfModel(modelId);
            long datasetId = databaseService.getDatasetId(datasetVersionId);
            MLDataset dataset = databaseService.getDataset(tenantId, userName, datasetId);
            String dataSourceType = dataset.getDataSourceType();
            String dataType = databaseService.getDataTypeOfModel(modelId);
            String columnSeparator = ColumnSeparatorFactory.getColumnSeparator(dataType);
            String dataUrl = databaseService.getDatasetVersionUri(datasetVersionId);
            handleNull(dataUrl, "Target path is null for dataset version [id]: " + datasetVersionId);
            MLModelData model = databaseService.getModel(tenantId, userName, modelId);
            Workflow facts = databaseService.getWorkflow(model.getAnalysisId());
            facts.setDatasetVersion(databaseService.getVersionset(tenantId, userName, datasetVersionId).getName());
            facts.setDatasetURL(dataUrl);

            JavaRDD<String> lines;

            JavaSparkContext sparkContext = null;
            // java spark context
            sparkContext = MLCoreServiceValueHolder.getInstance().getSparkContext();

            try {
                lines = extractLines(tenantId, datasetId, sparkContext, dataUrl, dataSourceType, dataType);
            } catch (MLMalformedDatasetException e) {
                throw new MLModelBuilderException("Failed to build the model [id] " + modelId, e);
            }

            MLModelConfigurationContext context = buildMLModelConfigurationContext(modelId, datasetVersionId,
                    columnSeparator, model, facts, lines, sparkContext);

            // build the model asynchronously
            ModelBuilder task = new ModelBuilder(modelId, context);
            threadExecutor.execute(task);
            threadExecutor.afterExecute(task, null);

            databaseService.updateModelStatus(modelId, MLConstants.MODEL_STATUS_IN_PROGRESS);
            log.info(String.format("Build model [id] %s job is successfully submitted to Spark.", modelId));

            return facts;
        } catch (DatabaseHandlerException e) {
            throw new MLModelBuilderException("An error occurred while saving model [id] " + modelId + " to database: "
                    + e.getMessage(), e);
        }
    }

    private MLModelConfigurationContext buildMLModelConfigurationContext(long modelId, long datasetVersionId,
                                                                         String columnSeparator, MLModelData model, Workflow facts, JavaRDD<String> lines,
                                                                         JavaSparkContext sparkContext) throws DatabaseHandlerException {
        MLModelConfigurationContext context = new MLModelConfigurationContext();
        context.setModelId(modelId);
        context.setColumnSeparator(columnSeparator);
        context.setFacts(facts);
        context.setModel(model);
        Map<String, String> summaryStatsOfFeatures = databaseService.getSummaryStats(datasetVersionId);
        context.setSummaryStatsOfFeatures(summaryStatsOfFeatures);
        int responseIndex = MLUtils.getFeatureIndex(facts.getResponseVariable(), facts.getFeatures());
        context.setIncludedFeaturesMap(MLUtils.getIncludedFeatures(facts, responseIndex));
        context.setNewToOldIndicesList(getNewToOldIndicesList(context.getIncludedFeaturesMap()));
        context.setResponseIndex(responseIndex);
        context.setSparkContext(sparkContext);
        context.setLines(lines);
        // get header line
        String headerRow = databaseService.getFeatureNamesInOrderUsingDatasetVersion(datasetVersionId, columnSeparator);
        context.setHeaderRow(headerRow);
        return context;
    }

    public List<?> predict(int tenantId, String userName, long modelId, String dataFormat, InputStream dataStream)
            throws MLModelHandlerException {
        List<String[]> data = new ArrayList<String[]>();
        CSVFormat csvFormat = DataTypeFactory.getCSVFormat(dataFormat);
        BufferedReader br = new BufferedReader(new InputStreamReader(dataStream, StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] dataRow = line.split(csvFormat.getDelimiter() + "");
                data.add(dataRow);
            }
            return predict(tenantId, userName, modelId, data);
        } catch (IOException e) {
            String msg = "Failed to read the data points for prediction for model [id] " + modelId;
            log.error(msg, e);
            throw new MLModelHandlerException(msg, e);
        } finally {
            try {
                dataStream.close();
                br.close();
            } catch (IOException ignore) {
            }
        }

    }

    public String streamingPredict(int tenantId, String userName, long modelId, String dataFormat,
                                   String columnHeader, InputStream dataStream) throws MLModelHandlerException {
        List<String[]> data = new ArrayList<String[]>();
        CSVFormat csvFormat = DataTypeFactory.getCSVFormat(dataFormat);
        MLModel mlModel = retrieveModel(modelId);
        BufferedReader br = new BufferedReader(new InputStreamReader(dataStream, StandardCharsets.UTF_8));
        StringBuilder predictionsWithData = new StringBuilder();
        try {
            String line;
            if((line = br.readLine()) != null && line.split(csvFormat.getDelimiter() + "").length == mlModel.getNewToOldIndicesList().size()) {
                if(columnHeader.equalsIgnoreCase(MLConstants.NO)) {
                    String[] dataRow = line.split(csvFormat.getDelimiter() + "");
                    data.add(dataRow);
                } else {
                    predictionsWithData.append(line).append(MLConstants.NEW_LINE);
                }
                while ((line = br.readLine()) != null) {
                    String[] dataRow = line.split(csvFormat.getDelimiter() + "");
                    data.add(dataRow);
                }
                // cloning unencoded data to append with predictions
                List<String[]> unencodedData = new ArrayList<String[]>(data.size());
                for (String[] item : data) {
                    unencodedData.add(item.clone());
                }
                List<?> predictions = predict(tenantId, userName, modelId, data);
                for (int i = 0; i < predictions.size(); i++) {
                    predictionsWithData.append(MLUtils.arrayToCsvString(unencodedData.get(i), csvFormat.getDelimiter()))
                            .append(String.valueOf(predictions.get(i)))
                            .append(MLConstants.NEW_LINE);
                }
            } else {
                int responseVariableIndex = mlModel.getResponseIndex();
                List<Integer> includedFeatureIndices = mlModel.getNewToOldIndicesList();
                List<String[]> unencodedData = new ArrayList<String[]>();
                if(columnHeader.equalsIgnoreCase(MLConstants.NO)) {
                    int count = 0;
                    String[] dataRow = line.split(csvFormat.getDelimiter() + "");
                    unencodedData.add(dataRow.clone());
                    String[] includedFeatureValues = new String[includedFeatureIndices.size()];
                    for (int index : includedFeatureIndices) {
                        includedFeatureValues[count++] = dataRow[index];
                    }
                    data.add(includedFeatureValues);
                } else {
                    predictionsWithData.append(line).append(MLConstants.NEW_LINE);
                }
                while ((line = br.readLine()) != null) {
                    int count = 0;
                    String[] dataRow = line.split(csvFormat.getDelimiter() + "");
                    unencodedData.add(dataRow.clone());
                    String[] includedFeatureValues = new String[includedFeatureIndices.size()];
                    for (int index : includedFeatureIndices) {
                        includedFeatureValues[count++] = dataRow[index];
                    }
                    data.add(includedFeatureValues);
                }

                List<?> predictions = predict(tenantId, userName, modelId, data);
                for (int i = 0; i < predictions.size(); i++) {
                    // replace with predicted value
                    unencodedData.get(i)[responseVariableIndex] = String.valueOf(predictions.get(i));
                    predictionsWithData.append(MLUtils.arrayToCsvString(unencodedData.get(i), csvFormat.getDelimiter()));
                    predictionsWithData.deleteCharAt(predictionsWithData.length() - 1);
                    predictionsWithData.append(MLConstants.NEW_LINE);
                }
            }
            return predictionsWithData.toString();
        } catch (IOException e) {
            String msg = "Failed to read the data points for prediction for model [id] " + modelId;
            log.error(msg, e);
            throw new MLModelHandlerException(msg, e);
        } finally {
            try {
                if (dataStream != null && br != null) {
                    dataStream.close();
                    br.close();
                }
            } catch (IOException e) {
                String msg = MLUtils.getErrorMsg(String.format(
                        "Error occurred while closing the streams for model [id] %s of tenant [id] %s and [user] %s.", modelId,
                        tenantId, userName), e);
                log.warn(msg, e);
            }
        }

    }

    public List<?> predict(int tenantId, String userName, long modelId, List<String[]> data)
            throws MLModelHandlerException {

        if (!isValidModelId(tenantId, userName, modelId)) {
            String msg = String.format("Failed to build the model. Invalid model id: %s for tenant: %s and user: %s",
                    modelId, tenantId, userName);
            throw new MLModelHandlerException(msg);
        }

        if (!isValidModelStatus(modelId, tenantId, userName)) {
            String msg = String
                    .format("This model cannot be used for prediction. Status of the model for model id: %s for tenant: %s and user: %s is not 'Complete'",
                            modelId, tenantId, userName);
            throw new MLModelHandlerException(msg);
        }

        MLModel builtModel = retrieveModel(modelId);

        // Validate number of features in predict dataset
        if (builtModel.getNewToOldIndicesList().size() != data.get(0).length) {
            String msg = String.format("Prediction failed from model [id] %s since [number of features of model]" +
                            " %s does not match [number of features in the input data] %s",
                    modelId, builtModel.getFeatures().size(), data.get(0).length);
            throw new MLModelHandlerException(msg);
        }

        // Validate numerical feature type in predict dataset
        for (Feature feature: builtModel.getFeatures()) {
            if (feature.getType().equals(FeatureType.NUMERICAL)) {
                int actualIndex = builtModel.getNewToOldIndicesList().indexOf(feature.getIndex());
                for (String[] dataPoint: data) {
                    if(!NumberUtils.isNumber(dataPoint[actualIndex])) {
                        String msg = String.format("Invalid value: %s for the feature: %s at feature index: %s",
                                dataPoint[actualIndex], feature.getName(), actualIndex);
                        throw new MLModelHandlerException(msg);
                    }
                }
            }
        }

        // predict
        Predictor predictor = new Predictor(modelId, builtModel, data);
        List<?> predictions = predictor.predict();

        return predictions;
    }

    public List<?> predict(int tenantId, String userName, long modelId, String dataFormat, InputStream dataStream,
            double percentile, boolean skipDecoding) throws MLModelHandlerException {
        List<String[]> data = new ArrayList<String[]>();
        CSVFormat csvFormat = DataTypeFactory.getCSVFormat(dataFormat);
        BufferedReader br = new BufferedReader(new InputStreamReader(dataStream, StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] dataRow = line.split(csvFormat.getDelimiter() + "");
                data.add(dataRow);
            }
            return predict(tenantId, userName, modelId, data, percentile, skipDecoding);
        } catch (IOException e) {
            String msg = "Failed to read the data points for prediction for model [id] " + modelId;
            log.error(msg, e);
            throw new MLModelHandlerException(msg, e);
        } finally {
            try {
                dataStream.close();
                br.close();
            } catch (IOException ignore) {
            }
        }

    }

    public String streamingPredict(int tenantId, String userName, long modelId, String dataFormat,
                                   String columnHeader, InputStream dataStream, double percentile, boolean skipDecoding) throws MLModelHandlerException {
        List<String[]> data = new ArrayList<String[]>();
        CSVFormat csvFormat = DataTypeFactory.getCSVFormat(dataFormat);
        MLModel mlModel = retrieveModel(modelId);
        BufferedReader br = new BufferedReader(new InputStreamReader(dataStream, StandardCharsets.UTF_8));
        StringBuilder predictionsWithData = new StringBuilder();
        try {
            String line;
            if((line = br.readLine()) != null && line.split(csvFormat.getDelimiter() + "").length == mlModel.getNewToOldIndicesList().size()) {
                if(columnHeader.equalsIgnoreCase(MLConstants.NO)) {
                    String[] dataRow = line.split(csvFormat.getDelimiter() + "");
                    data.add(dataRow);
                } else {
                    predictionsWithData.append(line).append(MLConstants.NEW_LINE);
                }
                while ((line = br.readLine()) != null) {
                    String[] dataRow = line.split(csvFormat.getDelimiter() + "");
                    data.add(dataRow);
                }
                // cloning unencoded data to append with predictions
                List<String[]> unencodedData = new ArrayList<String[]>(data.size());
                for (String[] item : data) {
                    unencodedData.add(item.clone());
                }
                List<?> predictions = predict(tenantId, userName, modelId, data, percentile, skipDecoding);
                for (int i = 0; i < predictions.size(); i++) {
                    predictionsWithData.append(MLUtils.arrayToCsvString(unencodedData.get(i), csvFormat.getDelimiter()))
                            .append(String.valueOf(predictions.get(i)))
                            .append(MLConstants.NEW_LINE);
                }
            } else {
                int responseVariableIndex = mlModel.getResponseIndex();
                List<Integer> includedFeatureIndices = mlModel.getNewToOldIndicesList();
                List<String[]> unencodedData = new ArrayList<String[]>();
                if(columnHeader.equalsIgnoreCase(MLConstants.NO)) {
                    int count = 0;
                    String[] dataRow = line.split(csvFormat.getDelimiter() + "");
                    unencodedData.add(dataRow.clone());
                    String[] includedFeatureValues = new String[includedFeatureIndices.size()];
                    for (int index : includedFeatureIndices) {
                        includedFeatureValues[count++] = dataRow[index];
                    }
                    data.add(includedFeatureValues);
                } else {
                    predictionsWithData.append(line).append(MLConstants.NEW_LINE);
                }
                while ((line = br.readLine()) != null) {
                    int count = 0;
                    String[] dataRow = line.split(csvFormat.getDelimiter() + "");
                    unencodedData.add(dataRow.clone());
                    String[] includedFeatureValues = new String[includedFeatureIndices.size()];
                    for (int index : includedFeatureIndices) {
                        includedFeatureValues[count++] = dataRow[index];
                    }
                    data.add(includedFeatureValues);
                }

                List<?> predictions = predict(tenantId, userName, modelId, data, percentile, skipDecoding);
                for (int i = 0; i < predictions.size(); i++) {
                    // replace with predicted value
                    unencodedData.get(i)[responseVariableIndex] = String.valueOf(predictions.get(i));
                    predictionsWithData.append(MLUtils.arrayToCsvString(unencodedData.get(i), csvFormat.getDelimiter()));
                    predictionsWithData.deleteCharAt(predictionsWithData.length() - 1);
                    predictionsWithData.append(MLConstants.NEW_LINE);
                }
            }
            return predictionsWithData.toString();
        } catch (IOException e) {
            String msg = "Failed to read the data points for prediction for model [id] " + modelId;
            log.error(msg, e);
            throw new MLModelHandlerException(msg, e);
        } finally {
            try {
                if (dataStream != null && br != null) {
                    dataStream.close();
                    br.close();
                }
            } catch (IOException e) {
                String msg = MLUtils.getErrorMsg(String.format(
                        "Error occurred while closing the streams for model [id] %s of tenant [id] %s and [user] %s.", modelId,
                        tenantId, userName), e);
                log.warn(msg, e);
            }
        }

    }



    public List<?> predict(int tenantId, String userName, long modelId, List<String[]> data, double percentile,
            boolean skipDecoding) throws MLModelHandlerException {

        if (!isValidModelId(tenantId, userName, modelId)) {
            String msg = String.format("Failed to build the model. Invalid model id: %s for tenant: %s and user: %s",
                    modelId, tenantId, userName);
            throw new MLModelHandlerException(msg);
        }

        if (!isValidModelStatus(modelId, tenantId, userName)) {
            String msg = String
                    .format("This model cannot be used for prediction. Status of the model for model id: %s for tenant: %s and user: %s is not 'Complete'",
                            modelId, tenantId, userName);
            throw new MLModelHandlerException(msg);
        }

        if (data.size() == 0) {
            throw new MLModelHandlerException("Predict dataset is empty.");
        }

        MLModel builtModel = retrieveModel(modelId);

        // Validate number of features in predict dataset
        if (builtModel.getNewToOldIndicesList().size() != data.get(0).length) {
            String msg = String.format("Prediction failed from model [id] %s since [number of features of model]" +
                            " %s does not match [number of features in the input data] %s",
                    modelId, builtModel.getFeatures().size(), data.get(0).length);
            throw new MLModelHandlerException(msg);
        }

        // Validate numerical feature type in predict dataset
        for (Feature feature: builtModel.getFeatures()) {
            if (feature.getType().equals(FeatureType.NUMERICAL)) {
                int actualIndex = builtModel.getNewToOldIndicesList().indexOf(feature.getIndex());
                for (String[] dataPoint: data) {
                    if(!NumberUtils.isNumber(dataPoint[actualIndex])) {
                        String msg = String.format("Invalid value: %s for the feature: %s at feature index: %s",
                                dataPoint[actualIndex], feature.getName(), actualIndex);
                        throw new MLModelHandlerException(msg);
                    }
                }
            }
        }

        // predict
        Predictor predictor = new Predictor(modelId, builtModel, data, percentile, skipDecoding);
        List<?> predictions = predictor.predict();

        return predictions;
    }

    public List<?> getProductRecommendations(int tenantId, String userName, long modelId, int userId, int noOfProducts)
            throws MLModelHandlerException {

        MatrixFactorizationModel model = getMatrixFactorizationModel(tenantId, userName, modelId);
        List<?> recommendations = CollaborativeFiltering.recommendProducts(model, userId, noOfProducts);

        log.info(String.format("Recommendations from model [id] %s was successful.", modelId));
        return recommendations;

    }

    public List<?> getUserRecommendations(int tenantId, String userName, long modelId, int productId, int noOfUsers)
            throws MLModelHandlerException {

        MatrixFactorizationModel model = getMatrixFactorizationModel(tenantId, userName, modelId);
        List<?> recommendations = CollaborativeFiltering.recommendUsers(model, productId, noOfUsers);

        log.info(String.format("Recommendations from model [id] %s was successful.", modelId));
        return recommendations;

    }

    private MatrixFactorizationModel getMatrixFactorizationModel(int tenantId, String userName, long modelId)
            throws MLModelHandlerException {
        if (!isValidModelId(tenantId, userName, modelId)) {
            String msg = String.format("Failed to build the model. Invalid model id: %s for tenant: %s and user: %s",
                    modelId, tenantId, userName);
            throw new MLModelHandlerException(msg);
        }

        MLModel builtModel = retrieveModel(modelId);

        //validate if retrieved model is a MatrixFactorizationModel
        if (!(builtModel.getModel() instanceof MLMatrixFactorizationModel)) {
            String msg =
                    String.format("Cannot get recommendations for model [id] %s , since it is not generated from a "
                            + "Recommendation algorithm.", modelId);
            throw new MLModelHandlerException(msg);
        }
        //get recommendations
        MatrixFactorizationModel model = ((MLMatrixFactorizationModel) builtModel.getModel()).getModel();
        return model;
    }

    private void persistModel(long modelId, String modelName, MLModel model) throws MLModelBuilderException {
        try {
            MLStorage storage = databaseService.getModelStorage(modelId);
            if (storage == null) {
                throw new MLModelBuilderException("Invalid model ID: " + modelId);
            }
            String storageType = storage.getType();
            String storageLocation = storage.getLocation();
            String outPath = storageLocation + File.separator + modelName;

            // if this is a deeplearning model, need to set the storage location for writing
            // then the sparkdeeplearning model will use ObjectTreeBinarySerializer to write it to the given directory
            // the DeeplearningModel will be saved as a .bin file
            if (MLConstants.DEEPLEARNING.equalsIgnoreCase(model.getAlgorithmClass())) {
                MLDeeplearningModel mlDeeplearningModel = (MLDeeplearningModel) model.getModel();
                mlDeeplearningModel.setStorageLocation(storageLocation);
                model.setModel(mlDeeplearningModel);

                // Write POJO if it is a Deep Learning model
                // convert model name
                String dlModelName = modelName.replace('.', '_').replace('-', '_');
                File file = new File(storageLocation + "/" + dlModelName + "_dl" + ".java");
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                DeepLearningModel deepLearningModel = mlDeeplearningModel.getDlModel();
                deepLearningModel.toJava(fileOutputStream, false, false);
                fileOutputStream.close();

                MLModel dlModel = new MLModel();
                dlModel.setAlgorithmClass(model.getAlgorithmClass());
                dlModel.setAlgorithmName(model.getAlgorithmName());
                dlModel.setEncodings(model.getEncodings());
                dlModel.setFeatures(model.getFeatures());
                dlModel.setResponseIndex(model.getResponseIndex());
                dlModel.setResponseVariable(model.getResponseVariable());
                dlModel.setNewToOldIndicesList(model.getNewToOldIndicesList());

                // Writing the DL model without Deep Learning logic
                // For prediction with POJO
                MLIOFactory ioFactoryDl = new MLIOFactory(mlProperties);
                MLOutputAdapter outputAdapterDl = ioFactoryDl.getOutputAdapter(storageType + MLConstants.OUT_SUFFIX);
                ByteArrayOutputStream baosDl = new ByteArrayOutputStream();
                ObjectOutputStream oosDl = new ObjectOutputStream(baosDl);
                oosDl.writeObject(dlModel);
                oosDl.flush();
                oosDl.close();
                InputStream isDl = new ByteArrayInputStream(baosDl.toByteArray());
                // adapter will write the model and close the stream.
                outputAdapterDl.write(outPath + "_dl", isDl);
            }

            MLIOFactory ioFactory = new MLIOFactory(mlProperties);
            MLOutputAdapter outputAdapter = ioFactory.getOutputAdapter(storageType + MLConstants.OUT_SUFFIX);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(model);
            oos.flush();
            oos.close();
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            // adapter will write the model and close the stream.
            outputAdapter.write(outPath, is);
            databaseService.updateModelStorage(modelId, storageType, outPath);
            log.info(String.format("Successfully persisted the model [id] %s", modelId));
        } catch (Exception e) {
            throw new MLModelBuilderException("Failed to persist the model [id] " + modelId + ". " + e.getMessage(), e);
        }
    }

    private List<Integer> getNewToOldIndicesList(SortedMap<Integer, String> includedFeatures) {
        List<Integer> indicesList = new ArrayList<Integer>();
        for (int featureIdx : includedFeatures.keySet()) {
            indicesList.add(featureIdx);
        }
        return indicesList;
    }

    public MLModel retrieveModel(long modelId) throws MLModelHandlerException {
        InputStream in = null;
        ObjectInputStream ois = null;
        String storageLocation = null;
        try {
            MLStorage storage = databaseService.getModelStorage(modelId);
            if (storage == null) {
                throw new MLModelHandlerException("Invalid model ID: " + modelId);
            }
            String storageType = storage.getType();
            storageLocation = storage.getLocation();
            MLIOFactory ioFactory = new MLIOFactory(mlProperties);
            MLInputAdapter inputAdapter = ioFactory.getInputAdapter(storageType + MLConstants.IN_SUFFIX);
            in = inputAdapter.read(storageLocation);
            ois = new ObjectInputStream(in);

            // for the DeeplearningModel since the storageLocation is serialized
            // so the ObjectTreeBinarySerializer will get the storageLocation and deserialize
            MLModel model = (MLModel) ois.readObject();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved model");
            }

            return model;
        } catch (Exception e) {
            throw new MLModelHandlerException("Failed to retrieve the model [id] " + modelId, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    /**
     * Publish a ML model to registry.
     *
     * @param tenantId Unique ID of the tenant.
     * @param userName Username of the user.
     * @param modelId  Unique ID of the built ML model
     * @throws InvalidRequestException, MLModelPublisherException, MLModelHandlerException
     */
    public String publishModel(int tenantId, String userName, long modelId, Format mode)
            throws InvalidRequestException, MLModelPublisherException, MLModelHandlerException, MLPmmlExportException {
        InputStream in = null;
        String errorMsg = "Failed to publish the model [id] " + modelId;
        RegistryOutputAdapter registryOutputAdapter = new RegistryOutputAdapter();
        String relativeRegistryPath = null;

        switch (mode) {
            case SERIALIZED:
                try {
                    // read model
                    MLStorage storage = databaseService.getModelStorage(modelId);
                    if (storage == null) {
                        throw new InvalidRequestException("Invalid model [id] " + modelId);
                    }
                    String storageType = storage.getType();
                    String storageLocation = storage.getLocation();
                    MLIOFactory ioFactory = new MLIOFactory(mlProperties);
                    MLInputAdapter inputAdapter = ioFactory.getInputAdapter(storageType + MLConstants.IN_SUFFIX);
                    in = inputAdapter.read(storageLocation);
                    if (in == null) {
                        throw new InvalidRequestException("Invalid model [id] " + modelId);
                    }
                    // create registry path
                    MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
                    String modelName = databaseService.getModel(tenantId, userName, modelId).getName();
                    relativeRegistryPath = "/" + valueHolder.getModelRegistryLocation() + "/" + modelName;
                    // publish to registry
                    registryOutputAdapter.write(relativeRegistryPath, in);
                } catch (DatabaseHandlerException e) {
                    throw new MLModelPublisherException(errorMsg, e);
                } catch (MLInputAdapterException e) {
                    throw new MLModelPublisherException(errorMsg, e);
                } catch (MLOutputAdapterException e) {
                    throw new MLModelPublisherException(errorMsg, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
                break;

            case PMML:
                MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
                try {
                    String modelName = databaseService.getModel(tenantId, userName, modelId).getName();
                    relativeRegistryPath = "/" + valueHolder.getModelRegistryLocation() + "/" + modelName + ".xml";

                    MLModel model = retrieveModel(modelId);
                    String pmmlModel = exportAsPMML(model);
                    InputStream stream = new ByteArrayInputStream(pmmlModel.getBytes(StandardCharsets.UTF_8));
                    registryOutputAdapter.write(relativeRegistryPath, stream);

                } catch (DatabaseHandlerException e) {
                    throw new MLModelPublisherException(errorMsg, e);
                } catch (MLModelHandlerException e) {
                    throw new MLModelHandlerException("Failed to retrieve the model [id] " + modelId, e);
                } catch (MLOutputAdapterException e) {
                    throw new MLModelPublisherException(errorMsg, e);
                } catch (MLPmmlExportException e) {
                    throw new MLPmmlExportException("PMML export not supported for model type");
                }
                break;

            default:
                throw new MLModelPublisherException(errorMsg);
        }

        return RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + relativeRegistryPath;
    }


    public List<ClusterPoint> getClusterPoints(int tenantId, String userName, long datasetId, String featureListString,
                                               int noOfClusters) throws MLMalformedDatasetException, MLModelHandlerException {
        JavaSparkContext sparkContext = null;
        List<String> features = Arrays.asList(featureListString.split("\\s*,\\s*"));

        try {
            List<ClusterPoint> clusterPoints = new ArrayList<ClusterPoint>();

            String datasetURL = databaseService.getDatasetUri(datasetId);
            MLDataset dataset = databaseService.getDataset(tenantId, userName, datasetId);
            String dataSourceType = dataset.getDataSourceType();
            String dataType = dataset.getDataType();
            // java spark context
            sparkContext = MLCoreServiceValueHolder.getInstance().getSparkContext();
            JavaRDD<String> lines;
            // parse lines in the dataset
            lines = extractLines(tenantId, datasetId, sparkContext, datasetURL, dataSourceType, dataType);
            // get column separator
            String columnSeparator = ColumnSeparatorFactory.getColumnSeparator(dataType);
            // get header line
            String headerRow = databaseService.getFeatureNamesInOrder(datasetId, columnSeparator);
            Pattern pattern = MLUtils.getPatternFromDelimiter(columnSeparator);
            // get selected feature indices
            List<Integer> featureIndices = new ArrayList<Integer>();
            for (String feature : features) {
                featureIndices.add(MLUtils.getFeatureIndex(feature, headerRow, columnSeparator));
            }
            JavaRDD<org.apache.spark.mllib.linalg.Vector> featureVectors = null;

            double sampleSize = (double) MLCoreServiceValueHolder.getInstance().getSummaryStatSettings()
                    .getSampleSize();
            double sampleFraction = sampleSize / (lines.count() - 1);
            HeaderFilter headerFilter = new HeaderFilter.Builder().header(headerRow).build();
            LineToTokens lineToTokens = new LineToTokens.Builder().separator(pattern).build();
            MissingValuesFilter missingValuesFilter = new MissingValuesFilter.Builder().build();
            TokensToVectors tokensToVectors = new TokensToVectors.Builder().indices(featureIndices).build();

            // Use entire dataset if number of records is less than or equal to sample fraction
            if (sampleFraction >= 1.0) {
                featureVectors = lines.filter(headerFilter).map(lineToTokens).filter(missingValuesFilter)
                        .map(tokensToVectors);
            }
            // Use ramdomly selected sample fraction of rows if number of records is > sample fraction
            else {
                featureVectors = lines.filter(headerFilter).sample(false, sampleFraction).map(lineToTokens)
                        .filter(missingValuesFilter).map(tokensToVectors);
            }
            KMeans kMeans = new KMeans();
            KMeansModel kMeansModel = kMeans.train(featureVectors, noOfClusters, 100);
            // Populate cluster points list with predicted clusters and features
            List<Tuple2<Integer, org.apache.spark.mllib.linalg.Vector>> kMeansPredictions = kMeansModel
                    .predict(featureVectors).zip(featureVectors).collect();
            for (Tuple2<Integer, org.apache.spark.mllib.linalg.Vector> kMeansPrediction : kMeansPredictions) {
                ClusterPoint clusterPoint = new ClusterPoint();
                clusterPoint.setCluster(kMeansPrediction._1());
                clusterPoint.setFeatures(kMeansPrediction._2().toArray());
                clusterPoints.add(clusterPoint);
            }
            return clusterPoints;
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException("An error occurred while generating cluster points: " + e.getMessage(), e);
        }
    }

    private JavaRDD<String> extractLines(int tenantId, long datasetId, JavaSparkContext sparkContext,
                                         String datasetURL, String dataSourceType, String dataType) throws MLMalformedDatasetException {
        JavaRDD<String> lines;
        if (DatasetType.DAS == DatasetType.getDatasetType(dataSourceType)) {
            try {
                lines = MLUtils.getLinesFromDASTable(datasetURL, tenantId, sparkContext);
            } catch (Exception e) {
                throw new MLMalformedDatasetException("Unable to extract the data from DAS table: " + datasetURL, e);
            }
        } else {
            // parse lines in the dataset
            lines = sparkContext.textFile(datasetURL);
        }
        return lines;
    }

    /**
     * Export a ML model in PMML format.
     *
     * @param model the model to be exported
     * @return PMML model as a String
     * @throws MLPmmlExportException
     */
    public String exportAsPMML(MLModel model) throws MLPmmlExportException {
        Externalizable extModel = model.getModel();

        try {
            if (extModel instanceof PMMLModelContainer) {
                PMMLExportable pmmlExportableModel = ((PMMLModelContainer) extModel).getPMMLExportable();
                String pmmlString = pmmlExportableModel.toPMML();
                try {
                    //temporary fix for appending version
                    String pmmlWithVersion = appendVersionToPMML(pmmlString);
                    // print the model in the log
                    log.info(pmmlWithVersion);
                    return pmmlWithVersion;
                } catch (Exception e) {
                    String msg = "Error while appending version attribute to pmml";
                    log.error(msg, e);
                    throw new MLPmmlExportException(msg);
                }
            } else {
                throw new MLPmmlExportException("PMML export not supported for model type");
            }
        } catch (MLPmmlExportException e) {
            throw new MLPmmlExportException("PMML export not supported for model type");
        }
    }

    /**
     * Append version attribute to pmml (temporary fix)
     *
     * @param pmmlString the pmml string to be appended
     * @return PMML with version as a String
     * @throws MLPmmlExportException
     */
    private String appendVersionToPMML(String pmmlString) throws MLPmmlExportException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        StringWriter stringWriter = null;

        try {
            //convert the string to xml to append the version attribute
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(pmmlString)));
            Element root = document.getDocumentElement();
            root.setAttribute("version", "4.2");

            // convert it back to string
            stringWriter = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            return stringWriter.toString();
        } catch (Exception e) {
            String msg = "Error while appending version attribute to pmml";
            log.error(msg, e);
            throw new MLPmmlExportException(msg);
        } finally {
            try {
                if(stringWriter != null) {
                    stringWriter.close();
                }
            } catch (IOException e) {
                String msg = "Error while closing stringWriter stream resource";
                log.error(msg, e);
                throw new MLPmmlExportException(msg);
            }
        }
    }

    class ModelBuilder implements Runnable {

        private long id;
        private MLModelConfigurationContext ctxt;
        private int tenantId;
        private String tenantDomain;
        private String username;
        private String emailNotificationEndpoint = MLCoreServiceValueHolder.getInstance()
                .getEmailNotificationEndpoint();

        public ModelBuilder(long modelId, MLModelConfigurationContext context) {
            id = modelId;
            ctxt = context;
            CarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            tenantId = carbonContext.getTenantId();
            tenantDomain = carbonContext.getTenantDomain();
            username = carbonContext.getUsername();
        }

        @Override
        public void run() {
            org.wso2.carbon.metrics.manager.Timer timer = MetricManager.timer(Level.INFO,
                    "org.wso2.carbon.ml.model-building-time."+ctxt.getFacts().getAlgorithmName());
            Context context = timer.start();
            String[] emailTemplateParameters = new String[2];
            try {
                long t1 = System.currentTimeMillis();
                emailTemplateParameters[0] = username;
                // Set tenant info in the carbon context
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);

                String algorithmType = ctxt.getFacts().getAlgorithmClass();
                List<Map<String, Integer>> encodings = SparkModelUtils.buildEncodings(ctxt);
                ctxt.setEncodings(encodings);

                // gets the model builder
                MLModelBuilder modelBuilder = ModelBuilderFactory.getModelBuilder(algorithmType, ctxt);
                // pre-process and build the model
                MLModel model = modelBuilder.build();
                log.info(String.format("Successfully built the model [id] %s in %s seconds.", id,
                        (double) (System.currentTimeMillis() - t1) / 1000));

                persistModel(id, ctxt.getModel().getName(), model);

                if (emailNotificationEndpoint != null) {

                    emailTemplateParameters[1] = getLink(ctxt, MLConstants.MODEL_STATUS_COMPLETE);
                    EmailNotificationSender.sendModelBuildingCompleteNotification(emailNotificationEndpoint,
                            emailTemplateParameters);
                }
            } catch (MLInputValidationException e) {
                log.error(String.format("Failed to build the model [id] %s ", id), e);
                try {
                    databaseService.updateModelStatus(id, MLConstants.MODEL_STATUS_FAILED);
                    databaseService.updateModelError(id, e.getMessage() + "\n" + ctxt.getFacts().toString());
                    emailTemplateParameters[1] = getLink(ctxt, MLConstants.MODEL_STATUS_FAILED);
                } catch (DatabaseHandlerException e1) {
                    log.error(String.format("Failed to update the status of model [id] %s ", id), e1);
                }
                EmailNotificationSender.sendModelBuildingFailedNotification(emailNotificationEndpoint,
                        emailTemplateParameters);
            } catch (MLModelBuilderException e) {
                log.error(String.format("Failed to build the model [id] %s ", id), e);
                try {
                    databaseService.updateModelStatus(id, MLConstants.MODEL_STATUS_FAILED);
                    databaseService.updateModelError(id, e.getMessage() + "\n" + ctxt.getFacts().toString());
                    emailTemplateParameters[1] = getLink(ctxt, MLConstants.MODEL_STATUS_FAILED);
                } catch (DatabaseHandlerException e1) {
                    log.error(String.format("Failed to update the status of model [id] %s ", id), e1);
                }
                EmailNotificationSender.sendModelBuildingFailedNotification(emailNotificationEndpoint,
                        emailTemplateParameters);
            } finally {
                context.stop();
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private void handleNull(Object obj, String msg) throws MLModelHandlerException {
        if (obj == null) {
            throw new MLModelHandlerException(msg);
        }
    }

    /**
     * Method to get the link to model build result page
     *
     * @param context ML model configuration context
     * @param status Model building status
     * @return link to model build result page
     */
    private String getLink(MLModelConfigurationContext context, String status) {

        MLModelData mlModelData = context.getModel();
        long modelId = mlModelData.getId();
        String modelName = mlModelData.getName();
        long analysisId = mlModelData.getAnalysisId();
        int tenantId = mlModelData.getTenantId();
        String userName = mlModelData.getUserName();

        MLAnalysis analysis;
        String analysisName;
        MLProject mlProject;
        String projectName;
        long datasetId;
        DatabaseService databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();

        try {
            analysis = databaseService.getAnalysis(tenantId, userName, analysisId);
            analysisName = analysis.getName();
            long projectId = analysis.getProjectId();

            mlProject = databaseService.getProject(tenantId, userName, projectId);
            projectName = mlProject.getName();
            datasetId = mlProject.getDatasetId();
        } catch (DatabaseHandlerException e) {
            log.warn(String.format("Failed to generate link for model [id] %s ", modelId), e);
            return "[Failed to generate link for model ID: " + modelId + "]";
        }

        ConfigurationContextService configContextService = MLCoreServiceValueHolder.getInstance()
                .getConfigurationContextService();
        String mlUrl = configContextService.getServerConfigContext().getProperty("ml.url").toString();
        String link = mlUrl + "/site/analysis/analysis.jag?analysisId=" + analysisId + "&analysisName=" + analysisName + "&datasetId=" + datasetId;
        if(status.equals(MLConstants.MODEL_STATUS_COMPLETE)) {
            link = mlUrl + "/site/analysis/view-model.jag?analysisId=" + analysisId + "&datasetId=" + datasetId + "&modelId=" + modelId + "&projectName=" + projectName + "&" +
                    "analysisName=" + analysisName + "&modelName=" + modelName +"&fromCompare=false";
        }
        return link;
    }


}
