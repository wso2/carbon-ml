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

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public String getSummaryStats() {
		return summaryStats;
	}

	public void setSummaryStats(String summaryStats) {
		this.summaryStats = summaryStats;
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

	public ImputeOption getImputeOperation() {
		return imputeOperation;
	}

	public void setImputeOperation(ImputeOption imputeOperation) {
		this.imputeOperation = imputeOperation;
	}
}
