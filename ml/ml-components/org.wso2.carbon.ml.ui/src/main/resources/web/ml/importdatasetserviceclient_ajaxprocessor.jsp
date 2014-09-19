<%@page import="org.wso2.carbon.ml.ui.helper.DatasetUploader"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ml.ui.helper.DatasetServiceClient"%>
<%@ page import="org.wso2.carbon.ml.ui.helper.DatasetServiceClient"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%
	String serverURL = CarbonUIUtil.getServerURL(
			config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config
			.getServletContext().getAttribute(
					CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session
			.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

	DatasetServiceClient client;
	DatasetUploader uploader;

	try {
		client = new DatasetServiceClient(configContext, serverURL,cookie);
		long uploadingLimit = client.getDatasetUploadingLimit();
		int memThreshold = client.getDatasetInMemoryThreshold();
		String uploadingDir = client.getDatasetUploadingDir();		
		
		uploader = new DatasetUploader(request, uploadingDir, memThreshold, uploadingLimit);
		boolean result = uploader.doUplod();
		
		if(result){ 
			// calling summary statistics calcution service
			int datasetId = client.importDataset(uploader.getDatasetName());
			session.setAttribute("datasetId", datasetId);			
		}else{
			// redirect to the error page
			//TODO: error message
		}
		

	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
				CarbonUIMessage.ERROR, request, e);
	}
%>