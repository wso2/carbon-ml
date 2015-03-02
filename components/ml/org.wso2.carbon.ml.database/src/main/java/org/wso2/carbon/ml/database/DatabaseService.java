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

import org.json.JSONArray;
import org.wso2.carbon.ml.commons.domain.FeatureSummary;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

import java.util.List;

public interface DatabaseService {

    /**
     * Retrieves the path of the value-set having the given ID, from the
     * database.
     *
     * @param datasetID     Unique Identifier of the value-set
     * @return              Absolute path of a given value-set
     * @throws              DatabaseHandlerException
     */
    public String getValueSetUri(String datasetID) throws DatabaseHandlerException;

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
    public void insertDatasetDetails(String name, String tenantID, String username, String comments,
                                     String sourceType, String targetType, String dataType)
            throws DatabaseHandlerException;

    /**
     * Insert the data-set-version details to the database
     *
     * @param datasetId
     * @param tenantId
     * @param version
     * @throws DatabaseHandlerException
     */
    public void insertDatasetVersionDetails(String datasetId, String tenantId, String version)
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
    public void insertFeatureDefaults(String datasetVersionId, String featureName, String type, int featureIndex, String summary)
            throws DatabaseHandlerException;

    /**
     * Inser the value-set to the database
     * @param datasetVersionId
     * @param tenantId
     * @param uri
     * @param samplePoints
     * @throws DatabaseHandlerException
     */
    public void insertValueSet(String datasetVersionId, String tenantId, String uri, SamplePoints samplePoints)
            throws DatabaseHandlerException;

    /**
     * Update the value-set table with a data-set sample.
     *
     * @param valueSet          Unique Identifier of the value-set
     * @param valueSetSample    SamplePoints object of the value-set
     * @throws                  DatabaseHandlerException
     */
    public void updateValueSetSample(String valueSet, SamplePoints valueSetSample)
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
    public JSONArray getScatterPlotPoints(String valueSetId, String xAxisFeature, String yAxisFeature,
                                          String groupByFeature) throws DatabaseHandlerException;

    /**
     * Returns sample data for selected features
     *
     * @param valueSet          Unique Identifier of the value-set
     * @param featureListString String containing feature name list
     * @return                  A JSON array of data points
     * @throws                  DatabaseHandlerException
     */
    public JSONArray getChartSamplePoints(String valueSet, String featureListString)
            throws DatabaseHandlerException;

    /**
     * This method extracts and retures default features available in a given dataset version
     * @param datasetVersionId The dataset varsion id associated with this dataset version
     * @return                 A list of FeatureSummaries
     * @throws                 DatabaseHandlerException
     */
    public List<FeatureSummary> getDefaultFeatures(String datasetVersionId, int startIndex, int numberOfFeatures)
            throws DatabaseHandlerException;

    /**
     * Retrieve and returns the Summary statistics for a given feature of a
     * given data-set version, from the database
     *
     * @param datasetVersionId     Unique identifier of the data-set
     * @param featureName          Name of the feature of which summary statistics are needed
     * @return                     JSON string containing the summary statistics
     * @throws                     DatabaseHandlerException
     */
    public String getSummaryStats(String datasetVersionId, String featureName) throws DatabaseHandlerException;

    /**
     * Returns the number of features of a given data-set version
     *
     * @param datasetVersionId     Unique identifier of the data-set version
     * @return                     Number of features in the data-set version
     * @throws                     DatabaseHandlerException
     */
    public int getFeatureCount(String datasetVersionId) throws DatabaseHandlerException;
}
