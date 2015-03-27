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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

@Test(groups="buildModels", dependsOnGroups="createModelStorage")
public class LoigsticRegressionTestCase extends MLBaseTest {
    
    private MLHttpClient mlHttpclient;
    private static final String analysisName = "TestAnalysisForLogisticRegressionTestcase";
    private static final String modelName = "TestModelForLogisticRegression";
    private static int analysisId;
    private static int modelId;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException, MLHttpClientException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        // Check whether the project exists.
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME);
        if (MLIntegrationTestConstants.HTTP_OK != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue a project is not available");
        }
        //Create an analysis
        mlHttpclient.createAnalysis(analysisName, MLIntegrationTestConstants.PROJECT_ID);
        analysisId = mlHttpclient.getAnalysisId(analysisName);
       
        //Set Model Configurations
        Map <String,String> configurations = new HashMap<String,String>();
        configurations.put(MLConstants.ALGORITHM_NAME, "LOGISTIC_REGRESSION");
        configurations.put(MLConstants.ALGORITHM_TYPE, "Classification");
        configurations.put(MLConstants.RESPONSE, "Class");
        configurations.put(MLConstants.TRAIN_DATA_FRACTION, "0.7");
        mlHttpclient.setModelConfiguration(analysisId, configurations);
        
        //Set default Hyper-parameters
        mlHttpclient.doHttpPost("/api/analyses/" + analysisId + "/hyperParams/defaults", null);
        
        // Create a model
        mlHttpclient.createModel(modelName, analysisId, MLIntegrationTestConstants.VERSIONSET_ID);
        modelId = mlHttpclient.getModelId(modelName); 
        
        //Set storage location for model
        mlHttpclient.createFileModelStorage(modelId, getModelStorageDirectory());
    }
    
    @Test(description = "Build a Logistic Regression model")
    public void testBuildLogisticRegressionModel() throws MLHttpClientException, IOException{
        CloseableHttpResponse response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
    
    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException, InterruptedException {
        // FIXME:
        Thread.sleep(5000);
    }
}