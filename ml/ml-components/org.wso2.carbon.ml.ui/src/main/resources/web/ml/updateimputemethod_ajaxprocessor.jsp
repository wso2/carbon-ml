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
		DatasetServiceClient client = new DatasetServiceClient(configContext, serverURL,
				cookie);
		Integer datasetId = (Integer) session.getAttribute("datasetId");
		if (datasetId != null && datasetId > 0) {
			String imputeOption = request.getParameter("IMPUTE_OPTION");
			String featureName = request.getParameter("FEATURE_NAME");
			client.updateImputeOption(featureName, datasetId,
					imputeOption);
		} else {
			//TODO: handle this error	
		}

	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
				CarbonUIMessage.ERROR, request, e);
	}
%>