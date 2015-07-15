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
package org.wso2.carbon.ml.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.config.MLConfiguration;
import org.wso2.carbon.ml.core.impl.MLConfigurationParser;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

/**
 * @scr.component name="ml.core" immediate="true"
 * @scr.reference name="databaseService" interface="org.wso2.carbon.ml.database.DatabaseService" cardinality="1..1"
 *                policy="dynamic" bind="setDatabaseService" unbind="unsetDatabaseService"
 * @scr.reference name="configurationcontext.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic" bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="outputEventAdapterService"
 *                interface="org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService" cardinality="1..1"
 *                policy="dynamic" bind="setOutputEventAdapterService" unbind="unsetOutputEventAdapterService"
 */
public class MLCoreDS {

    private static final Log log = LogFactory.getLog(MLCoreDS.class);
    private OutputEventAdapterService emailAdapterService;

    protected void activate(ComponentContext context) {

        try {
            MLConfigurationParser mlConfigParser = new MLConfigurationParser();
            MLConfiguration mlConfig = mlConfigParser.getMLConfiguration(MLConstants.MACHINE_LEARNER_XML);
            MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();

            valueHolder.setSummaryStatSettings(mlConfig.getSummaryStatisticsSettings());
            valueHolder.setMlProperties(MLUtils.getProperties(mlConfig.getProperties()));
            valueHolder.setHdfsUrl(mlConfig.getHdfsUrl());
            valueHolder.setAlgorithms(mlConfig.getMlAlgorithms());
            valueHolder.setEmailNotificationEndpoint(mlConfig.getEmailNotificationEndpoint());
            valueHolder.setModelRegistryLocation(mlConfig.getModelRegistryLocation());
            valueHolder.setModelStorage(mlConfig.getModelStorage());

            SparkConf sparkConf = mlConfigParser.getSparkConf(MLConstants.SPARK_CONFIG_XML);
            sparkConf.setAppName("ML-SPARK-APPLICATION-" + Math.random());
            valueHolder.setSparkConf(sparkConf);
            
            // create a new java spark context
            JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
            sparkContext.hadoopConfiguration().set("fs.hdfs.impl",
                    org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            sparkContext.hadoopConfiguration()
                    .set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

            valueHolder.setSparkContext(sparkContext);

            // Creating an email output adapter
            this.emailAdapterService = valueHolder.getOutputEventAdapterService();
            OutputEventAdapterConfiguration outputAdapterConfig = new OutputEventAdapterConfiguration();
            outputAdapterConfig.setName(MLConstants.ML_EMAIL_ADAPTER);
            outputAdapterConfig.setType(EmailEventAdapterConstants.ADAPTER_TYPE_EMAIL);
            this.emailAdapterService.create(outputAdapterConfig);
            // Hostname
            String hostName = "localhost";
            try {
                hostName = NetworkUtils.getMgtHostName();
            } catch (Exception ignored) {
            }

            // HTTPS port
            String mgtConsoleTransport = CarbonUtils.getManagementTransport();
            ConfigurationContextService configContextService = MLCoreServiceValueHolder.getInstance()
                    .getConfigurationContextService();
            int httpsPort = CarbonUtils.getTransportPort(configContextService, mgtConsoleTransport);
            int httpsProxyPort = CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                    mgtConsoleTransport);
            // set the ml.url property which will be used to print in the console by the ML jaggery app.
            configContextService.getServerConfigContext().setProperty("ml.url",
                    "https://" + hostName + ":" + (httpsProxyPort != -1 ? httpsProxyPort : httpsPort) + "/ml");
            log.info("ML core bundle activated successfully.");
        } catch (Throwable e) {
            log.error("Could not create ModelService: " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext context) {
        // Destroy the created email output adapter
        if (emailAdapterService != null) {
            emailAdapterService.destroy("TestEmailAdapter");
        }
        if (MLCoreServiceValueHolder.getInstance().getSparkContext() != null) {
            MLCoreServiceValueHolder.getInstance().getSparkContext().close();
        }
        
    }

    protected void setDatabaseService(DatabaseService databaseService) {
        MLCoreServiceValueHolder.getInstance().registerDatabaseService(databaseService);
    }

    protected void unsetDatabaseService(DatabaseService databaseService) {
        MLCoreServiceValueHolder.getInstance().registerDatabaseService(databaseService);
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        MLCoreServiceValueHolder.getInstance().registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        MLCoreServiceValueHolder.getInstance().registerConfigurationContextService(null);
    }

    protected void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        MLCoreServiceValueHolder.getInstance().registerOutputEventAdapterService(outputEventAdapterService);
    }

    protected void unsetOutputEventAdapterService(OutputEventAdapterService configurationContextService) {
        MLCoreServiceValueHolder.getInstance().registerConfigurationContextService(null);
    }

}
