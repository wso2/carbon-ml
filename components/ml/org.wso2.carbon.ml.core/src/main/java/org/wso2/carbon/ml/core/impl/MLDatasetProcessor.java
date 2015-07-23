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
import org.wso2.carbon.ml.commons.domain.MLDataset;
import org.wso2.carbon.ml.commons.domain.MLDatasetVersion;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.commons.domain.ScatterPlotPoints;
import org.wso2.carbon.ml.commons.domain.config.SummaryStatisticsSettings;
import org.wso2.carbon.ml.core.exceptions.MLConfigurationParserException;
import org.wso2.carbon.ml.core.exceptions.MLDataProcessingException;
import org.wso2.carbon.ml.core.exceptions.MLInputValidationException;
import org.wso2.carbon.ml.core.factories.DatasetProcessorFactory;
import org.wso2.carbon.ml.core.interfaces.DatasetProcessor;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.core.utils.ThreadExecutor;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

import java.io.InputStream;
import java.util.*;

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

    public List<MLDatasetVersion> getAllDatasetVersions(int tenantId, String userName, long datasetId)
            throws MLDataProcessingException {
        try {
            return databaseService.getAllVersionsetsOfDataset(tenantId, userName, datasetId);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }

    public MLDatasetVersion getVersionset(int tenantId, String userName, long versionsetId)
            throws MLDataProcessingException {
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

    public List<MLDatasetVersion> getAllVersionsetsOfDataset(int tenantId, String userName, long datasetId)
            throws MLDataProcessingException {
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

    public List<Object> getScatterPlotPointsOfLatestVersion(long datasetId, ScatterPlotPoints scatterPlotPoints)
            throws MLDataProcessingException {
        try {
            List<MLDatasetVersion> versions = databaseService.getAllVersionsetsOfDataset(
                    scatterPlotPoints.getTenantId(), scatterPlotPoints.getUser(), datasetId);
            long versionsetId = versions.get(versions.size() - 1).getId();
            scatterPlotPoints.setVersionsetId(versionsetId);
            return databaseService.getScatterPlotPoints(scatterPlotPoints);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }

    public List<Object> getChartSamplePoints(int tenantId, String user, long versionsetId, String featureListString)
            throws MLDataProcessingException {
        try {
            return databaseService.getChartSamplePoints(tenantId, user, versionsetId, featureListString);
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }

    public List<Object> getChartSamplePointsOfLatestVersion(int tenantId, String user, long datasetId,
            String featureListString) throws MLDataProcessingException {
        try {
            List<MLDatasetVersion> versions = databaseService.getAllVersionsetsOfDataset(tenantId, user, datasetId);
            long versionsetId = versions.get(versions.size() - 1).getId();
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
     * @throws MLInputValidationException
     */
    public void process(MLDataset dataset, InputStream inputStream) throws MLDataProcessingException,
            MLInputValidationException {
        try {
            DatasetProcessor datasetProcessor = DatasetProcessorFactory.buildDatasetProcessor(dataset, inputStream);
            datasetProcessor.process();
            String targetPath = datasetProcessor.getTargetPath();
            SamplePoints samplePoints = datasetProcessor.getSamplePoints();
            // persist dataset
            persistDataset(dataset);

            long datasetSchemaId = dataset.getId();

            List<String> featureNames = retreiveFeatureNames(datasetSchemaId);

            // If size is zero, then it is the first version of the dataset
            if(featureNames.size() != 0){
                // Validate number of features
                if(samplePoints.getHeader().size() != featureNames.size()){
                    String msg = String.format("Creating dataset version failed because number of features[%s] in" +
                            " the dataset version does not match the number of features[%s] in the original" +
                            " dataset.", samplePoints.getHeader().size(), featureNames.size());
                    throw new MLDataProcessingException(msg);
                }

                // Validate feature names
                for(int i=0; i<featureNames.size(); i++){
                    // Since header is a HashMap and it is not ordered, need to get keys by values(ordered)
                    String headerEntry = getKeyByValue(samplePoints.getHeader(), i);
                    if(!featureNames.get(i).equals(headerEntry)){
                        String msg = String.format("Creating dataset version failed because Feature name: %s in" +
                                " the dataset version does not match the feature name: %s in the original" +
                                " dataset.", headerEntry, featureNames.get(i));
                        throw new MLDataProcessingException(msg);
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("datasetSchemaId: " + datasetSchemaId);
            }
            String versionsetName = dataset.getName() + "-" + dataset.getVersion();

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

            // Persist dataset version
            persistDatasetVersion(datasetVersion);
            datasetVersionId = retrieveDatasetVersionId(datasetVersion);

            if (log.isDebugEnabled()) {
                log.debug("datasetVersionId: " + datasetVersionId);
            }

            // start summary stats generation in a new thread, pass data set version id
            threadExecutor.execute(new SummaryStatsGenerator(datasetSchemaId, datasetVersionId, summaryStatsSettings,
                    samplePoints));
            log.info(String.format("[Created] %s", dataset));
        } catch (MLConfigurationParserException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }

    private String getKeyByValue(Map<String, Integer> hashMap, int value) {
        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private List<String> retreiveFeatureNames(long datasetId) throws MLDataProcessingException {
        List<String> featureNames;
        try {
            featureNames = databaseService.getFeatureNames(datasetId);
            return featureNames;
        } catch (DatabaseHandlerException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
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
            log.info(String.format("[Deleted] [dataset version] %s of [user] %s of [tenant] %s", versionsetId,
                    userName, tenantId));
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
