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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.dataset.exceptions.MLConfigurationParserException;
import org.wso2.carbon.ml.dataset.internal.dto.DataUploadSettings;
import org.wso2.carbon.ml.dataset.internal.dto.SummaryStatisticsSettings;

public class MLConfigurationParserTest {

    @Test
    public void testMLConfigParser() throws MLConfigurationParserException {
        MLConfigurationParser config = new MLConfigurationParser("src/test/resources/ml-config.xml");
        SummaryStatisticsSettings settings = config.getSummaryStatisticsSettings();
        Assert.assertEquals(10000, settings.getSampleSize());
        Assert.assertEquals(20, settings.getCategoricalThreshold());
        Assert.assertEquals(20, settings.getHistogramBins());
        
        DataUploadSettings dataUploadSettings = config.getDataUploadSettings();
        Assert.assertEquals(1024, dataUploadSettings.getInMemoryThreshold());
        Assert.assertEquals(20971520, dataUploadSettings.getUploadLimit());
        Assert.assertEquals("USER_HOME", dataUploadSettings.getUploadLocation());
        
        Assert.assertEquals("jdbc/WSO2ML_DB", config.getDatabaseName());
        
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
