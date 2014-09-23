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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseHandler {
	Connection connection = null;
	private final Log logger = LogFactory.getLog(DatabaseHandler.class);

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

	public Connection getConnection() {
		return this.connection;
	}

	/*
	 * get the default uploading location
	 */
	// TODO: use JDBC preparedstatement to avoid SQL injection
	public String getDefaultUploadLocation() throws DatabaseHandlerException {

		try {
			ResultSet result =
					connection.createStatement()
					.executeQuery("SELECT DATASET_UPLOADING_DIR FROM ML_CONFIGURATION");
			// if the default uploading location is set
			if (result.first()) {
				String location = result.getNString("DATASET_UPLOADING_DIR");
				logger.info("Default upload location: " + location);
				return location;
			} else {
				logger.error("Default uploading location is not set in the ML_CONFIGURATION database table.");
			}
		} catch (SQLException e) {
			String msg =
					"Error occured while retrieving the default upload location from the database. " +
							e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
		return null;
	}

	/*
	 * Retrieve the dataset-in-memory-threshold from the ML_CONFIGURATION
	 * database
	 */
	public int getDatasetInMemoryThreshold() throws DatabaseHandlerException {
		try {
			ResultSet result =
					connection.createStatement()
					.executeQuery("SELECT DATASET_IN_MEM_THRESHOLD FROM ML_CONFIGURATION");
			// if the dataset-in-memory-threshold is set
			if (result.first()) {
				int memoryThreshold = result.getInt("DATASET_IN_MEM_THRESHOLD");
				logger.info("Dataset in memory threshold: " + memoryThreshold + " bytes");
				return memoryThreshold;
			} else {
				logger.error("Dataset-in-memory-threshold is not set in the ML_CONFIGURATION database table.");
			}
		} catch (SQLException e) {
			String msg =
					"Error occured while retrieving the dataset-in-memory-threshold from the database. " +
							e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
		return -1;
	}

	/*
	 * Retrieve the Dataset uploading limit from the ML_CONFIGURATION database
	 */
	public long getDatasetUploadingLimit() throws DatabaseHandlerException {
		try {
			ResultSet result =
					connection.createStatement()
					.executeQuery("SELECT DATASET_UPLOADING_LIMIT FROM ML_CONFIGURATION");
			// if the uploading limit is set
			if (result.first()) {
				long memoryThreshold = result.getLong("DATASET_UPLOADING_LIMIT");
				logger.info("Dataset uploading limit: " + memoryThreshold + " bytes");
				return memoryThreshold;
			} else {
				logger.error("Dataset uploading limit is not set in the ML_CONFIGURATION database table.");
			}
		} catch (SQLException e) {
			String msg =
					"Error occured while retrieving the Dataset uploading limit from the database. " +
							e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
		return -1;
	}

	/*
	 * Retrieve the number of intervals to be used from the ML_CONFIGURATION
	 * database
	 */
	public int getNoOfIntervals() throws DatabaseHandlerException {
		try {
			ResultSet result =
					connection.createStatement()
					.executeQuery("SELECT INTERVALS FROM ML_CONFIGURATION");
			// if the number of intervals is set
			if (result.first()) {
				int intervals = result.getInt("INTERVALS");
				logger.info("Number of intervals uses to categorize numerical data: " + intervals);
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
		}
	}

	/*
	 * Retrieve the separator from the ML_CONFIGURATION database
	 */
	public String getSeparator() throws DatabaseHandlerException {
		try {
			ResultSet result =
					connection.createStatement()
					.executeQuery("SELECT SEPARATOR FROM ML_CONFIGURATION");
			// if the separator is set
			if (result.first()) {
				String separator = result.getNString("SEPARATOR");
				logger.info("Data points separator: " + separator);
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
		}
	}

	/*
	 * get the URI of the data source having the given ID, from the database
	 */
	public String getDataSource(String dataSourceId) throws Exception {
		try {
			ResultSet result =
					connection.createStatement()
					.executeQuery("SELECT URI FROM ML_DATASET WHERE ID='" +
							dataSourceId + "';");
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
		}
	}

	/*
	 * insert the new data set details to the the database
	 */
	// TODO: use JDBC preparedstatement to avoid SQL injection
	public String insertDatasetDetails(String uri, String source) throws DatabaseHandlerException {
		Statement statement;
		try {
			statement = connection.createStatement();

			// get the latest auto-generated Id
			ResultSet latestID =
					statement.executeQuery("SELECT ID FROM ML_DATASET order by CAST(ID AS INTEGER)");
			String newID;
			// If there are datasets already in the table
			if (latestID.last()) {
				// get the latest dataset ID and increment by one
				newID = String.valueOf(Integer.parseInt(latestID.getNString(1)) + 1);
			} else {
				// else, set the new dataset ID to one
				newID = String.valueOf(1);
			}
			// insert the dataset details to the database
			statement.execute("INSERT INTO ML_Dataset(ID,URI) VALUES('" + newID + "','" + uri +
			                  "/" + source + "');");
			logger.info("Successfully updated the details of data set: " + uri + "/" + source);
			logger.info("Dataset ID: " + newID);
			return newID;
		} catch (SQLException e) {
			String msg =
					"Error occured while inserting data source details to the database." +
							e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
	}

	/*
	 * update details for a given feature
	 */
	// TODO: use JDBC preparedstatement to avoid SQL injection
	public boolean updateFeature(String name, String dataSet, String type,
	                             ImputeOption imputeOption, boolean important)
	                            		 throws DatabaseHandlerException {
		try {
			// update database table with the new details
			return connection.createStatement().execute("UPDATE  ML_FEATURE SET TYPE ='" + type +
			                                            "',IMPUTE_METHOD='" +
			                                            imputeOption.toString() +
			                                            "', IMPORTANT=" + important +
			                                            " WHERE name='" + name +
			                                            "' AND Dataset='" + dataSet + "';");
		} catch (SQLException e) {
			String msg =
					"Error occured while updating the feature : " + name + " of data set: " +
							dataSet + " ." + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
	}

	/*
	 * Update the data type of a given feature
	 */
	public boolean updateDataType(String featureName, String datasetId, String featureType)
			throws DatabaseHandlerException {
		try {
			// update the database and return whether the query successfully
			// executed or not
			return connection.createStatement().execute("UPDATE  ML_FEATURE SET TYPE ='" +
					featureType + "' WHERE name='" +
					featureName + "' AND Dataset='" +
					datasetId + "';");
		} catch (SQLException e) {
			String msg =
					"Error occured while updating the feature : " + featureName +
					" of dataset ID: " + datasetId + " ." + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
	}

	/*
	 * Update the impute method option of a given feature
	 */
	public boolean updateImputeOption(String featureName, String datasetId, String imputeOption)
			throws DatabaseHandlerException {
		try {
			// update the database and return whether the query successfully
			// executed or not
			return connection.createStatement().execute("UPDATE  ML_FEATURE SET IMPUTE_METHOD ='" +
					imputeOption + "' WHERE name='" +
					featureName + "' AND Dataset='" +
					datasetId + "';");
		} catch (SQLException e) {
			String msg =
					"Error occured while updating the feature : " + featureName +
					" of dataset ID: " + datasetId + " ." + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
	}

	/*
	 * change whether a feature should be included as an input or not.
	 */
	public boolean updateIsIncludedFeature(String featureName, String datasetId, boolean isInput)
			throws DatabaseHandlerException {
		try {
			// update the database and return whether the query successfully executed or not
			return connection.createStatement().execute("UPDATE  ML_FEATURE SET IMPORTANT =" +
					isInput + " WHERE name='" +
					featureName + "' AND Dataset='" +
					datasetId + "';");
		} catch (SQLException e) {
			String msg =
					"Error occured while updating the feature : " + featureName +
					" of dataset ID: " + datasetId + " ." + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
	}

	/*
	 * Update the database with all the summary stats of the sample
	 */
	// TODO: use JDBC preparedstatement to avoid SQL injection
	public void updateSummaryStatistics(String dataSourceId, String[] header, FeatureType[] type,
	                                    List<Map<String, Integer>> graphFrequencies,
	                                    List<Integer> missing, List<Integer> unique,
	                                    List<DescriptiveStatistics> descriptiveStats)
	                                    		throws Exception {
		try {
			JSONObject summaryStat;
			for (int column = 0; column < header.length; column++) {
				// get the json representation of the column
				summaryStat =
						createJson(column, type, graphFrequencies, missing, unique,
						           descriptiveStats);
				// put the values to the database table. If the feature already
				// exists, updates the row. if not, inserts as a new row.
				connection.createStatement()
				.execute("MERGE INTO ML_FEATURE(NAME,DATASET,TYPE,SUMMARY,IMPUTE_METHOD,IMPORTANT) VALUES('" +
						header[column] +
						"','" +
						dataSourceId +
						"','" +
						type[column] +
						"','" +
						summaryStat.toString() +
						"','" +
						ImputeOption.DISCARD + "','TRUE')");
				connection.commit();
			}
			logger.info("Successfully updated the summary statistics for data source: " +
					dataSourceId);
		} catch (SQLException e) {
			String msg =
					"Error occured while updating the database with summary statistics of the data source: " +
							dataSourceId + "." + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
	}

	/*
	 * Create the json string with summary stat for a given column
	 */
	// TODO: don't send NaN for int fields, that will throw error in parsing
	private JSONObject createJson(int column, FeatureType[] type,
	                              List<Map<String, Integer>> graphFrequencies,
	                              List<Integer> missing, List<Integer> unique,
	                              List<DescriptiveStatistics> descriptiveStats) {
		JSONObject json = new JSONObject();
		JSONArray freqs = new JSONArray();
		Object[] categoryNames = graphFrequencies.get(column).keySet().toArray();
		// create an array with intervals/categories and their frequencies
		for (int i = 0; i < graphFrequencies.get(column).size(); i++) {
			JSONObject temp = new JSONObject();
			temp.put("range", categoryNames[i].toString());
			temp.put("frequency", graphFrequencies.get(column).get(categoryNames[i].toString()));
			freqs.put(temp);
		}
		// put the statistics to an json object
		json.put("type", type[column].toString());
		json.put("unique", unique.get(column));
		json.put("missing", missing.get(column));
		json.put("mean", descriptiveStats.get(column).getMean());
		json.put("median", descriptiveStats.get(column).getPercentile(50));
		json.put("std", descriptiveStats.get(column).getStandardDeviation());
		json.put("frequencies", freqs);

		return json;
	}

	/*
	 * Returns a set of features in a given range of a data set.
	 */
	// TODO: use JDBC preparedstatement to avoid SQL injection
	public Feature[] getFeatures(String dataSet, int startPoint, int numberOfFeatures)
			throws DatabaseHandlerException {
		List<Feature> features = new ArrayList<Feature>();
		try {
			// get the set of features starting from @startPoint
			ResultSet result =
					connection.createStatement()
					.executeQuery("SELECT * FROM ML_FEATURE WHERE dataset='" +
							dataSet + "' ORDER BY NAME LIMIT " +
							numberOfFeatures + " OFFSET " +
							(startPoint - 1) + "");

			while (result.next()) {
				FeatureType featureType = FeatureType.NUMERICAL;
				if ("CATEGORICAL".equals(result.getNString(3))) {
					featureType = FeatureType.CATEGORICAL;
				}

				ImputeOption imputeOperation = ImputeOption.DISCARD;
				if ("REPLACE_WTH_MEAN".equals(result.getNString(5))) {
					imputeOperation = ImputeOption.REPLACE_WTH_MEAN;
				} else if ("REGRESSION_IMPUTATION".equals(result.getNString(5))) {
					imputeOperation = ImputeOption.REPLACE_WTH_MEAN;
				}
				features.add(new Feature(result.getNString(1), result.getBoolean(6), featureType,
				                         imputeOperation, result.getNString(4)));
			}
		} catch (SQLException e) {
			String msg =
					"Error occured while retireving features of data set: " + dataSet + " ." +
							e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
		return features.toArray(new Feature[features.size()]);
	}
}
