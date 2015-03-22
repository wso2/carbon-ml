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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.analysis.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

@Test(groups="addModelConfigs", dependsOnGroups="createAnalyses")
public class AddModelConfigurationsTestCase extends MLIntegrationBaseTest {
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        // Check whether the analysis exists.
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/analyses/" + 
                MLIntegrationTestConstants.ANALYSIS_NAME));
        if (MLIntegrationTestConstants.HTTP_OK != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue an analysis is not available");
        }
    }

    /**
     * Test adding default values to customized features an analysis.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Add model configurations to the analysis")
    public void testSetModelConfigurations() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        Map <String,String> configurations = new HashMap<String,String>();
        configurations.put(MLConstants.ALGORITHM_NAME, "LOGISTIC_REGRESSION");
        configurations.put(MLConstants.ALGORITHM_TYPE, "Classification");
        configurations.put(MLConstants.RESPONSE, "Cover_Type");
        configurations.put(MLConstants.TRAIN_DATA_FRACTION, "0.7");
        CloseableHttpResponse response = setModelConfiguration(MLIntegrationTestConstants.ANALYSIS_ID, configurations);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
}