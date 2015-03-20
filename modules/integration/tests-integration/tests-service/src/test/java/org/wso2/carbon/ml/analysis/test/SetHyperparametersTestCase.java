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
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONObject;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

public class SetHyperparametersTestCase extends MLIntegrationBaseTest {
    
    private static final String DatasetName = "SampleDataForAddFeaturesTestCase";
    private static final String projectName = "TestProjectForAddFeaturesTestcase";
    private static final String analysisName = "TestAnalysisForAddFeaturesTestcase";
    private static int projectId;
    private static int analysisId;

    @BeforeClass(alwaysRun = true, groups = "wso2.ml.integration")
    public void initTest() throws Exception {
        super.init();
        // Upload a dataset
        uploadDatasetFromCSV(DatasetName, "1.0", "data/fcSample.csv");
        //Create a project
        createProject(projectName, DatasetName);
        projectId = getProjectId(projectName);
        //Create an analysis
        createAnalysis(analysisName, projectId);
        analysisId = getAnalysisId(analysisName);
        //Set Model Configurations
        Map <String,String> configurations = new HashMap<String,String>();
        configurations.put("algorithmName", "LOGISTIC_REGRESSION");
        setModelConfiguration(analysisId, configurations);
    }

    /**
     * Test setting default values to hyper-parameters of an analysis.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Set default values to hyperparameters")
    public void testSetDefaultHyperparameters() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpPost(new URI(getServerUrlHttps() + "/api/analyses/" + analysisId +
                "/hyperParams/defaults"), null);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test setting customized hyper-parameters of an analysis.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Set customized hyperparameters", dependsOnMethods = 
            "testSetDefaultHyperparameters")
    public void testSetCustomizedHyperParameters() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        String payload ="[{\"key\" :\"Learning_Rate\",\"value\" : \"0.1\"},{\"key\":\"Iterations\",\"value\":\"100\"}]";
        CloseableHttpResponse response = doHttpPost(new URI(getServerUrlHttps() + "/api/analyses/" + analysisId + 
                "/hyperParams"), payload);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
}