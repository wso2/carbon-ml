<%@page import="org.wso2.carbon.ml.ui.helper.UIConstants"%>
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

	try {
		// creates service client
		DatasetServiceClient client = new DatasetServiceClient(
				configContext, serverURL, cookie);
		
		long uploadingLimit = client.getDatasetUploadingLimit();
		int memThreshold = client.getDatasetInMemoryThreshold();
		String uploadingDir = client.getDatasetUploadingDir();

		// creates uploader 
		DatasetUploader uploader = new DatasetUploader(request,
				uploadingDir, memThreshold, uploadingLimit);
		boolean result = uploader.doUplod();

		if (result) {
			// calling summary statistics calcution service
			String datasetId = client.importDataset(uploader
					.getDatasetName());
			session.setAttribute("datasetId", datasetId);
			session.setMaxInactiveInterval(UIConstants.MAX_SESSION_LIFE_TIME);
				//calling summary statistics calcution service
				int numOfFeatues = client.generateSummaryStatistics(
						datasetId,
						UIConstants.DATA_SAMPLE_SIZE_FOR_SUMMARY_STATS);
				session.setAttribute("numOfFeatues", numOfFeatues);
				session.setMaxInactiveInterval(UIConstants.MAX_SESSION_LIFE_TIME);
		} else {
			// redirect to the error page
			//TODO: error message
		}
	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
				CarbonUIMessage.ERROR, request, e);
	}
%>
