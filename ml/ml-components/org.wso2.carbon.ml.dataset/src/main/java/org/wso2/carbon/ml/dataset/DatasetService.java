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

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
			DatabaseHandler handler = new DatabaseHandler();
			return handler.getDatasetConfig();
		} catch (DatabaseHandlerException ex) {
			String msg = "Error has occurred while reading dataset config from database";
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
	//TODO:register data-set, arguments : name+description
	public String registerDataset(String name) throws DatasetServiceException {
		String msg;
		String description="";
		try {
			// get the default upload location of the file
			DatabaseHandler dbHandler = new DatabaseHandler();
			String uploadDir = dbHandler.getDatasetConfig().getDatasetUploadingLoc();
			if (uploadDir != null) {
				// check whether the file is a valid one
				if (isValidFile(uploadDir + "/" + name)) {
					// insert the details to the table
					String datasetId = dbHandler.insertDatasetDetails(uploadDir + "/" + name, description);
					return datasetId;
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
	 * @param dataSourceID
	 * @param noOfRecords
	 * @return
	 * @throws DatasetServiceException
	 */
	//TODO : use debug logger
	public int generateSummaryStats(String dataSourceID, int noOfRecords)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			DatasetSummary summary = new DatasetSummary();
			int noOfFeatures =
					summary.generateSummary(dataSourceID, noOfRecords,
					                        dbHandler.getNumberOfBucketsInHistogram(),
					                        dbHandler.getSeparator());
			logger.debug("Summary statistics successfully generated. ");
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
	 * Update feature with the given details
	 * 
	 * @param name
	 * @param dataSet
	 * @param type
	 * @param imputeOption
	 * @param important
	 * @throws DatasetServiceException
	 */
	public void updateFeature(String name, String dataSet, String type, ImputeOption imputeOption,
	                          boolean important) throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			dbHandler.updateFeature(name, dataSet, type, imputeOption, important);
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
	 * @param datasetId
	 * @param featureType
	 * @throws DatasetServiceException
	 */
	public void updateDataType(String featureName, String datasetId, String featureType)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			dbHandler.updateDataType(featureName, datasetId, featureType);
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
	 * @param datasetId
	 * @param imputeOption
	 * @throws DatasetServiceException
	 */
	public void updateImputeOption(String featureName, String datasetId, String imputeOption)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			dbHandler.updateImputeOption(featureName, datasetId, imputeOption);
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
	 * @param datasetId
	 * @param isInput
	 * @throws DatasetServiceException
	 */
	public void updateIsIncludedFeature(String featureName, String datasetId, boolean isInput)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			dbHandler.updateIsIncludedFeature(featureName, datasetId, isInput);
		} catch (DatabaseHandlerException e) {
			String msg = "Updating impute option failed. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/**
	 * Returns a set of features in a given range of a data set.
	 * 
	 * @param dataSet
	 * @param startIndex
	 * @param numberOfFeatures
	 * @return
	 * @throws DatasetServiceException
	 */
	public Feature[] getFeatures(String dataSet, int startIndex, int numberOfFeatures)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			return dbHandler.getFeatures(dataSet, startIndex, numberOfFeatures);
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

	// TODO
	public List<Object> getSamplePoints(String feature1, String feature2, int maxNoOfPoints,
	                                    String selectionPolicy) {
		return null;
	}
}
