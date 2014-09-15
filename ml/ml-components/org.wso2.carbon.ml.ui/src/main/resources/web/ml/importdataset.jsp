<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ml.ui.helper.DatabaseServiceClient"%>
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

	DatabaseServiceClient client;

	try {
		client = new DatabaseServiceClient(configContext, serverURL,
				cookie);
		long uploadingLimit = client.getDatasetUploadingLimit();
		long memThreshold = client.getDatasetInMemoryThreshold();
		String uploadingDir = client.getDatasetUploadingDir();		
		

	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
				CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
	location.href = "../admin/error.jsp";
</script>
<%
	return;
	}
%>

<%
  String menuPath = "./includes/wizardmenu.jsp";  
%>
<div id="middle">
	<h2>WSO2 Machine Learner</h2>

	<div id="workArea">
		<jsp:include page="<%= menuPath%>"></jsp:include>

		<div id="importDialog">
			<form id="multiform" action="./importdatasetserviceclient.jsp" method="post"
				enctype="multipart/form-data">

				<input type="text" id="datasetName" /> <input type="file" size="60"
					name="uploadDataset" id="uploadDataset"> <input
					type="button" id="datasetOpen" value="Browse" /> <input
					type="submit" value="Import"><span id="validatorMsg"></span>
			</form>
		</div>
	</div>
</div>

<link rel="stylesheet" type="text/css" href="./css/mlmain.css">
<script src="./js/importdataset.js"></script>