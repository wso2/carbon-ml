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
package org.wso2.carbon.ml.database;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.wso2.carbon.ml.commons.domain.*;
import org.wso2.carbon.ml.commons.domain.config.HyperParameter;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public interface DatabaseService {

    public void insertDatasetSchema(MLDataset dataset) throws DatabaseHandlerException;

    public void insertDatasetVersion(MLDatasetVersion mlValueset) throws DatabaseHandlerException;

    public void insertProject(MLProject project) throws DatabaseHandlerException;

    public void insertAnalysis(MLAnalysis analysis) throws DatabaseHandlerException;

    public void insertModel(MLModelNew model) throws DatabaseHandlerException;

    /**
     * Retrieves the path of the value-set having the given ID, from the
     * database.
     *
     * @param datasetVersionId  Unique Identifier of the value-set
     * @return                  Absolute path of a given value-set
     * @throws                  DatabaseHandlerException
     */
    public String getDatasetVersionUri(long datasetVersionId) throws DatabaseHandlerException;

    /**
     * Insert the new data-set details to the the database
     *
     * @param name
     * @param tenantID
     * @param username
     * @param comments
     * @param sourceType
     * @param targetType
     * @param dataType
     * @throws DatabaseHandlerException
     */
    public void insertDatasetDetails(String name, int tenantID, String username, String comments,
                                     String sourceType, String targetType, String dataType)
            throws DatabaseHandlerException;

    /**
     * @param datasetName   Name of the data-set
     * @param tenantId      Tenant Id
     * @return              Unique Id of the data-set
     * @throws              DatabaseHandlerException
     */
    public long getDatasetId(String datasetName, int tenantId, String userName) throws DatabaseHandlerException;

    /**
     * Insert the data-set-version details to the database
     *
     * @param datasetId
     * @param tenantId
     * @param version
     * @throws DatabaseHandlerException
     */
    public void insertDatasetVersionDetails(long datasetId, int tenantId, String username, String version)
            throws DatabaseHandlerException;

    /**
     * Insert the feature defaults to the database
     * @param datasetVersionId
     * @param featureName
     * @param type
     * @param featureIndex
     * @param summary
     * @throws DatabaseHandlerException
     */
    public void insertFeatureDefaults(long datasetVersionId, String featureName, String type, int featureIndex, String
        summary) throws DatabaseHandlerException;

    /**
     * Insert the value-set to the database
     * @param datasetVersionId
     * @param tenantId
     * @param uri
     * @param samplePoints
     * @throws DatabaseHandlerException
     */
    public void insertValueSet(long datasetVersionId, String name, int tenantId, String username, String uri, 
            SamplePoints samplePoints) throws DatabaseHandlerException;
    
    public long getVersionsetId(String valueSetName, int tenantId) throws DatabaseHandlerException;

    /**
     * Insert data-source to the database
     * @param valuesetId
     * @param tenantId
     * @param username
     * @param key
     * @param value
     * @throws DatabaseHandlerException
     */
    public void insertDataSource(long valuesetId, int tenantId, String username, String key, String value)
            throws DatabaseHandlerException;

    /**
     * Check whether the given database name exists in the given tenantId
     * @param tenantId                 tenant Id
     * @param datasetName              name of the data-set
     * @return                         true if the name exists
     * @throws DatabaseHandlerException
     */
    public boolean isDatasetNameExist(int tenantId, String userName, String datasetName) throws DatabaseHandlerException;

    /**
     * Check whether the given database version exists for the given data-set name and tenant ID
     * @param tenantId                 tenantId
     * @param datasetName              name of the data-set
     * @param datasetVersion           version of the data-set
     * @return                         true if the version of the data-set exists
     * @throws DatabaseHandlerException
     */
    public boolean isDatasetVersionExist(int tenantId, String datasetName, String datasetVersion) throws DatabaseHandlerException;

    /**
     * @param datasetId
     * @param datasetVersion
     * @return
     * @throws DatabaseHandlerException
     */
    public long getDatasetVersionId(long datasetId, String datasetVersion) throws DatabaseHandlerException;

    /**
     * Update the value-set table with a data-set sample.
     *
     * @param valueSetId        Unique Identifier of the value-set
     * @param valueSetSample    SamplePoints object of the value-set
     * @throws                  DatabaseHandlerException
     */
    public void updateVersionsetSample(long valueSetId, SamplePoints valueSetSample)
            throws DatabaseHandlerException;

    /**
     * Returns data points of the selected sample as coordinates of three
     * features, needed for the scatter plot.
     *
     * @param valueSetId        Unique Identifier of the value-set
     * @param xAxisFeature      Name of the feature to use as the x-axis
     * @param yAxisFeature      Name of the feature to use as the y-axis
     * @param groupByFeature    Name of the feature to be grouped by (color code)
     * @return                  A JSON array of data points
     * @throws                  DatabaseHandlerException
     */
    public JSONArray getScatterPlotPoints(long valueSetId, String xAxisFeature, String yAxisFeature,
                                          String groupByFeature) throws DatabaseHandlerException;

    /**
     * Returns sample data for selected features
     *
     * @param valueSetId        Unique Identifier of the value-set
     * @param featureListString String containing feature name list
     * @return                  A JSON array of data points
     * @throws                  DatabaseHandlerException
     */
    public JSONArray getChartSamplePoints(long valueSetId, String featureListString)
            throws DatabaseHandlerException;

    /**
     * Returns a set of features in a given range, from the alphabetically ordered set
     * of features, of a data-set.
     *
     * @param datasetID         Unique Identifier of the data-set
     * @param startIndex        Starting index of the set of features needed
     * @param numberOfFeatures  Number of features needed, from the starting index
     * @return                  A list of Feature objects
     * @throws                  DatabaseHandlerException
     */
    public List<FeatureSummary> getFeatures(String datasetID, String modelId, int startIndex,
                                            int numberOfFeatures) throws DatabaseHandlerException;


    /**
     * This method extracts and retures default features available in a given dataset version
     * @param datasetVersionId The dataset varsion id associated with this dataset version
     * @return                 A list of FeatureSummaries
     * @throws                 DatabaseHandlerException
     */
    public List<FeatureSummary> getDefaultFeatures(long datasetVersionId, int startIndex, int numberOfFeatures)
            throws DatabaseHandlerException;

    /**
     * Returns the names of the features, belongs to a particular data-type
     * (Categorical/Numerical), of the work-flow.
     *
     * @param modelId       Unique identifier of the current model
     * @return              A list of feature names
     * @throws              DatabaseHandlerException
     */
    public List<String> getFeatureNames(long modelId) throws DatabaseHandlerException;

    /**
     * Retrieve and returns the Summary statistics for a given feature of a
     * given data-set version, from the database
     *
     * @param datasetVersionId     Unique identifier of the data-set
     * @param featureName          Name of the feature of which summary statistics are needed
     * @return                     JSON string containing the summary statistics
     * @throws                     DatabaseHandlerException
     */
    public String getSummaryStats(long datasetVersionId, String featureName) throws DatabaseHandlerException;

    /**
     * Returns the number of features of a given data-set version
     *
     * @param datasetVersionId     Unique identifier of the data-set version
     * @return                     Number of features in the data-set version
     * @throws                     DatabaseHandlerException
     */
    public int getFeatureCount(long datasetVersionId) throws DatabaseHandlerException;

    /**
     * Update the database with all the summary statistics of the sample.
     *
     * @param datasetVersionID  Unique Identifier of the data-set-version
     * @param headerMap         Array of names of features
     * @param type              Array of data-types of each feature
     * @param graphFrequencies  List of Maps containing frequencies for graphs, of each feature
     * @param missing           Array of Number of missing values in each feature
     * @param unique            Array of Number of unique values in each feature
     * @param descriptiveStats  Array of descriptiveStats object of each feature
     * @param                   include Default value to set for the flag indicating the feature is an input or not
     * @throws                  DatabaseHandlerException
     */
    public void updateSummaryStatistics(long datasetVersionID,  Map<String, Integer> headerMap, String[] type,
                                        List<SortedMap<?, Integer>> graphFrequencies, int[] missing, int[] unique,
                                        List<DescriptiveStatistics> descriptiveStats, Boolean include)
            throws DatabaseHandlerException;

    /**
     * Update the database with the summary stats of data-set-version
     * @param datasetVersionId  Unique Id of the data-set-version
     * @param summaryStats      Summary stats
     * @throws DatabaseHandlerException
     */
    public void updateSummaryStatistics(long datasetSchemaId, long datasetVersionId, SummaryStats summaryStats) 
            throws DatabaseHandlerException;

    /**
     * Set the default values for feature properties of a given workflow.
     *
     * @param datasetVersionId  Unique identifier of the data-set-version
     * @param modelId           Unique identifier of the current model
     * @throws                  DatabaseHandlerException
     */
    public void setDefaultFeatureSettings(long datasetVersionId, long modelId) throws DatabaseHandlerException;

    /**
     * Retrieves the type of a feature.
     *
     * @param modelId       Unique identifier of the model
     * @param featureName   Name of the feature
     * @return              Type of the feature (Categorical/Numerical)
     * @throws              DatabaseHandlerException
     */
    public String getFeatureType(long modelId, String featureName) throws DatabaseHandlerException;

    /**
     * Change whether a feature should be included as an input or not
     *
     * @param featureName   Name of the feature to be updated
     * @param modelId       Unique identifier of the current model
     * @param isInput       Boolean value indicating whether the feature is an input or not
     * @throws              DatabaseHandlerException
     */
    public void updateFeatureInclusion(String featureName, long modelId, boolean isInput)
            throws DatabaseHandlerException;

    /**
     * Update the impute method option of a given feature.
     *
     * @param featureName   Name of the feature to be updated
     * @param modelId       Unique identifier of the current model
     * @param imputeOption  Updated impute option of the feature
     * @throws              DatabaseHandlerException
     */
    public void updateImputeOption(String featureName, long modelId, String imputeOption)
            throws DatabaseHandlerException;

    /**
     * Update the data type of a given feature.
     *
     * @param featureName   Name of the feature to be updated
     * @param modelId       Unique identifier of the current model
     * @param featureType   Updated type of the feature
     * @throws              DatabaseHandlerException
     */
    public void updateDataType(String featureName, long modelId, String featureType)
            throws DatabaseHandlerException;

    /**
     * Creates a new project.
     *
     * @param projectName      Name of the project
     * @param description      Description of the project
     * @throws                 DatabaseHandlerException
     */
    public void createProject(String projectName, String description, int tenantId, String username) 
            throws DatabaseHandlerException;

    /**
     * Get the unique Id of the project
     * @param tenantId        tenant Id
     * @param userName        username of the project
     * @param projectName     name of the project
     * @return
     * @throws DatabaseHandlerException
     */
    public long getProjectId(int tenantId, String userName, String projectName) throws DatabaseHandlerException;

    /**
     * Delete the project
     * @param tenantId
     * @param userName
     * @param projectName
     * @throws DatabaseHandlerException
     */
    public void deleteProject(int tenantId, String userName, String projectName) throws DatabaseHandlerException;
    /**
     * Retrieve Details of a Project
     *
     * @param projectId     Unique identifier of the project
     * @return              DatabaseHandlerException
     */
    public String[] getProject(String projectId) throws DatabaseHandlerException;

    /**
     * Delete details of a given project from the database.
     *
     * @param projectId    Unique identifier for the project
     * @throws             DatabaseHandlerException
     */
    public void deleteProject(String projectId) throws DatabaseHandlerException;

    /**
     * Insert Analysis to the database
     * @param projectId
     * @param name
     * @param tenantId
     * @param comments
     * @throws DatabaseHandlerException
     */
    public void insertAnalysis(long projectId, String name, int tenantId, String username, String comments) 
            throws DatabaseHandlerException;

    /**
     * Get the analysis Id of a given tenant id, username, analysis combination
     * @param tenantId
     * @param userName
     * @param analysisName
     * @return
     * @throws DatabaseHandlerException
     */
    public long getAnalysisId(int tenantId, String userName, String analysisName) throws DatabaseHandlerException;
    
    public long getModelId(int tenantId, String userName, String modelName) throws DatabaseHandlerException;

    /**
     * Delete the analysis
     * @param tenantId
     * @param userName
     * @param analysisName
     * @throws DatabaseHandlerException
     */
    public void deleteAnalysis(int tenantId, String userName, String analysisName) throws DatabaseHandlerException;

    /**
     * Insert Model to the database
     * @param name model name
     * @param analysisId
     * @param tenantId
     * @param outputModel
     * @param username
     * @throws DatabaseHandlerException
     */
    public void insertModel(String name, long analysisId, long datasetVersionId, int tenantId, String username) 
            throws DatabaseHandlerException;

    /**
     * Insert model configuration to the database
     * @param modelId
     * @param key
     * @param value
     * @param type
     * @throws DatabaseHandlerException
     */
    public void insertModelConfiguration(long modelId, String key, String value) throws DatabaseHandlerException;

    /**
     * Insert feature-customized to the database
     * @param modelId
     * @param tenantId
     * @param featureName
     * @param imputeOption
     * @param inclusion
     * @param lastModifiedUser
     * @throws DatabaseHandlerException
     */
    public void insertFeatureCustomized(long modelId, int tenantId, String featureName, String type, String imputeOption,
            boolean inclusion, String lastModifiedUser) throws DatabaseHandlerException;

    /**
     * Get the project names and created dates, that a tenant is assigned to.
     *
     * @param tenantID     Unique identifier for the tenant.
     * @return             An array of project ID, Name and the created date of the projects
     *                     associated with a given tenant.
     * @throws             DatabaseHandlerException
     */
    public String[][] getTenantProjects(int tenantID) throws DatabaseHandlerException;

    //TODO workflow to be replaced with analysis

    public void createNewWorkflow(String workflowID, String parentWorkflowID, String projectID, String datasetID
            , String workflowName) throws DatabaseHandlerException;

    // TODO
    public void createWorkflow(String workflowID, String projectID, String datasetID
            , String workflowName) throws DatabaseHandlerException;

    // TODO
    public void deleteWorkflow(String workflowID) throws DatabaseHandlerException;

    // TODO
    public String[][] getProjectWorkflows(String projectId) throws DatabaseHandlerException;

    // TODO
    public void updateWorkdflowName(String workflowId, String name) throws DatabaseHandlerException;

    // TODO
    public String getdatasetID(String projectId) throws DatabaseHandlerException;

    // TODO
    public String getDatasetId(String projectId) throws DatabaseHandlerException;

    // TODO
    public String getModelId(String workflowId) throws DatabaseHandlerException;

    // TODO
    public ModelSummary getModelSummary(String modelID) throws DatabaseHandlerException;

    // TODO
    public void insertModel(String modelID, String workflowID, Time executionStartTime) throws DatabaseHandlerException;

    // TODO
    public void updateModel(String modelID, MLModel model, ModelSummary modelSummary, Time executionEndTime)
            throws DatabaseHandlerException;

    // TODO
    public MLModel getModel(String modelID) throws DatabaseHandlerException;

    // TODO
    public void insertModelSettings(String modelSettingsID, String workflowID, String algorithmName, 
            String algorithmClass, String response, double trainDataFraction, List<HyperParameter> hyperparameters)
            throws DatabaseHandlerException;

    // TODO
    public long getModelExecutionEndTime(String modelId) throws DatabaseHandlerException;

    // TODO
    public long getModelExecutionStartTime(String modelId) throws DatabaseHandlerException;

    public void insertFeatureCustomized(long analysisId, List<MLCustomizedFeature> customizedFeatures,int tenantId,
            String userName) throws DatabaseHandlerException;

    public void insertModelConfigurations(long analysisId, List<MLModelConfiguration> modelConfigs)
            throws DatabaseHandlerException;

    public void insertHyperParameters(long analysisId, List<MLHyperParameter> hyperParameters) 
            throws DatabaseHandlerException;

    public String getModelConfigurationValue(long modelId, String name) throws DatabaseHandlerException;

    public void updateModelSummary(long modelId, ModelSummary modelSummary) throws DatabaseHandlerException;

    public void updateModelStorage(long modelId, String storageType, String location) throws DatabaseHandlerException;

    public boolean isValidModelId(int tenantId, String userName, long modelId) throws DatabaseHandlerException;

    public void insertFeatureCustomized(long analysisId, MLCustomizedFeature customizedFeature) 
            throws DatabaseHandlerException;

    public long getDatasetVersionIdOfModel(long modelId) throws DatabaseHandlerException;

    public long getDatasetId(long datasetVersionId) throws DatabaseHandlerException;

    public String getDataTypeOfModel(long modelId) throws DatabaseHandlerException;

    public String getAStringModelConfiguration(long analysisId, String configKey) throws DatabaseHandlerException;

    public double getADoubleModelConfiguration(long modelId, String configKey) throws DatabaseHandlerException;

    public List<MLHyperParameter> getHyperParametersOfModel(long modelId) throws DatabaseHandlerException;

    public Map<String, String> getHyperParametersOfModelAsMap(long modelId) throws DatabaseHandlerException;

    public Workflow getWorkflow(long modelId) throws DatabaseHandlerException;

    public MLStorage getModelStorage(long modelId) throws DatabaseHandlerException;

    public MLProject getProject(int tenantId, String userName, String projectName) throws DatabaseHandlerException;

    public List<MLProject> getAllProjects(int tenantId, String userName) throws DatabaseHandlerException;

    public MLAnalysis getAnalysis(int tenantId, String userName, String analysisName) throws DatabaseHandlerException;

    public List<MLAnalysis> getAllAnalyses(int tenantId, String userName) throws DatabaseHandlerException;

    public MLModelNew getModel(int tenantId, String userName, String modelName) throws DatabaseHandlerException;

    public List<MLModelNew> getAllModels(int tenantId, String userName) throws DatabaseHandlerException;

    public List<MLDatasetVersion> getAllVersionsetsOfDataset(int tenantId, String userName, long datasetId)
            throws DatabaseHandlerException;

    public List<MLDataset> getAllDatasets(int tenantId, String userName) throws DatabaseHandlerException;

    public MLDataset getDataset(int tenantId, String userName, long datasetId) throws DatabaseHandlerException;

    public MLDatasetVersion getVersionset(int tenantId, String userName, long valuesetId) throws DatabaseHandlerException;

    public long getDatasetVersionId(long datasetId, String version, int tenantId, String userName)
            throws DatabaseHandlerException;

    public void insertDefaultsIntoFeatureCustomized(long analysisId, MLCustomizedFeature customizedValues)
            throws DatabaseHandlerException;

    public long getDatasetSchemaIdFromAnalysisId(long modelId) throws DatabaseHandlerException;

}
