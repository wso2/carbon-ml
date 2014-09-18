package org.wso2.carbon.ml.dataset;

//TODO: Write using Typesafeenum : http://stackoverflow.com/questions/5092015/advantages-of-javas-enum-over-the-old-typesafe-enum-pattern
public class FeatureType {

	private String featureType;
	
	public FeatureType(){
		featureType = NUMERICAL;
	}
	
	public String getFeatureType(){
		return featureType;
	}
	
	public void setFeatureType(String featureType){
		this.featureType = featureType;
	}
	
	private static final String NUMERICAL="NUMERICAL";
	private static final String CATEGORICAL="CATEGORICAL";
	
	public String numerical(){
		return NUMERICAL;
	}
	
	public String categorical(){
		return CATEGORICAL;
	}
	
}