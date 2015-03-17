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

package org.wso2.carbon.ml.integration.test.services.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONObject;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;

public class GetDatasetsTestCase extends MLIntegrationBaseTest {

    private static final Log logger = LogFactory.getLog(GetDatasetsTestCase.class);
    private int datasetId;

    @BeforeClass(alwaysRun = true, groups = "wso2.ml.integration")
    public void initTest() throws Exception {
        super.init();
        // Create a dataset
        String payload = "{\"name\" : \"ForestCoverDataset1\",\"dataSourceType\" : \"file\",\"dataTargetType\" : "
                        + "\"file\"," + "\"sourcePath\" : \""+ getResourceAbsolutePath("data/fcSample.csv") + "\",\"dataType\""
                        + " : \"csv\"," + "\"comments\" : \"fcSample\",\"version\" : \"1.0\"}";
        CloseableHttpResponse response = doPost(new URI("https://localhost:9443/api/datasets"), payload);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JSONObject responseJson = new JSONObject(bufferedReader.readLine());
        datasetId = responseJson.getInt("id");
        bufferedReader.close();
        response.close();
    }

    /**
     * Test retrieving a dataset, given the dataset ID
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test(groups = "wso2.ml.integration", description = "Get a datasets with a known ID")
    public void testGetDataset() throws ClientProtocolException, IOException, URISyntaxException {
        CloseableHttpResponse response = doGet(new URI("https://localhost:9443/api/datasets/" + datasetId));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        // Check whether the correct dataset is returned
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JSONObject responseJson = new JSONObject(bufferedReader.readLine());
        Assert.assertEquals(datasetId, responseJson.getInt("id"));
        bufferedReader.close();
        response.close();
    }
    
    /**
     * Test retrieving all the available version-sets of a dataset.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test(groups = "wso2.ml.integration", description = "Get all available version-sets of a dataset")
    public void testGetVersionSetsOfdataset() throws ClientProtocolException, IOException, URISyntaxException {
        CloseableHttpResponse response = doGet(new URI("https://localhost:9443/api/datasets/" + datasetId + "/versions"));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    //TODO
    /*@Test(groups = "wso2.ml.integration", description = "Get a versions of a dataset")
    public void testGetVersionset() throws ClientProtocolException, IOException, URISyntaxException {
        CloseableHttpResponse response = doGet(new URI("https://localhost:9443/api/datasets/" + datasetId + "/versions/" + versionsetID));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }*/
    
    
    /**
     * Test retrieving all the available value-sets of a dataset.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test(groups = "wso2.ml.integration", description = "Get all available value-sets of a dataset")
    public void testGetAllValueSetsOfDataset() throws ClientProtocolException, IOException, URISyntaxException {
        CloseableHttpResponse response = doGet(new URI("https://localhost:9443/api/datasets/" + datasetId + "/valuesets"));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test retrieving all the available value-sets.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test(groups = "wso2.ml.integration", description = "Get all available value-sets")
    public void testGetAllValueSets() throws ClientProtocolException, IOException, URISyntaxException {
        CloseableHttpResponse response = doGet(new URI("https://localhost:9443/api/datasets/valuesets/"));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    
    // TODO
    /*@Test(groups = "wso2.ml.integration", description = "Get all available value-sets")
    public void testGetAllValueSetsOfVersion() throws ClientProtocolException, IOException, URISyntaxException {
        CloseableHttpResponse response = doGet(new URI("https://localhost:9443/api/datasets/valuesets/versions" + versionsetId));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    @Test(groups = "wso2.ml.integration", description = "Get all available value-sets")
    public void testGetValueset() throws ClientProtocolException, IOException, URISyntaxException {
        CloseableHttpResponse response = doGet(new URI("https://localhost:9443/api/datasets/valuesets/" + versionsetId));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }*/
    
    /**
     * Test retrieving all the available data-sets
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test(groups = "wso2.ml.integration", description = "Get all available datasets")
    public void testGetAllDatasets() throws ClientProtocolException, IOException, URISyntaxException {
        CloseableHttpResponse response = doGet(new URI("https://localhost:9443/api/datasets"));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException {
    }
}