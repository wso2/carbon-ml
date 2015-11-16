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
import org.apache.spark.api.java.JavaSparkContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.ml.commons.domain.config.MLAlgorithm;
import org.wso2.carbon.ml.commons.domain.config.Storage;
import org.wso2.carbon.ml.commons.domain.config.SummaryStatisticsSettings;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class MLCoreServiceValueHolder {
    private static volatile MLCoreServiceValueHolder instance;
    private DatabaseService databaseService;
    private SummaryStatisticsSettings summaryStatSettings;
    private Properties mlProperties;
    private String  hdfsUrl;
    private String emailNotificationEndpoint;
    private String modelRegistryLocation;
    private List<MLAlgorithm> algorithms;
    private JavaSparkContext sparkContext;
    private SparkConf sparkConf;
    private ConfigurationContextService configurationContextService;
    private OutputEventAdapterService outputEventAdapterService;
    private Storage modelStorage;
    private Storage datasetStorage;
    private BlockingExecutor threadExecutor;
    private boolean sparkContextEnabled;

    public MLCoreServiceValueHolder() {
        sparkContextEnabled = true;
    }


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

    public List<MLAlgorithm> getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(List<MLAlgorithm> algorithms) {
        this.algorithms = algorithms;
    }

    public SparkConf getSparkConf() {
        return sparkConf;
    }

    public void setSparkConf(SparkConf sparkConf) {
        this.sparkConf = sparkConf;
    }

    public void registerConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }
    
    public ConfigurationContextService getConfigurationContextService() {
        return this.configurationContextService;
    }

    public void registerOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
		this.outputEventAdapterService = outputEventAdapterService;
	}
    
    public OutputEventAdapterService getOutputEventAdapterService() {
		return this.outputEventAdapterService;
	}

    public String getHdfsUrl() {
        return hdfsUrl;
    }

    public void setHdfsUrl(String hdfsUrl) {
        this.hdfsUrl = hdfsUrl;
    }
    
    public String getEmailNotificationEndpoint() {
        return emailNotificationEndpoint;
    }

    public void setEmailNotificationEndpoint(String emailNotificationEndpoint) {
        this.emailNotificationEndpoint = emailNotificationEndpoint;
    }

    public String getModelRegistryLocation() {
        return modelRegistryLocation;
    }

    public void setModelRegistryLocation(String modelRegistryLocation) {
        this.modelRegistryLocation = modelRegistryLocation;
    }
    
    public Storage getModelStorage() {
        return modelStorage;
    }

    public void setModelStorage(Storage modelStorage) {
        this.modelStorage = modelStorage;
    }

    public JavaSparkContext getSparkContext() {
        return sparkContext;
    }

    public void setSparkContext(JavaSparkContext sparkContext) {
        this.sparkContext = sparkContext;
    }

    public Storage getDatasetStorage() {
        return datasetStorage;
    }

    public void setDatasetStorage(Storage datasetStorage) {
        this.datasetStorage = datasetStorage;
    }

    public BlockingExecutor getThreadExecutor() {
        return threadExecutor;
    }

    public void setThreadExecutor(BlockingExecutor threadExecutor) {
        this.threadExecutor = threadExecutor;
    }

    public boolean isSparkContextEnabled() {
        return sparkContextEnabled;
    }

    public void setSparkContextEnabled(boolean sparkContextEnabled) {
        this.sparkContextEnabled = sparkContextEnabled;
    }

}
