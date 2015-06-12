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

    public List<MLDatasetVersion> getAllDatasetVersions(int tenantId, String userName, long datasetId) throws MLDataProcessingException {
        try {
            return databaseService.getAllVersionsetsOfDataset(tenantId, userName, datasetId);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }
    
    public MLDatasetVersion getVersionset(int tenantId, String userName, long versionsetId) throws MLDataProcessingException {
        try {
            return databaseService.getVersionset(tenantId, userName, versionsetId);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }
    
    public MLDatasetVersion getVersionSetWithVersion(int tenantId, String userName, long datasetId, String version)
            throws MLDataProcessingException {
        try {
            return databaseService.getVersionSetWithVersion(datasetId, version, tenantId, userName);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }
    
    public List<MLDataset> getAllDatasets(int tenantId, String userName) throws MLDataProcessingException {
        try {
            return databaseService.getAllDatasets(tenantId, userName);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }
    
    public MLDataset getDataset(int tenantId, String userName, long datasetId) throws MLDataProcessingException {
        try {
            return databaseService.getDataset(tenantId, userName, datasetId);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }
    
    public List<MLDatasetVersion> getAllVersionsetsOfDataset(int tenantId, String userName, long datasetId) throws MLDataProcessingException {
        try {
            return databaseService.getAllVersionsetsOfDataset(tenantId, userName, datasetId);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }
    
    public List<Object> getScatterPlotPoints(ScatterPlotPoints scatterPlotPoints) throws MLDataProcessingException {
        try {
            return databaseService.getScatterPlotPoints(scatterPlotPoints);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }
    
    public List<Object> getScatterPlotPointsOfLatestVersion(long datasetId, ScatterPlotPoints scatterPlotPoints) throws MLDataProcessingException {
        try {
            List<MLDatasetVersion> versions = databaseService.getAllVersionsetsOfDataset(scatterPlotPoints.getTenantId(), scatterPlotPoints.getUser(), datasetId);
            long versionsetId = versions.get(versions.size()-1).getId();
            scatterPlotPoints.setVersionsetId(versionsetId);
            return databaseService.getScatterPlotPoints(scatterPlotPoints);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }
    
    public List<Object> getChartSamplePoints(int tenantId, String user, long versionsetId, String featureListString) throws MLDataProcessingException {
        try {
            return databaseService.getChartSamplePoints(tenantId, user, versionsetId, featureListString);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }
    
    public List<Object> getChartSamplePointsOfLatestVersion(int tenantId, String user, long datasetId, String featureListString) throws MLDataProcessingException {
        try {
            List<MLDatasetVersion> versions = databaseService.getAllVersionsetsOfDataset(tenantId, user, datasetId);
            long versionsetId = versions.get(versions.size()-1).getId();
            return databaseService.getChartSamplePoints(tenantId, user, versionsetId, featureListString);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }
    
    /**
     * Process a given data-set; read the data-set as a stream, extract meta-data, persist the data-set in a target path
     * and persist meta-data in ML db.
     * 
     * @param dataset
     */
    public void process(MLDataset dataset, InputStream inputStream) throws MLDataProcessingException {
        MLIOFactory ioFactory = new MLIOFactory(mlProperties);
        MLInputAdapter inputAdapter = ioFactory.getInputAdapter(dataset.getDataSourceType() + MLConstants.IN_SUFFIX);
        MLOutputAdapter outputAdapter = ioFactory.getOutputAdapter(dataset.getDataTargetType() + MLConstants.OUT_SUFFIX);
        InputStream input = null;
        URI targetUri = null;
        String targetPath = null;
        SamplePoints samplePoints = null;
        try {
            //TODO introduce proper inheritance.
            // write the data-set to a server side location
            if (MLConstants.DATASET_SOURCE_TYPE_FILE.equalsIgnoreCase(dataset.getDataSourceType())) {
                // if the source is a file, read the inputstream
                input = inputStream;
            } else if (MLConstants.DATASET_SOURCE_TYPE_BAM.equalsIgnoreCase(dataset.getDataSourceType())) {
                handleNull(dataset.getSourcePath(), String.format("Invalid data source path: %s [data-set] %s", 
                        dataset.getSourcePath(), dataset.getName()));
                // in BAM case, we do not persist the data into a server side file.
                String sourcePath = dataset.getSourcePath().toString();
                if (!sourcePath.contains(":")) {
                    throw new MLDataProcessingException(String.format("Invalid data source path %s for [data set name] %s [version] %s", sourcePath, 
                            dataset.getName(), dataset.getVersion()));
                }
                
            } else {
                handleNull(dataset.getSourcePath(), String.format("Invalid data source path: %s [data-set] %s", 
                        dataset.getSourcePath(), dataset.getName()));
                // if the source is hdfs/bam read from the source path
                input = inputAdapter.read(new URI(dataset.getSourcePath()));
            }
            
            if (!MLConstants.DATASET_SOURCE_TYPE_BAM.equalsIgnoreCase(dataset.getDataSourceType())) {
                
                handleNull(input, String.format("Null input stream read from the source data-set path: %s [data-set] %s",
                        dataset.getSourcePath(), dataset.getName()));
                targetPath = ioFactory.getTargetPath(dataset.getName()+"."+dataset.getTenantId()+"."+System.currentTimeMillis());
                handleNull(targetPath, String.format("Null target path for the [data-set] %s ", dataset.getName()));
                targetUri = outputAdapter.write(targetPath, input);
                // extract sample points
                samplePoints = MLUtils.getSample(targetUri.toString(), dataset.getDataType(),
                        summaryStatsSettings.getSampleSize(), dataset.isContainsHeader(), dataset.getDataSourceType(), dataset.getTenantId());
                
            } else {
                targetPath = dataset.getSourcePath();
                // extract sample points
                samplePoints = MLUtils.getSample(dataset.getSourcePath(), "csv",
                        summaryStatsSettings.getSampleSize(), false, dataset.getDataSourceType(), dataset.getTenantId());
            }


            // persist data-set and data-set version in DB
            persistDataset(dataset);

            long datasetSchemaId = dataset.getId();
            if (log.isDebugEnabled()) {
                log.debug("datasetSchemaId: " + datasetSchemaId);
            }
            String versionsetName = dataset.getName()+"-"+dataset.getVersion();

            // build the MLDatasetVersion
            MLDatasetVersion datasetVersion = MLUtils.getMLDatsetVersion(dataset.getTenantId(), datasetSchemaId,
                    dataset.getUserName(), versionsetName, dataset.getVersion(), targetPath, samplePoints);
            
            long datasetVersionId = retrieveDatasetVersionId(datasetVersion);
            if (datasetVersionId != -1) {
                // dataset version is already exist
                throw new MLDataProcessingException(String.format(
                        "Dataset already exists; data set [name] %s [version] %s", dataset.getName(),
                        dataset.getVersion()));
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
        } catch (Exception e) {
            throw new MLDataProcessingException(e.getMessage(), e);
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
            throw new MLDataProcessingException(e.getMessage(), e);
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
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }

    private void handleNull(Object obj, String msg) throws MLDataProcessingException {
        if (obj == null) {
            throw new MLDataProcessingException(msg);
        }
    }

    public void deleteDataset(int tenantId, String userName, long datasetId) throws MLDataProcessingException {
        try {
            databaseService.deleteDataset(datasetId);
            log.info(String.format("[Deleted] [dataset] %s of [user] %s of [tenant] %s", datasetId, userName, tenantId));
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }

    public void deleteDatasetVersion(int tenantId, String userName, long versionsetId) throws MLDataProcessingException {
        try {
            databaseService.deleteDatasetVersion(versionsetId);
            log.info(String.format("[Deleted] [dataset version] %s of [user] %s of [tenant] %s", versionsetId, userName, tenantId));
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }

    public List<String> getFeatureNames(long datasetId, String featureType) throws MLDataProcessingException {
        try {
            return databaseService.getFeatureNames(datasetId, featureType);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }

    public String getSummaryStats(long datasetId, String featureName) throws MLDataProcessingException {
        try {
            return databaseService.getSummaryStats(datasetId, featureName);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }

}
