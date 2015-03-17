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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
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

	private static final Log logger = LogFactory.getLog(MLIntegrationBaseTest.class);
    protected CloseableHttpResponse response = null;
    
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
    protected CloseableHttpResponse doGet(URI uri) throws ClientProtocolException, IOException {
        CloseableHttpClient httpClient =  HttpClients.createDefault();
        HttpGet get = new HttpGet(uri);
    	get.setHeader(MLIntegrationTestConstants.CONTENT_TYPE, MLIntegrationTestConstants.APPLICATION_JSON);
    	String token = userInfo.getUserName() + ":" + userInfo.getPassword();
    	String encodedToken = new String(Base64.encodeBase64(token.getBytes()));
    	get.setHeader(MLIntegrationTestConstants.AUTHORIZATION_HEADER, MLIntegrationTestConstants.BASIC + encodedToken);
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
    protected CloseableHttpResponse doPost(URI uri, String parametersJson) throws ClientProtocolException, IOException {
        CloseableHttpClient httpClient =  HttpClients.createDefault();
        HttpPost post = new HttpPost(uri);
    	post.setHeader(MLIntegrationTestConstants.CONTENT_TYPE, MLIntegrationTestConstants.APPLICATION_JSON);
    	String token = userInfo.getUserName() + ":" + userInfo.getPassword();
    	String encodedToken = new String(Base64.encodeBase64(token.getBytes()));
    	post.setHeader(MLIntegrationTestConstants.AUTHORIZATION_HEADER, MLIntegrationTestConstants.BASIC + encodedToken);
    	StringEntity params =new StringEntity(parametersJson);
    	post.setEntity(params);
    	return httpClient.execute(post);
    }
}