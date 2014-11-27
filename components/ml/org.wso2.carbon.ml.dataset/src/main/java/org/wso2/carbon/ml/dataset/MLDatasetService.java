/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ml.dataset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ml.dataset.constants.DatasetConfigurations;
import org.wso2.carbon.ml.dataset.dto.DataUploadSettings;
import org.wso2.carbon.ml.dataset.dto.Feature;
import org.wso2.carbon.ml.dataset.dto.SummaryStatisticsSettings;
import org.wso2.carbon.ml.dataset.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.dataset.exceptions.DatasetServiceException;
import org.wso2.carbon.ml.dataset.exceptions.DatasetSummaryException;

/**
 * Class contains the services related to importing and exploring a data-set.
 *
 * @scr.component name="mlDatasetService" immediate="true"
 */
public class MLDatasetService implements DatasetService {
    private static final Log logger = LogFactory.getLog(MLDatasetService.class);
    private DataUploadSettings dataUploadSettings;
    private SummaryStatisticsSettings summaryStatSettings;
    private String mlDatabaseName;

    /*
     * Activates the Data-set Service.
     */
    protected void activate(ComponentContext context) {
        try {
            // Read data-set settings from ml-config.xml file.
            MLConfigurationParser mlConfigurationParser = new MLConfigurationParser();
            MLDatasetService datasetService = new MLDatasetService();
            datasetService.dataUploadSettings = mlConfigurationParser.getDataUploadSettings();
            datasetService.summaryStatSettings = mlConfigurationParser.getSummaryStatisticsSettings();
            datasetService.mlDatabaseName=mlConfigurationParser.getDatabaseName();
            // Register the service.
            context.getBundleContext().registerService(MLDatasetService.class.getName(),
                datasetService, null);
            logger.info("ML Dataset Service Started");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /*
     * Deactivates the Data-set Service.
     */
    protected void deactivate(ComponentContext context) {
        logger.info("Ml Dataset Service Stopped");
    }

    /**
     * Returns a absolute path of a given data source.
     *
     * @param datasetID Unique Identifier of the data-set
     * @return Absolute path of a given data-set
     * @throws DatasetServiceException
     */
    @Override
    public String getDatasetUrl(String datasetID) throws DatasetServiceException {
        try {
            DatabaseHandler handler = new DatabaseHandler(mlDatabaseName);
            return handler.getDatasetUrl(datasetID);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to read dataset path from database: " +
                e.getMessage(), e);
        }
    }

    /**
     * Upload the data file and calculate summary statistics.
     *
     * @param sourceInputStream Input Stream of the source data file
     * @param datasetID Unique Identifier of the data-set
     * @param fileName Name of the uploading file
     * @param projectID Unique Identifier of the project
     * @return Number of features in the data-set
     * @throws DatasetServiceException
     * @throws IOException
     */
    @Override
    public int uploadDataset(InputStream sourceInputStream, String datasetID, String fileName,
        String projectID) throws DatasetServiceException {
        String uploadDir = dataUploadSettings.getUploadLocation();
        try {
            String fileSeparator = System.getProperty(DatasetConfigurations.FILE_SEPARATOR);
            
            // Get user home, if the uploading directory is set to USER_HOME
            if (uploadDir.equalsIgnoreCase(DatasetConfigurations.USER_HOME)) {
                uploadDir = System.getProperty(DatasetConfigurations.HOME) + fileSeparator+
                    DatasetConfigurations.ML_PROJECTS;
            }
            
            if (FilePathValidator.isValid(uploadDir)) {
                // Upload the file.
                File targetFile = new File(uploadDir + fileSeparator + projectID + fileSeparator +
                    fileName);
                FileUtils.copyInputStreamToFile(sourceInputStream, targetFile);
                // Insert details of the file to the database.
                DatabaseHandler dbHandler = new DatabaseHandler(mlDatabaseName);
                dbHandler.insertDatasetDetails(datasetID, targetFile.getPath(), projectID);
                // Generate summary statistics.
                DatasetSummary summary = new DatasetSummary(targetFile, datasetID);
                int noOfFeatures = summary.generateSummary(summaryStatSettings.getSampleSize(),
                    summaryStatSettings.getHistogramBins(), summaryStatSettings
                    .getCategoricalThreshold(), true, mlDatabaseName);
                // Update the database with the data-set sample.
                dbHandler.updateDatasetSample(datasetID, summary.samplePoints());
                return noOfFeatures;
            } else {
                throw new DatasetServiceException("Invalid Uploading directory " + uploadDir);
            }
        } catch (DatasetSummaryException e) {
            throw new DatasetServiceException("Failed to generate summary statistics: " +
                    e.getMessage(), e);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to update sample points: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DatasetServiceException("Failed to upload the file " + fileName + " : " +
                e.getMessage(), e);
        }
    }

    /**
     * Update the data type of a given feature.
     *
     * @param featureName Name of the feature to be updated
     * @param workflowID Unique identifier of the current workflow
     * @param featureType Updated type of the feature
     * @throws DatasetServiceException
     */
    @Override
    public void updateDataType(String featureName, String workflowID, String featureType)
            throws DatasetServiceException {
        try {
            DatabaseHandler dbHandler = new DatabaseHandler(mlDatabaseName);
            dbHandler.updateDataType(featureName, workflowID, featureType);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to update feature type: " + e.getMessage(), e);
        }
    }

    /**
     * Update the impute option of a given feature.
     *
     * @param featureName Name of the feature to be updated
     * @param workflowID Unique identifier of the current workflow
     * @param imputeOption Updated impute option of the feature
     * @throws DatasetServiceException
     */
    @Override
    public void updateImputeOption(String featureName, String workflowID, String imputeOption)
            throws DatasetServiceException {
        try {
            DatabaseHandler dbHandler = new DatabaseHandler(mlDatabaseName);
            dbHandler.updateImputeOption(featureName, workflowID, imputeOption);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to update impute option: " + e.getMessage(),
                e);
        }
    }

