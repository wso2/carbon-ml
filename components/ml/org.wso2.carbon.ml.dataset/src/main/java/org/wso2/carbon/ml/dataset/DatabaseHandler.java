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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.ml.dataset.constants.FeatureType;
import org.wso2.carbon.ml.dataset.constants.ImputeOption;
import org.wso2.carbon.ml.dataset.constants.SQLQueries;
import org.wso2.carbon.ml.dataset.dto.Feature;
import org.wso2.carbon.ml.dataset.dto.SamplePoints;
import org.wso2.carbon.ml.dataset.exceptions.DatabaseHandlerException;

/**
 * Class handles all the database operations in the DatasetService component.
 */
public class DatabaseHandler {

    private DataSource dataSource;
    private static final Log logger = LogFactory.getLog(DatabaseHandler.class);

    /**
     * Creates a singleton DatabaseHandler instance.
     *
     * @param mlDatabaseName JNDI lookup name of the ML Database
     * @throws DatabaseHandlerException
     */
    protected DatabaseHandler(String mlDatabaseName) throws DatabaseHandlerException {
        try {
            Context initContext = new InitialContext();
            this.dataSource = (DataSource) initContext.lookup(mlDatabaseName);
        } catch (Exception e) {
            throw new DatabaseHandlerException(
                "An error occured while connecting to ML database: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the path of the data-set having the given ID, from the
     * database.
     *
     * @param datasetID Unique Identifier of the data-set
     * @return Absolute path of a given data-set
     * @throws DatabaseHandlerException
     * @throws DatabaseHandlerException
     */
    protected String getDatasetUrl(String datasetID) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            getStatement = connection.prepareStatement(SQLQueries.GET_DATASET_LOCATION);
            getStatement.setString(1, datasetID);
            result = getStatement.executeQuery();
            if (result.first()) {
                return result.getNString(1);
            } else {
                logger.error("Invalid dataset ID: " + datasetID);
                throw new DatabaseHandlerException("Invalid dataset ID: " + datasetID);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occured while reading the Dataset " +
                    datasetID + " from the database: " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }
    }

    /**
     * Insert the new data-set details to the the database.
     *
     * @param datasetID Unique Identifier of the data-set
     * @param filePath Absolute path of the data-set
     * @param projectID Unique Identifier of the project
     * @throws DatabaseHandlerException
     */
    protected void insertDatasetDetails(String datasetID, String filePath, String projectID)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the data-set details to the database.
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_DATASET);
            insertStatement.setString(1, datasetID);
            insertStatement.setString(2, filePath);
            insertStatement.setString(3, projectID);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the details of data set " + filePath +
                    ". Dataset ID " + datasetID);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                "An error occured while inserting details of dataset " + datasetID +
                " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    /**
     * Update the data type of a given feature.
     *
     * @param featureName Name of the feature to be updated
     * @param workflowID Unique identifier of the current workflow
     * @param featureType Updated type of the feature
     * @throws DatabaseHandlerException
     */
    protected void updateDataType(String featureName, String workflowID, String featureType)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            // Update the database with data type.
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_DATA_TYPE);
            updateStatement.setString(1, featureType);
            updateStatement.setString(2, featureName);
            updateStatement.setString(3, workflowID);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the data-type of feature" + featureName +
                    " of workflow " + workflowID);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                "An error occured while updating the data type of feature \"" + featureName +
                "\" of workflow " + workflowID + ": " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * Update the impute method option of a given feature.
     *
     * @param featureName Name of the feature to be updated
     * @param workflowID Unique identifier of the current workflow
     * @param imputeOption Updated impute option of the feature
     * @throws DatabaseHandlerException
     */
    protected void updateImputeOption(String featureName, String workflowID, String imputeOption)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            // Update the database.
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_IMPUTE_METHOD);
            updateStatement.setString(1, imputeOption);
            updateStatement.setString(2, featureName);
            updateStatement.setString(3, workflowID);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the impute-option of feature" + featureName +
                    " of workflow " + workflowID);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occured while updating the feature \"" +
                    featureName + "\" of workflow " + workflowID + ": " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * Change whether a feature should be included as an input or not.
     *
     * @param featureName Name of the feature to be updated
     * @param workflowID Unique identifier of the current workflow
     * @param isInput Boolean value indicating whether the feature is an input or not
     * @throws DatabaseHandlerException
     */
    protected void updateIsIncludedFeature(String featureName, String workflowID, boolean isInput)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_IS_INCLUDED);
            updateStatement.setBoolean(1, isInput);
            updateStatement.setString(2, featureName);
            updateStatement.setString(3, workflowID);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the include-option of feature" + featureName +
                    "of workflow " + workflowID);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                "An error occured while updating the feature included option of feature \"" +
                        featureName + "\" of workflow " + workflowID + ": " + e, e);
        } finally {
            // Enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * Update the database with all the summary statistics of the sample.
     *
     * @param datasetID Unique Identifier of the data-set
     * @param header Array of names of features
     * @param type Array of data-types of each feature
     * @param graphFrequencies List of Maps containing frequencies for graphs, of each feature
     * @param missing Array of Number of missing values in each feature
     * @param unique Array of Number of unique values in each feature
     * @param descriptiveStats Array of descriptiveStats object of each feature
     * @param include Default value to set for the flag indicating the feature is an input or not
     * @throws DatabaseHandlerException
     */
    protected void updateSummaryStatistics(String datasetID, String[] header, String[] type,
        List<SortedMap<?, Integer>> graphFrequencies, int[] missing, int[] unique,
        List<DescriptiveStatistics> descriptiveStats, Boolean include)
                throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            JSONArray summaryStat;
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            for (int column = 0; column < header.length; column++) {
                // Get the JSON representation of the column summary.
                summaryStat = createJson(type[column], graphFrequencies.get(column), missing[column],
                    unique[column], descriptiveStats.get(column));

                // Put the values to the database table. If the feature already exists, updates
                // the row. If not, inserts as a new row.
                updateStatement = connection.prepareStatement(SQLQueries.UPDATE_SUMMARY_STATS);
                updateStatement.setString(1, header[column]);
                updateStatement.setString(2, datasetID);
                updateStatement.setString(3, summaryStat.toString());
                updateStatement.setString(4, type[column].toString());
                updateStatement.setString(5, ImputeOption.DISCARD.toString());
                updateStatement.setBoolean(6, include);
                updateStatement.execute();
            }
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the summary statistics for dataset " + datasetID);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occured while updating the database " +
                    "with summary statistics of the dataset " + datasetID + ": " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * Create the JSON string with summary statistics for a column.
     *
     * @param type Data-type of the column
     * @param graphFrequencies Bin frequencies of the column
     * @param missing Number of missing values in the column
     * @param unique Number of unique values in the column
     * @param descriptiveStats DescriptiveStats object of the column
     * @return JSON representation of the summary statistics of the column
     */
    private JSONArray createJson(String type, SortedMap<?, Integer> graphFrequencies,
        int missing, int unique, DescriptiveStatistics descriptiveStats) {
        JSONObject json = new JSONObject();
        JSONArray freqs = new JSONArray();
        Object[] categoryNames = graphFrequencies.keySet().toArray();
        // Create an array with intervals/categories and their frequencies.
        for (int i = 0; i < graphFrequencies.size(); i++) {
            JSONArray temp = new JSONArray();
            temp.put(categoryNames[i].toString());
            temp.put(graphFrequencies.get(categoryNames[i]));
            freqs.put(temp);
        }
        // Put the statistics to a json object
        json.put("unique", unique);
        json.put("missing", missing);

        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        if (descriptiveStats.getN() != 0) {
            json.put("mean", decimalFormat.format(descriptiveStats.getMean()));
            json.put("median", decimalFormat.format(descriptiveStats.getPercentile(50)));
            json.put("std", decimalFormat.format(descriptiveStats.getStandardDeviation()));
            if (type.equals(FeatureType.NUMERICAL)) {
                json.put("skewness", decimalFormat.format(descriptiveStats.getSkewness()));
            }
        }
        json.put("values", freqs);
        json.put("bar", true);
        json.put("key", "Frequency");
        JSONArray summaryStatArray = new JSONArray();
        summaryStatArray.put(json);
        return summaryStatArray;
    }

    /**
     * Update the data-set table with a data-set sample.
     *
     * @param datasetID Unique Identifier of the data-set
     * @param datasetSample SamplePoints object of the data-set
     * @throws DatabaseHandlerException
     */
    protected void updateDatasetSample(String datasetID, SamplePoints datasetSample)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_SAMPLE_POINTS);
            updateStatement.setObject(1, datasetSample);
            updateStatement.setString(2, datasetID);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the sample of dataset " + datasetID);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException( "An error occurred while updating the sample " +
                    "points of dataset " + datasetID + ": " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * Returns data points of the selected sample as coordinates of three
     * features, needed for the scatter plot.
     *
     * @param datasetID Unique Identifier of the data-set
     * @param xAxisFeature Name of the feature to use as the x-axis
     * @param yAxisFeature Name of the feature to use as the y-axis
     * @param groupByFeature Name of the feature to be grouped by (color code)
     * @return A JSON array of data points
     * @throws DatabaseHandlerException
     */
    protected JSONArray getScatterPlotPoints(String datasetID, String xAxisFeature, String yAxisFeature,
        String groupByFeature) throws DatabaseHandlerException {
        // Get the sample from the database.
        SamplePoints sample = getDatasetSample(datasetID);

        // Converts the sample to a JSON array.
        List<List<String>> columnData = sample.getSamplePoints();
        Map<String, Integer> dataHeaders = sample.getHeader();
        Map<String, JSONObject> pointsGroups = new HashMap<String, JSONObject>();
        JSONArray samplePointsArray = new JSONArray();
        int firstFeatureColumn = dataHeaders.get(xAxisFeature);
        int secondFeatureColumn = dataHeaders.get(yAxisFeature);
        int thirdFeatureColumn = dataHeaders.get(groupByFeature);
        // create JSON Objects for each category
        for (int row = 0; row < columnData.get(thirdFeatureColumn).size(); row++) {
            if (!columnData.get(firstFeatureColumn).get(row).isEmpty() &&
                    !columnData.get(secondFeatureColumn).get(row).isEmpty() &&
                    !columnData.get(thirdFeatureColumn).get(row).isEmpty()) {
                String category =columnData.get(thirdFeatureColumn).get(row);
                JSONObject point = new JSONObject();
                point.put("x", Double.parseDouble(columnData.get(firstFeatureColumn).get(row)));
                point.put("y", Double.parseDouble(columnData.get(secondFeatureColumn).get(row)));
                if(pointsGroups.containsKey(category)){
                    pointsGroups.get(category).getJSONArray("values").put(point);
                } else {
                    JSONObject group = new JSONObject();
                    group.put("key", category);
                    group.put("values", new JSONArray("[" + point.toString() + "]"));
                    pointsGroups.put(category, group);
                }
            }
        }
        // Put all JSON Objects to a JSON Array
        for(String groupName : pointsGroups.keySet()){
            samplePointsArray.put(pointsGroups.get(groupName));
        }
        return samplePointsArray;
    }

    /**
     * Retrieve the SamplePoints object for a given data-set.
     *
     * @param datasetID Unique Identifier of the data-set
     * @return SamplePoints object of the data-set
     * @throws DatabaseHandlerException
     */
    private SamplePoints getDatasetSample(String datasetID) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        ResultSet result = null;
        SamplePoints samplePoints = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            updateStatement = connection.prepareStatement(SQLQueries.GET_SAMPLE_POINTS);
            updateStatement.setString(1, datasetID);
            result = updateStatement.executeQuery();
            if (result.first()) {
                samplePoints = (SamplePoints) result.getObject(1);
            }
            return samplePoints;
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occured while retrieving the sample of " +
                    "dataset " + datasetID + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement, result);
        }
    }

    /**
     * Returns a set of features in a given range, from the alphabetically ordered set
     * of features, of a data-set.
     *
     * @param datasetID Unique Identifier of the data-set
     * @param startIndex Starting index of the set of features needed
     * @param numberOfFeatures Number of features needed, from the starting index
     * @return A list of Feature objects
     * @throws DatabaseHandlerException
     */
    protected List<Feature> getFeatures(String datasetID, String workflowID, int startIndex,
        int numberOfFeatures) throws DatabaseHandlerException {
        List<Feature> features = new ArrayList<Feature>();
        Connection connection = null;
        PreparedStatement getFeatues = null;
        ResultSet result = null;
        try {
            // Create a prepared statement and retrieve data-set configurations.
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            getFeatues = connection.prepareStatement(SQLQueries.GET_FEATURES);
            getFeatues.setString(1, datasetID);
            getFeatues.setString(2, datasetID);
            getFeatues.setString(3, workflowID);
            getFeatues.setInt(4, numberOfFeatures);
            getFeatues.setInt(5, startIndex);
            result = getFeatues.executeQuery();
            while (result.next()) {
                String featureType = FeatureType.NUMERICAL;
                if (FeatureType.CATEGORICAL.toString().equalsIgnoreCase(result.getString(3))) {
                    featureType = FeatureType.CATEGORICAL;
                }
                // Set the impute option
                String imputeOperation = ImputeOption.DISCARD;
                if (ImputeOption.REPLACE_WTH_MEAN.equalsIgnoreCase(result.getString(5))) {
                    imputeOperation = ImputeOption.REPLACE_WTH_MEAN;
                } else if (ImputeOption.REGRESSION_IMPUTATION.equalsIgnoreCase(
                    result.getString(5))) {
                    imputeOperation = ImputeOption.REGRESSION_IMPUTATION;
                }
                String featureName = result.getString(1);
                boolean isImportantFeature = result.getBoolean(4);
                String summaryStat = result.getString(2);

                features.add(new Feature(featureName, isImportantFeature, featureType,
                    imputeOperation, summaryStat));
            }
            return features;
        } catch (SQLException e) {
            throw new DatabaseHandlerException( "An error occured while retrieving features of " +
                    "the data set: " + datasetID + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getFeatues, result);
        }
    }

    /**
     * Returns the names of the features, belongs to a particular data-type
     * (Categorical/Numerical), of the work-flow.
     *
     * @param workflowID Unique identifier of the current work-flow
     * @param featureType Data-type of the feature
     * @return A list of feature names
     * @throws DatabaseHandlerException
     */
    protected List<String> getFeatureNames(String workflowID, String featureType)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement getFeatureNamesStatement = null;
        ResultSet result = null;
        List<String> featureNames = new ArrayList<String>();
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            // Create a prepared statement and retrieve data-set configurations.
            getFeatureNamesStatement = connection.prepareStatement(SQLQueries.GET_FEATURE_NAMES);
            getFeatureNamesStatement.setString(1, workflowID);
            // Select the data type.
            if (featureType.equalsIgnoreCase(FeatureType.CATEGORICAL)) {
                getFeatureNamesStatement.setString(2, FeatureType.CATEGORICAL);
            } else {
                getFeatureNamesStatement.setString(2, FeatureType.NUMERICAL);
            }
            result = getFeatureNamesStatement.executeQuery();
            // Convert the result in to a string array to e returned.
            while (result.next()) {
                featureNames.add(result.getString(1));
            }
            return featureNames;
        } catch (SQLException e) {
            throw new DatabaseHandlerException( "An error occurred while retrieving feature " +
                    "names of the dataset for workflow: " + workflowID + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getFeatureNamesStatement, result);
        }
    }

    /**
     * Retrieve and returns the Summary statistics for a given feature of a
     * given data-set, from the database.
     *
     * @param datasetID Unique identifier of the data-set
     * @param featureName Name of the feature of which summary statistics are needed
     * @return JSON string containing the summary statistics
     * @throws DatabaseHandlerException
     */
    protected String getSummaryStats(String datasetID, String featureName)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement getSummaryStatement = null;
        ResultSet result = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            getSummaryStatement = connection.prepareStatement(SQLQueries.GET_SUMMARY_STATS);
            getSummaryStatement.setString(1, featureName);
            getSummaryStatement.setString(2, datasetID);
            result = getSummaryStatement.executeQuery();
            result.first();
            return result.getString(1);
        } catch (SQLException e) {
            throw new DatabaseHandlerException( "An error occured while retireving summary " +
                    "statistics for the feature \"" + featureName + "\" of the data set " +
                    datasetID + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, getSummaryStatement, result);
        }
    }

    /**
     * Returns the number of features of a given data-set.
     *
     * @param datasetID Unique identifier of the data-set
     * @return Number of features in the data-set
     * @throws DatabaseHandlerException
     */
    protected int getFeatureCount(String datasetID) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement getFeatues = null;
        ResultSet result = null;
        int featureCount = 0;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            // Create a prepared statement and extract data-set configurations.
            getFeatues = connection.prepareStatement(SQLQueries.GET_FEATURE_COUNT);
            getFeatues.setString(1, datasetID);
            result = getFeatues.executeQuery();
            if (result.first()) {
                featureCount = result.getInt(1);
            }
            return featureCount;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(
                "An error occured while retireving feature count of the dataset " + datasetID +
                ": " + e.getMessage(), e);
        } finally {
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, getFeatues, result);
        }
    }
}
