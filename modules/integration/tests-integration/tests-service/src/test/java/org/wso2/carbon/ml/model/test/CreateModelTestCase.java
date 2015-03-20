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

package org.wso2.carbon.ml.model.test;

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

public class CreateModelTestCase extends MLIntegrationBaseTest {
    
    private static final String DatasetName = "SampleDataForLogisticRegressionTestCase";
    private static final String projectName = "TestProjectForLogisticRegressionTestcase";
    private static final String analysisName = "TestAnalysisForLogisticRegressionTestcase";
    private static final String modelName = "TestModelForLogisticRegression";
    private static int datasetId;
    private static int projectId;
    private static int analysisId;
    private static int versionSetId;
    private static int modelId;

    @BeforeClass(alwaysRun = true, groups = "wso2.ml.integration")
    public void initTest() throws Exception {
        super.init();
        // Upload a dataset
        CloseableHttpResponse response = uploadDatasetFromCSV(DatasetName, "1.0", "data/fcSample.csv");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JSONObject responseJson = new JSONObject(bufferedReader.readLine());
        bufferedReader.close();
        response.close();
        datasetId = responseJson.getInt("id");
        //Create a project
        createProject(projectName, DatasetName);
        projectId = getProjectId(projectName);
        //Create an analysis
        createAnalysis(analysisName, projectId);
        analysisId = getAnalysisId(analysisName);
        //Set Model Configurations
        Map <String,String> configurations = new HashMap<String,String>();
        configurations.put("algorithmName", "LOGISTIC_REGRESSION");
        configurations.put("algorithmType", "Classification");
        configurations.put("responseVariable", "Cover_Type");
        configurations.put("trainDataFraction", "0.7");
        setModelConfiguration(analysisId, configurations);
        //Set default Hyper-parameters
        doHttpPost(new URI(getServerUrlHttps() + "/api/analyses/" + analysisId + "/hyperParams/defaults"), null);
        versionSetId = getAVersionSetIdOfDataset(datasetId);
    }

    /**
     * Test creating a Logistic Regression model.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Set default values to hyperparameters")
    public void testCreateModel() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = createModel(modelName, analysisId, versionSetId);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
}