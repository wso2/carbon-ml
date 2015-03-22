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

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

@Test(groups="addFeatures", dependsOnGroups="createAnalysisSuccess")
public class AddFeaturesTestCase extends MLIntegrationBaseTest {

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
    @Test(description = "Add default values to customized features")
    public void testAddDefaultsToCustomizedFeatures() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = setFeartureDefaults(MLIntegrationTestConstants.ANALYSIS_ID);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test adding customized features an analysis.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Add customized features")
    public void testAddCustomizedFeatures() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        String payload ="[{\"type\" :\"CATEGORICAL\",\"include\" : true,\"imputeOption\":\"DISCARD\",\"name\":\"" +
                "Cover_Type\"}]";
        CloseableHttpResponse response = doHttpPost(new URI(getServerUrlHttps() + "/api/analyses/" + 
                MLIntegrationTestConstants.ANALYSIS_ID + "/features"), payload);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    //TODO: add non existing features
}