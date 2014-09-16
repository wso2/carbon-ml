<%@page import="org.wso2.carbon.ml.ui.helper.DatatableHelper"%>
<%@page import="org.wso2.carbon.ml.ui.helper.DatasetUploader"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ml.ui.helper.DatabaseServiceClient"%>
<%@ page import="org.wso2.carbon.ml.db.xsd.Feature" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
	
<%
	response.resetBuffer();
	String serverURL = CarbonUIUtil.getServerURL(
			config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config
			.getServletContext().getAttribute(
					CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session
			.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	
	DatabaseServiceClient client;
	
	try {
		client = new DatabaseServiceClient(configContext, serverURL,
				cookie);
		int start = Integer.parseInt(request.getParameter("iDisplayStart"));
		int len = Integer.parseInt(request.getParameter("iDisplayLength"));		
		Feature[] features = client.getFeatures(0,2);
		DatatableHelper datatableHelper = new DatatableHelper();
		datatableHelper.populateDatatable(response, request, features);
		
	
	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
				CarbonUIMessage.ERROR, request, e);
	}
%>

