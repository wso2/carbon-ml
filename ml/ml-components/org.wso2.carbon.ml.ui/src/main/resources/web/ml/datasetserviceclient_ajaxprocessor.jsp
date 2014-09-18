<%@page import="org.wso2.carbon.ml.ui.helper.DatatableHelper"%>
<%@page import="org.wso2.carbon.ml.ui.helper.DatasetUploader"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ml.ui.helper.DatasetServiceClient"%>
<%@ page import="org.wso2.carbon.ml.dataset.xsd.Feature" %>
	
<%	
	String serverURL = CarbonUIUtil.getServerURL(
			config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config
			.getServletContext().getAttribute(
					CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session
			.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	
	DatasetServiceClient client;
	
	try {
		client = new DatasetServiceClient(configContext, serverURL,
				cookie);
		int start = Integer.parseInt(request.getParameter("iDisplayStart"));
		int len = Integer.parseInt(request.getParameter("iDisplayLength"));		
		//TODO: remove hard-coded numbers
		Feature[] features = client.getFeatures(0,2);
		DatatableHelper datatableHelper = new DatatableHelper();
		
		datatableHelper.populateDatatable(response, request, features);		
	
	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
				CarbonUIMessage.ERROR, request, e);
	}
%>



