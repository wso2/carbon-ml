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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

public class AddModelConfigurationsTestCase extends MLIntegrationBaseTest {
    
    private static final String DatasetName = "SampleDataForAddModelConfigsTestCase";
    private static final String projectName = "TestProjectForAddModelConfigsTestcase";
    private static final String analysisName = "TestAnalysisForAddModelConfigsTestcase";
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
    }

    /**
     * Test adding default values to customized features an analysis.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException 
     */
    @Test(groups = "wso2.ml.integration", description = "Add model configurations to the analysis")
    public void testSetModelConfigurations() throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        Map <String,String> configurations = new HashMap<String,String>();
        configurations.put("algorithmName", "LOGISTIC_REGRESSION");
        configurations.put("algorithmType", "Classification");
        configurations.put("responseVariable", "Cover_Type");
        configurations.put("trainDataFraction", "0.7");
        setModelConfiguration(analysisId, configurations);
        CloseableHttpResponse response = setModelConfiguration(analysisId, configurations);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
}