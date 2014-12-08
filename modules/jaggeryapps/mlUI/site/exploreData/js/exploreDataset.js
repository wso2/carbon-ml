/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
$('document').ready(function() {
    $('#exploreData').addClass('top_Menu_button menuHiligher');
    
    disableWizardMenu();

    // redraw upon numerical features list 1, list 2 or list 3 selection
	$( ".numFeaturesDropdown1, .numFeaturesDropdown2, .catFeaturesDropdown" ).change(function() {
    	drawPlotsAjax();
	});

	// at the loading event also, drawing graphs
    drawPlotsAjax();
});

function disableWizardMenu() {
    var color='#848484	';
    $('#evaluate').css('color',color);
    $('#evaluate').removeAttr("href");
};

function drawPlotsAjax(){
    var numFeature1 = $(".numFeaturesDropdown1").val();
    var numFeature2 = $(".numFeaturesDropdown2").val();
    var catFeature = $(".catFeaturesDropdown").val();
                
    $.ajax({
        type: "POST",
        url: "./ajax/fetchSamplePoints.jag",
        data: {
            'numFeature1': numFeature1,
            'numFeature2': numFeature2,
            'catFeature': catFeature
        },
        success : function(data){
            drawScatterPlot(JSON.parse(data),"#scatter",numFeature1,numFeature2);
            $("#scatterPlotTitle").html(numFeature1+" Vs. "+numFeature2);
        }
    });

    $.ajax({
        type: "POST",
        url: "./ajax/fetchSummaryStatistics.jag",
        data: {
            'numFeature': numFeature1
        },
        success : function(data){
            var jsonObj  = JSON.parse(data);
            var summary = "Mean: "+jsonObj[0].mean+ "&emsp;&emsp;&emsp;  Median: "+jsonObj[0].median+ "&emsp;&emsp;&emsp; Std: "+jsonObj[0].std+ "&emsp;&emsp;&emsp; Skewness: "+jsonObj[0].skewness;
            $("#histogram1Title").html(numFeature1);
            $("#numFeature1Summary").html(summary);
            
            // transform dataset
            drawHistogram(jsonObj,"#histogram1");
        }
    });
    
    $.ajax({
        type: "POST",
        url: "./ajax/fetchSummaryStatistics.jag",
        data: {
            'numFeature': numFeature2
        },
        success : function(data){
            var jsonObj  = JSON.parse(data);
            var summary = "Mean: "+jsonObj[0].mean+ "&emsp;&emsp;&emsp; Median: "+jsonObj[0].median+ "&emsp;&emsp;&emsp; Std: "+jsonObj[0].std+ "&emsp;&emsp;&emsp; Skewness: "+jsonObj[0].skewness;
            $("#histogram2Title").html(numFeature2);
            $("#numFeature2Summary").html(summary);                 
            
            // transform dataset
            drawHistogram(jsonObj,"#histogram2");
        }
    });
}

// drawing a simple scatter graph
function drawScatterPlot(data,cssClass,xLabel,yLabel){
  $(cssClass).empty();
  var scatter = new ScatterPlot(data);
  
  scatter.setPlotingAreaWidth(600);
  scatter.setPlotingAreaHeight(350);
  scatter.setMarkerSize("2");
  scatter.setXAxisText(xLabel);
  scatter.setYAxisText(yLabel);
  scatter.plot(d3.select(cssClass));
};

function drawHistogram(data,divID){
    $(divID+' svg').empty();
    
    nv.addGraph(function() {
    
    var chart = nv.models.linePlusBarChart()
      	.margin({top: 30, right: 60, bottom: 50, left: 70})
      	.x(function(d,i) { return i })
      	.y(function(d) { return d[1] })
      	.color(["#C99614"])
      	;

    chart.xAxis
      	.showMaxMin(false)
      	.tickFormat(function(d) {
        	return data[0].values[d][0];
      	});

    chart.y1Axis
      .tickFormat(d3.format(',f'));

    chart.y2Axis
      .tickFormat(function(d) { return '$' + d3.format(',f')(d) });

    
    chart.bars.forceY([0]);

    d3.select(divID+' svg')
      .datum(data)
      .transition().duration(500)
      .call(chart);

    nv.utils.windowResize(chart.update);

    return chart;
});
}
