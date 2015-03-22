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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;

@Test(groups="buildModel", dependsOnGroups="createModelStorageSuccess")
public class LoigsticRegressionTestCase extends MLIntegrationBaseTest {
    
    private static final String analysisName = "TestAnalysisForLogisticRegressionTestcase";
    private static final String modelName = "TestModelForLogisticRegression";
    private static int analysisId;
    private static int modelId;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        // Check whether the project exists.
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/projects/" + 
                MLIntegrationTestConstants.PROJECT_NAME));
        if (MLIntegrationTestConstants.HTTP_OK != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue a project is not available");
        }
        //Create an analysis
        createAnalysis(analysisName, MLIntegrationTestConstants.PROJECT_ID);
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
        // Create a model
        createModel(modelName, analysisId, MLIntegrationTestConstants.VERSIONSET_ID);
        modelId = getModelId(modelName); 
        //Set storage location for model
        createFileModelStorage(modelId, MLIntegrationTestConstants.FILE_STORAGE_LOCATION);
    }
}