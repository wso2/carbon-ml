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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.*;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.MLModelNew;
import org.wso2.carbon.ml.commons.domain.MLStorage;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.commons.domain.config.ModelStorage;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.exceptions.MLModelPublisherException;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.algorithms.KMeans;
import org.wso2.carbon.ml.core.spark.algorithms.SupervisedModel;
import org.wso2.carbon.ml.core.spark.algorithms.UnsupervisedModel;
import org.wso2.carbon.ml.core.spark.transformations.HeaderFilter;
import org.wso2.carbon.ml.core.spark.transformations.LineToTokens;
import org.wso2.carbon.ml.core.spark.transformations.MissingValuesFilter;
import org.wso2.carbon.ml.core.spark.transformations.RowsToLines;
import org.wso2.carbon.ml.core.spark.transformations.TokensToVectors;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.core.utils.ThreadExecutor;
import org.wso2.carbon.ml.core.utils.MLUtils.ColumnSeparatorFactory;
import org.wso2.carbon.ml.core.utils.MLUtils.DataTypeFactory;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

import scala.Tuple2;

/**
 * {@link MLModelHandler} is responsible for handling/delegating all the model related requests.
 */
public class MLModelHandler {
    private static final Log log = LogFactory.getLog(MLModelHandler.class);
    private DatabaseService databaseService;
    private Properties mlProperties;
    private ThreadExecutor threadExecutor;

    public MLModelHandler() {
        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        databaseService = valueHolder.getDatabaseService();
        mlProperties = valueHolder.getMlProperties();
        threadExecutor = new ThreadExecutor(mlProperties);
    }

