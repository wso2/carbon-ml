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

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;

/**
 * Class contains test cases related to creating analyses
 */
@Test(groups="createAnalyses")
public class CreateAnalysesTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    
    @BeforeClass(alwaysRun = true, groups = "wso2.ml.integration")
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        // Check whether the project exists.
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue a project is not available");
        }
    }

    /**
     * Test creating an analysis.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(groups = "createAnalysisSuccess", description = "Create an analysis")
    public void testCreateAnalysis() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createAnalysis(MLIntegrationTestConstants.ANALYSIS_NAME, 
                MLIntegrationTestConstants.PROJECT_ID);
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test creating an analysis without the Name.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(groups = "wso2.ml.integration", description = "Create an analysis without a name")
    public void testCreateAnalysisWithoutName() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createAnalysis(null, MLIntegrationTestConstants.PROJECT_ID);
        assertEquals("Unexpected response recieved", Response.Status.BAD_REQUEST.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating an analysis without a project ID.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create an analysis without a ProjectId")
    public void testCreateAnalysisWithoutProjectID() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createAnalysis("TestAnalysisForAnalysis", -1);
        assertEquals("Unexpected response recieved", Response.Status.BAD_REQUEST.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
}