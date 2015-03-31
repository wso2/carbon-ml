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
 * Contains test cases related to retrieving projects
 */
@Test(groups="getProjects", dependsOnGroups="createProjects")
public class GetProjectsTestCase extends MLBaseTest {
    
    private MLHttpClient mlHttpclient;
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
    }
    
    /**
     * Test retrieving all projects.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Retrieve a project")
    public void testGetAllProjects() throws MLHttpClientException, IOException   {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects");
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test retrieving a project.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Retrieve a project")
    public void testGetProject() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants.PROJECT_NAME);
        assertEquals("Unexpected response recieved", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    //TODO: Add retrieving a non existing project
}