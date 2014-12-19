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

public class SummaryStatisticsSettings {
	private int histogramBins;
	private int categoricalThreshold;
	private int sampleSize;

	/**
	 * Returns the number of bins for the histogram.
	 *
	 * @return Number of bins for the histogram
	 */
	public int getHistogramBins() {
		return histogramBins;
	}

	/**
	 * Sets the number of bins for the histogram.
	 *
	 * @param histogramBins Number of bins for the histogram
	 */
	public void setHistogramBins(int histogramBins) {
		this.histogramBins = histogramBins;
	}

	/**
	 * Returns threshold of unique values for selecting categorical data.
	 *
	 * @return Threshold of unique values for selecting categorical data
	 */
	public int getCategoricalThreshold() {
		return categoricalThreshold;
	}

	/**
	 * Sets threshold of unique values for selecting categorical data.
	 *
	 * @param categoricalThreshold Threshold of unique values for selecting categorical data
	 */
	public void setCategoricalThreshold(int categoricalThreshold) {
		this.categoricalThreshold = categoricalThreshold;
	}

	/**
	 * Returns size of the data sample.
	 *
	 * @return Size of the data sample
	 */
	public int getSampleSize() {
		return sampleSize;
	}

	/**
	 * Sets size of the data sample.
	 *
	 * @param sampleSize Size of the data sample
	 */
	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}
}
