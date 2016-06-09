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

import java.util.HashMap;
import java.util.Properties;

import org.wso2.carbon.analytics.spark.core.interfaces.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants;
import org.wso2.carbon.metrics.manager.Gauge;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.config.MLConfiguration;
import org.wso2.carbon.ml.core.impl.H2OConfigurationParser;
import org.wso2.carbon.ml.core.impl.H2OServer;
import org.wso2.carbon.ml.core.utils.BlockingExecutor;
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
 * @scr.reference name="analytics.core" interface="org.wso2.carbon.analytics.spark.core.interfaces.SparkContextService"
 *                cardinality="1..1" policy="dynamic" bind="setSparkContextService" unbind="unsetSparkContextService"
 */
public class MLCoreDS {

    private static final Log log = LogFactory.getLog(MLCoreDS.class);
    private OutputEventAdapterService emailAdapterService;
    private SparkContextService sparkContextService;

    protected void activate(ComponentContext context) {

        try {
            MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
            MLConfiguration mlConfig = valueHolder.getDatabaseService().getMlConfiguration();

            valueHolder.setSummaryStatSettings(mlConfig.getSummaryStatisticsSettings());
            valueHolder.setMlProperties(MLUtils.getProperties(mlConfig.getProperties()));
            valueHolder.setHdfsUrl(mlConfig.getHdfsUrl());
            valueHolder.setAlgorithms(mlConfig.getMlAlgorithms());
            valueHolder.setEmailNotificationEndpoint(mlConfig.getEmailNotificationEndpoint());
            valueHolder.setModelRegistryLocation(mlConfig.getModelRegistryLocation());
            valueHolder.setModelStorage(mlConfig.getModelStorage());
            valueHolder.setDatasetStorage(mlConfig.getDatasetStorage());

            Properties mlProperties = valueHolder.getMlProperties();
            String poolSizeStr = mlProperties
                    .getProperty(org.wso2.carbon.ml.core.utils.MLConstants.ML_THREAD_POOL_SIZE);
            String poolQueueSizeStr = mlProperties
                    .getProperty(org.wso2.carbon.ml.core.utils.MLConstants.ML_THREAD_POOL_QUEUE_SIZE);
            int poolSize = 50;
            int poolQueueSize = 1000;
            if (poolSizeStr != null) {
                try {
                    poolSize = Integer.parseInt(poolSizeStr);
                } catch (Exception ignore) {
                    // use the default
                }
            }

            if (poolQueueSizeStr != null) {
                try {
                    poolQueueSize = Integer.parseInt(poolQueueSizeStr);
                } catch (Exception ignore) {
                    // use the default
                }
            }
            valueHolder.setThreadExecutor(new BlockingExecutor(poolSize, poolQueueSize));
            
            JavaSparkContext sparkContext = sparkContextService.getJavaSparkContext();
            sparkContext.hadoopConfiguration().set("fs.hdfs.impl",
                    org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            sparkContext.hadoopConfiguration()
                    .set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
            valueHolder.setSparkContext(sparkContext);

//            // Checks whether ML spark context disabling JVM option is set
//            if (System.getProperty(MLConstants.DISABLE_ML_SPARK_CONTEXT_JVM_OPT) != null) {
//                if (Boolean.parseBoolean(System.getProperty(MLConstants.DISABLE_ML_SPARK_CONTEXT_JVM_OPT))) {
//                    valueHolder.setSparkContextEnabled(false);
//                    log.info("ML Spark context will not be initialized.");
//                }
//            }
//
//            if (valueHolder.isSparkContextEnabled()) {
//                SparkConf sparkConf = mlConfigParser.getSparkConf(MLConstants.SPARK_CONFIG_XML);
//
//                // Add extra class paths for DAS Spark cluster
//                String sparkClassPath = ComputeClasspath.getSparkClasspath("", CarbonUtils.getCarbonHome());
//                try {
//                    sparkConf.set(MLConstants.SPARK_EXECUTOR_CLASSPATH,
//                            sparkConf.get(MLConstants.SPARK_EXECUTOR_CLASSPATH) + ":" + sparkClassPath);
//                } catch (NoSuchElementException e) {
//                    sparkConf.set(MLConstants.SPARK_EXECUTOR_CLASSPATH, "");
//                }
//
//                try {
//                    sparkConf.set(MLConstants.SPARK_DRIVER_CLASSPATH,
//                            sparkConf.get(MLConstants.SPARK_DRIVER_CLASSPATH) + ":" + sparkClassPath);
//                } catch (NoSuchElementException e) {
//                    sparkConf.set(MLConstants.SPARK_DRIVER_CLASSPATH, "");
//                }
//
//                sparkConf.setAppName("ML-SPARK-APPLICATION-" + Math.random());
//                String portOffset = System.getProperty("portOffset",
//                        ServerConfiguration.getInstance().getFirstProperty("Ports.Offset"));
//                int sparkUIPort = Integer.parseInt(portOffset) + Integer.parseInt(sparkConf.get("spark.ui.port"));
//                sparkConf.set("spark.ui.port", String.valueOf(sparkUIPort));
//                valueHolder.setSparkConf(sparkConf);
//
//                // create a new java spark context
//                JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
//                sparkContext.hadoopConfiguration().set("fs.hdfs.impl",
//                        org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
//                sparkContext.hadoopConfiguration()
//                        .set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
//
//                valueHolder.setSparkContext(sparkContext);
//            }

            // Retrieving H2O configurations
            HashMap<String, String> h2oConf = new H2OConfigurationParser().getH2OConf(MLConstants.H2O_CONFIG_XML);

            // Start H2O server only if it is enabled
            if (h2oConf.get("enabled").equals("true")) {
                if (h2oConf.get("mode").equals("local")) {
                    valueHolder.setH2oClientModeEnabled(false);
                    log.info("H2O Server will start in local mode.");
                } else if (h2oConf.get("mode").equals("client")) {
                    valueHolder.setH2oClientModeEnabled(true);
                    log.info("H2O Server will start in client mode.");
                } else {
                    log.error(String.format("H2O server failed to start. Unsupported H2O mode: %s", h2oConf.get("mode")));
                }

                if (valueHolder.isH2oClientModeEnabled()) {
                    H2OServer.startH2O(h2oConf.get("ip"), h2oConf.get("port"), h2oConf.get("name"));
                } else {
                    String portOffset = System.getProperty("portOffset",
                            ServerConfiguration.getInstance().getFirstProperty("Ports.Offset"));
                    String port = String.valueOf(54321 + Integer.parseInt(portOffset));
                    H2OServer.startH2O(port);
                }
            }

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
            
            // ML metrices
            MetricManager.gauge(Level.INFO, "org.wso2.carbon.ml.thread-pool-active-count", activeCountGauge);
            MetricManager.gauge(Level.INFO, "org.wso2.carbon.ml.thread-pool-queue-size", queueSizeGauge);
            
            log.info("ML core bundle activated successfully.");
        } catch (Throwable e) {
            log.error("Could not create ModelService: " + e.getMessage(), e);
        }
    }
    
    Gauge<Integer> activeCountGauge = new Gauge<Integer>() {
        @Override
        public Integer getValue() {
            // Return a value
            return MLCoreServiceValueHolder.getInstance().getThreadExecutor().getActiveCount();
        }
    };

    Gauge<Integer> queueSizeGauge = new Gauge<Integer>() {
        @Override
        public Integer getValue() {
            // Return a value
            return MLCoreServiceValueHolder.getInstance().getThreadExecutor().getQueue().size();
        }
    };

    protected void deactivate(ComponentContext context) {
        // Destroy the created email output adapter
        if (emailAdapterService != null) {
            emailAdapterService.destroy("TestEmailAdapter");
        }
        if (MLCoreServiceValueHolder.getInstance().getSparkContext() != null) {
            MLCoreServiceValueHolder.getInstance().getSparkContext().close();
        }
//        H2OServer.stopH2O();
    }

    protected void setDatabaseService(DatabaseService databaseService) {
        MLCoreServiceValueHolder.getInstance().registerDatabaseService(databaseService);
    }

    protected void unsetDatabaseService(DatabaseService databaseService) {
        MLCoreServiceValueHolder.getInstance().registerDatabaseService(null);
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

    protected void setSparkContextService(SparkContextService scs) {
        this.sparkContextService = scs;
    }
    
    protected void unsetSparkContextService(SparkContextService scs) {
        this.sparkContextService = null;
    }
}
