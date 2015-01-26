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
import org.wso2.carbon.ml.database.dto.*;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public interface DatabaseService {
    
    /**
     * Retrieves the path of the data-set having the given ID, from the
     * database.
     *
     * @param datasetID     Unique Identifier of the data-set
     * @return              Absolute path of a given data-set
     * @throws              DatabaseHandlerException
     */
    public String getDatasetUrl(String datasetID) throws DatabaseHandlerException;

    /**
     * Insert the new data-set details to the the database.
     *
     * @param datasetID     Unique Identifier of the data-set
     * @param filePath      Absolute path of the data-set
     * @param projectID     Unique Identifier of the project
     * @throws              DatabaseHandlerException
     */
    public void insertDatasetDetails(String datasetID, String filePath, String projectID)
            throws DatabaseHandlerException;

    /**
     * Update the data type of a given feature.
     *
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param featureType   Updated type of the feature
     * @throws              DatabaseHandlerException
     */
    public void updateDataType(String featureName, String workflowID, String featureType)
            throws DatabaseHandlerException;

    /**
     * Update the impute method option of a given feature.
     *
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param imputeOption  Updated impute option of the feature
     * @throws              DatabaseHandlerException
     */
    public void updateImputeOption(String featureName, String workflowID, String imputeOption)
            throws DatabaseHandlerException;

    /**
     * Change whether a feature should be included as an input or not.
     *
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param isInput       Boolean value indicating whether the feature is an input or not
     * @throws              DatabaseHandlerException
     */
    public void updateIsIncludedFeature(String featureName, String workflowID, boolean isInput)
            throws DatabaseHandlerException;

    /**
     * Update the database with all the summary statistics of the sample.
     *
     * @param datasetID         Unique Identifier of the data-set
     * @param headerMap         Array of names of features
     * @param type              Array of data-types of each feature
     * @param graphFrequencies  List of Maps containing frequencies for graphs, of each feature
     * @param missing           Array of Number of missing values in each feature
     * @param unique            Array of Number of unique values in each feature
     * @param descriptiveStats  Array of descriptiveStats object of each feature
     * @param                   include Default value to set for the flag indicating the feature is an input or not
     * @throws                  DatabaseHandlerException
     */
    public void updateSummaryStatistics(String datasetID,  Map<String, Integer> headerMap, String[] type,
        List<SortedMap<?, Integer>> graphFrequencies, int[] missing, int[] unique,
        List<DescriptiveStatistics> descriptiveStats, Boolean include)
                throws DatabaseHandlerException;

    /**
     * Update the data-set table with a data-set sample.
     *
     * @param datasetID         Unique Identifier of the data-set
     * @param datasetSample     SamplePoints object of the data-set
     * @throws                  DatabaseHandlerException
     */
    public void updateDatasetSample(String datasetID, SamplePoints datasetSample)
            throws DatabaseHandlerException;

    /**
     * Returns data points of the selected sample as coordinates of three
     * features, needed for the scatter plot.
     *
     * @param datasetID         Unique Identifier of the data-set
     * @param xAxisFeature      Name of the feature to use as the x-axis
     * @param yAxisFeature      Name of the feature to use as the y-axis
     * @param groupByFeature    Name of the feature to be grouped by (color code)
     * @return                  A JSON array of data points
     * @throws                  DatabaseHandlerException
     */
    public JSONArray getScatterPlotPoints(String datasetID, String xAxisFeature, String yAxisFeature,
        String groupByFeature) throws DatabaseHandlerException;

	/**
	 * Returns sample data for selected features
	 * 
	 * @param datasetID
	 *            Unique Identifier of the data-set
	 * @param featureListString
	 *            String containing feature name list
	 * @return A JSON array of data points
	 * @throws DatasetServiceException
	 */
	public JSONArray getChartSamplePoints(String datasetID, String featureListString)
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
    public List<FeatureSummary> getFeatures(String datasetID, String workflowID, int startIndex,
        int numberOfFeatures) throws DatabaseHandlerException;

    /**
     * Returns the names of the features, belongs to a particular data-type
     * (Categorical/Numerical), of the work-flow.
     *
     * @param workflowID    Unique identifier of the current work-flow
     * @param featureType   Data-type of the feature
     * @return              A list of feature names
     * @throws              DatabaseHandlerException
     */
    public List<String> getFeatureNames(String workflowID, String featureType)
            throws DatabaseHandlerException;

    /**
     * Retrieve and returns the Summary statistics for a given feature of a
     * given data-set, from the database.
     *
     * @param datasetID     Unique identifier of the data-set
     * @param featureName   Name of the feature of which summary statistics are needed
     * @return              JSON string containing the summary statistics
     * @throws              DatabaseHandlerException
     */
    public String getSummaryStats(String datasetID, String featureName)
            throws DatabaseHandlerException;

    /**
     * Returns the number of features of a given data-set.
     *
     * @param datasetID     Unique identifier of the data-set
     * @return              Number of features in the data-set
     * @throws              DatabaseHandlerException
     */
    public int getFeatureCount(String datasetID) throws DatabaseHandlerException;
    
    /**
     * Returns model id associated with given workflow id
     * 
     * @param workflowId    Unique identifier of the work-flow
     * @return model id     Unique identifier of the model associated with the work-flow
     * @throws              DatabaseHandlerException
     */
    public String getModelId(String workflowId) throws DatabaseHandlerException;

    /**
     *
     * @param modelSettingsID
     * @param workflowID
     * @param algorithmName
     * @param algorithmClass
     * @param response
     * @param trainDataFraction
     * @param hyperparameters
     * @throws DatabaseHandlerException
     */
    public void insertModelSettings(String modelSettingsID, String workflowID, String
            algorithmName, String algorithmClass, String response, double trainDataFraction,
            List<HyperParameter> hyperparameters) throws DatabaseHandlerException;

    /**
     *
     * @param modelID
     * @param workflowID
     * @param executionStartTime
     * @throws DatabaseHandlerException
     */
    public void insertModel(String modelID, String workflowID, Time executionStartTime)
            throws DatabaseHandlerException;

    /**
     *
     * @param modelID
     * @param model
     * @param modelSummary
     * @param executionEndTime
     * @param model
     * @throws DatabaseHandlerException
     */
    public void updateModel(String modelID, MLModel model,
                                ModelSummary modelSummary, Time executionEndTime)
            throws DatabaseHandlerException;

    /**
     *
     * @param modelID
     * @return
     * @throws DatabaseHandlerException
     */
    public ModelSummary getModelSummary(String modelID) throws DatabaseHandlerException;

    /**
     *
     * @param workflowID
     * @return
     * @throws DatabaseHandlerException
     */
    public Workflow getWorkflow(String workflowID) throws DatabaseHandlerException;

    /**
     *
     * @param modelId
     * @return
     * @throws DatabaseHandlerException
     */
    public long getModelExecutionEndTime(String modelId) throws DatabaseHandlerException;

    /**
     *
     * @param modelId
     * @return
     * @throws DatabaseHandlerException
     */
    public long getModelExecutionStartTime(String modelId) throws DatabaseHandlerException;

    /**
     *
     * @param projectId
     * @return
     * @throws DatabaseHandlerException
     */
    public String getDatasetId(String projectId) throws DatabaseHandlerException;
}
