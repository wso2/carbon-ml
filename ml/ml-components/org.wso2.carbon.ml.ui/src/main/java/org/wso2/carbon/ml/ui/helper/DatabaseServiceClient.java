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
import org.wso2.carbon.ml.db.stub.DatabaseServiceStub;

/**
 * 
 */
public class DatabaseServiceClient {

	private static final Log LOGGER = LogFactory
			.getLog(DatabaseServiceClient.class);
	private DatabaseServiceStub stub;

	public DatabaseServiceClient(ConfigurationContext configCtx,
			String backendServerURL, String cookie)
			throws DatabaseServiceClientException {

		try {
			String serviceURL = backendServerURL + "DatabaseService";
			stub = new DatabaseServiceStub(configCtx, serviceURL);
			ServiceClient client = stub._getServiceClient();
			Options options = client.getOptions();
			options.setManageSession(true);
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
					cookie);
		} catch (AxisFault ex) {
			String msg = "An error has occurred while initilizing the DatabaseServiceStub, error message: "
					+ ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatabaseServiceClientException(msg);
		}
	}

	public String getDatasetUploadingDir()
			throws DatabaseServiceClientException {
		try {
			return stub.getDatasetUploadingDir();
		} catch (RemoteException ex) {
			String msg = "An error has occurred while calling getDatasetUploadingDir() error message: "
					+ ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatabaseServiceClientException(msg);
		}
	}

	public int getDatasetInMemoryThreshold()
			throws DatabaseServiceClientException {
		try {
			return stub.getDatasetInMemoryThreshold();
		} catch (RemoteException ex) {
			String msg = "An error has occurred while calling getDatasetInMemoryThreshold() error message: "
					+ ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatabaseServiceClientException(msg);
		}
	}

	public long getDatasetUploadingLimit()
			throws DatabaseServiceClientException {
		try {
			return stub.getDatasetUploadingLimit();
		} catch (RemoteException ex) {
			String msg = "An error has occurred while calling getDatasetUploadingLimit() error message: "
					+ ex.getMessage();
			LOGGER.error(msg, ex);
			throw new DatabaseServiceClientException(msg);
		}
	}
}
