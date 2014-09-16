package org.wso2.carbon.ml.db;

public class Feature {

	private String fieldName;
	private boolean isInput;
	private FeatureType type;
	private ImputeOperation imputeOperation;

	public Feature(String fieldName, boolean isInput, FeatureType type,
			ImputeOperation imputeOperation) {

		this.fieldName = fieldName;
		this.isInput = isInput;
		this.type = type;
		this.imputeOperation = imputeOperation;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean isInput() {
		return isInput;
	}

	public void setInput(boolean isInput) {
		this.isInput = isInput;
	}

	public FeatureType getType() {
		return type;
	}

	public void setType(FeatureType type) {
		this.type = type;
	}

	public ImputeOperation getImputeOperation() {
		return imputeOperation;
	}

	public void setImputeOperation(ImputeOperation imputeOperation) {
		this.imputeOperation = imputeOperation;
	}
}
