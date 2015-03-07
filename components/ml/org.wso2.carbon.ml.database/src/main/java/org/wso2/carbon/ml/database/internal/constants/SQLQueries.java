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

    public static final String INSERT_DATASET_VERSION = "INSERT INTO ML_DATASET_VERSION(DATASET_ID, TENANT_ID, USERNAME, VERSION) " +
            "VALUES(?,?,?,?)";

    public static final String INSERT_FEATURE_DEFAULTS = "INSERT INTO ML_FEATURE_DEFAULTS(DATASET_VERSION_ID, FEATURE_NAME, TYPE, FEATURE_INDEX, SUMMARY) " +
            "VALUES(?,?,?,?,?)";

    public static final String INSERT_VALUE_SET = "INSERT INTO ML_VALUE_SET(DATASET_VERSION_ID, NAME, TENANT_ID, USERNAME, URI, SAMPLE_POINTS) " +
            "VALUES(?,?,?,?,?,?)";

    public static final String INSERT_DATA_SOURCE = "INSERT INTO ML_DATA_SOURCE(VALUE_SET_ID, TENANT_ID, USERNAME, KEY, VALUE) VALUES (?,?,?,?,?)";

    public static final String UPDATE_SUMMARY_STATS = "MERGE INTO ML_FEATURE_DEFAULTS(DATASET_VERSION_ID, FEATURE_NAME, TYPE, FEATURE_INDEX, SUMMARY) " +
            "VALUES(?,?,?,?,?)";

    public static final String GET_DATASET_ID = "SELECT DATASET_ID FROM ML_DATASET WHERE NAME = ? AND TENANT_ID = ? ";
    
    public static final String GET_VALUESET_ID = "SELECT VALUE_SET_ID FROM ML_VALUE_SET WHERE NAME = ? AND TENANT_ID = ? ";

    public static final String GET_DATASET_VERSION_ID = "SELECT DATASET_VERSION_ID FROM ML_DATASET_VERSION WHERE DATASET_ID = ? AND VERSION = ? ";

    public static final String GET_DATASET = "SELECT * FROM ML_DATASET WHERE TENANT_ID = ? AND NAME = ? ";

    public static final String GET_DATASET_VERSION = "SELECT ML_DATASET_VERSION.VERSION FROM ML_DATASET, ML_DATASET_VERSION " +
            "WHERE ML_DATASET.DATASET_ID = ML_DATASET_VERSION.DATASET_ID AND ML_DATASET.NAME = ? AND ML_DATASET.TENANT_ID = ? AND ML_DATASET_VERSION.VERSION = ?";

    public static final String GET_VALUE_SET_LOCATION = "SELECT URI FROM ML_VALUE_SET WHERE VALUE_SET_ID=?";

    public static final String UPDATE_SAMPLE_POINTS = "UPDATE ML_VALUE_SET SET SAMPLE_POINTS=? where VALUE_SET_ID=?";

    public static final String GET_SAMPLE_POINTS = "SELECT SAMPLE_POINTS FROM ML_VALUE_SET WHERE VALUE_SET_ID=?";

    public static final String GET_FEATURE_COUNT = "SELECT COUNT(FEATURE_NAME) FROM ML_FEATURE_DEFAULTS WHERE DATASET_VERSION_ID=?";

    public static final String GET_DEFAULT_FEATURES = "SELECT FEATURE_NAME, SUMMARY, TYPE FROM  ML_FEATURE_DEFAULTS  " +
            "WHERE  DATASET_VERSION_ID = ? LIMIT ? OFFSET ?";

    public static final String GET_SUMMARY_STATS = "SELECT SUMMARY FROM ML_FEATURE_DEFAULTS WHERE FEATURE_NAME=? AND " +
            "DATASET_VERSION_ID=?";

    public static final String GET_DEFAULT_FEATURE_SETTINGS = "SELECT TENANT_ID, FEATURE_NAME, FEATURE_INDEX, TYPE, " +
            "FROM ML_FEATURE_DEFAULTS WHERE DATASET_VERSION_ID=?";

    public static final String GET_FEATURE_TYPE = "SELECT  TYPE FROM ML_FEATURE_CUSTOMIZED  WHERE  MODEL_ID  =  ? AND " +
            "FEATURE_NAME = ?";

    public static final String GET_FEATURE_NAMES = "SELECT FEATURE_NAME FROM ML_FEATURE_CUSTOMIZED WHERE MODEL_ID=? AND " +
            "INCLUSION=TRUE";

    public static final String UPDATE_FEATURE_INCLUSION = "UPDATE  ML_FEATURE_CUSTOMIZED SET INCLUSION = ? WHERE " +
            "FEATURE_NAME=? AND MODEL_ID=?";

    public static final String UPDATE_IMPUTE_METHOD = "UPDATE  ML_FEATURE_CUSTOMIZED SET " +
            "IMPUTE_METHOD = ? WHERE FEATURE_NAME = ? AND MODEL_ID = ?";

    public static final String UPDATE_DATA_TYPE = "UPDATE ML_FEATURE_CUSTOMIZED SET TYPE =? WHERE FEATURE_NAME=? AND " +
            "MODEL_ID=?";

    public static final String INSERT_ANALYSIS = "INSERT INTO ML_ANALYSIS(PROJECT_ID, NAME, TENANT_ID, USERNAME, COMMENTS) VALUES(?,?,?,?,?)";

    public static final String INSERT_MODEL = "INSERT INTO ML_MODEL(ANALYSIS_ID, VALUE_SET_ID, TENANT_ID, USERNAME, CREATED_TIME) " +
            "VALUES(?,?,?,?, CURRENT_TIMESTAMP())";

    public static final String INSERT_MODEL_CONFIGURATION = "INSERT INTO ML_MODEL_CONFIGURATION(MODEL_ID, KEY, VALUE, TYPE) " +
            "VALUES(?,?,?,?)";

    public static final String INSERT_HYPER_PARAMETER = "INSERT INTO ML_HYPER_PARAMETER(MODEL_ID, NAME, TENANT_ID, VALUE, LAST_MODIFIED_USER, LAST_MODIFIED_TIME) " +
            "VALUES(?,?,?,?,?, CURRENT_TIMESTAMP())";

    public static final String INSERT_FEATURE_CUSTOMIZED = "INSERT INTO ML_FEATURE_CUSTOMIZED(MODEL_ID, TENANT_ID, FEATURE_NAME, FEATURE_TYPE, IMPUTE_OPTION, " +
            "INCLUSION, LAST_MODIFIED_USER, LAST_MODIFIED_TIME) VALUES(?,?,?,?,?,?,?, CURRENT_TIMESTAMP())";

    public static final String GET_ANALYSIS_ID = "SELECT ANALYSIS_ID FROM ML_ANALYSIS WHERE NAME = ? AND TENANT_ID = ? AND USERNAME = ? ";
    
    public static final String GET_ML_MODEL_ID = "SELECT MODEL_ID FROM ML_MODEL WHERE NAME = ? AND TENANT_ID = ? AND USERNAME = ? ";

    public static final String DELETE_ANALYSIS = "DELETE FROM ML_ANALYSIS WHERE NAME = ? AND TENANT_ID = ? AND USERNAME = ? ";

    public static final String GET_FEATURES = "SELECT DEFAULT.FEATURE_NAME, DEFAULT.SUMMARY, WORKFLOW_SETTINGS.TYPE, " +
            "WORKFLOW_SETTINGS.INCLUDE, WORKFLOW_SETTINGS.IMPUTE_METHOD FROM (SELECT FEATURE_NAME, SUMMARY FROM " +
            "ML_FEATURE_DEFAULTS  WHERE DATASET_ID=?) As DEFAULT INNER JOIN (SELECT WORKFLOW.DATASET_ID, " +
            "FEATURE_SETTINGS.TYPE, FEATURE_SETTINGS.FEATURE_NAME,FEATURE_SETTINGS.INCLUDE,FEATURE_SETTINGS.IMPUTE_METHOD" +
            " FROM ML_FEATURE_SETTINGS FEATURE_SETTINGS, ML_WORKFLOW WORKFLOW WHERE FEATURE_SETTINGS.WORKFLOW_ID = " +
            "WORKFLOW.WORKFLOW_ID AND WORKFLOW.dataset_id=? AND WORKFLOW.WORKFLOW_ID=?) AS WORKFLOW_SETTINGS ON " +
            "WORKFLOW_SETTINGS.FEATURE_NAME = DEFAULT.FEATURE_NAME ORDER BY DEFAULT.FEATURE_NAME LIMIT ? OFFSET ?";

    public static final String INSERT_PROJECT = "INSERT INTO ML_PROJECT(NAME, DESCRIPTION, TENANT_ID," +
            " USERNAME, CREATED_TIME) VALUES(?,?,?,?, CURRENT_TIMESTAMP())";

    public static final String GET_PROJECT_ID = "SELECT PROJECT_ID FROM ML_PROJECT WHERE NAME = ? AND TENANT_ID = ? AND USERNAME = ?";

    public static final String DELETE_PROJECT = "DELETE FROM ML_PROJECT WHERE NAME = ? AND TENANT_ID = ? AND USERNAME = ? ";

    // TODO to be removed
    public static final String DELETE_PROJECT_GIVEN_ID = "DELETE FROM ML_PROJECT WHERE PROJECT_ID=?";

    public static final String GET_PROJECT = "SELECT PROJECT_NAME ,DESCRIPTION, TENANT_ID " +
            "USERNAME, CREATED_TIME FROM ML_PROJECT WHERE PROJECT_ID=?";

    public static final String ADD_TENANT_TO_PROJECT = "INSERT INTO ML_TENANT_PROJECTS (TENANT_ID, PROJECT_ID) " +
            "VALUES(?,?)";

    public static final String GET_TENANT_PROJECTS = "SELECT PROJECT_ID, NAME, CREATED_TIME, DESCRIPTION FROM  ML_PROJECT WHERE " +
            "TENANT_ID=?";

    // TODO
    public static final String GET_MODEL_ID = " SELECT MODEL_ID FROM ML_MODEL WHERE WORKFLOW_ID = ?";

    // TODO
    public static final String GET_MODEL_SUMMARY = "SELECT MODEL_SUMMARY FROM ML_MODEL WHERE MODEL_ID=?";

    // TODO
    public static final String GET_MODEL = "SELECT MODEL FROM ML_MODEL WHERE MODEL_ID=?";

    // TODO
    public static final String GET_MODEL_EXE_END_TIME = " SELECT MODEL_EXECUTION_END_TIME FROM ML_MODEL WHERE " +
            "MODEL_ID = ?";

    // TODO
    public static final String GET_MODEL_EXE_START_TIME = " SELECT MODEL_EXECUTION_START_TIME FROM ML_MODEL WHERE " +
            "MODEL_ID = ?";

    /*
     * private Constructor to prevent any other class from instantiating.
     */
    private SQLQueries() {
    }
}
