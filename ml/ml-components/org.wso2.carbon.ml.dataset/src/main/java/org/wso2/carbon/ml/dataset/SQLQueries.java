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
package org.wso2.carbon.ml.dataset;

public class SQLQueries {

	public static final String GET_DATASET_CONFIG =
			"SELECT DATASET_UPLOADING_DIR, DATASET_IN_MEM_THRESHOLD, "
					+ "DATASET_UPLOADING_LIMIT FROM ML_CONFIGURATION";

	public static final String GET_FEATURES = "SELECT * FROM ML_FEATURE WHERE "
			+ "DATASET= ? ORDER BY NAME LIMIT ? OFFSET ? ";

	public static final String GET_FEATURE_NAMES =
			"SELECT NAME FROM ML_FEATURE WHERE DATASET=? AND TYPE=?";

	public static final String GET_SUMMARY_STATS =
			"SELECT SUMMARY FROM ML_FEATURE WHERE NAME=? AND DATASET=?";

	public static final String GET_DATASET_ID = "SELECT ID FROM ML_DATASET ORDER BY "
			+ " CAST(ID AS INTEGER)";

	public static final String INSERT_DATASET =
			"INSERT INTO ML_Dataset(ID,DESCRIPTION,URI) VALUES(?,?,?)";

	public static final String UPDATE_IS_INCLUDED = "UPDATE  ML_FEATURE SET IMPORTANT = ? "
			+ "WHERE NAME=? AND DATASET=?";

	public static final String GET_SEPARATOR = "SELECT SEPARATOR FROM ML_CONFIGURATION";

	public static final String GET_DATASET_LOCATION = "SELECT URI FROM ML_DATASET WHERE ID=?";

	public static final String UPDATE_FEATURE =
			"UPDATE  ML_FEATURE SET TYPE =?, IMPUTE_METHOD=?, IMPORTANT=? WHERE NAME=? AND DATASET=?";

	public static final String UPDATE_DATA_TYPE =
			"UPDATE ML_FEATURE SET TYPE =? WHERE NAME=? AND DATASET=?";

	public static final String UPDATE_IMPUTE_METHOD =
			"UPDATE  ML_FEATURE SET IMPUTE_METHOD=? WHERE NAME=? AND DATASET=?";

	public static final String UPDATE_SUMMARY_STATS =
			"MERGE INTO ML_FEATURE(NAME,DATASET,TYPE,SUMMARY,IMPUTE_METHOD,IMPORTANT) VALUES(?,?,?,?,?,TRUE)";
}
