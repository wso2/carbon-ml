package org.wso2.carbon.ml.dataset;

import java.io.IOException;
import java.util.List;

public class DatasetService {
	

	public void importData(String source, String destination){
		//TODO
	}
	
	/*
	 * Calculate summary stats and populate the database
	 */
	public void generateSummaryStats(String dataSource,int dataSourceID,int noOfRecords, int noOfIntervals, String seperator) throws Exception {
		DatasetSummary summary=new DatasetSummary();
		summary.calculateSummary(dataSourceID, noOfRecords, noOfIntervals, seperator);
		summary.updateDatabase(dataSourceID);
	}
	
	/*
	 * Get the summary stats from the database
	 */
	public String getSummaryStats(String dataSource,int noOfRecords) throws IOException{
		//TODO
		String summary="";
		return summary;
	}

	public List<Object> getSamplePoints(String feature1, String feature2, int maxNoOfPoints, String SelectionPolicy){
		//TODO
		return null;
	}
	
	public List<Object> getSampleDistribution(String feature, int noOfBins){
		//TODO
		return null;		
	}
}
