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

package org.wso2.carbon.ml.model.test;

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
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * Contains test cases related to creating models
 */
@Test(groups="createModels")
public class CreateModelTestCase extends MLBaseTest {
    
    private MLHttpClient mlHttpclient;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException, MLHttpClientException, IOException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        // Check whether the version-set exists.
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/datasets/versions/" + MLIntegrationTestConstants
                .VERSIONSET_ID);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue a version-set is not available");
        }
        response.close();
        //Check whether analysis exists.
        response = mlHttpclient.doHttpGet("/api/analyses/" + MLIntegrationTestConstants.ANALYSIS_NAME);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue an analysis is not available");
        }
    }

    /**
     * Test creating a model.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create a Model")
    public void testCreateModel() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createModel(MLIntegrationTestConstants.ANALYSIS_ID,
                MLIntegrationTestConstants.VERSIONSET_ID);
        MLIntegrationTestConstants.MODEL_NAME = mlHttpclient.getModelName(response);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a Model with an invalid analysis.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create a Model with an invalid analysis")
    public void testCreateModelWithInvalidAnalysis() throws MLHttpClientException, IOException  {
        CloseableHttpResponse response = mlHttpclient.createModel(999, MLIntegrationTestConstants.VERSIONSET_ID);
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a Model with an invalid version-set.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create a Model with an invalid versionset")
    public void testCreateModelWithInvalidVersionset() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createModel(MLIntegrationTestConstants.ANALYSIS_ID, 999);
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
}