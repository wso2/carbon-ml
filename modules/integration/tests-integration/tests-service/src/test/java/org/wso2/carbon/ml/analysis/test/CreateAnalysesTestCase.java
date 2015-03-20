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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONObject;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

public class CreateAnalysesTestCase extends MLIntegrationBaseTest {
    
    private static final String DatasetName = "SampleDataForAnalysisTestCase";
    private static final String projectName = "TestProjectForAnalysisTestcase";
    private static int projectId;

    @BeforeClass(alwaysRun = true, groups = "wso2.ml.integration")
    public void initTest() throws Exception {
        super.init();
        uploadDatasetFromCSV(DatasetName, "1.0", "data/fcSample.csv");
        createProject(projectName, DatasetName);
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/projects/" + projectName));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JSONObject responseJson = new JSONObject(bufferedReader.readLine());
        projectId = responseJson.getInt("id");
        bufferedReader.close();
        response.close();
    }

    /**
     * Test creating an analysis.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Create an analysis")
    public void testCreateAnalysis() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = createAnalysis("TestAnalysisForAnalysis", projectId);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating an analysis without the Name.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Create an analysis without a name")
    public void testCreateAnalysisWithoutName() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = createAnalysis(null, projectId);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_BAD_REQUEST, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating an analysis without a project ID.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    /*@Test(groups = "wso2.ml.integration", description = "Create an analysis without a ProjectId")
    public void testCreateAnalysisWithoutProjectID() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = createAnalysis("TestAnalysisForAnalysis", -1);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_BAD_REQUEST, response.getStatusLine().getStatusCode());
        response.close();
    }*/
}