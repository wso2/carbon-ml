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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.commons.domain.*;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.database.internal.constants.SQLQueries;
import org.wso2.carbon.ml.database.internal.ds.LocalDatabaseCreator;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Map.Entry;

public class MLDatabaseService implements DatabaseService{

    private static final Log logger = LogFactory.getLog(MLDatabaseService.class);
    private MLDataSource dbh;
    private static final String DB_CHECK_SQL = "SELECT * FROM ML_PROJECT";
    
    public MLDatabaseService () {
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
     * Retrieves the path of the data-set having the given ID, from the
     * database.
     *
     * @param datasetID     Unique Identifier of the data-set
     * @return              Absolute path of a given data-set
     * @throws              DatabaseHandlerException
     */
    public String getDatasetUrl(String datasetID) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
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
            throw new DatabaseHandlerException("An error occurred while reading the Dataset " +
                    datasetID + " from the database: " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }
    }

    /**
     * Insert the new data-set details to the the database.
     *
     * @param datasetID     Unique Identifier of the data-set
     * @param filePath      Absolute path of the data-set
     * @param projectID     Unique Identifier of the project
     * @throws              DatabaseHandlerException
     */
    public void insertDatasetDetails(String datasetID, String filePath, String projectID)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the data-set details to the database.
            connection = dbh.getDataSource().getConnection();
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
                "An error occurred while inserting details of dataset " + datasetID +
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
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param featureType   Updated type of the feature
     * @throws              DatabaseHandlerException
     */
    public void updateDataType(String featureName, String workflowID, String featureType)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            // Update the database with data type.
            connection = dbh.getDataSource().getConnection();
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
                "An error occurred while updating the data type of feature \"" + featureName +
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
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param imputeOption  Updated impute option of the feature
     * @throws              DatabaseHandlerException
     */
    public void updateImputeOption(String featureName, String workflowID, String imputeOption)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            // Update the database.
            connection = dbh.getDataSource().getConnection();
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
            throw new DatabaseHandlerException("An error occurred while updating the feature \"" +
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
     * @param featureName   Name of the feature to be updated
     * @param workflowID    Unique identifier of the current workflow
     * @param isInput       Boolean value indicating whether the feature is an input or not
     * @throws              DatabaseHandlerException
     */
    public void updateIsIncludedFeature(String featureName, String workflowID, boolean isInput)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
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
                "An error occurred while updating the feature included option of feature \"" +
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
                throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            JSONArray summaryStat;
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            int columnIndex;
            for (Entry<String, Integer> columnNameMapping : headerMap.entrySet()) {
                columnIndex = columnNameMapping.getValue();
                // Get the JSON representation of the column summary.
                summaryStat = createJson(type[columnIndex], graphFrequencies.get(columnIndex), missing[columnIndex],
                    unique[columnIndex], descriptiveStats.get(columnIndex));
                // Put the values to the database table. If the feature already exists, updates
                // the row. If not, inserts as a new row.
                updateStatement = connection.prepareStatement(SQLQueries.UPDATE_SUMMARY_STATS);
                updateStatement.setString(1, columnNameMapping.getKey());
                updateStatement.setInt(2, columnIndex);
                updateStatement.setString(3, datasetID);
                updateStatement.setString(4, summaryStat.toString());
                updateStatement.setString(5, type[columnIndex].toString());
                updateStatement.setString(6, ImputeOption.DISCARD.toString());
                updateStatement.setBoolean(7, include);
                updateStatement.execute();
            }
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the summary statistics for dataset " + datasetID);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while updating the database " +
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
     * @param type              Data-type of the column
     * @param graphFrequencies  Bin frequencies of the column
     * @param missing           Number of missing values in the column
     * @param unique            Number of unique values in the column
     * @param descriptiveStats  DescriptiveStats object of the column
     * @return                  JSON representation of the summary statistics of the column
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
            if (type.equalsIgnoreCase(FeatureType.NUMERICAL)) {
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
     * @param datasetID         Unique Identifier of the data-set
     * @param datasetSample     SamplePoints object of the data-set
     * @throws                  DatabaseHandlerException
     */
    public void updateDatasetSample(String datasetID, SamplePoints datasetSample)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
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
     * @param datasetID         Unique Identifier of the data-set
     * @param xAxisFeature      Name of the feature to use as the x-axis
     * @param yAxisFeature      Name of the feature to use as the y-axis
     * @param groupByFeature    Name of the feature to be grouped by (color code)
     * @return                  A JSON array of data points
     * @throws                  DatabaseHandlerException
     */
    public JSONArray getScatterPlotPoints(String datasetID, String xAxisFeature, String yAxisFeature,
        String groupByFeature) throws DatabaseHandlerException {

        // Get the sample from the database.
        SamplePoints sample = getDatasetSample(datasetID);

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
     * @param datasetID
     *            Unique Identifier of the data-set
     * @param featureListString
     *            String containing feature name list
     * @return A JSON array of data points
     * @throws DatasetServiceException
     */
    public JSONArray getChartSamplePoints(String datasetID, String featureListString)
                                                                                        throws DatabaseHandlerException {
        // Get the sample from the database.
        SamplePoints sample = getDatasetSample(datasetID);

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
     * Retrieve the SamplePoints object for a given data-set.
     *
     * @param datasetID     Unique Identifier of the data-set
     * @return              SamplePoints object of the data-set
     * @throws              DatabaseHandlerException
     */
    private SamplePoints getDatasetSample(String datasetID) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        ResultSet result = null;
        SamplePoints samplePoints = null;
        try {
            connection = dbh.getDataSource().getConnection();
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
            throw new DatabaseHandlerException("An error occurred while retrieving the sample of " +
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
     * @param datasetID         Unique Identifier of the data-set
     * @param startIndex        Starting index of the set of features needed
     * @param numberOfFeatures  Number of features needed, from the starting index
     * @return                  A list of Feature objects
     * @throws                  DatabaseHandlerException
     */
    public List<FeatureSummary> getFeatures(String datasetID, String workflowID, int startIndex,
        int numberOfFeatures) throws DatabaseHandlerException {
        List<FeatureSummary> features = new ArrayList<FeatureSummary>();
        Connection connection = null;
        PreparedStatement getFeatues = null;
        ResultSet result = null;
        try {
            // Create a prepared statement and retrieve data-set configurations.
            connection = dbh.getDataSource().getConnection();
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

                features.add(new FeatureSummary(featureName, isImportantFeature, featureType,
                    imputeOperation, summaryStat));
            }
            return features;
        } catch (SQLException e) {
            throw new DatabaseHandlerException( "An error occurred while retrieving features of " +
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
     * @param workflowID    Unique identifier of the current work-flow
     * @param featureType   Data-type of the feature
     * @return              A list of feature names
     * @throws              DatabaseHandlerException
     */
    public List<String> getFeatureNames(String workflowID, String featureType)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement getFeatureNamesStatement = null;
        ResultSet result = null;
        List<String> featureNames = new ArrayList<String>();
        try {
            connection = dbh.getDataSource().getConnection();
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
     * @param datasetID     Unique identifier of the data-set
     * @param featureName   Name of the feature of which summary statistics are needed
     * @return              JSON string containing the summary statistics
     * @throws              DatabaseHandlerException
     */
    public String getSummaryStats(String datasetID, String featureName)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement getSummaryStatement = null;
        ResultSet result = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getSummaryStatement = connection.prepareStatement(SQLQueries.GET_SUMMARY_STATS);
            getSummaryStatement.setString(1, featureName);
            getSummaryStatement.setString(2, datasetID);
            result = getSummaryStatement.executeQuery();
            result.first();
            return result.getString(1);
        } catch (SQLException e) {
            throw new DatabaseHandlerException( "An error occurred while retireving summary " +
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
     * @param datasetID     Unique identifier of the data-set
     * @return              Number of features in the data-set
     * @throws              DatabaseHandlerException
     */
    public int getFeatureCount(String datasetID) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement getFeatues = null;
        ResultSet result = null;
        int featureCount = 0;
        try {
            connection = dbh.getDataSource().getConnection();
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
                "An error occurred while retrieving feature count of the dataset " + datasetID +
                ": " + e.getMessage(), e);
        } finally {
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, getFeatues, result);
        }
    }

    /**
     * Returns model id associated with given workflow id
     * 
     * @param workflowId    Unique identifier of the work-flow
     * @return model id     Unique identifier of the model associated with the work-flow
     * @throws              DatabaseHandlerException
     */
    public String getModelId(String workflowId) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement model = null;
        ResultSet result = null;

        try {
            connection = dbh.getDataSource().getConnection();
            model = connection.prepareStatement(SQLQueries.GET_MODEL_ID);
            model.setString(1, workflowId);
            result = model.executeQuery();
            if(!result.first()) {
                // need to query ML_MODEL table, just before model building process is started
                // to overcome building same model two (or more) times.
                // hence, null will be checked in UI.
                return null;
            }
            return result.getString(1);

        } catch (SQLException e){
            throw new DatabaseHandlerException(
                "An error occurred white retrieving model associated with workflow id "+ workflowId +
                ":" + e.getMessage(), e);
        } finally {
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, model, result);
        }
    }
    
    /**
     * This method returns machine learning model
     *
     * @param modelID Model ID
     * @return {@link MLModel} instance
     * @throws DatabaseHandlerException
     */
    public MLModel getModel(String modelID) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            getStatement = connection.prepareStatement(SQLQueries.GET_MODEL);
            getStatement.setString(1, modelID);
            result = getStatement.executeQuery();
            if (result.first()) {
                return (MLModel) result.getObject(1);
            } else {
                throw new DatabaseHandlerException("Invalid model ID: " + modelID);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while reading model for " +
                    modelID + " from the database: " + e.getMessage(),
                    e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }

    }
    
    /**
     * This method inserts model settings to database
     *
     * @param modelSettingsID   Model settings ID
     * @param workflowID        Workflow ID
     * @param algorithmName     Machine learning algorithm name
     * @param algorithmClass    Type of machine learning algorithm: e.g. Classification
     * @param response          Name of the response variable
     * @param trainDataFraction Training data fraction
     * @param hyperparameters   Hyper-parameters
     * @throws DatabaseHandlerException
     */
    public void insertModelSettings(String modelSettingsID, String workflowID, String
            algorithmName, String algorithmClass, String response, double trainDataFraction,
                                    List<HyperParameter> hyperparameters)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // insert model settings to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_ML_MODEL_SETTINGS);
            insertStatement.setString(1, modelSettingsID);
            insertStatement.setString(2, workflowID);
            insertStatement.setString(3, algorithmName);
            insertStatement.setString(4, algorithmClass);
            insertStatement.setString(5, response);
            insertStatement.setDouble(6, trainDataFraction);
            insertStatement.setObject(7, hyperparameters);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Successfully updated model settings for model settings id " +
                                modelSettingsID);
            }
        } catch (SQLException e) {
            // rollback the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occurred while inserting model settings for model settings id " +
                            modelSettingsID + " to the database: " + e.getMessage(), e);
        } finally {
            // enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    /**
     * This method initialize insering model into the database
     *
     * @param modelID            Model ID
     * @param workflowID         Workflow ID
     * @param executionStartTime Model execution start time
     * @throws DatabaseHandlerException
     */
    public void insertModel(String modelID, String workflowID, Time executionStartTime)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // insert model settings to the database.
            connection = dbh.getDataSource().getConnection();

            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_ML_MODEL);
            insertStatement.setString(1, modelID);
            insertStatement.setString(2, workflowID);
            insertStatement.setTime(3, executionStartTime);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted model details for model id " + modelID);
            }
        } catch (SQLException e) {
            // rollback the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occurred while inserting model details for model id " + modelID + " " +
                            "to the database: " + e.getMessage(),
                    e);
        } finally {
            // enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }
    /**
     * This method inserts model and model summary into the database
     *
     * @param modelID          Model ID
     * @param model            Machine learning model
     * @param modelSummary     Machine learning model summary
     * @param executionEndTime Model execution end time
     * @param model            Machine learning  model
     * @throws DatabaseHandlerException
     */
    public void updateModel(String modelID, MLModel model,
                                ModelSummary modelSummary, Time executionEndTime)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_ML_MODEL);
            updateStatement.setObject(1, model);
            updateStatement.setObject(2, modelSummary);
            updateStatement.setTime(3, executionEndTime);
            updateStatement.setString(4, modelID);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the details of model: model ID" + modelID);
            }

        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occurred while updating the details of model id " + modelID + " : "
                            + e.getMessage(), e);

        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * This method returns machine learning model summary
     *
     * @param modelID Model ID
     * @return Model summary
     * @throws DatabaseHandlerException
     */
    public ModelSummary getModelSummary(String modelID) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            getStatement = connection.prepareStatement(SQLQueries.GET_MODEL_SUMMARY);
            getStatement.setString(1, modelID);
            result = getStatement.executeQuery();
            if (result.first()) {
                return (ModelSummary) result.getObject(1);
            } else {
                throw new DatabaseHandlerException("Invalid model ID: " + modelID);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while reading model summary for " +
                    modelID + " from the database: " + e.getMessage(),
                    e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }

    }

    /**
     *
     * @param workflowID    Machine learning workflow ID
     * @return              Returns a machine learning workflow object
     * @throws              DatabaseHandlerException
     */
    public Workflow getWorkflow(String workflowID) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            Workflow mlWorkflow = new Workflow();
            mlWorkflow.setWorkflowID(workflowID);
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            getStatement = connection.prepareStatement(SQLQueries.GET_WORKFLOW_DATASET_LOCATION);
            getStatement.setString(1, workflowID);
            result = getStatement.executeQuery();
            if (result.first()) {
                mlWorkflow.setDatasetURL(result.getString(1));
            }
            List<Feature> mlFeatures = new ArrayList<Feature>();
            getStatement = connection.prepareStatement(SQLQueries.GET_ML_FEATURE_SETTINGS);
            getStatement.setString(1, workflowID);
            result = getStatement.executeQuery();
            while (result.next()) {
                // check whether to include the feature or not
                if (result.getBoolean(5) == true) {
                    Feature mlFeature = new Feature();
                    mlFeature.setName(result.getString(1));
                    mlFeature.setIndex(result.getInt(2));
                    mlFeature.setType(result.getString(3));
                    mlFeature.setImputeOption(result.getString(4));
                    mlFeature.setInclude(result.getBoolean(5));
                    mlFeatures.add(mlFeature);
                }
            }
            mlWorkflow.setFeatures(mlFeatures);
            getStatement = connection.prepareStatement(SQLQueries.GET_ML_MODEL_SETTINGS);
            getStatement.setString(1, workflowID);
            result = getStatement.executeQuery();
            if (result.first()) {
                mlWorkflow.setAlgorithmClass(result.getString(1));
                mlWorkflow.setAlgorithmName(result.getString(2));
                mlWorkflow.setResponseVariable(result.getString(3));
                mlWorkflow.setTrainDataFraction(result.getDouble(4));
                List<HyperParameter> hyperParameters = (List<HyperParameter>) result.getObject(5);
                mlWorkflow.setHyperParameters(MLDatabaseUtils.getHyperParamsAsAMap(hyperParameters));
            }
            return mlWorkflow;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }
    }

    /**
     * Reads model execution completion time for a given model id.
     *
     * @param modelId
     * @return Returns the number of millis since Jan 1, 1970, 00:00:00 GMT represented by
     * model execution
     * end time.
     * @throws DatabaseHandlerException
     */
    public long getModelExecutionEndTime(String modelId) throws DatabaseHandlerException {
        return getModelExecutionTime(modelId, SQLQueries.GET_MODEL_EXE_END_TIME);
    }

    /**
     * Read model execution start time for a given model id.
     *
     * @param modelId
     * @return Returns the number of millis since Jan 1, 1970, 00:00:00 GMT represented by model
     * execution
     * start time
     * @throws DatabaseHandlerException
     */
    public long getModelExecutionStartTime(String modelId) throws DatabaseHandlerException {
        return getModelExecutionTime(modelId, SQLQueries.GET_MODEL_EXE_START_TIME);
    }

    @Override
    public String getDatasetId(String projectId) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;

        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_DATASET_ID);
            statement.setString(1, projectId);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getString(1);
            } else {
                throw new DatabaseHandlerException(
                        "No dataset id associated with project id: " + projectId);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(
                    " An error has occurred while extracting dataset id for project id: " + projectId);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    /**
     * This helper class is used to extract model execution start/end time
     *
     * @param modelId
     * @param query
     * @return
     * @throws DatabaseHandlerException
     */
    public long getModelExecutionTime(String modelId, String query)
            throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, modelId);
            result = statement.executeQuery();
            if (result.first()) {
                Timestamp time = result.getTimestamp(1);
                if (time != null) {
                    return time.getTime();
                }
                return 0;
            } else {
                throw new DatabaseHandlerException(
                        "No timestamp data associated with model id: " + modelId);
            }

        } catch (SQLException e) {
            throw new DatabaseHandlerException(
                    " An error has occurred while reading execution time from the database: " + e
                            .getMessage(), e);
        } finally {
            // closing database resources
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

// Database Services related to Project Management Module.
    
    /**
     * Creates a new project.
     *
     * @param projectID        Unique identifier for the project
     * @param projectName      Name of the project
     * @param description      Description of the project
     * @throws                 DatabaseHandlerException
     */
    public void createProject(String projectID, String projectName, String description)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement createProjectStatement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            createProjectStatement = connection.prepareStatement(SQLQueries.CREATE_PROJECT);
            createProjectStatement.setString(1, projectID);
            createProjectStatement.setString(2, projectName);
            createProjectStatement.setString(3, description);
            createProjectStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted details of project: " + projectName +
                             ". Project ID: " + projectID.toString());
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("Error occurred while inserting details of project: " + projectName + 
                    " to the database: " + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, createProjectStatement);
        }
    }

    /**
     * Delete details of a given project from the database.
     *
     * @param projectId    Unique identifier for the project
     * @throws             DatabaseHandlerException
     */
    public void deleteProject(String projectId) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement deleteProjectStatement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            deleteProjectStatement = connection.prepareStatement(SQLQueries.DELETE_PROJECT);
            deleteProjectStatement.setString(1, projectId);
            deleteProjectStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully deleted the project: " + projectId);
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("Error occurred while deleting the project: " + projectId + ": " + 
                    e.getMessage(),e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, deleteProjectStatement);
        }
    }

    /**
     * Assign a tenant to a given project.
     *
     * @param tenantID     Unique identifier for the current tenant.
     * @param projectID    Unique identifier for the project.
     * @throws             DatabaseHandlerException
     */
    public void addTenantToProject(String tenantID, String projectID)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement addTenantStatement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            addTenantStatement = connection.prepareStatement(SQLQueries.ADD_TENANT_TO_PROJECT);
            addTenantStatement.setString(1, tenantID);
            addTenantStatement.setString(2, projectID);
            addTenantStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully added the tenant: " + tenantID + " to the project: " + projectID);
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("Error occurred while adding the tenant " + tenantID + " to the project "
                    + projectID + ": " + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, addTenantStatement);
        }
    }

    /**
     * Get the project names and created dates, that a tenant is assigned to.
     *
     * @param tenantID     Unique identifier for the tenant.
     * @return             An array of project ID, Name and the created date of the projects 
     *                     associated with a given tenant.
     * @throws             DatabaseHandlerException.
     */
    public String[][] getTenantProjects(String tenantID) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement getTenantProjectsStatement = null;
        ResultSet result = null;
        String[][] projects = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getTenantProjectsStatement = connection.prepareStatement(SQLQueries.GET_TENANT_PROJECTS, 
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            getTenantProjectsStatement.setString(1, tenantID);
            result = getTenantProjectsStatement.executeQuery();
            // create a 2-d string array having the size of the result set
            result.last();
            int size = result.getRow();
            if (size > 0) {
                projects = new String[size][3];
                result.beforeFirst();
                // put the result set to the string array
                for (int i = 0; i < size; i++) {
                    result.next();
                    projects[i][0] = result.getObject(1).toString();
                    projects[i][1] = result.getString(2);
                    projects[i][2] = result.getDate(3).toString();
                }
            }
            return projects;
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException( "Error occurred while retrieving the projects of user " + tenantID + ": "
                    + e.getMessage(),e);
        } finally {
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, getTenantProjectsStatement, result);
        }
    }

    /**
     * Returns the ID of the data-set associated with the project.
     *
     * @param projectId    Unique identifier for the project.
     * @return             ID of the data-set associated with the project.
     * @throws             DatabaseHandlerException.
     */
    public String getdatasetID(String projectId) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement getDatasetID = null;
        ResultSet result = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getDatasetID = connection.prepareStatement(SQLQueries.GET_DATASET_ID);
            getDatasetID.setString(1, projectId);
            result = getDatasetID.executeQuery();
            result.first();
            return result.getObject(1).toString();
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException( "Error occurred while retrieving the Dataset Id of project " + projectId
                + ": " + e.getMessage(), e);
        } finally {
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, getDatasetID, result);
        }
    }

    /**
     * Creates a new work-flow.
     *
     * @param workflowID           Unique identifier for the new workflow.
     * @param parentWorkflowID     Unique identifier for the workflow from which the current workflow
     *                             is inherited from.
     * @param projectID            Unique identifier for the project for which the workflow is created.
     * @param datasetID            Unique identifier for the data-set associated with the workflow.
     * @param workflowName         Name of the project.
     * @throws                     DatabaseHandlerException
     */
    public void createNewWorkflow(String workflowID, String parentWorkflowID, String projectID, String datasetID,
            String workflowName) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement createNewWorkflow = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            createNewWorkflow = connection.prepareStatement(SQLQueries.CREATE_NEW_WORKFLOW);
            createNewWorkflow.setString(1, workflowID);
            createNewWorkflow.setString(2, parentWorkflowID);
            createNewWorkflow.setString(3, projectID);
            createNewWorkflow.setString(4, datasetID);
            createNewWorkflow.setString(5, workflowName);
            createNewWorkflow.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully created workflow: " + workflowID);
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while creating a new workflow " + workflowID + ": " +
                    e.getMessage(),e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, createNewWorkflow);
        }
    }

    /**
     * Update the name of a workflow.
     * 
     * @param workflowId    Unique identifier of the workflow.
     * @param name          New name for the workflow.
     * @throws              DatabaseHandlerException
     */
    public void updateWorkdflowName(String workflowId, String name) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateWorkflow = null;

        try{
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            updateWorkflow = connection.prepareStatement(SQLQueries.UPDATE_WORKFLOW_NAME);

            updateWorkflow.setString(1, name);
            updateWorkflow.setString(2, workflowId);
            updateWorkflow.executeUpdate();
            connection.commit();

            if(logger.isDebugEnabled()){
                if (logger.isDebugEnabled()) {
                    logger.debug("Successfully updated workflow: " + workflowId);
                }
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while updating workflow " + workflowId + ": " +
                    e.getMessage(),e);
        }finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);

            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, updateWorkflow);
        }
    }

    /**
     * Deletes a workflow.
     *
     * @param workflowID   Unique identifier of the workflow to be deleted
     * @throws             DatabaseHandlerException
     */
    public void deleteWorkflow(String workflowID)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement deleteWorkflow = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            deleteWorkflow = connection.prepareStatement(SQLQueries.DELETE_WORKFLOW);
            deleteWorkflow.setString(1, workflowID);
            deleteWorkflow.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully deleted workflow: " + workflowID);
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while deleting workflow " +
                    workflowID + ": " + e.getMessage(),e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(deleteWorkflow);
        }
    }

    /**
     * Get a list of workflows associated with a given project.
     *
     * @param projectId    Unique identifier for the project for which the wokflows are needed
     * @return             An array of workflow ID's and Names
     * @throws             DatabaseHandlerException
     */
    public String[][] getProjectWorkflows(String projectId) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement getProjectWorkflows = null;
        ResultSet result = null;
        String[][] workFlows = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getProjectWorkflows = connection.prepareStatement(SQLQueries.GET_PROJECT_WORKFLOWS, 
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            getProjectWorkflows.setString(1, projectId);
            result = getProjectWorkflows.executeQuery();

            // create a 2-d string array having the size of the result set
            result.last();
            int noOfWorkflows = result.getRow();
            if (noOfWorkflows > 0) {
                workFlows = new String[noOfWorkflows][2];
                result.beforeFirst();
                // put the result set to the string array
                for (int i = 0; i < noOfWorkflows; i++) {
                    result.next();
                    workFlows[i][0] = result.getString(1);
                    workFlows[i][1] = result.getString(2);
                }
            }
            return workFlows;
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException( "Error occurred while retrieving the Dataset Id of project " + 
                    projectId + " : " + e.getMessage(), e);
        } finally {
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, getProjectWorkflows, result);
        }
    }

    /**
     * Set the default values for feature properties of a given workflow.
     *
     * @param datasetID    Unique identifier of the data-set
     * @param workflowID   Unique identifier of the current workflow
     * @throws             DatabaseHandlerException
     */
    public void setDefaultFeatureSettings(String datasetID, String workflowID)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        PreparedStatement getDefaultFeatureSettings = null;
        ResultSet result = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            // read default feature settings from data-set summary table
            getDefaultFeatureSettings = connection.prepareStatement(SQLQueries.GET_DEFAULT_FEATURE_SETTINGS);
            getDefaultFeatureSettings.setString(1, datasetID);
            result = getDefaultFeatureSettings.executeQuery();
            // insert default feature settings into feature settings table
            connection.setAutoCommit(false);
            while (result.next()) {
                insertStatement = connection.prepareStatement(SQLQueries.INSERT_FEATURE_SETTINGS);
                insertStatement.setString(1, workflowID);
                insertStatement.setString(2, result.getString(1));
                insertStatement.setString(3, result.getString(2));
                insertStatement.setString(4, result.getString(3));
                insertStatement.setString(5, result.getString(4));
                insertStatement.setBoolean(6, result.getBoolean(5));
                insertStatement.execute();
                connection.commit();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted feature deafults of dataset: " + datasetID +
                             " of the workflow: " + datasetID);
            }
        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while setting details of dataset " + datasetID +
                    " of the workflow " + datasetID + " to the database:" + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement, result);
            MLDatabaseUtils.closeDatabaseResources(getDefaultFeatureSettings);
        }
    }
}
