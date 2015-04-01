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

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * Contains test cases related to deleting projects
 */
@Test(groups="deleteProjects", dependsOnGroups="createProjects")
public class DeleteProjectsTestCase extends MLBaseTest {
    
    private MLHttpClient mlHttpclient;
    private static final String projectName = "TestProjectForDeleteProjectTestcase";

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLHttpClientException, MLIntegrationBaseTestException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        //Check whether the dataset exists.
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/datasets/" + MLIntegrationTestConstants.DATASET_ID);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
        // Create a project to delete
        mlHttpclient.createProject(projectName, MLIntegrationTestConstants.DATASET_NAME);
    }
    
    /**
     * Test deleting a project.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Delete an exsisting project")
    public void testDeleteProject() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpDelete("/api/projects/" + projectName);
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test deleting a non-existing project.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Delete an exsisting project")
    public void testDeleteNonExistingProject() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpDelete("/api/projects/" + "NonExistingProjectName");
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
}