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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
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
    	if(parametersJson != null) {
    	    StringEntity params =new StringEntity(parametersJson);
    	    post.setEntity(params);
    	}
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
        HttpDelete delete = new HttpDelete(uri);
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
     * @throws              MLIntegrationBaseTestException 
     */
    protected CloseableHttpResponse uploadDatasetFromCSV(String DatasetName, String version, String resourcePath)
            throws ClientProtocolException, IOException, URISyntaxException, MLIntegrationBaseTestException {
        String payload;
        if (DatasetName == null) {
            payload = "{\"dataSourceType\" : \"file\",\"dataTargetType\" : \"file\",\"sourcePath\" : \"" +
                    getResourceAbsolutePath(resourcePath) + "\",\"dataType\":\"csv\"," + "\"comments\":\"fcSample\"," +
                    "\"version\" : \"" + version + "\"}";
        } else if (version == null) {
            payload = "{\"name\" : \"" + DatasetName + "\",\"dataSourceType\" : \"file\",\"dataTargetType\" : "
                    + "\"file\"," + "\"sourcePath\" : \""+ getResourceAbsolutePath(resourcePath) + "\",\"dataType\""
                    + " : \"csv\"," + "\"comments\" : \"fcSample\"}";
        } else if (resourcePath == null) {
            payload = "{\"name\" : \"" + DatasetName + "\",\"dataSourceType\" : \"file\",\"dataTargetType\" : "
                    + "\"file\",\"dataType\":\"csv\"," + "\"comments\" : \"fcSample\",\"version\" : \"" + version + "\"}";
        } else {
            payload = "{\"name\" : \"" + DatasetName + "\",\"dataSourceType\" : \"file\",\"dataTargetType\" : "
                    + "\"file\"," + "\"sourcePath\" : \""+ getResourceAbsolutePath(resourcePath) + "\",\"dataType\""
                    + " : \"csv\"," + "\"comments\" : \"fcSample\",\"version\" : \"" + version + "\"}";
        }
        return doHttpPost(new URI(getServerUrlHttps() + "/api/datasets"), payload);
    }
    
    /**
     * Create a project
     * 
     * @param ProjectName   Name for the project
     * @return              response from the backend
     * @throws              ClientProtocolException
     * @throws              IOException
     * @throws              URISyntaxException
     * @throws              MLIntegrationBaseTestException 
     */
    protected CloseableHttpResponse createProject(String ProjectName, String datasetName) throws IOException, 
        ClientProtocolException, URISyntaxException, MLIntegrationBaseTestException {
        String payload;
        if (ProjectName == null) {
            payload = "{\"description\" : \"Test Project\",\"datasetName\": \"" + datasetName + "\"}";
        } else if (datasetName == null) {
            payload = "{\"name\" : \"" + ProjectName + "\",\"description\" : \"Test Project\"}";
        } else {
            payload = "{\"name\" : \"" + ProjectName + "\",\"description\" : \"Test Project\",\"datasetName\": \""
                    + datasetName + "\"}";
        }
        return doHttpPost(new URI(getServerUrlHttps() + "/api/projects"), payload);
    }
    
    /**
     * Create an Analysis
     * 
     * @param AnalysisName  Name for the Analysis
     * @return              response from the backend
     * @throws              ClientProtocolException
     * @throws              IOException
     * @throws              URISyntaxException
     * @throws              MLIntegrationBaseTestException 
     */
    protected CloseableHttpResponse createAnalysis(String AnalysisName, int ProjectId) throws IOException, 
        ClientProtocolException, URISyntaxException, MLIntegrationBaseTestException {
        String payload;
        if (AnalysisName == null) {
            payload = "{\"comments\":\"Test Analysis\",\"projectId\":" + ProjectId + "}";
        } else if (ProjectId == -1) {
            payload = "{\"name\":\"" + AnalysisName + "\",\"comments\":\"Test Analysis\"}";
        } else {
            payload = "{\"name\":\"" + AnalysisName + "\",\"comments\":\"Test Analysis\",\"projectId\":" + 
                    ProjectId + "}";
        }
        return doHttpPost(new URI(getServerUrlHttps() + "/api/analyses"), payload);
    }
    
    /**
     * Set feature defaults for an analysis
     * 
     * @param analysisId
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException
     */
    protected CloseableHttpResponse setFeartureDefaults(int analysisId) throws ClientProtocolException, IOException, 
            URISyntaxException, MLIntegrationBaseTestException {
        String payload ="{\"include\" : true,\"imputeOption\": \"DISCARD\"}";
        return doHttpPost(new URI(getServerUrlHttps() + "/api/analyses/" + analysisId + "/features/defaults"), payload);
    }
    
    /**
     * Set Model Configurations of an analysis
     * 
     * @param analysisId
     * @param configurations
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException
     */
    protected CloseableHttpResponse setModelConfiguration(int analysisId, Map<String,String> configurations) throws 
            ClientProtocolException, IOException, URISyntaxException, MLIntegrationBaseTestException {
        String payload ="[";
        for (Entry<String, String> property : configurations.entrySet()) {
            payload = payload + "{\"key\":\"" + property.getKey() + "\",\"value\":\"" + property.getValue() + "\"},";
        }
        payload = payload.substring(0, payload.length()-1) + "]";
        return doHttpPost(new URI(getServerUrlHttps() + "/api/analyses/" + analysisId + "/configurations"), payload);
    }
    
    /**
     * Get the ID of the project from the name
     * 
     * @param projectName
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException
     */
    protected int getProjectId(String projectName) throws ClientProtocolException, IOException, URISyntaxException, 
            MLIntegrationBaseTestException{
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/projects/" + projectName));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JSONObject responseJson = new JSONObject(bufferedReader.readLine());
        bufferedReader.close();
        response.close();
        return responseJson.getInt("id");
    }
    
    /**
     * Get the ID of an analysis from the name
     * 
     * @param analysisName
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException
     */
    protected int getAnalysisId(String analysisName) throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/analyses/" + analysisName));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JSONObject responseJson = new JSONObject(bufferedReader.readLine());
        bufferedReader.close();
        response.close();
        return responseJson.getInt("id");
    }
    
    /**
     * Get a ID of the first version-set of a dataset
     * 
     * @param datasetId
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException
     */
    protected int getAVersionSetIdOfDataset(int datasetId) throws ClientProtocolException, IOException, URISyntaxException,
            MLIntegrationBaseTestException {
        CloseableHttpResponse response = doHttpGet(new URI(getServerUrlHttps() + "/api/datasets/" + datasetId + "/versions"));
        // Get the Id of the first dataset
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JSONArray responseJson = new JSONArray(bufferedReader.readLine());
        JSONObject datsetVersionJson = (JSONObject) responseJson.get(0);
        return datsetVersionJson.getInt("id");
    }

    /**
     * Create a Model
     * 
     * @param name
     * @param analysisId
     * @param versionSetId
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     * @throws MLIntegrationBaseTestException
     */
    protected CloseableHttpResponse createModel(String name, int analysisId, int versionSetId) throws ClientProtocolException, 
            IOException, URISyntaxException, MLIntegrationBaseTestException {
        String payload ="{\"name\" : \"" + name + "\",\"analysisId\" :" + analysisId + ",\"versionSetId\" :" +
                versionSetId + "}";
        return doHttpPost(new URI(getServerUrlHttps() + "/api/models/"), payload);
    }
    
}