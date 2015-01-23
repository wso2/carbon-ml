/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.database.dto;

/**
 * Feature Class.
 */
public class FeatureSummary {

	private String fieldName;
	private boolean isInput;
	private String type;
	private String imputeOperation;
	private String summaryStats;

	public FeatureSummary(String fieldName, boolean isInput, String type,
	        String imputeOperation, String summaryStats) {
		this.fieldName = fieldName;
		this.isInput = isInput;
		this.type = type;
		this.imputeOperation = imputeOperation;
		this.summaryStats = summaryStats;
	}

	/**
	 * Returns the name of the feature.
	 *
	 * @return     Name of the feature
	 */
	public String getFieldName() {
		return this.fieldName;
	}

	/**
	 * Set the name of the feature.
	 *
	 * @param fieldName    Name of the feature
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * Returns the summary statistics json string.
	 *
	 * @return     Summary statistics json string
	 */
	public String getSummaryStats() {
		return this.summaryStats;
	}

	/**
	 * Set the summary statistics json string.
	 *
	 * @param summaryStats     Summary statistics json string
	 */
	public void setSummaryStats(String summaryStats) {
		this.summaryStats = summaryStats;
	}

	/**
	 * Returns whether the feature is treated as an input or not.
	 *
	 * @return     Boolean value indicating whether the feature is treated as an
	 *             input or not
	 */
	public boolean isInput() {
		return isInput;
	}

	/**
	 * Sets whether the feature is treated as an input or not.
	 *
	 * @param isInput  Boolean value indicating whether the feature is treated as an
	 *                 input or not
	 */
	public void setInput(boolean isInput) {
		this.isInput = isInput;
	}

	/**
	 * Returns the data-type of the feature.
	 *
	 * @return     Data-type of the feature
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the data-type of the feature.
	 *
	 * @param type     Data-type of the feature
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Returns the impute method of the feature.
	 *
	 * @return     Impute method of the feature
	 */
	public String getImputeOperation() {
		return imputeOperation;
	}

	/**
	 * Sets the impute method of the feature.
	 *
	 * @param imputeOperation  Impute method of the feature
	 */
	public void setImputeOperation(String imputeOperation) {
		this.imputeOperation = imputeOperation;
	}
}
