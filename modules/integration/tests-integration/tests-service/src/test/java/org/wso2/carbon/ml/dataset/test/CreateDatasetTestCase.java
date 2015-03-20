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

public class CreateDatasetTestCase extends MLIntegrationBaseTest {

    @BeforeClass(alwaysRun = true, groups = "wso2.ml.integration")
    public void initTest() throws Exception {
        super.init();
    }

    /**
     * Test creating a dataset from a valid csv file.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Create a dataset from a CSV file")
    public void testCreateDatasetFromFile() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = uploadDatasetFromCSV("SampleDataForCreateDatasetTestCase", "1.0", "data/fcSample.csv");
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test Creating a new version of an existing dataset
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Create a new version of an existing dataset",
            dependsOnMethods="testCreateDatasetFromFile")
    public void testCreateNewDatasetVersion() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = uploadDatasetFromCSV("SampleDataForCreateDatasetTestCase", "2.0", "data/fcSample.csv");
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test Creating a new version of an existing dataset
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Create a duplicate version of an existing dataset",
            dependsOnMethods="testCreateNewDatasetVersion")
    public void testCreateDuplicateDatasetVersion() throws ClientProtocolException, IOException, URISyntaxException, 
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = uploadDatasetFromCSV("SampleDataForCreateDatasetTestCase", "2.0", "data/fcSample.csv");
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a dataset from a non-existing csv file.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Create a dataset from a non-existing CSV file")
    public void testCreateDatasetFromNonExistingFile() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = uploadDatasetFromCSV("SampleDataForCreateDatasetTestCase_2", "1.0", "data/xxx.csv");
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_INTERNAL_SERVER_ERROR, response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a dataset without name.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Create a dataset without name")
    public void testCreateDatasetWithoutName() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = uploadDatasetFromCSV(null, "1.0", "data/xxx.csv");
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a dataset without version.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Create a dataset without version")
    public void testCreateDatasetWithoutVersion() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = uploadDatasetFromCSV("SampleDataForCreateDatasetTestCase_3", null, "data/xxx.csv");
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a dataset without data source.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Create a dataset without version")
    public void testCreateDatasetWithoutDataSource() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = uploadDatasetFromCSV("SampleDataForCreateDatasetTestCase_4", "1.0", null);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a dataset from a WSO2 BAM table.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Create a dataset from a WSO2 BAM Table",
            dependsOnMethods="testCreateDatasetFromNonExistingFile")
    public void testCreateDatasetFromBam() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        String bamTableUrl = System.getProperty("bam.table.url");
        if (bamTableUrl == null || bamTableUrl.isEmpty()) {
            throw new SkipException("Skipping tests because WSO2 BAM table is not available.");
        }
        String payload = "{\"name\" : \"SampleDataForCreateDatasetTestCase_5\",\"dataSourceType\" : \"bam\",\"dataTargetType\" : "
                        + "\"file\"," + "\"sourcePath\" : \""+ bamTableUrl + "\",\"dataType\""
                        + " : \"csv\"," + "\"comments\" : \"fcSample\",\"version\" : \"1.0\"}";
        CloseableHttpResponse response = doHttpPost(new URI(getServerUrlHttps() + "/api/datasets"), payload);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
}