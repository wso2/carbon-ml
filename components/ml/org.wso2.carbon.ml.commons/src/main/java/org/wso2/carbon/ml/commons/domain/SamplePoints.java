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

package org.wso2.carbon.ml.commons.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SamplePoints implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<List<String>> samplePoints = new ArrayList<List<String>>();
	private Map<String, Integer> headerMap;
	// Array containing number of missing values of each feature in the data-set.
    private int[] missing;
    private int[] stringCellCount;

	/**
	 * Returns the raw-data of the sample.
	 *
	 * @return A list of data columns of the sample
	 */
	public List<List<String>> getSamplePoints() {
		return samplePoints;
	}

	/**
	 * Returns the header names.
	 *
	 * @return header names and their positions in the data-set as a Map
	 */
	public Map<String, Integer> getHeader() {
		return headerMap;
	}

	/**
	 * Set the sample points.
	 *
	 * @param samplePoints A list of data columns of the sample
	 */
	public void setSamplePoints(List<List<String>> samplePoints) {
		this.samplePoints = samplePoints;
	}

	/**
	 * Set the header of the sample points.
	 *
	 * @param headerMap header names and their positions in the data-set as a Map
	 */
	public void setHeader(Map<String, Integer> headerMap) {
		this.headerMap = headerMap;
	}

    public int[] getMissing() {
        return missing;
    }

    public void setMissing(int[] missing) {
        if (missing == null) {
            this.missing = new int[0];
        } else {
            this.missing = Arrays.copyOf(missing, missing.length);
        }
    }

    public int[] getStringCellCount() {
        return stringCellCount;
    }

    public void setStringCellCount(int[] stringCellCount) {
        if (stringCellCount == null) {
            this.stringCellCount = new int[0];
        } else {
            this.stringCellCount = Arrays.copyOf(stringCellCount, stringCellCount.length);
        }
    }
}
