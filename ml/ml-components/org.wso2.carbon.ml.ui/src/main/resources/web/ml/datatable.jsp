<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	
<link rel="stylesheet" type="text/css" href="./css/mlmain.css">
<link rel="stylesheet" type="text/css" href="./css/jquery.dataTables.css">
<script src="./js/jquery.js"></script>
<script src="./js/jquery.dataTables.js"></script>

<%
	response.resetBuffer();
%>

<div id="changeSampleButtons">
	<a href=# > <button class="greenButton">Random Sample</button></a>
	<a href=# > <button class="greenButton">Full Data Set</button></a>
</div>

<table id="datasetTable" class="display">
	<thead>
		<tr>
			<th>Feature</th>
			<th>Input</th>
			<th>Type</th>
			<th>Summary Statistics</th>
			<th>Impute</th>
		</tr>
	</thead>
	<tbody>
	</tbody>
</table>

<div class="bottomNavigationButtons">
	<a href=# > <button class="blueButton">Explore Data</button></a>
	<a href=# > <button class="blueButton">Build Model</button></a>
</div>

<script type="text/javascript">
    	$('document').ready(function() {    	
	    $('#datasetTable').dataTable({
	        "bServerSide": true,
	        "sAjaxSource": "datasetserviceclient_ajaxprocessor.jsp",
	        "bProcessing": false,
	        "bLengthChange": false,
	        "bFilter": false,
	    });

	    $('#datasetTable').on('draw.dt', function() {	        

	        $('.summaryStatistics').each(function() {
	            var json = $(this).text();
	            json = JSON.parse(json);
	            $(this).text("");
	            console.log(json["graph"]["type"]);
	            var svg = d3.select(this).append("svg").attr("width", 200).attr("height", 50);

	            var circle = svg.append("circle").
	            			attr("cx", 30).
	            			attr("cy", 30).
	            			attr("r", 20).
	            			style("fill","purple");
	            var rectangle = svg.append("rect")
	                              .attr("x", 60)
	                              .attr("y", 10)
	                              .attr("width", 300)
	                              .attr("height", 50)
	                              .style("fill","purple");
	        });
	        
            // TODO: AJAX call per change in the data-table is an overhead
            // findout a better approach
	        $('.fieldType').on('change', function(e) {
	            var closestTr = $(this).closest('tr');
	            var selectedRow = closestTr.find('.feature').text();
	            var selectedDataType = this.options[e.target.selectedIndex].text;

	            $.ajax({
	                type: "POST",
	                url: "/machinelearner/DatatableBackendService",
	                data: {
	                    'FIELD_PROP_NAME': 'selectedDataType',
	                    'FIELD_PROP_VALUE': selectedDataType,
	                    'FIELD_NAME': selectedRow
	                }
	            });
	        });

	        $('.includeFeature').on('change', function(e) {
	            var closestTr = $(this).closest('tr');
	            var selectedRow = closestTr.find('.feature').text();
	            var selectedFlag = 'false';

	            if (this.checked) {
	                selectionFlag = 'true';
	            }

	            $.ajax({
	                type: "POST",
	                url: "/machinelearner/DatatableBackendService",
	                data: {
	                    'FIELD_PROP_NAME': 'isSelected',
	                    'FIELD_PROP_VALUE': selectionFlag,
	                    'FIELD_NAME': selectedRow
	                }
	            });
	        });

	        $('.imputeMethod').on('change', function(e) {
	            var closestTr = $(this).closest('tr');
	            var selectedRow = closestTr.find('.feature').text();
	            var imputedMethod = this.options[e.target.selectedIndex].text;

	            $.ajax({
	                type: "POST",
	                url: "/machinelearner/DatatableBackendService",
	                data: {
	                    'FIELD_PROP_NAME': 'fieldImputeMethod',
	                    'FIELD_PROP_VALUE': imputedMethod,
	                    'FIELD_NAME': selectedRow
	                }
	            });
	        });


	    });


	});
    </script>
