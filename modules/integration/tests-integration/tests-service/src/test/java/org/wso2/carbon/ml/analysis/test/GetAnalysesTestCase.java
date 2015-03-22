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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

@Test(groups="getAnalyses", dependsOnGroups="createAnalysisSuccess")
public class GetAnalysesTestCase extends MLIntegrationBaseTest {

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
    }

    /**
     * Test retrieving all analyzes.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Get all analyses")
    public void testGetAllAnalyzes() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/analyses/"));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
    }
    
    /**
     * Test retrieving an analysis by name.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Retrieve an analysis by name")
    public void testGetAnalysis() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/analyses/" + 
                MLIntegrationTestConstants.ANALYSIS_NAME));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test retrieving a non-existing analysis.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Retrieve a non-existing analysis")
    public void testGetNonExistingAnalysis() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/analyses/" + 
                "nonExistinfAnalysisName"));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_NOT_FOUND, response.getStatusLine().getStatusCode());
        response.close();
    }
}