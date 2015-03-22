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
import org.wso2.carbon.ml.commons.domain.config.MLConfiguration;
import org.wso2.carbon.ml.core.exceptions.MLConfigurationParserException;

public class MLConfigurationParserTest {

    @Test
    public void testMLConfigParser() throws MLConfigurationParserException {
        MLConfigurationParser configParser = new MLConfigurationParser();
        MLConfiguration mlConfig = configParser.getMLConfiguration("src/test/resources/machine-learner.xml");
        Assert.assertNotNull(mlConfig);
        Assert.assertEquals(mlConfig.getDatabaseName(), "jdbc/WSO2ML_DB");
        
        Assert.assertNotNull(mlConfig.getMlAlgorithms());
        Assert.assertNotNull(mlConfig.getMlAlgorithms().get(0));
        Assert.assertEquals(mlConfig.getMlAlgorithms().size(), 8);
        Assert.assertEquals(mlConfig.getMlAlgorithms().get(0).getName(), "LINEAR_REGRESSION");
        
        Assert.assertNotNull(mlConfig.getProperties());
        Assert.assertEquals(mlConfig.getProperties().size(), 4);
        Assert.assertEquals(mlConfig.getProperties().get(0).getName(), "ml.thread.pool.size");

    }

    @Test
    public void testMLConfigParserOnError() throws MLConfigurationParserException {
        try {

            MLConfigurationParser parser = new MLConfigurationParser();
            @SuppressWarnings("unused")
            MLConfiguration config = parser.getMLConfiguration("src/test/resources/ml-config-temp.xml");
        } catch (Exception e) {
            Assert.assertEquals(true, e instanceof MLConfigurationParserException);
        }
    }
}
