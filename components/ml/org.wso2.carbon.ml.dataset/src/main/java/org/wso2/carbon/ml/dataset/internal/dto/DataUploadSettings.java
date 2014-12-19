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
package org.wso2.carbon.ml.dataset.internal.dto;

public class DataUploadSettings {
	private String uploadLocation;
	private int inMemoryThreshold;
	private long uploadLimit;

	/**
	 * Returns Data upload directory.
	 *
	 * @return absolute path of the data uploading directory
	 */
	public String getUploadLocation() {
		return uploadLocation;
	}

	/**
	 * Sets the data upload directory.
	 *
	 * @param uploadLocation Absolute path of the data uploading directory
	 */
	public void setUploadLocation(String uploadLocation) {
		this.uploadLocation = uploadLocation;
	}

	/**
	 * returns In-Memory-Threshold.
	 *
	 * @return Memory threshold in bytes
	 */
	public int getInMemoryThreshold() {
		return inMemoryThreshold;
	}

	/**
	 * Sets In-Memory-Threshold.
	 *
	 * @param inMemoryThreshold Memory threshold in bytes
	 */
	public void setInMemoryThreshold(int inMemoryThreshold) {
		this.inMemoryThreshold = inMemoryThreshold;
	}

	/**
	 * Returns maximum allowed size of a data set.
	 *
	 * @return Maximum allowed size of a data set in bytes
	 */
	public long getUploadLimit() {
		return uploadLimit;
	}

	/**
	 * Sets maximum allowed size of a data set.
	 *
	 * @param uploadLimit Maximum allowed size of a data set in bytes
	 */
	public void setUploadLimit(long uploadLimit) {
		this.uploadLimit = uploadLimit;
	}
}
