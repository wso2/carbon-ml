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

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.MLDataset;
import org.wso2.carbon.ml.commons.domain.config.MLConfiguration;
import org.wso2.carbon.ml.core.impl.MLConfigurationParser;
import org.wso2.carbon.ml.core.impl.MLDatasetProcessor;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.database.DatabaseService;

/**
 * @scr.component name="ml.core" immediate="true"
 * @scr.reference name="databaseService"
 * interface="org.wso2.carbon.ml.database.DatabaseService" cardinality="1..1"
 * policy="dynamic" bind="setDatabaseService" unbind="unsetDatabaseService"
 */
public class MLCoreDS {

    private static final Log log = LogFactory.getLog(MLCoreDS.class);

    protected void activate(ComponentContext context) {
        
        try {
            //TODO: Keep. Following is the one-time parsing of ml configurations.
            MLConfigurationParser mlConfigParser = new MLConfigurationParser();
            MLConfiguration mlConfig = mlConfigParser.getMLConfiguration(MLConstants.MACHINE_LEARNER_XML);
            MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
            
            valueHolder.setSummaryStatSettings(mlConfig.getSummaryStatisticsSettings());
            valueHolder.setMlProperties(MLUtils.getProperties(mlConfig.getProperties()));
            valueHolder.setBamServerUrl(mlConfig.getBamServerURL());
            valueHolder.setDataUploadSettings(mlConfig.getDataUploadSettings());
            valueHolder.setAlgorithms(mlConfig.getMlAlgorithms());
            
            SparkConf sparkConf = mlConfigParser.getSparkConf(MLConstants.SPARK_CONFIG_XML);
            valueHolder.setSparkConf(sparkConf);
            
            //FIXME this is temporarily added for testing purposes.
//            MLDatasetProcessor processor = new MLDatasetProcessor();
//            MLDataset dataset = new MLDataset();
//            dataset.setName("test-ml");
//            dataset.setSourcePath(new URI("file:///Volumes/wso2/ml/datasets/fcSample.csv"));
//            dataset.setComments("test-ml");
//            dataset.setDataSourceType("file");
//            dataset.setDataTargetType("file");
//            dataset.setDataType("csv");
//            dataset.setTenantId(-1234);
//            dataset.setUserName("admin");
//            dataset.setVersion("1.0");
//            processor.process(dataset);
        } catch (Throwable e) {
            log.error("Could not create ModelService: " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext context) {
    }
    
    protected void setDatabaseService(DatabaseService databaseService){
        MLCoreServiceValueHolder.getInstance().registerDatabaseService(databaseService);
    }

    protected void unsetDatabaseService(DatabaseService databaseService){
        MLCoreServiceValueHolder.getInstance().registerDatabaseService(databaseService);
    }
    
}
