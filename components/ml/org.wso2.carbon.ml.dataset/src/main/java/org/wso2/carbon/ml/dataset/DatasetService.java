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
package org.wso2.carbon.ml.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONArray;
import org.wso2.carbon.ml.database.dto.FeatureSummary;
import org.wso2.carbon.ml.dataset.exceptions.DatasetServiceException;

/**
 * Interface contains the services related to importing and exploring a
 * data-set.
 */
public interface DatasetService {

    /**
     * Returns a absolute path of a given data source.
     *
     * @param datasetID Unique Identifier of the data-set
     * @return Absolute path of a given data-set
     * @throws DatasetServiceException
     */
    public String getDatasetUrl(String datasetID) throws DatasetServiceException;

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
    public String uploadDataset(InputStream sourceInputStream,  String fileName, String projectID) 
            throws DatasetServiceException, IOException;

    /**
     * 
     * @param filePath      Path of the dataset to calculate summary statistics
     * @param datasetID     Unique Identifier of the data-set
     * @param projectID     Unique Identifier of the project associated with the dataset
     * @return              Number of features in the data-set
     * @throws              DatasetServiceException
     */
    public int calculateSummaryStatistics(String filePath, String datasetID, String projectID) 
            throws DatasetServiceException;
            
    /**
     * Update the data type of a given feature.
     *
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param featureType   Updated type of the feature
     * @throws              DatasetServiceException
     */
    public void updateDataType(String featureName, String workflowID, String featureType)
            throws DatasetServiceException;

    /**
     * Update the impute option of a given feature.
     *
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param imputeOption  Updated impute option of the feature
     * @throws              DatasetServiceException
     */
    public void updateImputeOption(String featureName, String workflowID, String imputeOption)
            throws DatasetServiceException;

    /**
     * change whether a feature should be included as an input or not.
     *
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param isInput       Boolean value indicating whether the feature is an input or not
     * @throws              DatasetServiceException
     */ 
    public void updateIsIncludedFeature(String featureName, String workflowID, boolean isInput)
            throws DatasetServiceException;

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
    public List<FeatureSummary> getFeatures(String datasetID, String workflowID, int startIndex,
            int numberOfFeatures) throws DatasetServiceException;

    /**
     * Returns the names of the features, belongs to a particular data-type
     * (Categorical/Numerical), of the work-flow.
     *
     * @param workflowID    Unique identifier of the current work-flow
     * @param featureType   Data-type of the feature
     * @return              A list of feature names
     * @throws              DatasetServiceException
     */
    public List<String> getFeatureNames(String workflowID, String featureType)
            throws DatasetServiceException;

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
    public JSONArray getScatterPlotPoints(String datasetID, String xAxisFeature, String yAxisFeature,
            String groupByFeature) throws DatasetServiceException;

    /**
     * Returns the summary statistics for a given feature of a given data-set
     *
     * @param datasetID     Unique Identifier of the data-set
     * @param feature       Name of the feature of which summary statistics are needed
     * @return              JSON string containing the summary statistics
     * @throws              DatasetServiceException
     */
    public String getSummaryStats(String datasetID, String feature) throws DatasetServiceException;

    /**
     * Returns the number of features of a given data-set.
     *
     * @param datasetID     Unique Identifier of the data-set
     * @return              Number of features in the data-set
     * @throws              DatasetServiceException
     */
    public int getFeatureCount(String datasetID) throws DatasetServiceException;
    
    /**
     * Returns the model id associated with a given workflow Id
     * 
     * @param workflowId    Unique identifier of the workflow
     * @returns             Model id associated with this workflow id
     * @throws              org.wso2.carbon.ml.dataset.exceptions.DatasetServiceException
     */
    public String getModelId(String workflowId) throws DatasetServiceException;
}
