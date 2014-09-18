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
				String msg =
						"Dataset uploading location can't be null or empty";
				LOGGER.error(msg);
				throw new DatasetServiceException(msg);
			}
		}catch(Exception ex){
			String msg =
					"Error occured while reading dataset uploading location" + ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatasetServiceException(msg);
		}
	}

	//TODO: read from DB
	public int getDatasetInMemoryThreshold() {
		return 1024 * 1024;
	}

	//TODO: read from DB
	public long getDatasetUploadingLimit() {
		return 1024 * 1024 * 256;
	}
	
	public Feature[] getFeatures(int start, int numOfFeatures) {

		// TODO:
		Feature f1 = new Feature("age", false, new FeatureType(),
				new ImputeOption());
		Feature f2 = new Feature("salary", false, new FeatureType(),
				new ImputeOption());

		Feature[] features = new Feature[2];
		features[0] = f1;
		features[1] = f2;

		return features;
	}

	public int importData(String source) throws Exception {
		try {
			// get the uri of the file
			DatabaseHandler dbHandler = new DatabaseHandler();
			String uri = dbHandler.getDefaultUploadLocation();
			
			if (uri!=null) {
				// insert the details to the table
				int datasetId =dbHandler.insertDatasetDetails(uri, source);
				
				//TODO: remove the following line. This line is only for the testing purpose.
				generateSummaryStats(datasetId, 1000, 20, ",");
				
				return datasetId;
			} else {
				LOGGER.error("Default uploading location not found.");
			}
		} catch (Exception e) {
			String msg =
					"Error occured while updating the data-source details in the database. " + e.getMessage();
			LOGGER.error(msg, e);
			throw new DatasetServiceException(msg);
		}
		return -1;
	}

	/*
	 * Calculate summary stats and populate the database
	 */
	public void generateSummaryStats(int dataSourceID, int noOfRecords,
	                                 int noOfIntervals, String seperator)
	                                		 throws DatasetServiceException {
		
		try {
			DatasetSummary summary = new DatasetSummary();
			summary.generateSummary(dataSourceID, noOfRecords, noOfIntervals, seperator);
			LOGGER.info("Summary statistics successfully generated. ");
		} catch (DatasetServiceException e) {
			String msg = "Summary Statistics calculation failed. " + e.getMessage();
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

	/*
	 * Get summary statistics of a data set from the database
	 */
	public String getSummaryStats(int dataSourceID, int noOfRecords) throws IOException {
		// TODO
		String summary = "";
		return summary;
	}

	public List<Object> getSamplePoints(String feature1, String feature2, int maxNoOfPoints,
	                                    String selectionPolicy) {
		// TODO
		return null;
	}

	public List<Object> getSampleDistribution(String feature, int noOfBins) {
		// TODO
		return null;
	}
}
