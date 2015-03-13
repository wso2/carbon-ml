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
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.MLCustomizedFeature;
import org.wso2.carbon.ml.commons.domain.MLHyperParameter;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.MLModelConfiguration;
import org.wso2.carbon.ml.commons.domain.MLModelNew;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.algorithms.SupervisedModel;
import org.wso2.carbon.ml.core.spark.algorithms.UnsupervisedModel;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.ThreadExecutor;
import org.wso2.carbon.ml.core.utils.MLUtils.ColumnSeparatorFactory;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

/**
 * {@link MLModelHandler} is responsible for handling/delegating all the model related requests.
 */
public class MLModelHandler {
    private static final Log log = LogFactory.getLog(MLModelHandler.class);
    private DatabaseService databaseService;
    private Properties mlProperties;
    private ThreadExecutor threadExecutor;

    public MLModelHandler() {
        databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
        mlProperties = MLCoreServiceValueHolder.getInstance().getMlProperties();
        threadExecutor = new ThreadExecutor(mlProperties);
    }

    /**
     * Create a new model.
     * 
     * @param model model to be created.
     * @throws MLModelHandlerException
     */
    public void createModel(MLModelNew model) throws MLModelHandlerException {
        try {
            int tenantId = model.getTenantId();
            String name = model.getName();
            String userName = model.getUserName();
            long analysisId = model.getAnalysisId();
            long valueSetId = model.getValueSetId();
            databaseService.insertModel(name, analysisId, valueSetId, tenantId, userName);
            log.info(String.format("[Created] %s", model));
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public void deleteModel(int tenantId, String userName, String modelName) throws MLModelHandlerException {
        // TODO 
    }

    public long getModelId(int tenantId, String userName, String modelName) throws MLModelHandlerException {
        try {
            return databaseService.getModelId(tenantId, userName, modelName);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }
    
    public MLModelNew getModel(int tenantId, String userName, String modelName) throws MLModelHandlerException {
        try {
            return databaseService.getModel(tenantId, userName, modelName);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }
    
    public List<MLModelNew> getAllModels(int tenantId, String userName) throws MLModelHandlerException {
        try {
            return databaseService.getAllModels(tenantId, userName);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public boolean isValidModelId(int tenantId, String userName, long modelId) throws MLModelHandlerException {
        try {
            return databaseService.isValidModelId(tenantId, userName, modelId);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public void addCustomizedFeatures(long modelId, List<MLCustomizedFeature> customizedFeatures)
            throws MLModelHandlerException {
        try {
            databaseService.insertFeatureCustomized(modelId, customizedFeatures);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public void addCustomizedFeature(long modelId, MLCustomizedFeature customizedFeature)
            throws MLModelHandlerException {
        try {
            databaseService.insertFeatureCustomized(modelId, customizedFeature);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public void addModelConfigurations(long modelId, List<MLModelConfiguration> modelConfigs)
            throws MLModelHandlerException {
        try {
            databaseService.insertModelConfigurations(modelId, modelConfigs);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public void addHyperParameters(long modelId, List<MLHyperParameter> hyperParameters) throws MLModelHandlerException {
        try {
            databaseService.insertHyperParameters(modelId, hyperParameters);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    /**
     * @param type type of the storage file, hdfs etc.
     * @param location root directory of the file location.
     * @throws MLModelHandlerException
     */
    public void addStorage(long modelId, String type, String location) throws MLModelHandlerException {
        try {
            databaseService.updateModelStorage(modelId, type, location);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
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
    public void buildModel(int tenantId, String userName, long modelId) throws MLModelHandlerException,
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
            // class loader is switched to JavaSparkContext.class's class loader
            Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
            long valuesetId = databaseService.getValueSetIdOfModel(modelId);
            // long datasetVersionId = databaseService.getDatasetVersionId(valuesetId);
            // long datasetId = databaseService.getDatasetId(datasetVersionId);
            String dataType = databaseService.getDataTypeOfModel(modelId);
            String columnSeparator = ColumnSeparatorFactory.getColumnSeparator(dataType);
            String dataUrl = databaseService.getValueSetUri(valuesetId);
            SparkConf sparkConf = MLCoreServiceValueHolder.getInstance().getSparkConf();
            Workflow facts = databaseService.getWorkflow(modelId);

            MLModelConfigurationContext context = new MLModelConfigurationContext();
            context.setModelId(modelId);
            context.setColumnSeparator(columnSeparator);
            context.setFacts(facts);

            JavaSparkContext sparkContext = null;
            sparkConf.setAppName(String.valueOf(modelId));
            // create a new java spark context
            sparkContext = new JavaSparkContext(sparkConf);
            // parse lines in the dataset
            JavaRDD<String> lines = sparkContext.textFile(dataUrl);
            // get header line
            String headerRow = lines.take(1).get(0);
            context.setSparkContext(sparkContext);
            context.setLines(lines);
            context.setHeaderRow(headerRow);

            // build the model asynchronously
            threadExecutor.execute(new ModelBuilder(modelId, context));
            
            log.info(String.format("Build model [id] %s job is successfully submitted to Spark.", modelId));

        } catch (DatabaseHandlerException e) {
            throw new MLModelBuilderException("An error occurred while saving model to database: " + e.getMessage(), e);
        } finally {
            // switch class loader back to thread context class loader
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    private void persistModel(long modelId, MLModel model) throws MLModelBuilderException {
        try {
            Map<String, String> storageMap = databaseService.getModelStorage(modelId);
            String storageType = storageMap.get(MLConstants.STORAGE_TYPE);
            String storageLocation = storageMap.get(MLConstants.STORAGE_LOCATION);
            MLIOFactory ioFactory = new MLIOFactory(mlProperties);
            MLOutputAdapter outputAdapter = ioFactory.getOutputAdapter(storageType + MLConstants.OUT_SUFFIX);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(model);
            oos.flush();
            oos.close();
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            // adapter will write the model and close the stream.
            String outPath = storageLocation + File.separator + modelId;
            outputAdapter.writeDataset(outPath, is);
            databaseService.updateModelStorage(modelId, storageType, outPath);
        } catch (Exception e) {
            throw new MLModelBuilderException("Failed to persist the model [id] " + modelId, e);
        }
    }

    class ModelBuilder implements Runnable {

        private long id;
        private MLModelConfigurationContext ctxt;

        public ModelBuilder(long modelId, MLModelConfigurationContext context) {
            id = modelId;
            ctxt = context;
        }

        @Override
        public void run() {
            try {
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

                persistModel(id, model);
            } catch (Exception e) {
                log.error(String.format("Failed to build the model [id] %s ", id), e);
            }
        }

    }
}
