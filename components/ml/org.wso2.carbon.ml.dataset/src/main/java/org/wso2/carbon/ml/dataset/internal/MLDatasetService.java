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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.dataset.internal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.dto.FeatureSummary;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.dataset.DatasetService;
import org.wso2.carbon.ml.dataset.exceptions.DatasetServiceException;
import org.wso2.carbon.ml.dataset.exceptions.DatasetSummaryException;
import org.wso2.carbon.ml.dataset.internal.constants.DatasetConfigurations;
import org.wso2.carbon.ml.dataset.internal.dto.DataUploadSettings;
import org.wso2.carbon.ml.dataset.internal.dto.SummaryStatisticsSettings;

import java.io.*;
import java.util.List;

/**
 * Class contains the services related to importing and exploring a data-set.
 *
 * @scr.component name="mlDatasetService" immediate="true"
 * @scr.reference name="databaseService"
 * interface="org.wso2.carbon.ml.database.DatabaseService" cardinality="1..1"
 * policy="dynamic" bind="setDatabaseService" unbind="unsetDatabaseService"
 */
public class MLDatasetService implements DatasetService {
    private static final Log logger = LogFactory.getLog(MLDatasetService.class);
    private DataUploadSettings dataUploadSettings;
    private SummaryStatisticsSettings summaryStatSettings;
    private String mlDatabaseName;

    /*
     * Activates the Data-set Service.
     */
    protected void activate(ComponentContext context) throws DatasetServiceException {
        try {
            // Read data-set settings from ml-config.xml file.
            MLConfigurationParser mlConfigurationParser = new MLConfigurationParser();
            MLDatasetService datasetService = new MLDatasetService();
            datasetService.dataUploadSettings = mlConfigurationParser.getDataUploadSettings();
            datasetService.summaryStatSettings = mlConfigurationParser.getSummaryStatisticsSettings();
            datasetService.mlDatabaseName=mlConfigurationParser.getDatabaseName();
            // Register the service.
            context.getBundleContext().registerService(DatasetService.class.getName(),
                datasetService, null);
            logger.info("ML Dataset Service Started");
        } catch (Exception e) {
            throw new DatasetServiceException("Failed to activate the ML Dataset Service component: "
                + e.getMessage(), e);
        }
    }

    /*
     * Deactivates the Data-set Service.
     */
    protected void deactivate(ComponentContext context) {
        logger.info("Ml Dataset Service Stopped");
    }

    protected void setDatabaseService(DatabaseService databaseService){
        MLDatasetServiceValueHolder.registerDatabaseService(databaseService);
    }

    protected void unsetDatabaseService(DatabaseService databaseService){
        MLDatasetServiceValueHolder.registerDatabaseService(null);
    }
    
