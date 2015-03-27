/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ml.integration.common.utils;

public class MLIntegrationTestConstants {
	public static final String ML_SERVER_NAME = "ML";
	public static final String ML_UI = "/ml/";
	public static final String ML_UI_ELEMENT_MAPPER = "/mlUiMapper.properties";
	public static final String CARBON_UI_ELEMENT_MAPPER = "/carbonUiMapper.properties";

	public static final String CARBON_CLIENT_TRUSTSTORE = "/keystores/products/client-truststore.jks";
	public static final String CARBON_CLIENT_TRUSTSTORE_PASSWORD = "wso2carbon";
	public static final String JKS = "JKS";
	public static final String TLS = "TLS";

	public static final String HTTPS = "https";
    
    // Constants related to REST calls
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String BASIC = "Basic ";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String APPLICATION_JSON = "application/json";

	// Http response codes
	public static final int HTTP_OK = 200;
	public static final int HTTP_BAD_REQUEST = 400;
	public static final int HTTP_UNAUTHORIZED = 401;
	public static final int HTTP_NOT_FOUND = 404;
	public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
	public static final int HTTP_FOUND = 302;
	
	// Constants for Test cases - Happy scenario
	public static final String FOREST_COVER_DATASET_SAMPLE = "data/fcSample.csv";
	public static final String DIABETES_DATASET_SAMPLE = "data/pIndiansDiabetes.csv";
	public static final String DATASET_NAME = "Forest_Cover";
	public static final int DATASET_ID = 1;
	public static final int VERSIONSET_ID = 1;
	public static final String PROJECT_NAME = "Forest_Cover_Project";
	public static final int PROJECT_ID = 1;
	public static final String ANALYSIS_NAME = "Forest_Cover_Analysis";
	public static final int ANALYSIS_ID = 1;
	public static final String MODEL_NAME = "Forest_Cover_Model";
    public static final int MODEL_ID = 1;
    
    public static final String FILE_STORAGE_LOCATION = "Models/file-storage";
}
