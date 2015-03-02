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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.database.internal.constants;

/**
 * A utility class to store SQL prepared statements
 */
public class SQLQueries {

    public static final String INSERT_DATASET = "INSERT INTO ML_DATASET(NAME, TENANT_ID, USERNAME, COMMENTS, SOURCE_TYPE, TARGET_TYPE, DATA_TYPE) " +
            "VALUES(?,?,?,?,?,?,?)";

    public static final String INSERT_DATASET_VERSION = "INSERT INTO ML_DATASET_VERSION(DATASET_ID, TEANANT_ID, VERSION) " +
            "VALUES(?,?,?)";

    public static final String INSERT_FEATURE_DEFAULTS = "INSERT INTO ML_FEATURE_DEFAULTS(DATASET_VERSION_ID, NAME, TYPE, INDEX, SUMMARY) " +
            "VALUES(?,?,?,?,?)";

    public static final String INSERT_VALUE_SET = "INSERT INTO ML_VALUE_SET(DATASET_VERSION_ID, TENANT_ID, URI, SAMPLE_POINTS) " +
            "VALUES(?,?,?,?)";

    public static final String UPDATE_SUMMARY_STATS = "MERGE INTO ML_FEATURE_DEFAULTS(DATASET_VERSION_ID, NAME, TYPE, INDEX, SUMMARY) " +
            "VALUES(?,?,?,?,?)";

    public static final String GET_VALUE_SET_LOCATION = "SELECT URI FROM ML_VALUE_SET WHERE VALUE_SET_ID=?";

    public static final String UPDATE_SAMPLE_POINTS = "UPDATE ML_VALUE_SET SET SAMPLE_POINTS=? where VALUE_SET_ID=?";

    public static final String GET_SAMPLE_POINTS = "SELECT SAMPLE_POINTS FROM ML_VALUE_SET WHERE VALUE_SET_ID=?";

    public static final String GET_FEATURE_COUNT = "SELECT COUNT(FEATURE_NAME) FROM ML_FEATURE_DEFAULTS WHERE DATASET_VERSION_ID=?";

    public static final String GET_DEFAULT_FEATURES = "SELECT FEATURE_NAME, SUMMARY, TYPE FROM  ML_FEATURE_DEFAULTS  " +
            "WHERE  DATASET_VERSION_ID = ? LIMIT ? OFFSET ?";

    public static final String GET_SUMMARY_STATS = "SELECT SUMMARY FROM ML_FEATURE_DEFAULTS WHERE FEATURE_NAME=? AND " +
            "DATASET_VERSION_ID=?";

    public static final String GET_DEFAULT_FEATURE_SETTINGS = "SELECT FEATURE_NAME, FEATURE_INDEX, TYPE, " +
            "FROM ML_FEATURE_DEFAULTS WHERE DATASET_VERSION_ID=?";
    /*
     * private Constructor to prevent any other class from instantiating.
     */
    private SQLQueries() {
    }
}
