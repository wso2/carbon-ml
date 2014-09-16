package org.wso2.carbon.ml.dataset;

public class FeatureType {

	private static final String NUMERICAL="NUMERICAL";
	private static final String CATEGORICAL="CATEGORICAL";
	
	public String numerical(){
		return NUMERICAL;
	}
	
	public String categorical(){
		return CATEGORICAL;
	}
}