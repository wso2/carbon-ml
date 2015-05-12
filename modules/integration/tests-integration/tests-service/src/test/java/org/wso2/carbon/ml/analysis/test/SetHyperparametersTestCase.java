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
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * Class contains test cases related to setting hyperparameters in a analysis
 */
@Test(groups="setHyperparameters", dependsOnGroups="addModelConfigs")
public class SetHyperparametersTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLHttpClientException, MLIntegrationBaseTestException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        // Check whether the analysis exists.
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/" + MLIntegrationTestConstants.ANALYSIS_NAME);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue an analysis is not available");
        }
    }

    /**
     * Test setting default values to hyper-parameters of an analysis.
     * 
     * @throws MLHttpClientException 
     * @throws IOException
     */
    @Test(description = "Set default values to hyperparameters")
    public void testSetDefaultHyperparameters() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpPost("/api/analyses/" + MLIntegrationTestConstants
                .ANALYSIS_ID + "/hyperParams/defaults", null);
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test setting customized hyper-parameters of an analysis.
     * 
     * @throws IOException
     * @throws MLHttpClientException 
     */
    @Test(description = "Set customized hyperparameters", dependsOnMethods = "testSetDefaultHyperparameters")
    public void testSetCustomizedHyperParameters() throws IOException, MLHttpClientException {
        String payload ="[{\"key\" :\"Learning_Rate\",\"value\" : \"0.1\"},{\"key\":\"Iterations\",\"value\":\"100\"}]";
        CloseableHttpResponse response = mlHttpclient.doHttpPost("/api/analyses/" + MLIntegrationTestConstants
                .ANALYSIS_ID + "/hyperParams", payload);
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
}