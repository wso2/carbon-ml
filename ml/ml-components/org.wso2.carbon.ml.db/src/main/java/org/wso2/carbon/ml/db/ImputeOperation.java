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

    //TODO: refactor this
	public static String[] getAllmputeOperations() {
		return new String[] { "REPLACE_WITH_MAX", "DROP_ROW",
				"REPLACE_WITH_MEAN" };
	}
}
