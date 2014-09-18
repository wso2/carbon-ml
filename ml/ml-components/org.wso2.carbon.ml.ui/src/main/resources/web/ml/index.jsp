<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%
  String menuPath = "./includes/wizardmenu.jsp";  
%>

<div id="middle">
	<h2>WSO2 Machine Learner</h2>

	<div id="workArea">
		<jsp:include
			page="<%= menuPath%>"></jsp:include>
	</div>
</div>

<link rel="stylesheet" type="text/css" href="./css/mlmain.css">
<script src="./js/home.js"></script> 