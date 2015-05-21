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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.core.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.utils.MLConstants;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

/**
 * Read data from WSO2 BAM.
 */
public class BAMInputAdapter implements MLInputAdapter {

    private CloseableHttpClient httpClient = null;
    private CloseableHttpResponse response = null;
    private String [] uriResourceParameters;
    
    /**
     * Read a data set from a table in BAM
     * 
     * @param tableResourcePath     Resource path of the table. Takes the format: {tableName}/{from}/{to}/{start}/{count}
     *                              where {tableName}   Name of the table from which, the records are retrieved
                                          {from}        The starting time to retrieve records from (optional)
                                          {to}          The ending time to get records to (optional)
                                          {start}       The paginated index from value (optional)
                                          {count}       The paginated records count to be read (optional)
                                    pass -1 for optional fields if they are not used.
     * @return                      the given data-set as an {@link InputStream}
     * @throws                      MLInputAdapterException
     */
    public InputStream readDataset(String tableResourcePath) throws MLInputAdapterException {
        String tableUri = MLCoreServiceValueHolder.getInstance().getBamServerUrl() + "/analytics/tables/" + 
                tableResourcePath;
        try {
            URI uri = new URI(tableUri);
            return read(uri);
        } catch (URISyntaxException e) {
            throw new MLInputAdapterException("Invalid URI Syntax: " + tableUri + " : " + e.getMessage(), e);
        }
    }
    
    @Override
    public InputStream read(URI uri) throws MLInputAdapterException {
        
        InputStream stream = IOUtils.toInputStream("");
        return stream;
//        try {
//            if (isValidTable(uri)) {
//                int sampleSize = MLCoreServiceValueHolder.getInstance().getSummaryStatSettings().getSampleSize();
//                // retrieve the data from BAM
//                HttpGet get = new HttpGet(getUriWithSampleSize(uri, sampleSize));
//                response = httpClient.execute(get);
//                // convert the json response to csv format
//                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//                JSONArray outputJson = new JSONArray();
//                String line;
//                while ((line = br.readLine()) != null) {
//                    JSONArray inputJson = new JSONArray(line);
//                    for (int i = 0; i < inputJson.length(); i++) {
//                        outputJson.put(inputJson.getJSONObject(i).getJSONObject(MLConstants.BAM_DATA_VALUES));
//                    }
//                }
//                // Check whether BAM Table contains any data
//                if (outputJson.length() == 0) {
//                    throw new MLInputAdapterException("No data available at: " + uri);
//                }
//                // create a inputstream from the csv.
//                return IOUtils.toInputStream(CDL.toString(outputJson), MLConstants.UTF_8);
//            } else {
//                throw new MLInputAdapterException("Invalid Data source : " + uri);
//            }
//        } catch (ClientProtocolException e) {
//            throw new MLInputAdapterException("Failed to read the dataset from uri " + uri + " : " + e.getMessage(), e);
//        } catch (IOException e) {
//            throw new MLInputAdapterException("Failed to read the dataset from uri " + uri + " : " + e.getMessage(), e);
//        } catch (URISyntaxException e) {
//            throw new MLInputAdapterException("Failed to read the dataset from uri " + uri + " : " + e.getMessage(), e);
//        } catch (JSONException e) {
//            throw new MLInputAdapterException("Failed to read the dataset from uri " + uri + " : " + e.getMessage(), e);        } finally{
//            if(httpClient != null){ 
//                try {
//                    httpClient.close();
//                } catch (IOException e) {
//                    throw new MLInputAdapterException("Failed to close the http client:" + e.getMessage(), e);
//                }
//            }
//        }
    }
    
    /**
     * Checks whether the data source uri refers to a valid table in BAM
     * 
     * @param uri   Data Source URI to be validated.
     * @return      Boolean indicating validity
     * @throws      MLInputAdapterException
     * @throws      ClientProtocolException
     * @throws      IOException
     */
    private boolean isValidTable(URI uri) throws MLInputAdapterException, ClientProtocolException, IOException {
        httpClient = HttpClients.createDefault();
        uriResourceParameters = uri.normalize().getPath().replaceFirst("/analytics/tables", "").replaceAll("^/", "")
                .replaceAll("/$", "").split("/");
        // if BAM table name is not empty, return false.
        if (uriResourceParameters[0].isEmpty()) {
            return false;
        }
        // check whether the BAM table exists.
        try {
            URI tableCheckUri = new URI(uri.getScheme() + "://" + uri.getRawAuthority() + 
                "/analytics/table_exists?tableName=" + uriResourceParameters[0]);
            HttpGet get = new HttpGet(tableCheckUri);
            CloseableHttpResponse response = httpClient.execute(get);
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            JSONObject outputJson = new JSONObject(br.readLine());
            return "success".equalsIgnoreCase(outputJson.get("status").toString());
        } catch (URISyntaxException e) {
            throw new MLInputAdapterException("Invalid Uri Syntax:" + e.getMessage(), e);
        } catch (JSONException e) {
            throw new MLInputAdapterException("Error while reading response:" + e.getMessage(), e);
        }
    }
    
    /**
     * Set the size of the sample to be retrieved from BAM, and return the new URI.
     * 
     * @param uri           URI of the BAM Data Source
     * @param sampleSize    Size of the sample needed.
     * @return              New URI having the sample size set.
     * @throws              URISyntaxException
     * @throws              MLInputAdapterException
     */
    private URI getUriWithSampleSize(URI uri, int sampleSize) throws URISyntaxException, MLInputAdapterException {
        if (uriResourceParameters.length == 1) {
            uri = new URI(uri.toString() + "/-1/-1/0/" + sampleSize);
        } else if (uriResourceParameters.length == 2) {
            uri = new URI(uri.toString() + "/-1/0/" + sampleSize);
        } else if (uriResourceParameters.length == 3) {
            uri = new URI(uri.toString() + "/0/" + sampleSize);
        } else if (uriResourceParameters.length == 4) {
            uri = new URI(uri.toString() + "/" + sampleSize);
        } else if (uriResourceParameters.length == 5) {
            uri = new URI(uri.getScheme() + "://" + uri.getAuthority() + "/analytics/tables/" +uriResourceParameters[0]
                    + "/" + uriResourceParameters[1] + "/" + uriResourceParameters[2] + "/" + uriResourceParameters[3]
                    + "/" + sampleSize);
        } else {
            throw new MLInputAdapterException("Invalid data source URI: " + uri);
        }
        return uri.normalize();
    }
}
