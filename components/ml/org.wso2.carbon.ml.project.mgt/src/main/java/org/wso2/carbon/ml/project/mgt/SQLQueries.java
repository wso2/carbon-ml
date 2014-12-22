/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ml.project.mgt;

public class SQLQueries {

	public static final String CREATE_PROJECT = "INSERT INTO ML_PROJECT(PROJECT_ID, NAME," +
			"DESCRIPTION, CREATED_TIME) VALUES(?,?,?, CURRENT_TIMESTAMP())";

	public static final String DELETE_PROJECT = "DELETE FROM ML_PROJECT WHERE PROJECT_ID=?";

	public static final String ADD_TENANT_TO_PROJECT = "INSERT INTO ML_TENANT_PROJECTS" +
			"(TENANT_ID, PROJECT_ID) VALUES(?,?)";

	public static final String GET_TENANT_PROJECTS = "SELECT PROJECT_ID, NAME, CREATED_TIME FROM " +
	        "ML_PROJECT WHERE PROJECT_ID IN (SELECT PROJECT_ID FROM ML_TENANT_PROJECTS WHERE " +
	        "TENANT_ID=?)";

	public static final String GET_DATASET_ID = "SELECT DATASET_ID FROM ML_DATASET WHERE PROJECT_ID=?";

	public static final String CREATE_NEW_WORKFLOW = "INSERT INTO ML_WORKFLOW (WORKFLOW_ID, " +
	        "PARENT_WORKFLOW_ID, PROJECT_ID, DATASET_ID,NAME) VALUES(?,?,?,?,?)";

	public static final String DELETE_WORKFLOW = "DELETE FROM ML_WORKFLOW WHERE WORKFLOW_ID = ?";

	public static final String GET_PROJECT_WORKFLOWS = "SELECT WORKFLOW_ID,NAME FROM ML_WORKFLOW" +
	        " WHERE PROJECT_ID=?";

	public static final String GET_DEFAULT_FEATURE_SETTINGS = "SELECT FEATURE_NAME, FEATURE_INDEX, " +
	        "TYPE, IMPUTE_METHOD, INCLUDE FROM ML_FEATURE_DEFAULTS WHERE DATASET_ID=?";

	public static final String INSERT_FEATURE_SETTINGS = "INSERT INTO ML_FEATURE_SETTINGS " +
	        "(WORKFLOW_ID, FEATURE_NAME, FEATURE_INDEX, TYPE, IMPUTE_METHOD, INCLUDE) " +
	        "VALUES(?,?,?,?,?,?)";

	//private Constructor to prevent class from instantiating.
	private SQLQueries() {
	}
}
