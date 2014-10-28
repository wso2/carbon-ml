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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatasetService {
	private static final Log logger = LogFactory.getLog(DatasetService.class);

	/**
	 * This method extract data-set configurations from the database
	 *
	 * @return
	 * @throws DatasetServiceException
	 */
	public DatasetConfig getDatasetConfig() throws DatasetServiceException {
		try {
			DatabaseHandler handler = DatabaseHandler.getDatabaseHandler();
			return handler.getDatasetConfig();
		} catch (DatabaseHandlerException ex) {
			String msg = "Error has occurred while reading dataset config from database";
			logger.error(msg, ex);
			throw new DatasetServiceException(msg);
		}
	}
	
	/**
	 * Returns a absolute uri of a given data source
	 * 
	 * @param dataSourceId
	 * @return
	 * @throws DatasetServiceException
	 */
	public String getDataSource(String dataSourceId) throws DatasetServiceException {
		try {
			DatabaseHandler handler = DatabaseHandler.getDatabaseHandler();
			return handler.getDataSource(dataSourceId);
		} catch (DatabaseHandlerException ex) {
			String msg = "Error has occurred while reading dataset URL from database";
			logger.error(msg, ex);
			throw new DatasetServiceException(msg);
		}
	}
	
	/**
	 * Returns the separator for the input file
	 * 
	 * @return
	 * @throws DatasetServiceException
	 */
	public String getSeparator() throws DatasetServiceException {
		try {
			DatabaseHandler handler = DatabaseHandler.getDatabaseHandler();
			return handler.getSeparator();
		} catch (DatabaseHandlerException ex) {
			String msg = "Error has occurred while reading dataset column separator from database";
			logger.error(msg, ex);
			throw new DatasetServiceException(msg);
		}
	}

	/**
	 * Update the database with the imported data set details
	 *
	 * @param name
	 * @return
	 * @throws DatasetServiceException
	 */
	public UUID registerDataset(String name,UUID projectID) throws DatasetServiceException {
		String msg;
		try {
			// get the default upload location of the file
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			String uploadDir = dbHandler.getDatasetConfig().getDatasetUploadingLoc();
			if (uploadDir != null) {
				// check whether the file is a valid one
				if (isValidFile(uploadDir + "/" + name)) {
					// insert the details to the table and return the ID
					return dbHandler.insertDatasetDetails(uploadDir+"/"+name, projectID,null);
				} else {
					msg = "Invalid input file: " + name;
				}
			} else {
				msg = "Default uploading location not found.";
			}
		} catch (DatabaseHandlerException e) {
			msg = "Failed to update the data-source details in the database. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
		logger.error(msg);
		throw new DatasetServiceException(msg);
	}

	/**
	 * Calculate summary statistics from a sample of given size and populate the
	 * database. Value of -1 for noOfRecords will generate summary statistics
	 * using the whole data set.
	 *
	 * @param dataSetId
	 * @param noOfRecords
	 * @return
	 * @throws DatasetServiceException
	 */
	public int generateSummaryStats(String dataSetId, int noOfRecords)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			DatasetSummary summary = new DatasetSummary();
			int noOfFeatures =
					summary.generateSummary(dataSetId, noOfRecords,
					                        dbHandler.getNumberOfBucketsInHistogram(),
					                        dbHandler.getSeparator());
			logger.debug("Summary statistics successfully generated. ");
			
			//update the dataset table with sample points
			updateDatasetSample(dataSetId,summary.samplePoints());
			return noOfFeatures;
		} catch (DatasetServiceException e) {
			String msg = "Failed to calculate summary Statistics. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to connect to database. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}
	
	/**
	 * 
	 * @param dataSamples
	 * @throws DatasetServiceException 
	 */
	public void updateDatasetSample(String datasetId, SamplePoints dataSamples) throws DatasetServiceException{
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.updateDatasetSample(datasetId, dataSamples);
		} catch (DatabaseHandlerException e) {
			String msg = "Updating feature failed. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/**
	 * Update feature with the given details
	 *
	 * @param name
	 * @param dataSetId
	 * @param type
	 * @param imputeOption
	 * @param important
	 * @throws DatasetServiceException
	 */
	public void updateFeature(String name, String dataSetId, String type,
	                          ImputeOption imputeOption, boolean important)
	                        		  throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.updateFeature(name, dataSetId, type, imputeOption, important);
		} catch (DatabaseHandlerException e) {
			String msg = "Updating feature failed. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/**
	 * Update the data type of a given feature
	 *
	 * @param featureName
	 * @param dataSetId
	 * @param featureType
	 * @throws DatasetServiceException
	 */
	public void updateDataType(String featureName, String dataSetId, String featureType)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.updateDataType(featureName, dataSetId, featureType);
		} catch (DatabaseHandlerException e) {
			String msg = "Updating feature type failed. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/**
	 * Update the impute method option of a given feature
	 *
	 * @param featureName
	 * @param dataSetId
	 * @param imputeOption
	 * @throws DatasetServiceException
	 */
	public void updateImputeOption(String featureName, String dataSetId, String imputeOption)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.updateImputeOption(featureName, dataSetId, imputeOption);
		} catch (DatabaseHandlerException e) {
			String msg = "Updating impute option failed. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/**
	 * change whether a feature should be included as an input or not.
	 *
	 * @param featureName
	 * @param dataSetId
	 * @param isInput
	 * @throws DatasetServiceException
	 */
	public void updateIsIncludedFeature(String featureName, String dataSetId, boolean isInput)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.updateIsIncludedFeature(featureName, dataSetId, isInput);
		} catch (DatabaseHandlerException e) {
			String msg = "Updating impute option failed. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/**
	 * Returns a set of features in a given range of a data set.
	 *
	 * @param dataSetId
	 * @param startIndex
	 * @param numberOfFeatures
	 * @return
	 * @throws DatasetServiceException
	 */
	public Feature[] getFeatures(String dataSetId, int startIndex, int numberOfFeatures)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			return dbHandler.getFeatures(dataSetId, startIndex, numberOfFeatures);
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to retrieve features. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/**
	 * Returns the names of the features having the given type
	 * (Categorical/Numerical) of a given data set
	 *
	 * @param dataSetId
	 * @param featureType
	 * @return
	 * @throws DatasetServiceException
	 */
	public String[] getFeatureNames(String dataSetId, String featureType)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			return dbHandler.getFeatureNames(dataSetId, featureType);
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to retrieve features. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/**
	 * Check whether the given file is valid
	 *
	 * @param path
	 * @return
	 */
	private boolean isValidFile(String path) {
		File file = new File(path);
		// check whether the file exists
		if (file.exists() && !file.isDirectory()) {
			// check whether it has the .csv extension
			return path.matches("(.)+(\\." + FileFormats.CSV.toString() + ")");
		} else {
			return false;
		}
	}

	/**
	 * Returns data point of the selected sample,needed for the scatter plot
	 * feature1 : x-Axis
	 * feature2 : Y-Axis
	 * feature3 : feature to be grouped by (color code)
	 *
	 * @param dataSetId
	 * @param feature1
	 * @param feature2
	 * @return
	 * @throws DatabaseHandlerException 
	 */
	public JSONArray getSamplePoints(String dataSetId, String feature1, String feature2,
	                                 String feature3) throws DatabaseHandlerException {
		DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
		SamplePoints sample=dbHandler.getDatasetSample(dataSetId);
		List<List<String>> columnData = sample.getSamplePoints();
		Map<String, Integer>  dataHeaders = sample.getHeader();
		JSONArray samplePointsArray = new JSONArray();
		int firstFeatureColumn = dataHeaders.get(feature1);
		int secondFeatureColumn = dataHeaders.get(feature2);
		int thirdFeatureColumn = dataHeaders.get(feature3);

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
	 * Returns the summary statistics for a given feature of a given data-set
	 *
	 * @param dataSetId
	 * @param feature
	 * @return
	 * @throws DatasetServiceException
	 */
	public JSONObject getSummaryStats(String dataSetId, String feature)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			return dbHandler.getSummaryStats(dataSetId, feature);
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to retrieve summary statistics. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}
}