    /**
     * Returns a absolute path of a given data source.
     *
     * @param datasetID     Unique Identifier of the data-set
     * @return              Absolute path of a given data-set
     * @throws              DatasetServiceException
     */
    @Override
    public String getDatasetUrl(String datasetID) throws DatasetServiceException {
        try {
            DatabaseService dbService =  MLDatasetServiceValueHolder.getDatabaseService();
            return dbService.getDatasetUrl(datasetID);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to read dataset path from database: " +
                e.getMessage(), e);
        }
    }

    /**
     * Upload the data file.
     *
     * @param sourceInputStream     Input Stream of the source data file
     * @param fileName              Name of the uploading file
     * @param projectID             Unique Identifier of the project
     * @return                      Path of the uploaded file
     * @throws                      DatasetServiceException
     * @throws                      IOException
     */
    @Override
    public String uploadDataset(InputStream sourceInputStream, String fileName, String projectID) 
            throws DatasetServiceException, IOException {
        String uploadDir = dataUploadSettings.getUploadLocation();
        BufferedWriter writer = null;
        BufferedReader bufferedReader = null;
        try {
            String fileSeparator = System.getProperty(DatasetConfigurations.FILE_SEPARATOR);
            
            // Get user home, if the uploading directory is set to USER_HOME
            if (uploadDir.equalsIgnoreCase(DatasetConfigurations.USER_HOME)) {
                uploadDir = System.getProperty(DatasetConfigurations.HOME) + fileSeparator +
                    DatasetConfigurations.ML_PROJECTS;
            }
            
            if (FilePathValidator.isValid(uploadDir)) {
                // Upload the file.
                File targetFile = new File(uploadDir + fileSeparator + projectID + fileSeparator +
                    fileName);
                FileUtils.copyInputStreamToFile(sourceInputStream, targetFile);
                return targetFile.getPath();
            } else {
                throw new DatasetServiceException("Invalid Uploading directory " + uploadDir);
            }
        } catch (IOException e) {
            throw new DatasetServiceException("Failed to upload the file " + fileName + " : " +
                e.getMessage(), e);
        } finally{
            if(writer!=null){
                writer.close();
            }
            if(bufferedReader!=null){
                bufferedReader.close();
            }
        }
    }

    public boolean deleteDataset(String fileName, String projectID) throws DatasetServiceException{
        String uploadDir = dataUploadSettings.getUploadLocation();
        try{
            String fileSeparator = System.getProperty(DatasetConfigurations.FILE_SEPARATOR);

            if (uploadDir.equalsIgnoreCase(DatasetConfigurations.USER_HOME)) {
                uploadDir = System.getProperty(DatasetConfigurations.HOME) + fileSeparator +
                        DatasetConfigurations.ML_PROJECTS;
            }
            String dataSetFullPath = uploadDir+fileSeparator+projectID+fileSeparator+fileName;
            String dataFolderFullPath = uploadDir+fileSeparator+projectID;
            File dataSet = new File(dataSetFullPath);
            boolean fileDeletingStatus =  dataSet.delete();

            File dataFolder = new File(dataFolderFullPath);
            boolean folderDeletingStatus = dataFolder.delete();

            return  (fileDeletingStatus && folderDeletingStatus);


        }catch (Exception ex){
            throw new DatasetServiceException("Failed to delete file:" + fileName + " : " +
                    ex.getMessage(), ex);
        }
    }
    
    /**
     * 
     * @param fileName      Path of the dataset to calculate summary statistics
     * @param datasetID     Unique Identifier of the data-set
     * @param projectID     Unique Identifier of the project associated with the dataset
     * @return              Number of features in the data-set
     * @throws              DatasetServiceException
     */
    @Override
    public int calculateSummaryStatistics(String fileName, String datasetID, String projectID)
            throws DatasetServiceException {
        try {
            String uploadDir = dataUploadSettings.getUploadLocation();
            String fileSeparator = System.getProperty(DatasetConfigurations.FILE_SEPARATOR);
            if (uploadDir.equalsIgnoreCase(DatasetConfigurations.USER_HOME)) {
                uploadDir = System.getProperty(DatasetConfigurations.HOME) + fileSeparator +
                        DatasetConfigurations.ML_PROJECTS;
            }
            String dataSetFullPath = uploadDir+fileSeparator+projectID+fileSeparator+fileName;

            File targetFile = new File(dataSetFullPath);
            if (targetFile.isFile() && targetFile.canRead()) {
                // Insert details of the file to the database.
                DatabaseService dbService =  MLDatasetServiceValueHolder.getDatabaseService();
                dbService.insertDatasetDetails(datasetID, targetFile.getPath(), projectID);
                // Generate summary statistics.
                DatasetSummary summary = new DatasetSummary(targetFile, datasetID);
                int noOfFeatures = summary.generateSummary(summaryStatSettings.getSampleSize(),
                    summaryStatSettings.getHistogramBins(), summaryStatSettings
                    .getCategoricalThreshold(), true, mlDatabaseName);
                // Update the database with the data-set sample.
                dbService.updateDatasetSample(datasetID, summary.samplePoints());
                return noOfFeatures;
            } else {
                throw new DatasetServiceException("");
            }
        } catch (DatasetSummaryException e) {
            throw new DatasetServiceException("Failed to generate summary statistics: " +
                    e.getMessage(), e);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to update sample points: " + e.getMessage(), e);
        } finally{
            
        }
    }

    /**
     * Update the data type of a given feature.
     *
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param featureType   Updated type of the feature
     * @throws              DatasetServiceException
     */
    @Override
    public void updateDataType(String featureName, String workflowID, String featureType)
            throws DatasetServiceException {
        try {
            DatabaseService dbService =  MLDatasetServiceValueHolder.getDatabaseService();
            dbService.updateDataType(featureName, workflowID, featureType);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to update feature type: " + e.getMessage(), e);
        }
    }

    /**
     * Update the impute option of a given feature.
     *
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param imputeOption  Updated impute option of the feature
     * @throws              DatasetServiceException
     */
    @Override
    public void updateImputeOption(String featureName, String workflowID, String imputeOption)
            throws DatasetServiceException {
        try {
            DatabaseService dbService =  MLDatasetServiceValueHolder.getDatabaseService();
            dbService.updateImputeOption(featureName, workflowID, imputeOption);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to update impute option: " + e.getMessage(),
                e);
        }
    }

    /**
     * change whether a feature should be included as an input or not.
     *
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param isInput       Boolean value indicating whether the feature is an input or not
     * @throws              DatasetServiceException
     */
    @Override
    public void updateIsIncludedFeature(String featureName, String workflowID, boolean isInput)
            throws DatasetServiceException {
        try {
            DatabaseService dbService =  MLDatasetServiceValueHolder.getDatabaseService();
            dbService.updateIsIncludedFeature(featureName, workflowID, isInput);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException( "Failed to update included option: "
                + e.getMessage(), e);
        }
    }

    /**
     * Returns a set of features in a given range, from the alphabetically
     * ordered set of features, of a data-set.
     *
     * @param datasetID         Unique Identifier of the data-set
     * @param startIndex        Starting index of the set of features needed
     * @param numberOfFeatures  Number of features needed, from the starting index
     * @return                  A list of Feature objects
     * @throws                  DatasetServiceException
     */
    @Override
    public List<FeatureSummary> getFeatures(String datasetID, String workflowID, int startIndex,
        int numberOfFeatures) throws DatasetServiceException {
        try {
            DatabaseService dbService =  MLDatasetServiceValueHolder.getDatabaseService();
            return dbService.getFeatures(datasetID, workflowID, startIndex, numberOfFeatures);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to retrieve features: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the names of the features, belongs to a particular data-type
     * (Categorical/Numerical), of the work-flow.
     *
     * @param workflowID    Unique identifier of the current work-flow
     * @param featureType   Data-type of the feature
     * @return              A list of feature names
     * @throws              DatasetServiceException
     */
    @Override
    public List<String> getFeatureNames(String workflowID, String featureType)
            throws DatasetServiceException {
        try {
            DatabaseService dbService =  MLDatasetServiceValueHolder.getDatabaseService();
            return dbService.getFeatureNames(workflowID, featureType);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to retrieve feature names: "
                + e.getMessage(), e);
        }
    }

    /**
     * Returns data points of the selected sample as coordinates of three
     * features, needed for the scatter plot.
     *
     * @param datasetID         Unique Identifier of the data-set
     * @param xAxisFeature      Name of the feature to use as the x-axis
     * @param yAxisFeature      Name of the feature to use as the y-axis
     * @param groupByFeature    Name of the feature to be grouped by (color code)
     * @return                  A JSON array of data points
     * @throws                  DatasetServiceException
     */
    @Override
    public JSONArray getScatterPlotPoints(String datasetID, String xAxisFeature, String yAxisFeature,
        String groupByFeature) throws DatasetServiceException {
        try {
            DatabaseService dbService =  MLDatasetServiceValueHolder.getDatabaseService();
            return dbService.getScatterPlotPoints(datasetID, xAxisFeature, yAxisFeature, groupByFeature);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException( "Failed to retrieve sample points: "
                + e.getMessage(), e);
        }
    }

    /**
     * Returns the summary statistics for a given feature of a given data-set
     *
     * @param datasetID     Unique Identifier of the data-set
     * @param feature       Name of the feature of which summary statistics are needed
     * @return              JSON string containing the summary statistics
     * @throws              DatasetServiceException
     */
    @Override
    public String getSummaryStats(String datasetID, String feature) throws DatasetServiceException {
        try {
            DatabaseService dbService =  MLDatasetServiceValueHolder.getDatabaseService();
            return dbService.getSummaryStats(datasetID, feature);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to retrieve summary statistics: " +
                e.getMessage(), e);
        }
    }

    /**
     * Returns the number of features of a given data-set.
     *
     * @param datasetID     Unique Identifier of the data-set
     * @return              Number of features in the data-set
     * @throws              DatasetServiceException
     */
    @Override
    public int getFeatureCount(String datasetID) throws DatasetServiceException {
        try {
            DatabaseService dbService =  MLDatasetServiceValueHolder.getDatabaseService();
            return dbService.getFeatureCount(datasetID);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to retrieve the feature count: " +
                e.getMessage(), e);
        }
    }

    /**
     * Returns the model id associated with given workflow id
     * 
     * @param workflowId    Unique identifier of the workflow
     * @return              Unique identifier of the model associated with workflow
     * @throws              DatasetServiceException
     */
    @Override
    public String getModelId(String workflowId) throws DatasetServiceException {
        try {
            DatabaseService dbService =  MLDatasetServiceValueHolder.getDatabaseService();
            return dbService.getModelId(workflowId);
        } catch (DatabaseHandlerException e){
            throw new DatasetServiceException("Failed to retrieve the model id associated with "+
                " workflow id: "+workflowId, e);
        }
    }
}
