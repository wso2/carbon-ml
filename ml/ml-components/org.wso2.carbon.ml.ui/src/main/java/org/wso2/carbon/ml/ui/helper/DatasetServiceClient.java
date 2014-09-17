package org.wso2.carbon.ml.ui.helper;


import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.dataset.stub.DatasetServiceStub;

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
}