    /**
     * change whether a feature should be included as an input or not.
     *
     * @param featureName Name of the feature to be updated
     * @param workflowID Unique identifier of the current workflow
     * @param isInput Boolean value indicating whether the feature is an input or not
     * @throws DatasetServiceException
     */
    @Override
    public void updateIsIncludedFeature(String featureName, String workflowID, boolean isInput)
            throws DatasetServiceException {
        try {
            DatabaseHandler dbHandler = new DatabaseHandler(mlDatabaseName);
            dbHandler.updateIsIncludedFeature(featureName, workflowID, isInput);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException( "Failed to update included option: "
                + e.getMessage(), e);
        }
    }

    /**
     * Returns a set of features in a given range, from the alphabetically
     * ordered set of features, of a data-set.
     *
     * @param datasetID Unique Identifier of the data-set
     * @param startIndex Starting index of the set of features needed
     * @param numberOfFeatures Number of features needed, from the starting index
     * @return A list of Feature objects
     * @throws DatasetServiceException
     */
    @Override
    public List<Feature> getFeatures(String datasetID, String workflowID, int startIndex,
        int numberOfFeatures) throws DatasetServiceException {
        try {
            DatabaseHandler dbHandler = new DatabaseHandler(mlDatabaseName);
            return dbHandler.getFeatures(datasetID, workflowID, startIndex, numberOfFeatures);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to retrieve features: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the names of the features, belongs to a particular data-type
     * (Categorical/Numerical), of the work-flow.
     *
     * @param workflowID Unique identifier of the current work-flow
     * @param featureType Data-type of the feature
     * @return A list of feature names
     * @throws DatasetServiceException
     */
    @Override
    public List<String> getFeatureNames(String workflowID, String featureType)
            throws DatasetServiceException {
        try {
            DatabaseHandler dbHandler = new DatabaseHandler(mlDatabaseName);
            return dbHandler.getFeatureNames(workflowID, featureType);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to retrieve feature names: "
                + e.getMessage(), e);
        }
    }

    /**
     * Returns data points of the selected sample as coordinates of three
     * features, needed for the scatter plot.
     *
     * @param datasetID Unique Identifier of the data-set
     * @param feature1 Name of the feature to use as the x-axis
     * @param feature2 Name of the feature to use as the y-axis
     * @param feature3 Name of the feature to be grouped by (color code)
     * @return A JSON array of data points
     * @throws DatasetServiceException
     */
    @Override
    public JSONArray getSamplePoints(String datasetID, String feature1, String feature2,
        String feature3) throws DatasetServiceException {
        try {
            DatabaseHandler dbHandler = new DatabaseHandler(mlDatabaseName);
            return dbHandler.getSamplePoints(datasetID, feature1, feature2, feature3);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException( "Failed to retrieve sample points: "
                + e.getMessage(), e);
        }
    }

    /**
     * Returns the summary statistics for a given feature of a given data-set
     *
     * @param datasetID Unique Identifier of the data-set
     * @param feature Name of the feature of which summary statistics are needed
     * @return JSON string containing the summary statistics
     * @throws DatasetServiceException
     */
    @Override
    public String getSummaryStats(String datasetID, String feature) throws DatasetServiceException {
        try {
            DatabaseHandler dbHandler = new DatabaseHandler(mlDatabaseName);
            return dbHandler.getSummaryStats(datasetID, feature);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to retrieve summary statistics: " +
                e.getMessage(), e);
        }
    }

    /**
     * Returns the number of features of a given data-set.
     *
     * @param datasetID Unique Identifier of the data-set
     * @return Number of features in the data-set
     * @throws DatasetServiceException
     */
    @Override
    public int getFeatureCount(String datasetID) throws DatasetServiceException {
        try {
            DatabaseHandler dbHandler = new DatabaseHandler(mlDatabaseName);
            return dbHandler.getFeatureCount(datasetID);
        } catch (DatabaseHandlerException e) {
            throw new DatasetServiceException("Failed to retrieve the feature count: " +
                e.getMessage(), e);
        }
    }
}
