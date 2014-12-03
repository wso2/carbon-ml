
$('document').ready(function() {
    $('#exploreData').addClass('top_Menu_button menuHiligher');
    disableWizardMenu();
});


function disableWizardMenu() {
    var color='#848484';
    $('#evaluate').css('color',color);
    $('#evaluate').removeAttr("href");
};

// redraw upon numerical features list 1 selection
$( ".numFeaturesDropdown1" ).change(function() {
    drawPlotsAjax();
});

// redraw upon numerical features list 2 selection
$( ".numFeaturesDropdown2" ).change(function() {
    drawPlotsAjax();
});

// redraw upon categorical features list selection
$( ".catFeaturesDropdown" ).change(function() {
    drawPlotsAjax();
});

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
            $("#scatterPlotTitle").empty();
            $("#scatterPlotTitle").append(numFeature1+" Vs. "+numFeature2);
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
            var summary = "Mean: "+jsonObj[0].mean+ "&emsp;&emsp;&emsp; Median: "+jsonObj[0].median+ "&emsp;&emsp;&emsp; Std: "+jsonObj[0].std+ "&emsp;&emsp;&emsp; Skewness: "+jsonObj[0].skewness;
            $("#histogram1Title").empty();
            $("#histogram1Title").append(numFeature1);
            $("#numFeature1Summary").empty();
            $("#numFeature1Summary").append(summary);
            var frequencies = jsonObj.frequencies;
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
            $("#histogram2Title").empty();
            $("#histogram2Title").append(numFeature2);
            $("#numFeature2Summary").empty();
            $("#numFeature2Summary").append(summary);
            var frequencies = jsonObj.frequencies;       
            // transform dataset
            drawHistogram(jsonObj,"#histogram2");
        }
    });

}

// drawing a simple scatter graph
function drawScatterPlot(data,cssClass,xLabel,yLabel){
    var sg = scatterPlot()
        .data(data)
        .width(600)
        .height(300)
        .markerSize(2)
        .xLabel(xLabel)
        .yLabel(yLabel)
        .exploreTwoFeatures(true); 
    $(cssClass).empty(); 
    d3.select(cssClass).call(sg);
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