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

package org.wso2.carbon.ml.ui.helper;

/**
 * This class contains a set of constants used by the UI components
 */
public final class UIConstants {
	
	// data types related constants
	public static final String CATEGORICAL = "CATEGORICAL";
	public static final String NUMERICAL = "NUMERICAL";
	
	// impute options related constants
	public static final String DISCARD = "DISCARD";
	public static final String REPLACE_WTH_MEAN = "REPLACE_WTH_MEAN";
	public static final String REGRESSION_IMPUTATION = "REGRESSION_IMPUTATION";
	
	// data sample related
	public static final int DATA_SAMPLE_SIZE_FOR_SUMMARY_STATS = 10000;
	
	// session related data
	public static final int MAX_SESSION_LIFE_TIME = 60*120;

}
