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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.FeatureSummary;
import org.wso2.carbon.ml.commons.domain.MLDataset;
import org.wso2.carbon.ml.commons.domain.MLDatasetVersion;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.commons.domain.ScatterPlotPoints;
import org.wso2.carbon.ml.commons.domain.config.SummaryStatisticsSettings;
import org.wso2.carbon.ml.core.exceptions.MLConfigurationParserException;
import org.wso2.carbon.ml.core.exceptions.MLDataProcessingException;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.exceptions.MLMalformedDatasetException;
import org.wso2.carbon.ml.core.exceptions.MLOutputAdapterException;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.core.utils.ThreadExecutor;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Properties;

/**
 * This object is responsible for reading a data-set using a {@link MLInputAdapter}, extracting meta-data, persist in ML
 * db and store the data stream in a user defined target path.
 */
public class MLDatasetProcessor {
    private static final Log log = LogFactory.getLog(MLDatasetProcessor.class);
    private Properties mlProperties;
    private SummaryStatisticsSettings summaryStatsSettings;
    private ThreadExecutor threadExecutor;
    private DatabaseService databaseService;

    public MLDatasetProcessor() {
        mlProperties = MLCoreServiceValueHolder.getInstance().getMlProperties();
        summaryStatsSettings = MLCoreServiceValueHolder.getInstance().getSummaryStatSettings();
        databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
        threadExecutor = new ThreadExecutor(mlProperties);
    }

    /**
     * Checks whether a given name for a data-set is valid against a given tenant id.
     * 
     * @param tenantId tenant who's uploading the data-set.
     * @param datasetName name of the data-set to be uploaded.
     * @return if it is existed in a different tenant -> return false, if it is existed in the same tenant -> return
     *         true
     */
    public boolean isValidName(int tenantId, String datasetName) {
        // TODO call db and check whether the datasetName is existed in a different tenant
        // if it is existed -> return false
        // if it is existed in the same tenant -> return true
        return false;
    }

    /**
     * Checks whether a given version is valid against the given data-set.
     * 
     * @param tenantId tenant who's requesting the validation.
     * @param datasetName name of the data-set to be uploaded.
     * @param datasetVersion version of the data-set to be uploaded.
     * @return if it is existed -> return false, if it is not -> return true
     */
    public boolean isValidVersion(int tenantId, String datasetName, String datasetVersion) {
        // TODO call db and check whether the same version is existed for this dataset name in this tenant.
        return false;
    }
    