    /**
     * Create a new model.
     * 
     * @param model model to be created.
     * @throws MLModelHandlerException
     */
    public MLModelNew createModel(MLModelNew model) throws MLModelHandlerException {
        try {
            // set the model storage configurations
            ModelStorage modelStorage = MLCoreServiceValueHolder.getInstance().getModelStorage();
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

    public MLModelNew getModel(int tenantId, String userName, String modelName) throws MLModelHandlerException {
        try {
            return databaseService.getModel(tenantId, userName, modelName);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e.getMessage(), e);
        }
    }

    public List<MLModelNew> getAllModels(int tenantId, String userName) throws MLModelHandlerException {
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

    /**
     * @param type      type of the storage file, hdfs etc.
     * @param location  root directory of the file location.
     * @throws          MLModelHandlerException
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
     * @param modelId   ID of the model
     * @return          Model Summary
     * @throws          MLModelHandlerException
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
     * @param storageType type of the storage bam, hdfs, file. Default storage is file.
     * @param StoragePath path of the provided storage where the model should be saved.
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
        

        /**
         * Spark looks for various configuration files using thread context class loader. Therefore, the class loader
         * needs to be switched temporarily.
         */
        // assign current thread context class loader to a variable
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            MLModelNew model = databaseService.getModel(tenantId, userName, modelId);
            // class loader is switched to JavaSparkContext.class's class loader
            Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
            long datasetVersionId = databaseService.getDatasetVersionIdOfModel(modelId);
            long datasetId = databaseService.getDatasetId(datasetVersionId);
            MLDataset dataset = databaseService.getDataset(tenantId, userName, datasetId);
            String dataSourceType = dataset.getDataSourceType();
            String dataType = databaseService.getDataTypeOfModel(modelId);
            String columnSeparator = ColumnSeparatorFactory.getColumnSeparator(dataType);
            String dataUrl = databaseService.getDatasetVersionUri(datasetVersionId);
            handleNull(dataUrl, "Target path is null for dataset version [id]: " + datasetVersionId);
            SparkConf sparkConf = MLCoreServiceValueHolder.getInstance().getSparkConf();
            Workflow facts = databaseService.getWorkflow(model.getAnalysisId());
            Map<String, String> summaryStatsOfFeatures = databaseService.getSummaryStats(datasetVersionId);

            JavaRDD<String> lines;

            MLModelConfigurationContext context = new MLModelConfigurationContext();
            context.setModelId(modelId);
            context.setColumnSeparator(columnSeparator);
            context.setFacts(facts);
            context.setModel(model);
            context.setSummaryStatsOfFeatures(summaryStatsOfFeatures);
            int responseIndex = MLUtils.getFeatureIndex(facts.getResponseVariable(), facts.getFeatures());
            context.setIncludedFeaturesMap(MLUtils.getIncludedFeatures(facts, responseIndex));
            context.setNewToOldIndicesList(getNewToOldIndicesList(context.getIncludedFeaturesMap()));
            context.setResponseIndex(responseIndex);

            JavaSparkContext sparkContext = null;
            sparkConf.setAppName(String.valueOf(modelId));
            // create a new java spark context
            sparkContext = new JavaSparkContext(sparkConf);

            if (MLConstants.DATASET_SOURCE_TYPE_BAM.equalsIgnoreCase(dataSourceType)) {
                CSVFormat dataFormat = DataTypeFactory.getCSVFormat(dataType);
                String tableName = MLUtils.extractTableName(dataUrl);
                String tableSchema = MLUtils.extractTableSchema(dataUrl);
                SQLContext sqlCtx = new SQLContext(sparkContext);
                sqlCtx.sql("CREATE TEMPORARY TABLE ML_MODEL_REF USING org.wso2.carbon.analytics.spark.core.util.AnalyticsRelationProvider "
                        + "OPTIONS ("
                        + "tenantId \""
                        + tenantId
                        + "\", "
                        + "tableName \""
                        + tableName
                        + "\", "
                        + "schema \"" + tableSchema + "\"" + ")");

                DataFrame dataFrame = sqlCtx.sql("select * from ML_MODEL_REF");
                JavaRDD<Row> rows = dataFrame.javaRDD();
                lines = rows.map(new RowsToLines(dataFormat.getDelimiter() + ""));
            } else {
                // parse lines in the dataset
                lines = sparkContext.textFile(dataUrl);
            }
            
            // get header line
            String headerRow = databaseService.getFeatureNamesInOrderUsingDatasetVersion(datasetVersionId, columnSeparator);
            context.setSparkContext(sparkContext);
            context.setLines(lines);
            context.setHeaderRow(headerRow);

            // build the model asynchronously
            threadExecutor.execute(new ModelBuilder(modelId, context));

            databaseService.updateModelStatus(modelId, MLConstants.MODEL_STATUS_IN_PROGRESS);
            log.info(String.format("Build model [id] %s job is successfully submitted to Spark.", modelId));

            return facts;
        } catch (DatabaseHandlerException e) {
            throw new MLModelBuilderException("An error occurred while saving model to database: " + e.getMessage(), e);
        } finally {
            // switch class loader back to thread context class loader
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }
    
    public List<?> predict(int tenantId, String userName, long modelId, List<String[]> data) throws MLModelHandlerException,
            MLModelBuilderException {

        if (!isValidModelId(tenantId, userName, modelId)) {
            String msg = String.format("Failed to build the model. Invalid model id: %s for tenant: %s and user: %s",
                    modelId, tenantId, userName);
            throw new MLModelHandlerException(msg);
        }

        MLModel builtModel = retrieveModel(modelId);
        // predict
        Predictor predictor = new Predictor(modelId, builtModel, data);
        List<?> predictions = predictor.predict();

        log.info(String.format("Prediction from model [id] %s was successful.", modelId));
        return predictions;
    }

    public List<?> predict(int tenantId, String userName, long modelId, String[] data) throws MLModelHandlerException,
            MLModelBuilderException {

        List<String[]> dataToBePredicted = new ArrayList<String[]>();
        dataToBePredicted.add(data);
        //predict
        return predict(tenantId, userName, modelId, dataToBePredicted);

    }

    private void persistModel(long modelId, String modelName, MLModel model) throws MLModelBuilderException {
        try {
            MLStorage storage = databaseService.getModelStorage(modelId);
            String storageType = storage.getType();
            String storageLocation = storage.getLocation();
            
            MLIOFactory ioFactory = new MLIOFactory(mlProperties);
            MLOutputAdapter outputAdapter = ioFactory.getOutputAdapter(storageType + MLConstants.OUT_SUFFIX);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(model);
            oos.flush();
            oos.close();
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            // adapter will write the model and close the stream.
            String outPath = storageLocation + File.separator + modelName;
            outputAdapter.write(outPath, is);
            databaseService.updateModelStorage(modelId, storageType, outPath);
        } catch (Exception e) {
            throw new MLModelBuilderException("Failed to persist the model [id] " + modelId, e);
        }
    }
    
    private List<Integer> getNewToOldIndicesList(SortedMap<Integer, String> includedFeatures) {
        List<Integer> indicesList = new ArrayList<Integer>();
        for (int featureIdx : includedFeatures.keySet()) {
            indicesList.add(featureIdx);
        }
        return indicesList;
    }
    
    public MLModel retrieveModel(long modelId) throws MLModelBuilderException {
        InputStream in = null;
        ObjectInputStream ois = null;
        try {
            MLStorage storage = databaseService.getModelStorage(modelId);
            String storageType = storage.getType();
            String storageLocation = storage.getLocation();
            MLIOFactory ioFactory = new MLIOFactory(mlProperties);
            MLInputAdapter inputAdapter = ioFactory.getInputAdapter(storageType + MLConstants.IN_SUFFIX);
            in = inputAdapter.read(new URI(storageLocation));
            ois = new ObjectInputStream(in);
            return (MLModel) ois.readObject();
            
        } catch (Exception e) {
            throw new MLModelBuilderException("Failed to retrieve the model [id] " + modelId, e);
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
     * @param tenantId    Unique ID of the tenant.
     * @param userName    Username of the user.
     * @param modelId     Unique ID of the built ML model.
     * @throws MLModelPublisherException
     */
    public void publishModel(int tenantId, String userName, long modelId) throws MLModelPublisherException {
        InputStream in = null;
        try {
            // read model
            MLStorage storage = databaseService.getModelStorage(modelId);
            String storageType = storage.getType();
            String storageLocation = storage.getLocation();
            MLIOFactory ioFactory = new MLIOFactory(mlProperties);
            MLInputAdapter inputAdapter = ioFactory.getInputAdapter(storageType + MLConstants.IN_SUFFIX);
            in = inputAdapter.read(new URI(storageLocation));
            if (in == null) {
                throw new MLModelPublisherException("Invalid model [id] " + modelId);
            }
            // create registry path
            MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
            String modelName = databaseService.getModel(tenantId, userName, modelId).getName();
            String registryPath = File.separator + valueHolder.getModelRegistryLocation() + File.separator + modelName;
            // publish to registry
            RegistryOutputAdapter registryOutputAdapter = new RegistryOutputAdapter();
            registryOutputAdapter.write(registryPath, in);

        } catch (Exception e) {
            throw new MLModelPublisherException("Failed to publish the model [id] " + modelId, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public List<ClusterPoint> getClusterPoints(int tenantId, String userName, long datasetId, String featureListString, int noOfClusters)
            throws DatabaseHandlerException {
        // assign current thread context class loader to a variable
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        JavaSparkContext sparkContext = null;
        List<String> features = Arrays.asList(featureListString.split("\\s*,\\s*"));

        try {
            List<ClusterPoint> clusterPoints = new ArrayList<ClusterPoint>();

            String datasetURL = databaseService.getDatasetUri(datasetId);
            // class loader is switched to JavaSparkContext.class's class loader
            Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
            // create a new spark configuration
            SparkConf sparkConf = MLCoreServiceValueHolder.getInstance().getSparkConf();
			// set app name
            sparkConf.setAppName(String.valueOf(datasetId));
            // create a new java spark context
            sparkContext = new JavaSparkContext(sparkConf);
            // parse lines in the dataset
            JavaRDD<String> lines = sparkContext.textFile(datasetURL);
            // get column separator
            String columnSeparator = ColumnSeparatorFactory.getColumnSeparator(datasetURL);
            // get header line
            String headerRow = databaseService.getFeatureNamesInOrder(datasetId, columnSeparator);
            Pattern pattern = Pattern.compile(columnSeparator);
            // get selected feature indices
            List<Integer> featureIndices = new ArrayList<Integer>();
            for (String feature : features) {
                featureIndices.add(MLUtils.getFeatureIndex(feature, headerRow, columnSeparator));
            }
            JavaRDD<org.apache.spark.mllib.linalg.Vector> featureVectors = null;

            double sampleSize = (double) MLCoreServiceValueHolder.getInstance().getSummaryStatSettings().getSampleSize();
            double sampleFraction = sampleSize / (lines.count() - 1);
            // Use entire dataset if number of records is less than or equal to sample fraction
            if (sampleFraction >= 1.0) {
                featureVectors = lines.filter(new HeaderFilter(headerRow)).map(new LineToTokens(pattern))
                        .filter(new MissingValuesFilter())
                        .map(new TokensToVectors(featureIndices));
            }
            // Use ramdomly selected sample fraction of rows if number of records is > sample fraction
            else {
                featureVectors = lines.filter(new HeaderFilter(headerRow))
                        .sample(false, sampleFraction).map(new LineToTokens(pattern))
                        .filter(new MissingValuesFilter())
                        .map(new TokensToVectors(featureIndices));
            }
            KMeans kMeans = new KMeans();
            KMeansModel kMeansModel = kMeans.train(featureVectors, noOfClusters, 100);
            // Populate cluster points list with predicted clusters and features
            List<Tuple2<Integer, org.apache.spark.mllib.linalg.Vector>> kMeansPredictions = kMeansModel.predict(featureVectors).zip(featureVectors)
                    .collect();
            for (Tuple2<Integer, org.apache.spark.mllib.linalg.Vector> kMeansPrediction : kMeansPredictions) {
                ClusterPoint clusterPoint = new ClusterPoint();
                clusterPoint.setCluster(kMeansPrediction._1());
                clusterPoint.setFeatures(kMeansPrediction._2().toArray());
                clusterPoints.add(clusterPoint);
            }
            return clusterPoints;
        } catch (DatabaseHandlerException e) {
            throw new DatabaseHandlerException("An error occurred while generating cluster points: " + e.getMessage(), e);
        } finally {
            if (sparkContext != null) {
                sparkContext.close();
            }
            // switch class loader back to thread context class loader
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    class ModelBuilder implements Runnable {

        private long id;
        private MLModelConfigurationContext ctxt;
        private int tenantId;
        private String tenantDomain;
        private String username;
        private String emailNotificationEndpoint = MLCoreServiceValueHolder.getInstance().getEmailNotificationEndpoint();

        public ModelBuilder(long modelId, MLModelConfigurationContext context) {
            id = modelId;
            ctxt = context;
            CarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            tenantId = carbonContext.getTenantId();
            tenantDomain = carbonContext.getTenantDomain();
            username  = carbonContext.getUsername();
        }

        @Override
        public void run() {
            String[] emailTemplateParameters = {username};
            try {
                //Set tenant info in the carbon context
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                
                // class loader is switched to JavaSparkContext.class's class loader
                Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());

                String algorithmType = ctxt.getFacts().getAlgorithmClass();
                MLModel model;
                if (MLConstants.CLASSIFICATION.equals(algorithmType)
                        || MLConstants.NUMERICAL_PREDICTION.equals(algorithmType)) {
                    SupervisedModel supervisedModel = new SupervisedModel();
                    model = supervisedModel.buildModel(ctxt);
                } else if (MLConstants.CLUSTERING.equals((algorithmType))) {
                    UnsupervisedModel unsupervisedModel = new UnsupervisedModel();
                    model = unsupervisedModel.buildModel(ctxt);
                } else {
                    throw new MLModelBuilderException(String.format(
                            "Failed to build the model [id] %s . Invalid algorithm type: %s", id, algorithmType));
                }
                persistModel(id, ctxt.getModel().getName(), model);
                EmailNotificationSender.sendModelBuildingCompleteNotification(emailNotificationEndpoint, emailTemplateParameters);
            } catch (Exception e) {
                log.error(String.format("Failed to build the model [id] %s ", id), e);
                try {
                    databaseService.updateModelStatus(id, MLConstants.MODEL_STATUS_FAILED);
                    databaseService.updateModelError(id, e.getMessage()+"\n"+ctxt.getFacts().toString());
                } catch (DatabaseHandlerException e1) {
                    log.error(String.format("Failed to update the status of model [id] %s ", id), e);
                }
                EmailNotificationSender.sendModelBuildingFailedNotification(emailNotificationEndpoint, emailTemplateParameters);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }
    
    private void handleNull(Object obj, String msg) throws MLModelHandlerException {
        if (obj == null) {
            throw new MLModelHandlerException(msg);
        }
    }
}
