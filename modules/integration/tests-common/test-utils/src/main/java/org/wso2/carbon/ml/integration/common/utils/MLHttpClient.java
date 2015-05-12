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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.engine.context.beans.Instance;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * This is a http client to call ML services through the REST API.
 */
public class MLHttpClient {
    
    private User userInfo;
    private Instance mlInstance;
    private static final Log logger = LogFactory.getLog(MLHttpClient.class);
    
    public MLHttpClient(Instance mlInstance, User userInfo) {
        this.mlInstance = mlInstance;
        this.userInfo = userInfo;
    }
    
    
    /**
     * Get the secured URL of the ml Server.
     * 
     * @return  Secured URL of the service.
     * @throws  MLIntegrationBaseTestException
     */
    protected String getServerUrlHttps() {
        String protocol = ContextXpathConstants.PRODUCT_GROUP_PORT_HTTPS;
        String host = UrlGenerationUtil.getWorkerHost(mlInstance);
        //Get port
        String port = null;
        boolean isNonBlockingEnabled = mlInstance.isNonBlockingTransportEnabled();
        if(isNonBlockingEnabled) {
            port = mlInstance.getPorts().get(ContextXpathConstants.PRODUCT_GROUP_PORT_NHTTPS);
        } else {
            port = mlInstance.getPorts().get(ContextXpathConstants.PRODUCT_GROUP_PORT_HTTPS);
        }
        return (protocol + "://"+ host + ":" + port);
    }
    
    /**
     * Get the Server URL.
     * 
     * @return  Non-secured URL of the service.
     * @throws  MLIntegrationBaseTestException
     */
    protected String getServerUrlHttp() {
        String protocol = ContextXpathConstants.PRODUCT_GROUP_PORT_HTTP;
        String host = UrlGenerationUtil.getWorkerHost(mlInstance);
        //Get port
        String port = null;
        boolean isNonBlockingEnabled = mlInstance.isNonBlockingTransportEnabled();
        if(isNonBlockingEnabled) {
            port = mlInstance.getPorts().get(ContextXpathConstants.PRODUCT_GROUP_PORT_NHTTP);
        } else {
            port = mlInstance.getPorts().get(ContextXpathConstants.PRODUCT_GROUP_PORT_HTTP);
        }
        return (protocol + "://"+ host + ":" + port);
    }
    
    
    /**
     * Send a HTTP GET request to the given URI and return the response.
     * 
     * @param uri   End-point URI
     * @return      Response from the endpoint
     * @throws      MLHttpClientException 
     */
    public CloseableHttpResponse doHttpGet(String resourcePath) throws MLHttpClientException {
        CloseableHttpClient httpClient =  HttpClients.createDefault();
        HttpGet get = null;
        try {
            get = new HttpGet(getServerUrlHttps() + resourcePath);
            get.setHeader(MLIntegrationTestConstants.CONTENT_TYPE, MLIntegrationTestConstants.CONTENT_TYPE_APPLICATION_JSON);
            get.setHeader(MLIntegrationTestConstants.AUTHORIZATION_HEADER, getBasicAuthKey());
            return httpClient.execute(get);
        } catch (ClientProtocolException e) {
            throw new MLHttpClientException("Failed to get " + resourcePath, e);
        } catch (IOException e) {
            throw new MLHttpClientException("Failed to get " + resourcePath, e);
        }
    	
    }
    
    /**
     * Send a HTTP GET request to the given URI and return the response.
     * 
     * @param uri               End-point URI
     * @param parametersJson    Payload JSON string
     * @return                  Response from the endpoint
     * @throws                  MLHttpClientException 
     */
    public CloseableHttpResponse doHttpPost(String resourcePath, String parametersJson) throws MLHttpClientException {
    	try {
    	    CloseableHttpClient httpClient =  HttpClients.createDefault();
            HttpPost post = new HttpPost(getServerUrlHttps() + resourcePath);
            post.setHeader(MLIntegrationTestConstants.CONTENT_TYPE, MLIntegrationTestConstants.CONTENT_TYPE_APPLICATION_JSON);
            post.setHeader(MLIntegrationTestConstants.AUTHORIZATION_HEADER, getBasicAuthKey());
            if(parametersJson != null) {
                StringEntity params = new StringEntity(parametersJson);
                post.setEntity(params);
            }
            return httpClient.execute(post);
        } catch (ClientProtocolException e) {
            throw new MLHttpClientException("Failed to post to " + resourcePath, e);
        } catch (IOException e) {
            throw new MLHttpClientException("Failed to post to " + resourcePath, e);
        }
    }
    
