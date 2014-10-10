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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseHandler {

	private final Connection connection;
	private static final Log logger = LogFactory.getLog(DatabaseHandler.class);

	/**
	 * Constructor method.
	 * Creates a connection to the database.
	 * @throws DatabaseHandlerException
	 */
	public DatabaseHandler() throws DatabaseHandlerException {
		try {
			// load the carbon data source configurations of the H2 database
			Context initContext = new InitialContext();
			DataSource ds = (DataSource) initContext.lookup("jdbc/WSO2CarbonDB");
			connection = ds.getConnection();

			// enable auto commit
			connection.setAutoCommit(true);
		} catch (Exception e) {
			String msg = "Error occured while connecting to database. " + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
	}

	/**
	 * Returns a database connection object.
	 * @return
	 */
	public Connection getConnection() {
		return this.connection;
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
	 * get the URI of the data source having the given ID, from the database
	 *
	 * @param dataSourceId
	 * @return
	 * @throws DatabaseHandlerException
	 */
	public String getDataSource(String dataSourceId) throws DatabaseHandlerException {
		ResultSet result = null;
		PreparedStatement getStatement = null;
		try {
			getStatement = connection.prepareStatement(SQLQueries.GET_DATASET_LOCATION);
			getStatement.setString(1, dataSourceId);
			result = getStatement.executeQuery();

			// if the URi for the given dataset exists
			if (result.first()) {
				return result.getNString("URI");
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
	 * @param source
	 * @return
	 * @throws DatabaseHandlerException
	 */
	public String insertDatasetDetails(String filePath, String description) throws DatabaseHandlerException {
		PreparedStatement getStatement = null;
		PreparedStatement insertStatement = null;
		ResultSet resultSet = null;
		try {
			getStatement = connection.prepareStatement(SQLQueries.GET_DATASET_ID);

			// get the latest auto-generated Id
			resultSet = getStatement.executeQuery();
			String newID;

			// If there are data-sets already in the table
			if (resultSet.last()) {
				// get the latest data set ID and increment by one
				newID = String.valueOf(Integer.parseInt(resultSet.getString("Id")) + 1);
			} else {
				// new data-set id is 1
				newID = "1";
			}
			// insert the data-set details to the database
			connection.setAutoCommit(false);
			insertStatement = connection.prepareStatement(SQLQueries.INSERT_DATASET);
			insertStatement.setString(1, newID);
			insertStatement.setString(2, description);
			insertStatement.setString(3, filePath);
			insertStatement.execute();
			connection.commit();
			logger.debug("Successfully updated the details of data set: " + filePath + ". Dataset ID" + newID);
			return newID;

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
			MLDatabaseUtil.closeResultSet(resultSet);
			MLDatabaseUtil.closeStatement(getStatement);
			MLDatabaseUtil.closeStatement(insertStatement);
		}
	}

	/**
	 * update details for a given feature
	 *
	 * @param name
	 * @param dataSet
	 * @param type
	 * @param imputeOption
	 * @param important
	 * @throws DatabaseHandlerException
	 */
	public void updateFeature(String name, String dataSet, String type, ImputeOption imputeOption,
	                          boolean important) throws DatabaseHandlerException {
		PreparedStatement updateStatement = null;
		try {
			// update database table with the new details
			connection.setAutoCommit(false);
			updateStatement = connection.prepareStatement(SQLQueries.UPDATE_FEATURE);
			updateStatement.setString(1, type);
			updateStatement.setString(2, imputeOption.toString());
			updateStatement.setBoolean(3, important);
			updateStatement.setString(4, name);
			updateStatement.setString(5, dataSet);
			updateStatement.execute();
			connection.commit();
		} catch (SQLException e) {
			// rollback the changes
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"Error occured while updating the feature : " + name + " of data set: " +
							dataSet + " ." + e.getMessage();
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
	 * Update the data type of a given feature
	 *
	 * @param featureName
	 * @param datasetId
	 * @param featureType
	 * @throws DatabaseHandlerException
	 */
	public void updateDataType(String featureName, String datasetId, String featureType)
			throws DatabaseHandlerException {
		PreparedStatement updateStatement = null;
		try {
			// update the database with data type
			connection.setAutoCommit(false);
			updateStatement = connection.prepareStatement(SQLQueries.UPDATE_DATA_TYPE);
			updateStatement.setString(1, featureType);
			updateStatement.setString(2, featureName);
			updateStatement.setString(3, datasetId);
			updateStatement.execute();
			connection.commit();
		} catch (SQLException e) {
			// rollback the changes
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"Error occured while updating the data type of feature : " + featureName +
					" of dataset ID: " + datasetId + " ." + e.getMessage();
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
	 * @param datasetId
	 * @param imputeOption
	 * @throws DatabaseHandlerException
	 */
	public void updateImputeOption(String featureName, String datasetId, String imputeOption)
			throws DatabaseHandlerException {
		PreparedStatement updateStatement = null;
		try {
			// update the database
			connection.setAutoCommit(false);
			updateStatement = connection.prepareStatement(SQLQueries.UPDATE_IMPUTE_METHOD);
			updateStatement.setString(1, imputeOption);
			updateStatement.setString(2, featureName);
			updateStatement.setString(3, datasetId);
			updateStatement.execute();
			connection.commit();
		} catch (SQLException e) {
			// rollback the changes
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"Error occured while updating the feature : " + featureName +
					" of dataset ID: " + datasetId + " ." + e.getMessage();
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
	 * @param datasetId
	 * @param isInput
	 * @throws DatabaseHandlerException
	 */
	public void updateIsIncludedFeature(String featureName, String datasetId, boolean isInput)
			throws DatabaseHandlerException {
		PreparedStatement updateStatement = null;
		try {
			connection.setAutoCommit(false);
			updateStatement = connection.prepareStatement(SQLQueries.UPDATE_IS_INCLUDED);
			updateStatement.setBoolean(1, isInput);
			updateStatement.setString(2, featureName);
			updateStatement.setString(3, datasetId);
			updateStatement.execute();
			connection.commit();
		} catch (SQLException e) {
			// rollback the changes
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"Error occured while updating the feature : " + featureName +
					" of dataset ID: " + datasetId + " ." + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		} finally {
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeStatement(updateStatement);
		}
	}

	/**
	 * Update the database with all the summary stats of the sample
	 *
	 * @param dataSourceId
	 * @param header
	 * @param type
	 * @param graphFrequencies
	 * @param missing
	 * @param unique
	 * @param descriptiveStats
	 * @throws DatabaseHandlerException
	 */
	public void updateSummaryStatistics(String dataSourceId, String [] header, FeatureType[] type,
	                                    List<SortedMap<?, Integer>> graphFrequencies,
	                                    int[] missing, int[] unique,
	                                    List<DescriptiveStatistics> descriptiveStats)
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
				updateStatement.setString(2, dataSourceId);
				updateStatement.setString(3, type[column].toString());
				updateStatement.setString(4, summaryStat.toString());
				updateStatement.setString(5, ImputeOption.DISCARD.toString());
				updateStatement.execute();
				connection.commit();
			}
			logger.debug("Successfully updated the summary statistics for data source: " +
					dataSourceId);
		} catch (SQLException e) {
			// rollback the changes
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"Error occured while updating the database with summary statistics of the data source: " +
							dataSourceId + "." + e.getMessage();
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
	// TODO: don't send NaN for int fields, that will throw error in parsing
	private JSONObject createJson(int column, FeatureType[] type,
	                              List<SortedMap<?, Integer>> graphFrequencies,
	                              int[] missing, int[] unique,
	                              List<DescriptiveStatistics> descriptiveStats) {
		JSONObject json = new JSONObject();
		JSONArray freqs = new JSONArray();
		Object[] categoryNames = graphFrequencies.get(column).keySet().toArray();
		// create an array with intervals/categories and their frequencies
		for (int i = 0; i < graphFrequencies.get(column).size(); i++) {
			JSONObject temp = new JSONObject();
			temp.put("range", categoryNames[i].toString());
			temp.put("frequency", String.valueOf(graphFrequencies.get(column).get(categoryNames[i])));
			freqs.put(temp);
		}
		// put the statistics to a json object
		json.put("type", type[column].toString());
		json.put("unique", unique[column]);
		json.put("missing", missing[column]);
		
		//TODO: change this to check only NaN
		if(descriptiveStats.get(column).getN()!=0){
			json.put("mean", descriptiveStats.get(column).getMean());
			json.put("median", descriptiveStats.get(column).getPercentile(50));
			json.put("std", descriptiveStats.get(column).getStandardDeviation());
		}
		json.put("frequencies", freqs);
		return json;
	}

	/**
	 * This method reads ( a given number of features ) from ML_FEATURE
	 *
	 * and creates a list of Feature
	 *
	 * @param dataSetName
	 * @param startIndex
	 * @param numberOfFeatures
	 * @return
	 * @throws DatabaseHandlerException
	 */
	public Feature[] getFeatures(String dataSetName, int startIndex, int numberOfFeatures)
			throws DatabaseHandlerException {

		List<Feature> features = new ArrayList<Feature>();
		PreparedStatement getFeatues = null;
		ResultSet result = null;
		try {
			// create a prepared statement and extract dataset configurations
			getFeatues = connection.prepareStatement(SQLQueries.GET_FEATURES);
			getFeatues.setString(1, dataSetName);
			getFeatues.setInt(2, numberOfFeatures);
			getFeatues.setInt(3, startIndex);
			result = getFeatues.executeQuery();

			while (result.next()) {
				FeatureType featureType = FeatureType.NUMERICAL;
				if (FeatureType.CATEGORICAL.toString().equalsIgnoreCase(result.getString("Type"))) {
					featureType = FeatureType.CATEGORICAL;
				}
				// set the impute option
				ImputeOption imputeOperation = ImputeOption.DISCARD;
				if (ImputeOption.REPLACE_WTH_MEAN.toString()
						.equalsIgnoreCase(result.getString("Impute_Method"))) {
					imputeOperation = ImputeOption.REPLACE_WTH_MEAN;
				} else if (ImputeOption.REGRESSION_IMPUTATION.toString()
						.equalsIgnoreCase(result.getString("Impute_Method"))) {
					imputeOperation = ImputeOption.REGRESSION_IMPUTATION;
				}

				String featureName = result.getString("Name");
				boolean isImportantFeature = result.getBoolean("Important");
				String summaryStat = result.getString("summary");

				features.add(new Feature(featureName, isImportantFeature, featureType,
				                         imputeOperation, summaryStat));
			}
			return features.toArray(new Feature[features.size()]);
		} catch (SQLException e) {
			String msg =
					"Error occured while retireving features of the data set: " + dataSetName +
					" Error message: " + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);

		} finally {
			// close the database resources
			MLDatabaseUtil.closeStatement(getFeatues);
			MLDatabaseUtil.closeResultSet(result);
		}
	}
}
