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
 * This class contains test cases related to adding features to a ML analysis
 */
@Test(groups="addFeatures", dependsOnGroups="createAnalyses")
public class AddFeaturesTestCase extends MLBaseTest {
    
    private MLHttpClient mlHttpclient;
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        // Check whether the analysis exists.
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/" + MLIntegrationTestConstants
                .ANALYSIS_NAME);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue an analysis is not available");
        }
    }

    /**
     * Test adding default values to customized features an analysis.
     * @throws MLHttpClientException 
     * @throws IOException
     */
    @Test(description = "Add default values to customized features")
    public void testAddDefaultsToCustomizedFeatures() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.setFeartureDefaults(MLIntegrationTestConstants.ANALYSIS_ID);
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test adding customized features an analysis.
     * 
     * @throws IOException
     * @throws MLHttpClientException 
     */
    @Test(description = "Add customized features")
    public void testAddCustomizedFeatures() throws  MLHttpClientException, IOException {
        String payload ="[{\"type\" :\"CATEGORICAL\",\"include\" : true,\"imputeOption\":\"DISCARD\",\"name\":\"" +
                "Cover_Type\"}]";
        CloseableHttpResponse response = mlHttpclient.doHttpPost("/api/analyses/" + MLIntegrationTestConstants
                .ANALYSIS_ID + "/features", payload);
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    //TODO: add non existing features
}