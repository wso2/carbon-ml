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

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * Contains test cases related to creating datasets
 */
@Test(groups="createDatasets")
public class CreateDatasetTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
    }
    
    /**
     * Test creating a dataset from a valid csv file.
     * @throws MLHttpClientException 
     * @throws IOException
     */
    @Test(description = "Create a dataset from a CSV file")
    public void testCreateDatasetFromFile() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME,
                "1.0", MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test Creating a new version of an existing dataset
     * @throws MLHttpClientException 
     * @throws IOException
     */
    @Test(description = "Create a new version of an existing dataset",
            dependsOnMethods="testCreateDatasetFromFile")
    public void testCreateNewDatasetVersion() throws MLHttpClientException, IOException  {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME,
                "2.0", MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test Creating a new version of an existing dataset
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create a duplicate version of an existing dataset",
            dependsOnMethods="testCreateNewDatasetVersion")
    public void testCreateDuplicateDatasetVersion() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME,
                "2.0", MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        assertEquals("Unexpected response recieved", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a dataset from a non-existing csv file.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create a dataset from a non-existing CSV file")
    public void testCreateDatasetFromNonExistingFile() throws MLHttpClientException, IOException  {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME,
                "1.0", "data/xxx.csv");
        assertEquals("Unexpected response recieved", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a dataset without name.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create a dataset without name")
    public void testCreateDatasetWithoutName() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(null, "1.0", "data/xxx.csv");
        assertEquals("Unexpected response recieved", Response.Status.BAD_REQUEST.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a dataset without version.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create a dataset without version")
    public void testCreateDatasetWithoutVersion() throws  MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV("SampleDataForCreateDatasetTestCase_3",
                null, "data/xxx.csv");
        assertEquals("Unexpected response recieved", Response.Status.BAD_REQUEST.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a dataset without data source.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create a dataset without version")
    public void testCreateDatasetWithoutDataSource() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV("SampleDataForCreateDatasetTestCase_4",
                "1.0", null);
        assertEquals("Unexpected response recieved",Response.Status.BAD_REQUEST.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
}