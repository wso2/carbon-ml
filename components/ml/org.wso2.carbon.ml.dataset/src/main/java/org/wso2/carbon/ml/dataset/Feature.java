/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.dataset;


public class Feature {

	private String fieldName;
	private boolean isInput;
	private FeatureType type;
	private ImputeOption imputeOperation;
	private String summaryStats;

	public Feature(String fieldName, boolean isInput, FeatureType type,
			ImputeOption imputeOperation, String summaryStats) {

		this.fieldName = fieldName;
		this.isInput = isInput;
		this.type = type;
		this.imputeOperation = imputeOperation;
		this.summaryStats = summaryStats;
	}

	/**
	 * Returns the name of the feature
	 * @return
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Set the name of the feature
	 * @param fieldName
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	/**
	 * Returns the summary statistics json string
	 * @return
	 */
	public String getSummaryStats() {
		return summaryStats;
	}

	/**
	 * Set the summary statistics json string
	 * @param summaryStats
	 */
	public void setSummaryStats(String summaryStats) {
		this.summaryStats = summaryStats;
	}

	/**
	 * Returns whether the feature is treated as an input or not
	 * @return
	 */
	public boolean isInput() {
		return isInput;
	}

	/**
	 * Sets whether the feature is treated as an input or not
	 * @param isInput
	 */
	public void setInput(boolean isInput) {
		this.isInput = isInput;
	}

	/**
	 * Returns the data-type of the feature
	 * @return
	 */
	public FeatureType getType() {
		return type;
	}

	/**
	 * Sets the data-type of the feature
	 * @param type
	 */
	public void setType(FeatureType type) {
		this.type = type;
	}

	/**
	 * Returns the impute method of the feature
	 * @return
	 */
	public ImputeOption getImputeOperation() {
		return imputeOperation;
	}

	/**
	 * Sets the impute method of the feature
	 * @param imputeOperation
	 */
	public void setImputeOperation(ImputeOption imputeOperation) {
		this.imputeOperation = imputeOperation;
	}
}