    /**
     * 
     * @param uri   End-point URI
     * @return      Response from the endpoint
     * @throws      MLHttpClientException 
     */
    public CloseableHttpResponse doHttpDelete(String resourcePath) throws MLHttpClientException {
        CloseableHttpClient httpClient =  HttpClients.createDefault();
        HttpDelete delete;
        try {
            delete = new HttpDelete(getServerUrlHttps() + resourcePath);
            delete.setHeader(MLIntegrationTestConstants.CONTENT_TYPE, MLIntegrationTestConstants.CONTENT_TYPE_APPLICATION_JSON);
            delete.setHeader(MLIntegrationTestConstants.AUTHORIZATION_HEADER, getBasicAuthKey());
            return httpClient.execute(delete);
        } catch (ClientProtocolException e) {
            throw new MLHttpClientException("Failed to delete " + resourcePath, e);
        } catch (IOException e) {
            throw new MLHttpClientException("Failed to delete " + resourcePath, e);
        }
    }
    
    /**
     * Get the Encoded Key for Basic auth header
     * 
     * @return  Encoded Key Basic auth Key
     */
    public String getBasicAuthKey() {
        String token = this.userInfo.getUserName() + ":" + userInfo.getPassword();
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
     * @throws              MLHttpClientException 
     */
    public CloseableHttpResponse uploadDatasetFromCSV(String DatasetName, String version, String resourcePath)
            throws MLHttpClientException {
        CloseableHttpClient httpClient =  HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(getServerUrlHttps() + "/api/datasets/");
            httpPost.setHeader(MLIntegrationTestConstants.AUTHORIZATION_HEADER, getBasicAuthKey());

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addPart("description", new StringBody("Sample dataset for Testing", ContentType.TEXT_PLAIN));
            multipartEntityBuilder.addPart("sourceType", new StringBody("file", ContentType.TEXT_PLAIN));
            multipartEntityBuilder.addPart("destination", new StringBody("file", ContentType.TEXT_PLAIN));
            multipartEntityBuilder.addPart("dataFormat", new StringBody("CSV", ContentType.TEXT_PLAIN));
            multipartEntityBuilder.addPart("containsHeader", new StringBody("true", ContentType.TEXT_PLAIN));

            if (DatasetName != null) {
                multipartEntityBuilder.addPart("datasetName", new StringBody(DatasetName, ContentType.TEXT_PLAIN));
            }
            if (version != null) {
                multipartEntityBuilder.addPart("version", new StringBody(version, ContentType.TEXT_PLAIN));
            }
            if (resourcePath != null) {
                File file = new File(getResourceAbsolutePath(resourcePath));
                multipartEntityBuilder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, "IndiansDiabetes.csv");
            }
            httpPost.setEntity(multipartEntityBuilder.build());
            return httpClient.execute(httpPost);
        } catch (ClientProtocolException e) {
            throw new MLHttpClientException("Failed to upload dataset from csv " + resourcePath, e);
        } catch (IOException e) {
            throw new MLHttpClientException("Failed to upload dataset from csv " + resourcePath, e);
        }
    }
    
    /**
     * Create a project
     * 
     * @param ProjectName   Name for the project
     * @return              response from the backend
     * @throws              MLHttpClientException 
     */
    public CloseableHttpResponse createProject(String ProjectName, String datasetName) throws MLHttpClientException {
        try {
            String payload;
            if (ProjectName == null) {
                payload = "{\"description\" : \"Test Project\",\"datasetName\": \"" + datasetName + "\"}";
            } else if (datasetName == null) {
                payload = "{\"name\" : \"" + ProjectName + "\",\"description\" : \"Test Project\"}";
            } else {
                payload = "{\"name\" : \"" + ProjectName + "\",\"description\" : \"Test Project\",\"datasetName\": \""
                        + datasetName + "\"}";
            }
            return doHttpPost("/api/projects", payload);
        } catch (MLHttpClientException e) {
            throw new MLHttpClientException("Failed to create project " + ProjectName, e);
        }
    }
    
    /**
     * Create an Analysis
     * 
     * @param AnalysisName  Name for the Analysis
     * @return              response from the backend
     * @throws              MLHttpClientException 
     */
    public CloseableHttpResponse createAnalysis(String AnalysisName, int ProjectId) throws MLHttpClientException {
        try {
            String payload;
            if (AnalysisName == null) {
                payload = "{\"comments\":\"Test Analysis\",\"projectId\":" + ProjectId + "}";
            } else if (ProjectId == -1) {
                payload = "{\"name\":\"" + AnalysisName + "\",\"comments\":\"Test Analysis\"}";
            } else {
                payload = "{\"name\":\"" + AnalysisName + "\",\"comments\":\"Test Analysis\",\"projectId\":" + ProjectId
                        + "}";
            }
            return doHttpPost("/api/analyses", payload);
        } catch (MLHttpClientException e) {
            throw new MLHttpClientException("Failed to create analysis: " + AnalysisName + " in project: " + ProjectId, e);
        }
    }
    
    /**
     * Set feature defaults for an analysis.
     * 
     * @param analysisId    ID of the analysis
     * @return              Response from the back-end
     * @throws              MLHttpClientException 
     */
    public CloseableHttpResponse setFeartureDefaults(int analysisId) throws MLHttpClientException {
        String payload ="{\"include\" : true,\"imputeOption\": \"DISCARD\"}";
        try {
            return doHttpPost("/api/analyses/" + analysisId + "/features/defaults", payload);
        } catch (MLHttpClientException e) {
            throw new MLHttpClientException("Failed to set Feature defaults to analysis: " + analysisId, e);
        }
    }
    
    /**
     * Set Model Configurations of an analysis
     * 
     * @param analysisId        ID of the analysis
     * @param configurations    Map of configurations
     * @return                  Response from the back-end
     * @throws                  MLHttpClientException 
     */
    public CloseableHttpResponse setModelConfiguration(int analysisId, Map<String,String> configurations) 
            throws MLHttpClientException {
        try {
            String payload ="[";
            for (Entry<String, String> property : configurations.entrySet()) {
                payload = payload + "{\"key\":\"" + property.getKey() + "\",\"value\":\"" + property.getValue() + "\"},";
            }
            payload = payload.substring(0, payload.length()-1) + "]";
            return doHttpPost("/api/analyses/" + analysisId + "/configurations", payload);
        } catch (MLHttpClientException e) {
            throw new MLHttpClientException("Failed to set model configurations to analysis: " + analysisId, e);
        }
    }
    
    /**
     * Get the ID of the project from the name
     * 
     * @param projectName   Name of the project
     * @return              ID of the project
     * @throws              MLHttpClientException 
     */
    public int getProjectId(String projectName) throws MLHttpClientException {
        CloseableHttpResponse response;
        try {
            response = doHttpGet("/api/projects/" + projectName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            JSONObject responseJson = new JSONObject(bufferedReader.readLine());
            bufferedReader.close();
            response.close();
            return responseJson.getInt("id");
        } catch (Exception e) {
            throw new MLHttpClientException("Failed to get ID of project: " + projectName, e);
        }
    }
    
    /**
     * Get the ID of an analysis from the name
     * 
     * @param analysisName  Name of the analysis
     * @return              ID of the analysis
     * @throws              MLHttpClientException 
     */
    public int getAnalysisId(String analysisName) throws MLHttpClientException {
        CloseableHttpResponse response;
        try {
            response = doHttpGet("/api/analyses/" + analysisName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            JSONObject responseJson = new JSONObject(bufferedReader.readLine());
            bufferedReader.close();
            response.close();
            return responseJson.getInt("id");
        } catch (Exception e) {
            throw new MLHttpClientException("Failed to get ID of analysis: " + analysisName, e);
        }
    }
    
    /**
     * Get a ID of the first version-set of a dataset
     * 
     * @param datasetId ID of the dataset
     * @return          ID of the first versionset of the dataset
     * @throws          ClientProtocolException
     * @throws          MLHttpClientException 
     */
    public int getAVersionSetIdOfDataset(int datasetId) throws MLHttpClientException {
        CloseableHttpResponse response;
        try {
            response = doHttpGet("/api/datasets/" + datasetId + "/versions");
            // Get the Id of the first dataset
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            JSONArray responseJson = new JSONArray(bufferedReader.readLine());
            JSONObject datsetVersionJson = (JSONObject) responseJson.get(0);
            return datsetVersionJson.getInt("id");
        } catch (Exception e) {
            throw new MLHttpClientException("Failed to get a version set ID of dataset: " + datasetId, e);
        }
    }

    /**
     * Create a Model
     * 
     * @param name          Name of the model
     * @param analysisId    ID of the  analysis associated with the model
     * @param versionSetId  ID of the version set to be used for the model
     * @return              Response from the back-end
     * @throws              MLHttpClientException 
     */
    public CloseableHttpResponse createModel(int analysisId, int versionSetId) throws MLHttpClientException {
        try {
            String payload ="{\"analysisId\" :" + analysisId + ",\"versionSetId\" :" +
                    versionSetId + "}";
            return doHttpPost("/api/models/", payload);
        } catch (MLHttpClientException e) {
            throw new MLHttpClientException("Failed to create a model in analysis: " + analysisId + "using versionset: "
                    + versionSetId, e);
        }
    }
    
    /**
     * Get the model ID using the name of the model
     * 
     * @param modelName Name of the model
     * @return          ID of the model
     * @throws          MLHttpClientException 
     */
    public int getModelId(String modelName) throws MLHttpClientException {
        CloseableHttpResponse response;
        try {
            response = doHttpGet("/api/models/" + modelName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            JSONObject responseJson = new JSONObject(bufferedReader.readLine());
            bufferedReader.close();
            response.close();
            return responseJson.getInt("id");
        } catch (Exception e) {
            throw new MLHttpClientException("Failed to get a version set ID of model: " + modelName, e);
        }
    }
    
    /**
     * Create the file storage for a model
     * 
     * @param modelId       ID of the model
     * @param folderName    Name of the directory/sub-directory
     * @return              Response from the back-end
     * @throws              MLHttpClientException 
     */
    public CloseableHttpResponse createFileModelStorage(int modelId, String folderName) throws MLHttpClientException {
        String payload ="{\"type\":\"file\",\"location\":\"" + folderName + "\"}";
        try {
            return doHttpPost("/api/models/"+ modelId + "/storages", payload);
        } catch (MLHttpClientException e) {
            throw new MLHttpClientException("Failed to file storage for model: " + modelId, e);
        }
    }
    
    
    /**
     * Retrieves the absolute path of a test resource.
     * 
     * @param resourceRelativePath  Relative path of the test resource.
     * @return                      Absolute path of the test resource
     */
    public String getResourceAbsolutePath(String resourceRelativePath) {
        return FrameworkPathUtil.getSystemResourceLocation() + resourceRelativePath;
    }

    /**
     * Extract the model name from the response
     * @param response
     * @return
     * @throws MLHttpClientException
     */
    public String getModelName(CloseableHttpResponse response) throws MLHttpClientException {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            JSONObject responseJson = new JSONObject(bufferedReader.readLine());
            bufferedReader.close();
            response.close();
            return responseJson.getString("name");
        } catch (Exception e) {
            throw new MLHttpClientException("Failed to get the name of model" , e);
        }
    }
}