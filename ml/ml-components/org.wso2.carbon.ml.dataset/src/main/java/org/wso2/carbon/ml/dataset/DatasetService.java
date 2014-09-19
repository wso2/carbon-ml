/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.dataset;

import java.io.IOException;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DatasetService {
	private static final Log LOGGER = LogFactory.getLog(DatasetService.class);
	
	public String getDatasetUploadingDir() throws DatasetServiceException {
		try{
			DatabaseHandler dbHandler = new DatabaseHandler();
			String uri = dbHandler.getDefaultUploadLocation();
			if(uri != null && uri.length() > 0){
				return uri;
			}else{
				String msg = "Dataset uploading location can't be null or empty. ";
				LOGGER.error(msg);
				throw new DatasetServiceException(msg);
			}
		}catch(Exception ex){
			String msg =
					"Failed to retrieve dataset uploading location. " + ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * Retrieve the dataset-in-memory-threshold from the ML_CONFIGURATION database
	 */
	//TODO: read from DB
	public int getDatasetInMemoryThreshold() throws DatasetServiceException {
		try {
	        DatabaseHandler dbHandler = new DatabaseHandler();
			return dbHandler.getDatasetInMemoryThreshold();
        } catch (DatabaseHandlerException e) {
        	String msg = "Failed to retrieve dataset-in-memory-threshold. ";
			LOGGER.error(msg);
			throw new DatasetServiceException(msg);
        }
	}

	/*
	 * Retrieve the Dataset uploading limit from the ML_CONFIGURATION database
	 */
	//TODO: read from DB
	public long getDatasetUploadingLimit() throws DatasetServiceException {
		try {
	        DatabaseHandler dbHandler = new DatabaseHandler();
			return dbHandler.getDatasetUploadingLimit();
        } catch (DatabaseHandlerException e) {
        	String msg = "Failed to retrieve dataset uploading limit. ";
			LOGGER.error(msg);
			throw new DatasetServiceException(msg);
        }
	}

	public int importData(String source) throws Exception {
		try {
			// get the uri of the file
			DatabaseHandler dbHandler = new DatabaseHandler();
			String uri = dbHandler.getDefaultUploadLocation();
			
			if (uri!=null) {
				// insert the details to the table
				int datasetId =dbHandler.insertDatasetDetails(uri, source);
				return datasetId;
			} else {
				LOGGER.error("Default uploading location not found.");
			}
		} catch (Exception e) {
			String msg =
					"Failed to update the data-source details in the database. " + e.getMessage();
			LOGGER.error(msg, e);
			throw new DatasetServiceException(msg);
		}
		return -1;
	}

	/*
	 * Calculate summary stats and populate the database
	 */
	public int generateSummaryStats(int dataSourceID, int noOfRecords)
	                                		 throws DatasetServiceException {
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			DatasetSummary summary = new DatasetSummary();
			int noOfFeatures = summary.generateSummary(dataSourceID, noOfRecords, dbHandler.getNoOfIntervals(), dbHandler.getSeperator());
			LOGGER.info("Summary statistics successfully generated. ");
			return noOfFeatures;
		} catch (DatasetServiceException e) {
			String msg = "Failed to calculate summary Statistics. " + e.getMessage();
			LOGGER.error(msg, e);
			throw new DatasetServiceException(msg);
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to connect to database. " + e.getMessage();
			LOGGER.error(msg, e);
			throw new DatasetServiceException(msg);
        }
	}

	/*
	 * Update feature with the given details
	 */
	public boolean updateFeature(String name, int dataSet, String type, ImputeOption imputeOption,
	                             boolean important) throws DatasetServiceException {
		DatabaseHandler dbHandler;
        try {
	        dbHandler = new DatabaseHandler();
	        return dbHandler.updateFeature(name, dataSet, type, imputeOption, important);
        } catch (DatabaseHandlerException e) {
        	String msg = "Updating feature failed. " + e.getMessage();
			LOGGER.error(msg, e);
	        throw new DatasetServiceException(msg);
        }
	}
	
	/*
	 * Returns a set of features in a given range of a data set.
	 */
	public Feature[] getFeatures(int dataSet, int startPoint, int numberOfFeatures) throws DatasetServiceException{
		DatabaseHandler dbHandler;
        try {
	        dbHandler = new DatabaseHandler();
	        return dbHandler.getFeatures(dataSet, startPoint, numberOfFeatures);
        } catch (DatabaseHandlerException e) {
        	String msg = "Failed to retrieve features. " + e.getMessage();
			LOGGER.error(msg, e);
	        throw new DatasetServiceException(msg);
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
