package org.wso2.carbon.ml.db;

public class ImputeOperation {

	private String imputateOperation;

	public ImputeOperation() {
		this.imputateOperation = "REPLACE_WITH_MAX";
	}

	public String getImputeOperation() {
		return imputateOperation;
	}

	public void setImputeOperation(String imputeOperation) {
		this.imputateOperation = imputeOperation;
	}
}
