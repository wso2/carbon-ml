<%@page import="org.wso2.carbon.ml.dataset.xsd.FeatureType"%>
<%@page import="org.wso2.carbon.ml.dataset.xsd.ImputeOption"%>
<%@page import="org.wso2.carbon.ml.ui.helper.DatatableHelper"%>
<%@page import="org.wso2.carbon.ml.ui.helper.DatasetUploader"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ml.ui.helper.DatasetServiceClient"%>
<%@ page import="org.wso2.carbon.ml.dataset.xsd.Feature"%>

<%
	String serverURL = CarbonUIUtil.getServerURL(
			config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config
			.getServletContext().getAttribute(
					CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session
			.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	try {
		// create client
		DatasetServiceClient client = new DatasetServiceClient(
				configContext, serverURL, cookie);
		String datasetId = (String) session.getAttribute("datasetId");

		if (datasetId != null && datasetId.length() > 0) {
			boolean isSelection = Boolean.parseBoolean(request
					.getParameter("IS_FEATURE_SELECTED"));
			String featureName = request.getParameter("FEATURE_NAME");

			// update Dabase using the dataset service
			client.updateIsIncludedFeature(featureName, datasetId,
					isSelection);
		} else {
			//TODO: handle this error
		}

	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
				CarbonUIMessage.ERROR, request, e);
	}
%>