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
package org.wso2.carbon.ml.database.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.ml.commons.domain.FeatureSummary;
import org.wso2.carbon.ml.commons.domain.FeatureType;
import org.wso2.carbon.ml.commons.domain.ImputeOption;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.database.internal.constants.SQLQueries;
import org.wso2.carbon.ml.database.internal.ds.LocalDatabaseCreator;
import org.wso2.carbon.ml.database.internal.ds.MLDatabaseServiceValueHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MLDatabaseService implements DatabaseService{

    private static final Log logger = LogFactory.getLog(MLDatabaseService.class);
    private MLDataSource dbh;
    private AnalyticsDataService analyticsDataService;
    private static final String DB_CHECK_SQL = "SELECT * FROM ML_PROJECT";
    
    public MLDatabaseService () {
        analyticsDataService = MLDatabaseServiceValueHolder.getAnalyticsService();
        try {
            dbh = new MLDataSource();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        
        String value = System.getProperty("setup");
        if (value != null) {
            LocalDatabaseCreator databaseCreator = new LocalDatabaseCreator(dbh.getDataSource());
            try {
                if (!databaseCreator.isDatabaseStructureCreated(DB_CHECK_SQL)) {
                    databaseCreator.createRegistryDatabase();
                } else {
                    logger.info("Machine Learner database already exists. Not creating a new database.");
                }
            } catch (Exception e) {
                String msg = "Error in creating the Machine Learner database";
                throw new RuntimeException(msg, e);
            }
        }
        
    }

    /**
     * Retrieves the path of the value-set having the given ID, from the
     * database.
     *
     * @param valueSetId    Unique Identifier of the value-set
     * @return              Absolute path of a given value-set
     * @throws              DatabaseHandlerException
     */
    public String getValueSetUri(String valueSetId) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getStatement = connection.prepareStatement(SQLQueries.GET_VALUE_SET_LOCATION);
            getStatement.setString(1, valueSetId);
            result = getStatement.executeQuery();
            if (result.first()) {
                return result.getNString(1);
            } else {
                logger.error("Invalid value set ID: " + valueSetId);
                throw new DatabaseHandlerException("Invalid value set ID: " + valueSetId);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while reading the Value set " +
                    valueSetId + " from the database: " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }
    }

    /**
     *
     * @param name
     * @param tenantID
     * @param username
     * @param comments
     * @param sourceType
     * @param targetType
     * @param dataType
     * @throws              DatabaseHandlerException
     */
    public void insertDatasetDetails(String name, String tenantID, String username, String comments,
                                     String sourceType, String targetType, String dataType)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the data-set details to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_DATASET);
            insertStatement.setString(1, name);
            insertStatement.setString(2, tenantID);
            insertStatement.setString(3, username);
            insertStatement.setString(4, comments);
            insertStatement.setString(5, sourceType);
            insertStatement.setString(6, targetType);
            insertStatement.setString(7, dataType);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the details of data set");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occurred while inserting details of dataset " +
                            " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertDatasetVersionDetails(String datasetId, String tenantId, String version) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the data-set details to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_DATASET_VERSION);
            insertStatement.setString(1, datasetId);
            insertStatement.setString(2, tenantId);
            insertStatement.setString(3, version);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the details of data set version");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occurred while inserting details of dataset version " +
                            " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertFeatureDefaults(String datasetVersionId, String featureName, String type, int featureIndex, String summary)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the data-set details to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_FEATURE_DEFAULTS);
            insertStatement.setString(1, datasetVersionId);
            insertStatement.setString(2, featureName);
            insertStatement.setString(3, type);
            insertStatement.setInt(4, featureIndex);
            insertStatement.setString(5, summary);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the details of feature defaults");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occurred while inserting details of feature defaults " +
                            " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertValueSet(String datasetVersionId, String tenantId, String uri, SamplePoints samplePoints) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the data-set details to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_VALUE_SET);
            insertStatement.setString(1, datasetVersionId);
            insertStatement.setString(2, tenantId);
            insertStatement.setString(3, uri);
            insertStatement.setObject(4, samplePoints);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the value set");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occurred while inserting value set " +
                            " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    /**
     * Update the value-set table with a value-set sample.
     *
     * @param valueSet          Unique Identifier of the value-set
     * @param valueSetSample    SamplePoints object of the value-set
     * @throws                  DatabaseHandlerException
     */
    public void updateValueSetSample(String valueSet, SamplePoints valueSetSample)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_SAMPLE_POINTS);
            updateStatement.setObject(1, valueSetSample);
            updateStatement.setString(2, valueSet);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the sample of value set " + valueSet);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException( "An error occurred while updating the sample " +
                    "points of value set " + valueSet + ": " + e.getMessage(), e);
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
     * @param valueSetId        Unique Identifier of the value-set
     * @param xAxisFeature      Name of the feature to use as the x-axis
     * @param yAxisFeature      Name of the feature to use as the y-axis
     * @param groupByFeature    Name of the feature to be grouped by (color code)
     * @return                  A JSON array of data points
     * @throws                  DatabaseHandlerException
     */
    public JSONArray getScatterPlotPoints(String valueSetId, String xAxisFeature, String yAxisFeature,
                                          String groupByFeature) throws DatabaseHandlerException {

        // Get the sample from the database.
        SamplePoints sample = getValueSetSample(valueSetId);

        // Converts the sample to a JSON array.
        List<List<String>> columnData = sample.getSamplePoints();
        Map<String, Integer> dataHeaders = sample.getHeader();
        JSONArray samplePointsArray = new JSONArray();
        int firstFeatureColumn = dataHeaders.get(xAxisFeature);
        int secondFeatureColumn = dataHeaders.get(yAxisFeature);
        int thirdFeatureColumn = dataHeaders.get(groupByFeature);
        for (int row = 0; row < columnData.get(thirdFeatureColumn).size(); row++) {
            if (!columnData.get(firstFeatureColumn).get(row).isEmpty() &&
                    !columnData.get(secondFeatureColumn).get(row).isEmpty() &&
                    !columnData.get(thirdFeatureColumn).get(row).isEmpty()) {
                JSONArray point = new JSONArray();
                point.put(Double.parseDouble(columnData.get(firstFeatureColumn).get(row)));
                point.put(Double.parseDouble(columnData.get(secondFeatureColumn).get(row)));
                point.put(columnData.get(thirdFeatureColumn).get(row));
                samplePointsArray.put(point);
            }
        }

        return samplePointsArray;
    }

    /**
     * Returns sample data for selected features
     *
     * @param valueSet          Unique Identifier of the value-set
     * @param featureListString String containing feature name list
     * @return                  A JSON array of data points
     * @throws                  DatabaseHandlerException
     */
    public JSONArray getChartSamplePoints(String valueSet, String featureListString) throws DatabaseHandlerException {

        // Get the sample from the database.
        SamplePoints sample = getValueSetSample(valueSet);

        // Converts the sample to a JSON array.
        List<List<String>> columnData = sample.getSamplePoints();
        Map<String, Integer> dataHeaders = sample.getHeader();
        JSONArray samplePointsArray = new JSONArray();

        // split categoricalFeatureListString String into a String array
        String[] featureList = featureListString.split(",");

        // for each row in a selected categorical feature, iterate through all features
        for (int row = 0; row < columnData.get(dataHeaders.get(featureList[0])).size(); row++) {

            JSONObject point = new JSONObject();
            // for each categorical feature in same row put value into a point(JSONObject)
            // {"Soil_Type1":"0","Soil_Type11":"0","Soil_Type10":"0","Cover_Type":"4"}
            for (int featureCount = 0; featureCount < featureList.length; featureCount++) {
                point.put(featureList[featureCount],
                        columnData.get(dataHeaders.get(featureList[featureCount])).get(row));
            }
            samplePointsArray.put(point);
        }
        return samplePointsArray;
    }

    /**
     * Retrieve the SamplePoints object for a given value-set.
     *
     * @param valueSetId    Unique Identifier of the value-set
     * @return              SamplePoints object of the value-set
     * @throws              DatabaseHandlerException
     */
    private SamplePoints getValueSetSample(String valueSetId) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement updateStatement = null;
        ResultSet result = null;
        SamplePoints samplePoints = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            updateStatement = connection.prepareStatement(SQLQueries.GET_SAMPLE_POINTS);
            updateStatement.setString(1, valueSetId);
            result = updateStatement.executeQuery();
            if (result.first()) {
                samplePoints = (SamplePoints) result.getObject(1);
            }
            return samplePoints;
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while retrieving the sample of " +
                    "value set " + valueSetId + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement, result);
        }
    }

    /**
     * This method extracts and returns default features available in a given dataset version
     * @param datasetVersionId      The dataset version id
     * @return                      A list of FeatureSummary
     * @throws                      DatabaseHandlerException
     */
    @Override
    public List<FeatureSummary> getDefaultFeatures(String datasetVersionId, int startIndex, int numberOfFeatures)
            throws DatabaseHandlerException {

        List<FeatureSummary> features = new ArrayList<FeatureSummary>();
        Connection connection = null;
        PreparedStatement getDefaultFeatues = null;
        ResultSet result = null;

        try {
            connection = dbh.getDataSource().getConnection();
            getDefaultFeatues = connection.prepareStatement(SQLQueries.GET_DEFAULT_FEATURES);

            getDefaultFeatues.setString(1, datasetVersionId);
            getDefaultFeatues.setInt(2, numberOfFeatures);
            getDefaultFeatues.setInt(3, startIndex);

            result = getDefaultFeatues.executeQuery();

            while (result.next()) {
                String featureType = FeatureType.NUMERICAL;
                if (FeatureType.CATEGORICAL.equalsIgnoreCase(result.getString(3))) {
                    featureType = FeatureType.CATEGORICAL;
                }
                // Set the impute option
                String imputeOperation = ImputeOption.REPLACE_WTH_MEAN;

                String featureName = result.getString(1);

                // Setting up default include flag
                boolean isImportantFeature = true;

                String summaryStat = result.getString(2);

                features.add(new FeatureSummary(featureName, isImportantFeature, featureType, imputeOperation,
                        summaryStat));
            }
            return features;
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while retrieving features of " + "the data set version: "
                    + datasetVersionId + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getDefaultFeatues, result);
        }
    }

    /**
     * Retrieve and returns the Summary statistics for a given feature of a
     * given data-set version, from the database.
     *
     * @param datasetVersionId      Unique identifier of the data-set version
     * @param featureName           Name of the feature of which summary statistics are needed
     * @return                      JSON string containing the summary statistics
     * @throws                      DatabaseHandlerException
     */
    public String getSummaryStats(String datasetVersionId, String featureName)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement getSummaryStatement = null;
        ResultSet result = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getSummaryStatement = connection.prepareStatement(SQLQueries.GET_SUMMARY_STATS);
            getSummaryStatement.setString(1, featureName);
            getSummaryStatement.setString(2, datasetVersionId);
            result = getSummaryStatement.executeQuery();
            result.first();
            return result.getString(1);
        } catch (SQLException e) {
            throw new DatabaseHandlerException( "An error occurred while retrieving summary " +
                    "statistics for the feature \"" + featureName + "\" of the data set version " +
                    datasetVersionId + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, getSummaryStatement, result);
        }
    }

    /**
     * Returns the number of features of a given data-set version
     *
     * @param datasetVersionId     Unique identifier of the data-set version
     * @return                     Number of features in the data-set version
     * @throws                     DatabaseHandlerException
     */
    public int getFeatureCount(String datasetVersionId) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement getFeatues = null;
        ResultSet result = null;
        int featureCount = 0;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            // Create a prepared statement and extract data-set configurations.
            getFeatues = connection.prepareStatement(SQLQueries.GET_FEATURE_COUNT);
            getFeatues.setString(1, datasetVersionId);
            result = getFeatues.executeQuery();
            if (result.first()) {
                featureCount = result.getInt(1);
            }
            return featureCount;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(
                    "An error occurred while retrieving feature count of the dataset version " + datasetVersionId +
                            ": " + e.getMessage(), e);
        } finally {
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, getFeatues, result);
        }
    }

}
