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

package org.wso2.carbon.ml.integration.test.services.project;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.ClientProtocolException;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.test.exceptions.MLIntegrationTestException;

public class CreateProjectTestCase extends MLIntegrationBaseTest {

	private static final Log logger = LogFactory.getLog(CreateProjectTestCase.class);
	
    @BeforeClass(alwaysRun = true, groups = "wso2.ml.integration")
    public void initTest() throws Exception {
        super.init();
    }
    
    /**
     * Test creating a project.
     * 
     * @throws MLIntegrationTestException
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test(groups = "wso2.ml.integration", description = "Create a project")
    public void testCreateProject() throws MLIntegrationTestException, ClientProtocolException,
            IOException, URISyntaxException {
        String payload = "{\"name\" : \"WSO2-ML-Test-Project\",\"description\" : \"Test Project\"}";
        response = doPost(new URI("https://localhost:9443/api/projects"), payload);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
    }
    
    /*@Test(groups = "wso2.ml.integration", description = "Create a project")
    public void testGetProjectId() throws MLIntegrationTestException, ClientProtocolException, IOException, 
            URISyntaxException {
        response = doGet(new URI("https://localhost:9443/api/datasets"));
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
    }*/
}