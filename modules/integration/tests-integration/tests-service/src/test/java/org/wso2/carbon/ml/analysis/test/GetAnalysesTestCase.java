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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;

@Test(groups="getAnalyses", dependsOnGroups="createAnalyses")
public class GetAnalysesTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
    }

    /**
     * Test retrieving all analyzes.
     * 
     * @throws MLHttpClientException 
     */
    @Test(description = "Get all analyses")
    public void testGetAllAnalyzes() throws MLHttpClientException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/");
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
    }
    
    /**
     * Test retrieving an analysis by name.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Retrieve an analysis by name")
    public void testGetAnalysis() throws MLHttpClientException, IOException  {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/" + MLIntegrationTestConstants
                .ANALYSIS_NAME);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test retrieving a non-existing analysis.
     * 
     * @throws MLHttpClientException 
     * @throws IOException
     */
    @Test(description = "Retrieve a non-existing analysis")
    public void testGetNonExistingAnalysis() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/" + "nonExistinfAnalysisName");
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_NOT_FOUND, response.getStatusLine().getStatusCode());
        response.close();
    }
}