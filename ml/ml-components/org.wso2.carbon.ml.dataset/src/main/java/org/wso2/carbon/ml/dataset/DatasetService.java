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
	private final Log logger = LogFactory.getLog(DatasetService.class);

	/*
	 * Retrieve the file uploading directory from the database
	 */
	public String getDatasetUploadingDir() throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			String uri = dbHandler.getDefaultUploadLocation();
			if (uri != null && uri.length() > 0) {
				return uri;
			} else {
				String msg = "Dataset uploading location can't be null or empty. ";
				logger.error(msg);
				throw new DatasetServiceException(msg);
			}
		} catch (Exception ex) {
			String msg = "Failed to retrieve dataset uploading location. " + ex.getMessage();
			logger.error(msg, ex);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * Retrieve the dataset-in-memory-threshold from the ML_CONFIGURATION
	 * database
	 */
	public int getDatasetInMemoryThreshold() throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			return dbHandler.getDatasetInMemoryThreshold();
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to retrieve dataset-in-memory-threshold. ";
			logger.error(msg);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * Retrieve the Dataset uploading limit from the ML_CONFIGURATION database
	 */
	public long getDatasetUploadingLimit() throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			return dbHandler.getDatasetUploadingLimit();
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to retrieve dataset uploading limit. ";
			logger.error(msg);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * Update the database with the imported data set details
	 */
	public String updateDatasetDetails(String source) throws Exception {
		String msg;
		try {
			// get the uri of the file
			DatabaseHandler dbHandler = new DatabaseHandler();
			String uri = dbHandler.getDefaultUploadLocation();
			if (uri != null) {
				// check whether the file is a valid one
				if (isValidFile(uri + "/" + source)) {
					// insert the details to the table
					String datasetId = dbHandler.insertDatasetDetails(uri, source);
					return datasetId;
				} else {
					msg = "Invalid input file: " + source;
				}
			} else {
				msg = "Default uploading location not found.";
			}
		} catch (Exception e) {
			msg = "Failed to update the data-source details in the database. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
		logger.error(msg);
		throw new DatasetServiceException(msg);
	}

	/*
	 * Calculate summary stats from a sample of given size and populate the
	 * database. Value of -1 for noOfRecords will generate summary statistics
	 * using the whole data set.
	 */
	public int generateSummaryStats(String dataSourceID, int noOfRecords)
			throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			DatasetSummary summary = new DatasetSummary();
			int noOfFeatures =
					summary.generateSummary(dataSourceID, noOfRecords,
					                        dbHandler.getNoOfIntervals(),
					                        dbHandler.getSeparator());
			logger.info("Summary statistics successfully generated. ");
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

	/*
	 * Update feature with the given details
	 */
	public boolean updateFeature(String name, String dataSet, String type,
	                             ImputeOption imputeOption, boolean important)
	                            		 throws DatasetServiceException {
		DatabaseHandler dbHandler;
		try {
			dbHandler = new DatabaseHandler();
			return dbHandler.updateFeature(name, dataSet, type, imputeOption, important);
		} catch (DatabaseHandlerException e) {
			String msg = "Updating feature failed. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * Update the data type of a given feature
	 */
	public boolean updateDataType(String featureName, String datasetId, String featureType)
			throws DatasetServiceException {
		DatabaseHandler dbHandler;
		try {
			dbHandler = new DatabaseHandler();
			return dbHandler.updateDataType(featureName, datasetId, featureType);
		} catch (DatabaseHandlerException e) {
			String msg = "Updating feature type failed. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * Update the impute method option of a given feature
	 */
	public boolean updateImputeOption(String featureName, String datasetId, String imputeOption)
			throws DatasetServiceException {
		DatabaseHandler dbHandler;
		try {
			dbHandler = new DatabaseHandler();
			return dbHandler.updateImputeOption(featureName, datasetId, imputeOption);
		} catch (DatabaseHandlerException e) {
			String msg = "Updating impute option failed. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * change whether a feature should be included as an input or not.
	 */
	public boolean updateIsIncludedFeature(String featureName, String datasetId, boolean isInput)
			throws DatasetServiceException {
		DatabaseHandler dbHandler;
		try {
			dbHandler = new DatabaseHandler();
			return dbHandler.updateIsIncludedFeature(featureName, datasetId, isInput);
		} catch (DatabaseHandlerException e) {
			String msg = "Updating impute option failed. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * Returns a set of features in a given range of a data set.
	 */
	public Feature[] getFeatures(String dataSet, int startPoint, int numberOfFeatures)
			throws DatasetServiceException {
		DatabaseHandler dbHandler;
		try {
			dbHandler = new DatabaseHandler();
			return dbHandler.getFeatures(dataSet, startPoint, numberOfFeatures);
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to retrieve features. " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * Check whether the given file is valid
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

	// TODO
	public List<Object> getSampleDistribution(String feature, int noOfBins) {
		return null;
	}
}
