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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class DatabaseHandler {

    private static volatile DatabaseHandler databaseHandler = null;
    private Connection connection;
    private static final Log logger = LogFactory.getLog(DatabaseHandler.class);

    /*
     * private Constructor to prevent any other class from instantiating.
     */
    private DatabaseHandler() {
    }

    /**
     * Creates a singleton DatabaseHandler instance and returns it.
     *
     * @return
     * @throws DatabaseHandlerException
     */
    public static DatabaseHandler getDatabaseHandler() throws DatabaseHandlerException {
        try {
            if (databaseHandler == null) {
                synchronized (DatabaseHandler.class) {
                    if (databaseHandler == null) {
                        databaseHandler = new DatabaseHandler();
                        // load the carbon data source configurations of the H2
                        // database
                        Context initContext = new InitialContext();
                        DataSource ds = (DataSource) initContext.lookup("jdbc/WSO2ML_DB");
                        databaseHandler.connection = ds.getConnection();
                        // enable auto commit
                        databaseHandler.connection.setAutoCommit(true);
                    }
                }
            }
            return databaseHandler;
        } catch (Exception e) {
            String msg = "Error occured while connecting to database. " + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        }
    }

    /**
     * This method reads configurations from the database
     *
     * @return
     * @throws DatabaseHandlerException
     */
    public DatasetConfig getDatasetConfig() throws DatabaseHandlerException {
        ResultSet result = null;
        Statement getStatement = null;
        try {
            getStatement = connection.createStatement();
            result = getStatement.executeQuery(SQLQueries.GET_DATASET_CONFIG);
            if (result.first()) {
                String uploadingDir = result.getString("DATASET_UPLOADING_DIR");
                int memoryThreshold = result.getInt("DATASET_IN_MEM_THRESHOLD");
                long maxUploding = result.getLong("DATASET_UPLOADING_LIMIT");

                if (uploadingDir == null || uploadingDir.length() == 0) {
                    String msg = "DATASET_UPLOADING_DIR directory can not be null or empty";
                    logger.error(msg);
                    throw new DatabaseHandlerException(msg);
                }

                if (memoryThreshold == 0 || maxUploding == 0) {
                    String msg =
                            "DATASET_IN_MEM_THRESHOLD and/or DATASET_IN_MEM_THRESHOLD can't be empty";
                    logger.error(msg);
                    throw new DatabaseHandlerException(msg);
                }

                return new DatasetConfig(uploadingDir, memoryThreshold, maxUploding);
            } else {
                String msg = "An error has occurred while reading dataset config details";
                logger.error(msg);
                throw new DatabaseHandlerException(msg);
            }

        } catch (SQLException ex) {
            String msg =
                    "Error occured while retrieving the default upload location from the database. " +
                    ex.getMessage();
            logger.error(msg, ex);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeResultSet(result);
            MLDatabaseUtil.closeStatement(getStatement);
        }
    }

    /**
     * Retrieve the number of intervals to be used from the ML_CONFIGURATION
     * database
     *
     * @return
     * @throws DatabaseHandlerException
     */
    public int getNumberOfBucketsInHistogram() throws DatabaseHandlerException {
        ResultSet result = null;
        Statement selectStatement = null;
        try {
            selectStatement = connection.createStatement();
            result = selectStatement.executeQuery("SELECT INTERVALS FROM ML_CONFIGURATION");
            // if the number of intervals is set
            if (result.first()) {
                int intervals = result.getInt("INTERVALS");
                logger.debug("Number of intervals uses to categorize numerical data: " + intervals);
                return intervals;
            } else {
                String message =
                        "Number of intervals is not set in the ML_CONFIGURATION database table.";
                logger.error(message);
                throw new DatabaseHandlerException(message);
            }
        } catch (SQLException e) {
            String msg =
                    "Error occured while retrieving the Number of intervals from the database. " +
                    e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeResultSet(result);
            MLDatabaseUtil.closeStatement(selectStatement);
        }
    }

    /**
     * Retrieve the separator from the ML_CONFIGURATION database
     *
     * @return
     * @throws DatabaseHandlerException
     */
    public String getSeparator() throws DatabaseHandlerException {
        ResultSet result = null;
        Statement getStatement = null;
        try {
            getStatement = connection.createStatement();
            result = getStatement.executeQuery(SQLQueries.GET_SEPARATOR);
            // if the separator is set
            if (result.first()) {
                String separator = result.getNString("SEPARATOR");
                logger.debug("Data points separator: " + separator);
                return separator;
            } else {
                String message =
                        "Data points separator is not set in the ML_CONFIGURATION database table.";
                logger.error(message);
                throw new DatabaseHandlerException(message);
            }
        } catch (SQLException e) {
            String msg =
                    "Error occured while retrieving the Data points separator from the database. " +
                    e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeResultSet(result);
            MLDatabaseUtil.closeStatement(getStatement);
        }
    }

    /**
     * get the path of the data source having the given ID, from the database
     *
     * @param datasetID
     * @return
     * @throws DatabaseHandlerException
     */
    public String getDataSource(String datasetID) throws DatabaseHandlerException {
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            getStatement = connection.prepareStatement(SQLQueries.GET_DATASET_LOCATION);
            getStatement.setString(1, datasetID);
            result = getStatement.executeQuery();

            // if the path for the given dataset exists
            if (result.first()) {
                return result.getNString(1);
            } else {
                String msg = "Invalid data source ID.";
                logger.error(msg);
                throw new DatabaseHandlerException(msg);
            }
        } catch (Exception e) {
            String msg =
                    "Error occured while reading the Data source from the database." +
                    e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeResultSet(result);
            MLDatabaseUtil.closeStatement(getStatement);
        }
    }

    /**
     * insert the new data set details to the the database
     *
     * @param filePath
     * @param projectID
     * @return
     * @throws DatabaseHandlerException
     */
    public void insertDatasetDetails(String datasetID, String filePath, String projectID)
            throws DatabaseHandlerException {
        PreparedStatement insertStatement = null;
        try {
            // insert the data-set details to the database
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_DATASET);
            insertStatement.setString(1, datasetID);
            insertStatement.setString(2, filePath);
            insertStatement.setString(3, projectID);
            insertStatement.execute();
            connection.commit();
            logger.debug("Successfully updated the details of data set: " + filePath +
                         ". Dataset ID" + datasetID);

        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtil.rollBack(connection);
            String msg =
                    "Error occured while inserting data source details to the database." +
                    e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);

        } finally {
            // enable auto commit
            MLDatabaseUtil.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtil.closeStatement(insertStatement);
        }
    }

    /**
     * Update the data type of a given feature
     *
     * @param featureName
     * @param workflowID
     * @param featureType
     * @throws DatabaseHandlerException
     */
    public void updateDataType(String featureName, String workflowID, String featureType)
            throws DatabaseHandlerException {
        PreparedStatement updateStatement = null;
        try {
            // update the database with data type
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_DATA_TYPE);
            updateStatement.setString(1, featureType);
            updateStatement.setString(2, featureName);
            updateStatement.setString(3, workflowID);
            updateStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtil.rollBack(connection);
            String msg =
                    "An error occured while updating the data type of feature : " + featureName +
                    " of workflow configuration " + workflowID + " ." + e
                            .getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // enable auto commit
            MLDatabaseUtil.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtil.closeStatement(updateStatement);
        }
    }

    /**
     * Update the impute method option of a given feature
     *
     * @param featureName
     * @param workflowID
     * @param imputeOption
     * @throws DatabaseHandlerException
     */
    public void updateImputeOption(String featureName, String workflowID, String imputeOption)
            throws DatabaseHandlerException {
        PreparedStatement updateStatement = null;
        try {
            // update the database
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_IMPUTE_METHOD);
            updateStatement.setString(1, imputeOption);
            updateStatement.setString(2, featureName);
            updateStatement.setString(3, workflowID);
            updateStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtil.rollBack(connection);
            String msg =
                    "An error occured while updating the feature : " + featureName +
                    " of workflow configuration: " + workflowID + " ." + e
                            .getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // enable auto commit
            MLDatabaseUtil.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtil.closeStatement(updateStatement);
        }
    }

    /**
     * change whether a feature should be included as an input or not.
     *
     * @param featureName
     * @param workflowID
     * @param isInput
     * @throws DatabaseHandlerException
     */
    public void updateIsIncludedFeature(String featureName, String workflowID, boolean isInput)
            throws DatabaseHandlerException {
        PreparedStatement updateStatement = null;
        try {
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_IS_INCLUDED);
            updateStatement.setBoolean(1, isInput);
            updateStatement.setString(2, featureName);
            updateStatement.setString(3, workflowID);
            updateStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtil.rollBack(connection);
            String msg =
                    "An error occured while updating the feature : " + featureName +
                    " of workflow configuration ID: " + workflowID + " ." + e
                            .getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // enable auto commit
            MLDatabaseUtil.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtil.closeStatement(updateStatement);
        }
    }

    /**
     * Update the database with all the summary stats of the sample
     *
     * @param datasetID
     * @param header
     * @param type
     * @param graphFrequencies
     * @param missing
     * @param unique
     * @param descriptiveStats
     * @throws DatabaseHandlerException
     */
    public void updateSummaryStatistics(String datasetID, String[] header, FeatureType[] type,
                                        List<SortedMap<?, Integer>> graphFrequencies,
                                        int[] missing, int[] unique,
                                        List<DescriptiveStatistics> descriptiveStats,
                                        Boolean include)
            throws DatabaseHandlerException {
        PreparedStatement updateStatement = null;
        try {
            JSONObject summaryStat;
            for (int column = 0; column < header.length; column++) {
                // get the json representation of the column
                summaryStat =
                        createJson(column, type, graphFrequencies, missing, unique,
                                   descriptiveStats);

                // put the values to the database table. If the feature already
                // exists, updates the row. if not, inserts as a new row.
                connection.setAutoCommit(false);
                updateStatement = connection.prepareStatement(SQLQueries.UPDATE_SUMMARY_STATS);
                updateStatement.setString(1, header[column]);
                updateStatement.setString(2, datasetID);
                updateStatement.setString(3, summaryStat.toString());
                updateStatement.setString(4, type[column].toString());
                updateStatement.setString(5, ImputeOption.DISCARD.toString());
                updateStatement.setBoolean(6, include);
                updateStatement.execute();
                connection.commit();
            }
            logger.debug("Successfully updated the summary statistics for data source: " +
                         datasetID);
        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtil.rollBack(connection);
            String msg =
                    "Error occured while updating the database with summary statistics of the data source: " +
                    datasetID + "." + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // enable auto commit
            MLDatabaseUtil.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtil.closeStatement(updateStatement);
        }
    }

    /**
     *
     * @param datasetID
     * @param datasetSample
     * @throws DatabaseHandlerException
     */
    public void updateDatasetSample(String datasetID, SamplePoints datasetSample)
            throws DatabaseHandlerException {
        PreparedStatement updateStatement = null;
        try {
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_SAMPLE_POINTS);
            updateStatement.setObject(1, datasetSample);
            updateStatement.setString(2, datasetID);
            updateStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtil.rollBack(connection);
            String msg =
                    "An error occurred while updating the sample points of dataset : " + datasetID +
                    "." + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // enable auto commit
            MLDatabaseUtil.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtil.closeStatement(updateStatement);
        }
    }

    public SamplePoints getDatasetSample(String datasetID) throws DatabaseHandlerException {
        PreparedStatement updateStatement = null;
        ResultSet result = null;
        SamplePoints samplePoints = null;
        try {
            updateStatement = connection.prepareStatement(SQLQueries.GET_SAMPLE_POINTS);
            updateStatement.setString(1, datasetID);
            result = updateStatement.executeQuery();
            if (result.first()) {
                samplePoints = (SamplePoints) result.getObject(1);
            }
            return samplePoints;
        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtil.rollBack(connection);
            String msg =
                    "Error occured while retrieving the sample points of dataset : " + datasetID + "." + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeStatement(updateStatement);
        }
    }


    /**
     * Create the json string with summary stat for a given column
     *
     * @param column
     * @param type
     * @param graphFrequencies
     * @param missing
     * @param unique
     * @param descriptiveStats
     * @return
     */
    private JSONObject createJson(int column, FeatureType[] type,
                                  List<SortedMap<?, Integer>> graphFrequencies, int[] missing,
                                  int[] unique, List<DescriptiveStatistics> descriptiveStats) {
        JSONObject json = new JSONObject();
        JSONArray freqs = new JSONArray();
        Object[] categoryNames = graphFrequencies.get(column).keySet().toArray();
        // create an array with intervals/categories and their frequencies
        for (int i = 0; i < graphFrequencies.get(column).size(); i++) {
            JSONObject temp = new JSONObject();
            temp.put("range", categoryNames[i].toString());
            temp.put("frequency", graphFrequencies.get(column).get(categoryNames[i]));
            freqs.put(temp);
        }
        // put the statistics to a json object
        json.put("unique", unique[column]);
        json.put("missing", missing[column]);

        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        if (descriptiveStats.get(column).getN() != 0) {
            json.put("mean", decimalFormat.format(descriptiveStats.get(column).getMean()));
            json.put("median", decimalFormat.format(descriptiveStats.get(column).getPercentile(50)));
            json.put("std", decimalFormat.format(descriptiveStats.get(column).getStandardDeviation()));
            if (type[column].equals(FeatureType.NUMERICAL)) {
                json.put("skewness", decimalFormat.format(descriptiveStats.get(column).getSkewness()));
            }
        }
        json.put("frequencies", freqs);
        return json;
    }

    /**
     * This method reads ( a given number of features ) from ML_FEATURE
     * <p/>
     * and creates a list of Feature
     *
     * @param datasetID
     * @param startIndex
     * @param numberOfFeatures
     * @return
     * @throws DatabaseHandlerException
     */
    public Feature[] getFeatures(String datasetID, String workflowID, int startIndex, int numberOfFeatures)
            throws DatabaseHandlerException {

        List<Feature> features = new ArrayList<Feature>();
        PreparedStatement getFeatues = null;
        ResultSet result = null;
        try {
            // create a prepared statement and extract dataset configurations
            getFeatues = connection.prepareStatement(SQLQueries.GET_FEATURES);
            getFeatues.setString(1, datasetID);
	    getFeatues.setString(2,datasetID);
	    getFeatues.setString(3,workflowID);
            getFeatues.setInt(4, numberOfFeatures);
            getFeatues.setInt(5, startIndex);
            result = getFeatues.executeQuery();

            while (result.next()) {
                FeatureType featureType = FeatureType.NUMERICAL;
                if (FeatureType.CATEGORICAL.toString().equalsIgnoreCase(result.getString(3))) {
                    featureType = FeatureType.CATEGORICAL;
                }
                // set the impute option
                ImputeOption imputeOperation = ImputeOption.DISCARD;
                if (ImputeOption.REPLACE_WTH_MEAN.toString()
                        .equalsIgnoreCase(result.getString(5))) {
                    imputeOperation = ImputeOption.REPLACE_WTH_MEAN;
                } else if (ImputeOption.REGRESSION_IMPUTATION.toString()
                        .equalsIgnoreCase(result.getString(5))) {
                    imputeOperation = ImputeOption.REGRESSION_IMPUTATION;
                }

                String featureName = result.getString(1);
                boolean isImportantFeature = result.getBoolean(4);
                String summaryStat = result.getString(2);

                features.add(new Feature(featureName, isImportantFeature, featureType,
                                         imputeOperation, summaryStat));
            }
            return features.toArray(new Feature[features.size()]);
        } catch (SQLException e) {
            String msg =
                    "Error occured while retireving features of the data set: " + datasetID +
                    " Error message: " + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeStatement(getFeatues);
            MLDatabaseUtil.closeResultSet(result);
        }
    }

    /**
     * Retrieve and returns the names of the features having the given type
     * (Categorical/Numerical) of a given data set
     *
     * @param workflowID
     * @param featureType
     * @return
     * @throws DatabaseHandlerException
     */
    public String[] getFeatureNames(String workflowID, String featureType)
            throws DatabaseHandlerException {
        PreparedStatement getFeatureNamesStatement = null;
        ResultSet result = null;
        List<String> featureNames = new ArrayList<String>();
        try {
            // create a prepared statement and extract data-set configurations
            getFeatureNamesStatement = connection.prepareStatement(SQLQueries.GET_FEATURE_NAMES);
            getFeatureNamesStatement.setString(1, workflowID);
            // select the data type
            if (featureType.equalsIgnoreCase(FeatureType.CATEGORICAL.toString())) {
                getFeatureNamesStatement.setString(2, FeatureType.CATEGORICAL.toString());
            } else {
                getFeatureNamesStatement.setString(2, FeatureType.NUMERICAL.toString());
            }
            result = getFeatureNamesStatement.executeQuery();
            // convert the result in to a string array to e returned
            while (result.next()) {
                featureNames.add(result.getString(1));
            }
            return featureNames.toArray(new String[featureNames.size()]);
        } catch (SQLException e) {
            String msg =
                    "An error occurred while retrieving feature names from the workflow " +
                    "configuration: " +
                    workflowID + " Error message: " + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeStatement(getFeatureNamesStatement);
            MLDatabaseUtil.closeResultSet(result);
        }
    }

    /**
     * Retrieve and returns the Summary statistics for a given feature of a
     * given data-set, from the database.
     *
     * @param datasetID
     * @param featureName
     * @return
     * @throws DatabaseHandlerException
     */
    public String getSummaryStats(String datasetID, String featureName)
            throws DatabaseHandlerException {
        PreparedStatement getSummaryStatement = null;
        ResultSet result = null;
        JSONObject summary = null;
        try {
            getSummaryStatement = connection.prepareStatement(SQLQueries.GET_SUMMARY_STATS);
            getSummaryStatement.setString(1, featureName);
            getSummaryStatement.setString(2, datasetID);
            result = getSummaryStatement.executeQuery();
            result.first();
            return result.getString(1);
        } catch (SQLException e) {
            String msg =
                    "Error occured while retireving summary statistics for the feature: " +
                    featureName + " of the data set: " + datasetID +
                    " Error message: " + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeStatement(getSummaryStatement);
            MLDatabaseUtil.closeResultSet(result);
        }
    }

    public int getFeatureCount(String datasetID) throws DatabaseHandlerException {
        PreparedStatement getFeatues = null;
        ResultSet result = null;
        int featureCount = 0;
        try {
            // create a prepared statement and extract dataset configurations
            getFeatues = connection.prepareStatement(SQLQueries.GET_FEATURE_COUNT);
            getFeatues.setString(1, datasetID);
            result = getFeatues.executeQuery();
            if (result.first()) {
                featureCount = result.getInt(1);
            }
            return featureCount;
        } catch (SQLException e) {
            String msg =
                    "Error occured while retireving feature count of the data set: " + datasetID +
                    " Error message: " + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeStatement(getFeatues);
            MLDatabaseUtil.closeResultSet(result);
        }
    }

    public void setDefaultFeatureSettings(String datasetID, String workflowID)
            throws DatabaseHandlerException {
        PreparedStatement insertStatement = null;
        PreparedStatement getDefaultFeatureSettings = null;
        ResultSet result = null;
        try {
            // read default feature settings from dataset summary table
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
                insertStatement.setBoolean(5, result.getBoolean(4));
                insertStatement.execute();
                connection.commit();
                logger.debug("Successfully inserted feature: "+result.getString(1));
            }



        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtil.rollBack(connection);
            String msg =
                    "Error occured while inserting data source details to the database." +
                    e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);

        } finally {
            // enable auto commit
            MLDatabaseUtil.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtil.closeStatement(insertStatement);
            MLDatabaseUtil.closeStatement(getDefaultFeatureSettings);
        }
    }
}
