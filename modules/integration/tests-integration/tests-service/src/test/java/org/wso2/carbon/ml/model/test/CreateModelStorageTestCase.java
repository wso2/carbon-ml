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

@Test(groups="createModelStorage", dependsOnGroups="createModels")
public class CreateModelStorageTestCase extends MLBaseTest {
    
    private MLHttpClient mlHttpclient;
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLHttpClientException, MLIntegrationBaseTestException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        // Check whether the model exists.
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/models/" + MLIntegrationTestConstants.MODEL_NAME);
        if (MLIntegrationTestConstants.HTTP_OK != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests becasue the model is not available");
        }
    }

    /**
     * Test creating model storage type: file
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create model storage type: file")
    public void testCreateModelStorage() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createFileModelStorage(MLIntegrationTestConstants.MODEL_ID, 
                MLIntegrationTestConstants.FILE_STORAGE_LOCATION);
        Assert.assertEquals(MLIntegrationTestConstants.HTTP_OK, response.getStatusLine().getStatusCode());
        response.close();
    }
}