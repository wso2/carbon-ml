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
 * Class contains test cases related to deleting analyses 
 */
@Test(groups="getAnalyses", dependsOnGroups="createAnalyses")
public class DeleteAnalysesTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    private static final String analysisName = "TestAnalysisForDeleteAnalysesTestcase";

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        // Check whether a project exists.
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue a project is not available");
        }
        //Create an analysis in the project, for deletion
        mlHttpclient.createAnalysis(analysisName, MLIntegrationTestConstants.PROJECT_ID);
    }

    /**
     * Test deleting an analysis by name.
     * 
     * @throws MLHttpClientException 
     * @throws IOException
     */
    @Test(description = "Delete an analysis by name")
    public void testDeleteAnalysis() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpDelete("/api/analyses/" + analysisName);
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test deleting a non-existing analysis.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Delete a non-existing analysis")
    public void testDeleteNonExistingAnalysis() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpDelete("/api/analyses/" + "nonExistinfAnalysisName");
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
}