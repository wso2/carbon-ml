package org.wso2.carbon.ml.dataset;

//TODO: use type safe enum
public class ImputeOption {
/*	DISCARD,
	REPLACE_WTH_MEAN,
	REGRESSION_IMPUTATION
*/
	private String method;
	
	//default impute method
	public ImputeOption(){
		this.method="DISCARD";
	}
	
	public void setMethod(String type){
		this.method=type;
	}
	
	public String getMethod(){
		return this.method;
	}
}
