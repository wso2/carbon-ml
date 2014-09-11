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
	public void generateSummaryStats(String dataSource,int noOfRecords) throws Exception{
		Summary summary=new Summary();
		summary.calculateSummary(dataSource, noOfRecords);
		summary.updateDatabase();
	}
	
	/*
	 * Get the summary stats from the database
	 */
	public String getSummaryStats(String dataSource,int noOfRecords) throws IOException{
		//TODO
		return null;
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
