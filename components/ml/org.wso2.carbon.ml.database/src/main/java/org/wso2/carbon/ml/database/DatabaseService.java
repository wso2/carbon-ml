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

    /**
     * Insert a new dataset-schema into the database
     * @param dataset  MLDataset to be inserted
     * @throws DatabaseHandlerException
     */
    public void insertDatasetSchema(MLDataset dataset) throws DatabaseHandlerException;

    /**
     * Insert a new dataset-version into the database
     * @param datasetVersion MLDatasetVersion to be inserted
     * @throws DatabaseHandlerException
     */
    public void insertDatasetVersion(MLDatasetVersion datasetVersion) throws DatabaseHandlerException;

    /**
     * Insert a new project to the database
     * @param project MLProject to be inserted
     * @throws DatabaseHandlerException
     */
    public void insertProject(MLProject project) throws DatabaseHandlerException;

    /**
     * Insert a new analysis into the database
     * @param analysis MLAnalysis to be inserted
     * @throws DatabaseHandlerException
     */
    public void insertAnalysis(MLAnalysis analysis) throws DatabaseHandlerException;

    /**
     * Insert a new model into the database
     * @param model MLModelNew to be inserted
     * @throws DatabaseHandlerException
     */
    public void insertModel(MLModelNew model) throws DatabaseHandlerException;

    /**
     * Retrieves the path of the value-set having the given ID, from the
     * database.
     *
     * @param datasetVersionId  Unique Identifier of the dataset-version
     * @return                  Absolute path of a given dataset-version
     * @throws                  DatabaseHandlerException
     */
    public String getDatasetVersionUri(long datasetVersionId) throws DatabaseHandlerException;

    /**
     * @param datasetName   Name of the data-set
     * @param tenantId      Tenant Id
     * @return              Unique Id of the data-set
     * @throws              DatabaseHandlerException
     */
    public long getDatasetId(String datasetName, int tenantId, String userName) throws DatabaseHandlerException;

    /**
     * Get the dataset-version id
     * @param datasetVersionName name of the dataset-version
     * @param tenantId           tenant id
     * @return
     * @throws DatabaseHandlerException
     */
    public long getVersionsetId(String datasetVersionName, int tenantId) throws DatabaseHandlerException;

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
     * @param datasetVesionId   Unique Identifier of the value-set
     * @param valueSetSample    SamplePoints object of the value-set
     * @throws                  DatabaseHandlerException
     */
    public void updateVersionsetSample(long datasetVesionId, SamplePoints valueSetSample)
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
     * Update the database with the summary stats of data-set-version
     * @param datasetVersionId  Unique Id of the data-set-version
     * @param summaryStats      Summary stats
     * @throws DatabaseHandlerException
     */
    public void updateSummaryStatistics(long datasetSchemaId, long datasetVersionId, SummaryStats summaryStats) 
            throws DatabaseHandlerException;

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
     * Get the analysis Id of a given tenant id, username, analysis combination
     * @param tenantId
     * @param userName
     * @param analysisName
     * @return
     * @throws DatabaseHandlerException
     */
    public long getAnalysisId(int tenantId, String userName, String analysisName) throws DatabaseHandlerException;

    /**
     * Get the Id of the model
     * @param tenantId  Tenant Id
     * @param userName  Username
     * @param modelName Model name
     * @return
     * @throws DatabaseHandlerException
     */
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
     * Insert a list of Customized-Features into the database
     * @param analysisId           Analysis Id
     * @param customizedFeatures   MLCustomizedFeature list
     * @param tenantId             Tenant Id
     * @param userName             Username
     * @throws DatabaseHandlerException
     */
    public void insertFeatureCustomized(long analysisId, List<MLCustomizedFeature> customizedFeatures,int tenantId,
            String userName) throws DatabaseHandlerException;

    /**
     * Insert a list of ModelConfiguration into the database
     * @param analysisId         Analysis Id
     * @param modelConfigs       MLModelConfiguration list
     * @throws DatabaseHandlerException
     */
    public void insertModelConfigurations(long analysisId, List<MLModelConfiguration> modelConfigs)
            throws DatabaseHandlerException;

    /**
     * Insert a list of HyperParameters into the database
     * @param analysisId         Analysis Id
     * @param hyperParameters    MLHyperParameter list
     * @throws DatabaseHandlerException
     */
    public void insertHyperParameters(long analysisId, List<MLHyperParameter> hyperParameters) 
            throws DatabaseHandlerException;

    /**
     *
     * @param modelId
     * @param name
     * @return
     * @throws DatabaseHandlerException
     */
    public String getModelConfigurationValue(long modelId, String name) throws DatabaseHandlerException;

    /**
     * Update the model summary of a given model
     * @param modelId             model id
     * @param modelSummary        ModelSummary
     * @throws DatabaseHandlerException
     */
    public void updateModelSummary(long modelId, ModelSummary modelSummary) throws DatabaseHandlerException;

    /**
     * Update the storage details of a model
     * @param modelId            Model Id
     * @param storageType        Storage type
     * @param location           Storage location
     * @throws DatabaseHandlerException
     */
    public void updateModelStorage(long modelId, String storageType, String location) throws DatabaseHandlerException;

    /**
     * Check whether the given modelId is valid
     * @param tenantId
     * @param userName
     * @param modelId
     * @return
     * @throws DatabaseHandlerException
     */
    public boolean isValidModelId(int tenantId, String userName, long modelId) throws DatabaseHandlerException;

    /**
     * Insert cutomized feature to the database
     * @param analysisId           analysis id
     * @param customizedFeature    customized feature
     * @throws DatabaseHandlerException
     */
    public void insertFeatureCustomized(long analysisId, MLCustomizedFeature customizedFeature) 
            throws DatabaseHandlerException;

    /**
     * Get the id of the dataset-version used to generate the model
     * @param modelId  unique id of the model
     * @return
     * @throws DatabaseHandlerException
     */
    public long getDatasetVersionIdOfModel(long modelId) throws DatabaseHandlerException;

    /**
     * Insert data source to the database
     * @param datasetVersionId  unique id of the dataset-version
     * @param tenantId          tenant id
     * @param username          username
     * @param key               key
     * @param value             value
     * @throws DatabaseHandlerException
     */
    public void insertDataSource(long datasetVersionId, int tenantId, String username, String key, String value)
            throws DatabaseHandlerException;

    /**
     * Get the dataset-schema id of the dataset-version
     * @param datasetVersionId  unique id of the dataset version
     * @return
     * @throws DatabaseHandlerException
     */
    public long getDatasetId(long datasetVersionId) throws DatabaseHandlerException;

    /**
     * Get the data type of the model
     * @param modelId unique id of the model
     * @return
     * @throws DatabaseHandlerException
     */
    public String getDataTypeOfModel(long modelId) throws DatabaseHandlerException;

    public String getAStringModelConfiguration(long analysisId, String configKey) throws DatabaseHandlerException;

    public double getADoubleModelConfiguration(long modelId, String configKey) throws DatabaseHandlerException;

    public List<MLHyperParameter> getHyperParametersOfModel(long modelId) throws DatabaseHandlerException;

    public Map<String, String> getHyperParametersOfModelAsMap(long modelId) throws DatabaseHandlerException;

    public Workflow getWorkflow(long analysisId) throws DatabaseHandlerException;

    public MLStorage getModelStorage(long modelId) throws DatabaseHandlerException;

    public MLProject getProject(int tenantId, String userName, String projectName) throws DatabaseHandlerException;

    public List<MLProject> getAllProjects(int tenantId, String userName) throws DatabaseHandlerException;

    public MLAnalysis getAnalysis(int tenantId, String userName, String analysisName) throws DatabaseHandlerException;

    public List<MLAnalysis> getAllAnalyses(int tenantId, String userName) throws DatabaseHandlerException;

    public MLModelNew getModel(int tenantId, String userName, String modelName) throws DatabaseHandlerException;
    
    public MLModelNew getModel(int tenantId, String userName, long modelId) throws DatabaseHandlerException;

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

    void deleteModel(int tenantId, String userName, long modelId) throws DatabaseHandlerException;

}
