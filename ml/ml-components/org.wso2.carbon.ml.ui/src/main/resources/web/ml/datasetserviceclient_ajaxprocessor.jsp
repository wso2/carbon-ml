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
			int start = Integer.parseInt(request
					.getParameter("iDisplayStart"));
			int len = Integer.parseInt(request
					.getParameter("iDisplayLength"));			
			
			Integer datasetId = (Integer)session.getAttribute("datasetId");
			if( datasetId != null && datasetId.intValue() > 0){
				// numbers in DataTable starts with zero and in the DB its one
				Feature[] features = client.getFeatures(datasetId, start + 1,len);
				DatatableHelper datatableHelper = new DatatableHelper();
				
				int numOfFeatues = (Integer)session.getAttribute("numOfFeatues");
				datatableHelper.populateDatatable(response, request, features, numOfFeatues);
				
			}else{
				// no valid datasetId, report the error
				// TODO: error message
			}

		} catch (Exception e) {
			CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
					CarbonUIMessage.ERROR, request, e);
		}
	%>



