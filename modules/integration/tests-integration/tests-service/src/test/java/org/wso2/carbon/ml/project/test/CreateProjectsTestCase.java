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

package org.wso2.carbon.ml.project.test;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

@Test(groups="createProjects")
public class CreateProjectsTestCase extends MLBaseTest {
    
    private MLHttpClient mlHttpclient;
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException, MLHttpClientException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        //Check whether the dataset exists.
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/datasets/" + MLIntegrationTestConstants
                .DATASET_ID);
        if (MLIntegrationTestConstants.HTTP_OK != response.getStatusLine().getStatusCode()) {
            //TODO: pass the datsset ID
            throw new SkipException("Skipping tests becasue dataset is not available");
        }
    }

    /**
     * Test creating a project.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create a project")
    public void testCreateProject() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME, 
                MLIntegrationTestConstants.DATASET_NAME);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a project with a duplicate project name.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    //FIXME: This should fail!!
    @Test(description = "Create a project with duplicate Name", dependsOnMethods = "testCreateProject")
    public void testCreateProjectWithDuplicateName() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createProject("TestProjectForCreatProjectTestCase", 
                MLIntegrationTestConstants.DATASET_NAME);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a project without the project name.
     * @throws MLHttpClientException 
     * @throws IOException
     */
    @Test(description = "Create a project without name", dependsOnMethods = "testCreateProject")
    public void testCreateProjectWithoutName() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createProject(null, MLIntegrationTestConstants.DATASET_NAME);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_BAD_REQUEST, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a project without the project name.
     * @throws MLHttpClientException 
     * @throws IOException
     */
    @Test(description = "Create a project without a dataset")
    public void testCreateProjectWithoutDataset() throws MLHttpClientException, IOException  {
        CloseableHttpResponse response = mlHttpclient.createProject("TestProjectForCreatProjectTestCase-2", null);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_BAD_REQUEST, response.getStatusLine().getStatusCode());
        response.close();
    }
}