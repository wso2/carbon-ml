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

package org.wso2.carbon.ml.core.impl;

import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.commons.domain.DataUploadSettings;
import org.wso2.carbon.ml.commons.domain.SummaryStatisticsSettings;
import org.wso2.carbon.ml.core.exceptions.MLConfigurationParserException;
import org.wso2.carbon.ml.core.utils.MLConstants;

public class MLConfigurationParserTest {

    @Test
    public void testMLConfigParser() throws MLConfigurationParserException {
        MLConfigurationParser config = new MLConfigurationParser("src/test/resources/ml-config.xml");
        SummaryStatisticsSettings settings = config.getSummaryStatisticsSettings();
        Assert.assertEquals(settings.getSampleSize(), 10000);
        Assert.assertEquals(settings.getCategoricalThreshold(), 20);
        Assert.assertEquals(settings.getHistogramBins(), 20);

        DataUploadSettings dataUploadSettings = config.getDataUploadSettings();
        Assert.assertEquals(dataUploadSettings.getInMemoryThreshold(), 1024);
        Assert.assertEquals(dataUploadSettings.getUploadLimit(), 20971520);
        Assert.assertEquals(dataUploadSettings.getUploadLocation(), "USER_HOME");

        Assert.assertEquals(config.getDatabaseName(), "jdbc/WSO2ML_DB");

        Properties properties = config.getProperties();
        Assert.assertEquals(properties.size(), 2);
        Assert.assertEquals(properties.containsKey(MLConstants.ML_THREAD_POOL_SIZE), true);
        Assert.assertEquals(properties.get(MLConstants.TARGET_HOME_PROP), "/tmp");

    }

    @Test
    public void testMLConfigParserOnError() throws MLConfigurationParserException {
        try {

            @SuppressWarnings("unused")
            MLConfigurationParser config = new MLConfigurationParser("src/test/resources/ml-config-temp.xml");
        } catch (Exception e) {
            Assert.assertEquals(true, e instanceof MLConfigurationParserException);
        }
    }
}
