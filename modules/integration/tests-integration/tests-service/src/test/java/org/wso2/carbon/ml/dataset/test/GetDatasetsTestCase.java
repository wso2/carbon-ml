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

package org.wso2.carbon.ml.dataset.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

@Test(groups="getDatasets", dependsOnGroups="createDatasets")
public class GetDatasetsTestCase extends MLIntegrationBaseTest {

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
    }

    /**
     * Test retrieving a dataset, given the dataset ID
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Get a dataset with a known ID")
    public void testGetDataset() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/datasets/" + 
                MLIntegrationTestConstants.DATASET_ID));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        // Check whether the correct dataset is returned.
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JSONObject responseJson = new JSONObject(bufferedReader.readLine());
        Assert.assertEquals(MLIntegrationTestConstants.DATASET_ID, responseJson.getInt("id"));
        bufferedReader.close();
        response.close();
    }
    
    /**
     * Test retrieving a dataset, given a invalid dataset ID
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Get a dataset with an invalid ID")
    public void testGetDatasetWithInvalidId() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/datasets/" + 999));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_NOT_FOUND, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test retrieving all the available data-sets
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Get all available datasets")
    public void testGetAllDatasets() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/datasets"));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test retrieving all the available version-sets of a dataset.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Get all available versions of a dataset")
    public void testGetVersionSetsOfdataset() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/datasets/" + 
                MLIntegrationTestConstants.DATASET_ID + "/versions"));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        // Check whether the version set exists
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JSONArray responseJson = new JSONArray(bufferedReader.readLine());
        JSONObject datsetVersionJson = (JSONObject) responseJson.get(0);
        Assert.assertEquals(MLIntegrationTestConstants.VERSIONSET_ID,datsetVersionJson.getInt("id"));
        response.close();
        bufferedReader.close();
    }
    
    /**
     * Test retrieving all the available value-sets of a dataset.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Get dataset version from its ID", dependsOnMethods = 
            "testGetVersionSetsOfdataset")
    public void testGetVersionSet() throws ClientProtocolException, IOException, URISyntaxException, 
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/datasets/versions/" + 
                MLIntegrationTestConstants.VERSIONSET_ID) );
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test retrieving all the available value-sets of a dataset.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(description = "Get dataset version with a non-existing ID")
    public void testGetVersionSetWithInvalidId() throws ClientProtocolException, IOException, URISyntaxException, 
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/datasets/versions/" + 999));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_NOT_FOUND, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    
    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException {
    }
}