    public List<MLDatasetVersion> getAllDatasetVersions(int tenantId, String userName, long datasetId) throws MLDataProcessingException {
        try {
            return databaseService.getAllVersionsetsOfDataset(tenantId, userName, datasetId);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }
    
    public MLDatasetVersion getVersionset(int tenantId, String userName, long versionsetId) throws MLDataProcessingException {
        try {
            return databaseService.getVersionset(tenantId, userName, versionsetId);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }
    
    public List<MLDataset> getAllDatasets(int tenantId, String userName) throws MLDataProcessingException {
        try {
            return databaseService.getAllDatasets(tenantId, userName);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }
    
    public MLDataset getDataset(int tenantId, String userName, long datasetId) throws MLDataProcessingException {
        try {
            return databaseService.getDataset(tenantId, userName, datasetId);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }
    
    public List<MLDatasetVersion> getAllVersionsetsOfDataset(int tenantId, String userName, long datasetId) throws MLDataProcessingException {
        try {
            return databaseService.getAllVersionsetsOfDataset(tenantId, userName, datasetId);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }
    
    /**
     * FIXME Do we need tenant id and user name??
     * @param datasetVersionId
     * @return
     * @throws MLDataProcessingException
     */
    public long getDatasetId(long datasetVersionId) throws MLDataProcessingException {
        try {
            return databaseService.getDatasetId(datasetVersionId);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }
    
    public List<FeatureSummary> getDefaultFeatures(int tenantId, String userName, long versionsetId, int startIndex, int numberOfFeatures) throws MLDataProcessingException {
        
        long datasetVersionId = getDatasetId(versionsetId);
        try {
            return databaseService.getDefaultFeatures(datasetVersionId, startIndex, numberOfFeatures);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }
    
    public List<Object> getScatterPlotPoints(ScatterPlotPoints scatterPlotPoints) throws MLDataProcessingException {
        try {
            return databaseService.getScatterPlotPoints(scatterPlotPoints);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }
    
    public List<Object> getScatterPlotPointsOfLatestVersion(long datasetId, ScatterPlotPoints scatterPlotPoints) throws MLDataProcessingException {
        try {
            List<MLDatasetVersion> versions = databaseService.getAllVersionsetsOfDataset(scatterPlotPoints.getTenantId(), scatterPlotPoints.getUser(), datasetId);
            long versionsetId = versions.get(versions.size()-1).getId();
            scatterPlotPoints.setVersionsetId(versionsetId);
            return databaseService.getScatterPlotPoints(scatterPlotPoints);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }
    
    public List<Object> getChartSamplePoints(int tenantId, String user, long versionsetId, String featureListString) throws MLDataProcessingException {
        try {
            return databaseService.getChartSamplePoints(tenantId, user, versionsetId, featureListString);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }
    
    public List<Object> getChartSamplePointsOfLatestVersion(int tenantId, String user, long datasetId, String featureListString) throws MLDataProcessingException {
        try {
            List<MLDatasetVersion> versions = databaseService.getAllVersionsetsOfDataset(tenantId, user, datasetId);
            long versionsetId = versions.get(versions.size()-1).getId();
            return databaseService.getChartSamplePoints(tenantId, user, versionsetId, featureListString);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }
    
    public void deleteDataset(String datasetName) {
        //TODO
        
    }
    
    public void deleteDatasetVersion(String datasetVersion) {
        //TODO
        
    }
    
    
    
    /**
     * Process a given data-set; read the data-set as a stream, extract meta-data, persist the data-set in a target path
     * and persist meta-data in ML db.
     * 
     * @param dataset
     */
    public void process(MLDataset dataset) throws MLDataProcessingException {
        MLIOFactory ioFactory = new MLIOFactory(mlProperties);
        MLInputAdapter inputAdapter = ioFactory.getInputAdapter(dataset.getDataSourceType()+ MLConstants.IN_SUFFIX);
        handleNull(inputAdapter, String.format("Invalid data source type: %s [data-set] %s", 
                dataset.getDataSourceType(), dataset.getName()));
        MLOutputAdapter outputAdapter = ioFactory.getOutputAdapter(dataset.getDataTargetType() +MLConstants.OUT_SUFFIX);
        handleNull(outputAdapter, String.format("Invalid data target type: %s [data-set] %s", 
                dataset.getDataTargetType(), dataset.getName()));
        handleNull(dataset.getSourcePath(), String.format("Null data source path provided [data-set] %s", 
                dataset.getName()));
        InputStream input = null;
        URI targetUri = null;
        try {
            // write the data-set to a server side location
            input = inputAdapter.readDataset(dataset.getSourcePath());
            handleNull(input, String.format("Null input stream read from the source data-set path: %s [data-set] %s",
                    dataset.getSourcePath(), dataset.getName()));
            String targetPath = ioFactory.getTargetPath(dataset.getName()+"."+dataset.getTenantId()+"."+System.currentTimeMillis());
            handleNull(targetPath, String.format("Null target path for the [data-set] %s ", dataset.getName()));
            targetUri = outputAdapter.write(targetPath, input);

            // read the file that was written
            inputAdapter = ioFactory.getInputAdapter(dataset.getDataTargetType() + MLConstants.IN_SUFFIX);
            try {
                input = inputAdapter.readDataset(targetUri);
            } catch (MLInputAdapterException e) {
                throw new MLDataProcessingException("Unable to read the data-set file from: " + targetUri.toString(), e);
            }

            // extract sample points
            SamplePoints samplePoints = MLUtils.getSamplePoints(input, dataset.getDataType(),
                    summaryStatsSettings.getSampleSize());

            // persist data-set and data-set version in DB
            persistDataset(dataset);

            long datasetSchemaId = dataset.getId();
            if (log.isDebugEnabled()) {
                log.debug("datasetSchemaId: " + datasetSchemaId);
            }

            String versionsetName = dataset.getName()+"-"+dataset.getVersion();

            // build the MLDatasetVersion
            MLDatasetVersion datasetVersion = MLUtils.getMLDatsetVersion(dataset.getTenantId(), datasetSchemaId, dataset.getUserName(), versionsetName, dataset.getVersion(), targetUri, samplePoints);
            long datasetVersionId = retrieveDatasetVersionId(datasetVersion);
            if (datasetVersionId != -1) {
                // dataset version is already exist
                throw new MLDataProcessingException(String.format("Dataset already exists; data set [name] %s [version] %s", dataset.getName(), dataset.getVersion()));
            }
            persistDatasetVersion(datasetVersion);
            datasetVersionId = retrieveDatasetVersionId(datasetVersion);

            if (log.isDebugEnabled()) {
                log.debug("datasetVersionId: " + datasetVersionId);
            }

            // start summary stats generation in a new thread, pass data set version id
            threadExecutor.execute(new SummaryStatsGenerator(datasetSchemaId, datasetVersionId,  summaryStatsSettings,
                    samplePoints));
            
            log.info(String.format("[Created] %s", dataset));

        } catch (MLInputAdapterException e) {
            throw new MLDataProcessingException(e);
        } catch (MLOutputAdapterException e) {
            throw new MLDataProcessingException(e);
        } catch (MLMalformedDatasetException e) {
            throw new MLDataProcessingException(e);
        } catch (MLConfigurationParserException e) {
            throw new MLDataProcessingException(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error("Failed to close the input stream.", e);
                }
            }
        }
    }

    private void persistDatasetVersion(MLDatasetVersion versionset) throws MLDataProcessingException {
        try {
            databaseService.insertDatasetVersion(versionset);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }

    private long retrieveDatasetVersionId(MLDatasetVersion versionset) {
        long datasetVersionId;
        try {
            datasetVersionId = databaseService.getVersionsetId(versionset.getName(), versionset.getTenantId());
            return datasetVersionId;
        } catch (DatabaseHandlerException e) {
            return -1;
        }
    }

    private void persistDataset(MLDataset dataset) throws MLDataProcessingException {
        try {
            String name = dataset.getName();
            int tenantId = dataset.getTenantId();
            String userName = dataset.getUserName();
            long datasetId = databaseService.getDatasetId(name, tenantId, userName);
            if (datasetId == -1) {
                databaseService.insertDatasetSchema(dataset);
                datasetId = databaseService.getDatasetId(name, tenantId, userName);
            }
            dataset.setId(datasetId);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e);
        }
    }

    private void handleNull(Object obj, String msg) throws MLDataProcessingException {
        if (obj == null) {
            throw new MLDataProcessingException(msg);
        }
    }

}
