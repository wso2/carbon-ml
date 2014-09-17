package org.wso2.carbon.ml.db;

public class FeatureType {

	private String featureType;

	public FeatureType() {
		this.featureType = "NUMERICAL";
	}

	public String getFeatureType() {
		return featureType;
	}

	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}
	
	//TODO: refactor this
	public static String[] getAllFeatureTypes(){
		return new String[]{ "Numerical", "Categorical"};
	}
}
