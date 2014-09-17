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
  String menuPath = "./includes/wizardmenu.jsp";  
%>
<div id="middle">
	<h2>WSO2 Machine Learner</h2>

	<div id="workArea">
		<jsp:include page="<%= menuPath%>"></jsp:include>

		<div id="importDialog">
			<form id="multiform" action="./importdatasetserviceclient_ajaxprocessor.jsp" method="post"
				enctype="multipart/form-data">

				<input type="text" id="datasetName" name="datasetName"/> <input type="file" size="60"
					name="uploadDataset" id="uploadDataset"> <input
					type="button" id="datasetOpen" value="Browse" /> <input
					type="submit" value="Import"><span id="validatorMsg"></span>
			</form>
		</div>
		<div id="dataTable"></div>
	</div>
</div>

<link rel="stylesheet" type="text/css" href="./css/mlmain.css">
<script src="./js/importdataset.js"></script>