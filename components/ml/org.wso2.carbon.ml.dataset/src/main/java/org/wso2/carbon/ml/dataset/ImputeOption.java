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

public class ImputeOption {
	private final String option;
	public static final ImputeOption DISCARD = new ImputeOption("DISCARD");
	public static final ImputeOption REPLACE_WTH_MEAN = new ImputeOption(
			"REPLACE_WTH_MEAN");
	public static final ImputeOption REGRESSION_IMPUTATION = new ImputeOption(
			"REGRESSION_IMPUTATION");

	private ImputeOption(String option) {
		this.option = option;
	}

	/**
	 * Returns the impute option as a string
	 */
	public String toString() {
		return option;
	}
}
