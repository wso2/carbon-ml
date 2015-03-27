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

@Test(groups="createModels")
public class CreateModelTestCase extends MLIntegrationBaseTest {
    
    private static final String modelName = "TestModelForModelCreateModelTestcase";

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        // Check whether the version-set exists.
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/datasets/versions/" + 
                MLIntegrationTestConstants.VERSIONSET_ID) );
        if (MLIntegrationTestConstants.HTTP_OK != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue a version-set is not available");
        }
        response.close();
        //Check whether analysis exists.
        response = doHttpGet(new URI(getServerUrlHttps() + "/api/analyses/" + MLIntegrationTestConstants.ANALYSIS_NAME));
        if (MLIntegrationTestConstants.HTTP_OK != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue an analysis is not available");
        }
    }

    /**
     * Test creating a model.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Create a Model")
    public void testCreateModel() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = createModel(MLIntegrationTestConstants.MODEL_NAME, MLIntegrationTestConstants
                .ANALYSIS_ID, MLIntegrationTestConstants.VERSIONSET_ID);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a Model with an invalid analysis.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException
     */
    @Test(description = "Create a Model with an invalid analysis")
    public void testCreateModelWithInvalidAnalysis() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = createModel(modelName, 999, MLIntegrationTestConstants.VERSIONSET_ID);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a Model with an invalid version-set.
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException
     */
    @Test(description = "Create a Model with an invalid versionset")
    public void testCreateModelWithInvalidVersionset() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = createModel(modelName, MLIntegrationTestConstants.ANALYSIS_ID, 999);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
        response.close();
    }
}