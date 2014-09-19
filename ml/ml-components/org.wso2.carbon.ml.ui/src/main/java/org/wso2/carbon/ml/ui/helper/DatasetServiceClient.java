/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.ui.helper;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.dataset.stub.DatasetServiceStub;
import org.wso2.carbon.ml.dataset.xsd.Feature;



public class DatasetServiceClient {
	private static final Log LOGGER = LogFactory
			.getLog(DatasetServiceClient.class);

	private DatasetServiceStub stub;

	public DatasetServiceClient(ConfigurationContext configCtx,
			String backendServerURL, String cookie)
			throws DatasetServiceClientException {

		try {
			String serviceURL = backendServerURL + "DatasetService";
			stub = new DatasetServiceStub(configCtx, serviceURL);
			ServiceClient client = stub._getServiceClient();
			Options options = client.getOptions();
			options.setManageSession(true);
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
					cookie);
		} catch (AxisFault ex) {
			String msg = "An error has occurred while initilizing the DatasetServiceStub, error message: "
					+ ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatasetServiceClientException(msg);
		}
	}

	public int importDataset(String datasetName)
			throws DatasetServiceClientException {
		try {
			return stub.importData(datasetName);
		} catch (Exception ex) {
			String msg = "An error has occurred while calling importData() error message: "
					+ ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatasetServiceClientException(msg);
		}
	}

	public String getDatasetUploadingDir()
			throws DatasetServiceClientException {
		try {
			return stub.getDatasetUploadingDir();
		} catch (Exception ex) {
			String msg = "An error has occurred while calling getDatasetUploadingDir() error message: "
					+ ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatasetServiceClientException(msg);
		}
	}

	public int getDatasetInMemoryThreshold()
			throws DatasetServiceClientException {
		try {
			return stub.getDatasetInMemoryThreshold();
		} catch (RemoteException ex) {
			String msg = "An error has occurred while calling getDatasetInMemoryThreshold() error message: "
					+ ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatasetServiceClientException(msg);
		}
	}

	public long getDatasetUploadingLimit()
			throws DatasetServiceClientException {
		try {
			return stub.getDatasetUploadingLimit();
		} catch (RemoteException ex) {
			String msg = "An error has occurred while calling getDatasetUploadingLimit() error message: "
					+ ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatasetServiceClientException(msg);
		}
	}

	public Feature[] getFeatures(int start, int numberOfFeatures)
			throws DatasetServiceClientException {
		try {
			return stub.getFeatures(1, start, numberOfFeatures);
		} catch (Exception ex) {
			String msg = "An error has occurred while calling getFeatures() error message: "
					+ ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatasetServiceClientException(msg);
		}
	}
}
