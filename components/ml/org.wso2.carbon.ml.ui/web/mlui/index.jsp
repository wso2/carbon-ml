<!--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

<%@ page import="java.lang.*"%>
<script type="text/javascript">
    jQuery(document).ready(function(){
        var origin = window.location.origin;
        var mlDisabled = "<%= System.getProperty("disableMl") %>";
        if (mlDisabled == "false") {
        	var url = "<%= System.getProperty("ml.ui.url") %>";
            var mlui = window.open(url, '_blank');
            window.location.href = origin;
            mlui.focus();
        } else {
        	window.location.href = origin;
        }
  });
</script>