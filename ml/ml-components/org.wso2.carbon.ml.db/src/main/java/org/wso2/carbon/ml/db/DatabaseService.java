package org.wso2.carbon.ml.db;

public class DatabaseService {

	// TODO: read these from DB
	public String getDatasetUploadingDir() {
		return "/home/upul/temp/";
	}

	public int getDatasetInMemoryThreshold() {
		return 1024 * 1024;
	}

	public long getDatasetUploadingLimit() {
		return 1024 * 1024 * 256;
	}
	
	public Feature[] getFeatures(int start, int numOfFeatures){
		
		//TODO: 
		Feature f1 = new Feature("age", false, FeatureType.NUMERICAL, ImputeOperation.REPLACE_WITH_MAX);
		Feature f2 = new Feature("salary", false, FeatureType.NUMERICAL, ImputeOperation.REPLACE_WITH_MAX);
		
		Feature[] features = new Feature[2];
		features[0] = f1;
		features[1] = f2;
		
		return features;
	}
}
