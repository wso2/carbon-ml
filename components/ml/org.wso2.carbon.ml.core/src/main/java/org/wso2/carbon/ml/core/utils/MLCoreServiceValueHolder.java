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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.core.utils;

import java.util.List;
import java.util.Properties;

import org.apache.spark.SparkConf;
import org.wso2.carbon.ml.commons.domain.config.DataUploadSettings;
import org.wso2.carbon.ml.commons.domain.config.MLAlgorithm;
import org.wso2.carbon.ml.commons.domain.config.SummaryStatisticsSettings;
import org.wso2.carbon.ml.database.DatabaseService;

public class MLCoreServiceValueHolder {
    private static volatile MLCoreServiceValueHolder instance;
    private DatabaseService databaseService;
    private SummaryStatisticsSettings summaryStatSettings;
    private DataUploadSettings dataUploadSettings;
    private Properties mlProperties;
    private String  bamServerUrl;
    private List<MLAlgorithm> algorithms;
    private SparkConf sparkConf;
    
    public static MLCoreServiceValueHolder getInstance() {
        if (instance == null) {
            synchronized (MLCoreServiceValueHolder.class) {
                if (instance == null) {
                    instance = new MLCoreServiceValueHolder();
                }
            }
        }
        return instance;
    }

    public void registerDatabaseService(DatabaseService service) {
        this.databaseService = service;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public SummaryStatisticsSettings getSummaryStatSettings() {
        return summaryStatSettings;
    }

    public void setSummaryStatSettings(SummaryStatisticsSettings summaryStatSettings) {
        this.summaryStatSettings = summaryStatSettings;
    }

    public Properties getMlProperties() {
        return mlProperties;
    }

    public void setMlProperties(Properties mlProperties) {
        this.mlProperties = mlProperties;
    }

    public String getBamServerUrl() {
        return bamServerUrl;
    }

    public void setBamServerUrl(String bamServerUrl) {
        this.bamServerUrl = bamServerUrl;
    }

    public List<MLAlgorithm> getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(List<MLAlgorithm> algorithms) {
        this.algorithms = algorithms;
    }

    public DataUploadSettings getDataUploadSettings() {
        return dataUploadSettings;
    }

    public void setDataUploadSettings(DataUploadSettings dataUploadSettings) {
        this.dataUploadSettings = dataUploadSettings;
    }

    public SparkConf getSparkConf() {
        return sparkConf;
    }

    public void setSparkConf(SparkConf sparkConf) {
        this.sparkConf = sparkConf;
    }
}
