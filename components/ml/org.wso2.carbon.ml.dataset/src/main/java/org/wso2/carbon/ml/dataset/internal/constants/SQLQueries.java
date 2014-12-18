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
package org.wso2.carbon.ml.dataset.internal.constants;

public class SQLQueries {

	public static final String GET_FEATURE_NAMES =
			"SELECT FEATURE_NAME FROM ML_FEATURE_SETTINGS WHERE WORKFLOW_ID=? AND "
					+ "TYPE=? AND INCLUDE=TRUE";

	public static final String GET_FEATURES =
			"SELECT DEFAULT.FEATURE_NAME, DEFAULT.SUMMARY, WORKFLOW_SETTINGS.TYPE, WORKFLOW_SETTINGS.INCLUDE,WORKFLOW_SETTINGS.IMPUTE_METHOD FROM (SELECT FEATURE_NAME, SUMMARY FROM ML_FEATURE_DEFAULTS  WHERE DATASET_ID=?) As DEFAULT INNER JOIN (SELECT WORKFLOW.DATASET_ID,FEATURE_SETTINGS.TYPE,FEATURE_SETTINGS.FEATURE_NAME,FEATURE_SETTINGS.INCLUDE,FEATURE_SETTINGS.IMPUTE_METHOD FROM ML_FEATURE_SETTINGS FEATURE_SETTINGS, ML_WORKFLOW WORKFLOW WHERE FEATURE_SETTINGS.WORKFLOW_ID=WORKFLOW.WORKFLOW_ID AND WORKFLOW.dataset_id=? AND WORKFLOW.WORKFLOW_ID=?) AS WORKFLOW_SETTINGS ON WORKFLOW_SETTINGS.FEATURE_NAME=DEFAULT.FEATURE_NAME ORDER BY DEFAULT.FEATURE_NAME LIMIT ? OFFSET ?";

	public static final String GET_SUMMARY_STATS =
			"SELECT SUMMARY FROM ML_FEATURE_DEFAULTS WHERE FEATURE_NAME=? AND DATASET_ID=?";

	public static final String INSERT_DATASET =
			"INSERT INTO ML_DATASET(DATASET_ID,DATASET_URL,PROJECT_ID) VALUES(?,?,?)";

	public static final String UPDATE_IS_INCLUDED = "UPDATE  ML_FEATURE_SETTINGS SET INCLUDE = ? "
			+ "WHERE FEATURE_NAME=? AND WORKFLOW_ID=?";

	public static final String GET_SEPARATOR = "SELECT SEPARATOR FROM ML_CONFIGURATION";

	public static final String GET_DATASET_LOCATION = "SELECT DATASET_URL FROM ML_DATASET WHERE "
			+ "DATASET_ID=?";

	public static final String UPDATE_DATA_TYPE =
			"UPDATE ML_FEATURE_SETTINGS SET TYPE =? WHERE FEATURE_NAME=? AND WORKFLOW_ID=?";

	public static final String UPDATE_IMPUTE_METHOD =
			"UPDATE  ML_FEATURE_SETTINGS SET IMPUTE_METHOD=? WHERE FEATURE_NAME=? AND WORKFLOW_ID=?";

	public static final String UPDATE_SUMMARY_STATS =
			"MERGE INTO ML_FEATURE_DEFAULTS(FEATURE_NAME,DATASET_ID,SUMMARY, TYPE, "
					+ "IMPUTE_METHOD, INCLUDE) VALUES(?,?,?,?,?,?)";

	public static final String UPDATE_SAMPLE_POINTS = "UPDATE ML_DATASET SET SAMPLE_POINTS=? "
			+ "where DATASET_ID=?";

	public static final String GET_SAMPLE_POINTS = "SELECT SAMPLE_POINTS FROM ML_DATASET WHERE "
			+ "DATASET_ID=?";

	public static final String GET_FEATURE_COUNT = "SELECT COUNT(FEATURE_NAME) FROM "
			+ "ML_FEATURE_DEFAULTS" + " WHERE DATASET_ID=?";

	public static final String INSERT_FEATURE_SETTINGS = "INSERT INTO ML_FEATURE_SETTINGS"
			+ "(WORKFLOW_ID,"
			+ "FEATURE_NAME,TYPE,IMPUTE_METHOD,"
			+ "INCLUDE) VALUES(?,?,?,?,?)";

	public static final String GET_DEFAULT_FEATURE_SETTINGS =
			"SELECT FEATURE_NAME,TYPE,IMPUTE_METHOD,INCLUDE FROM ML_FEATURE_DEFAULTS WHERE "
					+ "DATASET_ID=?" + " ";

	public static final String GET_MODEL_ID = " SELECT MODEL_ID FROM ML_MODEL WHERE WORKFLOW_ID = ?";

	/*
	 * private Constructor to prevent any other class from instantiating.
	 */
	private SQLQueries() {
	}
}
