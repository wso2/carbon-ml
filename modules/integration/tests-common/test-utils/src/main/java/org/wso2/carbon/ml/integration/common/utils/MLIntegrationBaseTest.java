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

package org.wso2.carbon.ml.integration.common.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * This is the base class for all Integration tests of ML. Provides functions that are common 
 * to all the integration tests in ML.
 */
public abstract class MLIntegrationBaseTest extends MLBaseTest{

    protected void init() throws MLIntegrationBaseTestException {
        super.init();
    }
    
    /**
     * Send a HTTP GET request to the given URI and return the response.
     * 
     * @param uri   End-point URI
     * @return      Response from the endpoint
     * @throws      ClientProtocolException
     * @throws      IOException
     */
    protected CloseableHttpResponse doHttpGet(URI uri) throws ClientProtocolException, IOException {
        CloseableHttpClient httpClient =  HttpClients.createDefault();
        HttpGet get = new HttpGet(uri);
    	get.setHeader(MLIntegrationTestConstants.CONTENT_TYPE, MLIntegrationTestConstants.APPLICATION_JSON);
    	get.setHeader(MLIntegrationTestConstants.AUTHORIZATION_HEADER, getBasicAuthKey());
    	return httpClient.execute(get);
    }
    
    /**
     * Send a HTTP GET request to the given URI and return the response.
     * 
     * @param uri               End-point URI
     * @param parametersJson    Payload JSON string
     * @return                  Response from the endpoint
     * @throws                  ClientProtocolException
     * @throws                  IOException
     */
    protected CloseableHttpResponse doHttpPost(URI uri, String parametersJson) throws ClientProtocolException, IOException {
        CloseableHttpClient httpClient =  HttpClients.createDefault();
        HttpPost post = new HttpPost(uri);
    	post.setHeader(MLIntegrationTestConstants.CONTENT_TYPE, MLIntegrationTestConstants.APPLICATION_JSON);
    	post.setHeader(MLIntegrationTestConstants.AUTHORIZATION_HEADER, getBasicAuthKey());
    	StringEntity params =new StringEntity(parametersJson);
    	post.setEntity(params);
    	return httpClient.execute(post);
    }
    
    /**
     * 
     * @param uri   End-point URI
     * @return      Response from the endpoint
     * @throws      ClientProtocolException
     * @throws      IOException
     */
    protected CloseableHttpResponse doHttpDelete(URI uri) throws ClientProtocolException, IOException {
        CloseableHttpClient httpClient =  HttpClients.createDefault();
        HttpDelete delete = new HttpDelete();
        delete.setHeader(MLIntegrationTestConstants.CONTENT_TYPE, MLIntegrationTestConstants.APPLICATION_JSON);
        delete.setHeader(MLIntegrationTestConstants.AUTHORIZATION_HEADER, getBasicAuthKey());
        return httpClient.execute(delete);
    }
    
    /**
     * Get the Encoded Key for Basic auth header
     * 
     * @return  Encoded Key Basic auth Key
     */
    protected String getBasicAuthKey() {
        String token = userInfo.getUserName() + ":" + userInfo.getPassword();
        String encodedToken = new String(Base64.encodeBase64(token.getBytes()));
        return (MLIntegrationTestConstants.BASIC + encodedToken);
    }
    
    /**
     * Upload a sample datatset from resources
     * 
     * @param DatasetName   Name for the dataset
     * @param version       Version for the dataset
     * @param resourcePath  Relative path the CSV file in resources
     * @return              Response from the backend
     * @throws              ClientProtocolException
     * @throws              IOException
     * @throws              URISyntaxException
     */
    protected CloseableHttpResponse uploadDatasetFromCSV(String DatasetName, String version, String resourcePath) throws 
            ClientProtocolException, IOException, URISyntaxException {
        String payload = "{\"name\" : \"" + DatasetName + "\",\"dataSourceType\" : \"file\",\"dataTargetType\" : "
                + "\"file\"," + "\"sourcePath\" : \""+ getResourceAbsolutePath(resourcePath) + "\",\"dataType\""
                + " : \"csv\"," + "\"comments\" : \"fcSample\",\"version\" : \"" + version + "\"}";
        return doHttpPost(new URI("https://localhost:9443/api/datasets"), payload);
    }
}