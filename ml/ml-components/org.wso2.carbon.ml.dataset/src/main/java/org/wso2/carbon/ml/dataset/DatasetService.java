package org.wso2.carbon.ml.dataset;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DatasetService {
	private static final Log LOGGER = LogFactory.getLog(DatasetService.class);

	public int importData(String source) throws Exception {
		try {
			// get the uri of the file
			DatabaseHandler dbHandler = new DatabaseHandler();
			String uri = dbHandler.getDefaultUploadLocation();
			
			if (uri!=null) {
				// insert the details to the table
				Statement insert = dbHandler.getConnection().createStatement();
				insert.execute("INSERT INTO ML_Dataset(URI) VALUES('" + uri+"/"+source + "');");
				
				// get the latest auto-generated Id
				ResultSet latestID = insert.getGeneratedKeys();
				latestID.first();
				return Integer.parseInt(latestID.getNString(1));
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
	public void generateSummaryStats(String dataSource, int dataSourceID, int noOfRecords,
	                                 int noOfIntervals, String seperator)
	                                		 throws DatasetServiceException {
		
		try {
			DatasetSummary summary = new DatasetSummary();
			summary.generateSummary(dataSourceID, noOfRecords, noOfIntervals, seperator);
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
