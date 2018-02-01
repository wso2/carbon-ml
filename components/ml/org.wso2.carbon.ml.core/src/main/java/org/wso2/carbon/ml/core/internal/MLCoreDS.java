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
import org.apache.spark.api.java.JavaSparkContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.spark.core.interfaces.SparkContextService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
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

import java.util.HashMap;
import java.util.Properties;

/**
 * @scr.component name="ml.core" immediate="true"
 * @scr.reference name="databaseService" interface="org.wso2.carbon.ml.database.DatabaseService" cardinality="1..1"
 * policy="dynamic" bind="setDatabaseService" unbind="unsetDatabaseService"
 * @scr.reference name="configurationcontext.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="outputEventAdapterService"
 * interface="org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService" cardinality="1..1"
 * policy="dynamic" bind="setOutputEventAdapterService" unbind="unsetOutputEventAdapterService"
 * @scr.reference name="analytics.core" interface="org.wso2.carbon.analytics.spark.core.interfaces.SparkContextService"
 * cardinality="1..1" policy="dynamic" bind="setSparkContextService" unbind="unsetSparkContextService"
 */
public class MLCoreDS {

    private static final Log log = LogFactory.getLog(MLCoreDS.class);
    private OutputEventAdapterService emailAdapterService;
    private SparkContextService sparkContextService;

    protected void activate(ComponentContext context) {

        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        if (System.getProperty(MLConstants.DISABLE_ML) != null) {
            valueHolder.setMlDisabled(Boolean.parseBoolean(System.getProperty(MLConstants.DISABLE_ML)));
        }

        if (valueHolder.isMlDisabled()) {
            log.info("Machine learner functionality has been disabled.");
            return;
        }

        try {
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
            if (sparkContext == null) {
                String msg = "SparkContext is not available for ML. ML component initialization aborted!";
                log.warn(msg);
                return;
//                throw new RuntimeException(msg);
            }

            sparkContext.hadoopConfiguration().set("fs.hdfs.impl",
                    org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            sparkContext.hadoopConfiguration()
                    .set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
            valueHolder.setSparkContext(sparkContext);

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
            outputAdapterConfig.setType(MLConstants.ADAPTER_TYPE_EMAIL);
            this.emailAdapterService.create(outputAdapterConfig);
            // Hostname
            String hostName = "localhost";
            try {
                hostName = NetworkUtils.getMgtHostName();
            } catch (Exception ignored) {
            }

            // HTTPS port
            String mgtConsoleTransport = CarbonUtils.getManagementTransport();
            ConfigurationContextService configContextService = valueHolder.getConfigurationContextService();
            int httpsPort = CarbonUtils.getTransportPort(configContextService, mgtConsoleTransport);
            int httpsProxyPort = CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                                                                   mgtConsoleTransport);

            // set the ml.ui.url property which will be used to navigate from Mgt Console.
            String mlUiUrl = "https://" + hostName + ":" + (httpsProxyPort != -1 ? httpsProxyPort : httpsPort) +
                             MLConstants.ML_UI_CONTEXT;
            System.setProperty(MLConstants.ML_UI_URL, mlUiUrl);
            log.info("Machine Learner Wizard URL : " + mlUiUrl);

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
