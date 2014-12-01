
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
            var summary = "Mean: "+jsonObj.mean+ "&emsp;&emsp;&emsp; Median: "+jsonObj.median+ "&emsp;&emsp;&emsp; Std: "+jsonObj.std+ "&emsp;&emsp;&emsp; Skewness: "+jsonObj.skewness;
            $("#histogram1Title").empty();
            $("#histogram1Title").append(numFeature1);
            $("#numFeature1Summary").empty();
            $("#numFeature1Summary").append(summary);
            var frequencies = jsonObj.frequencies;       
            // transform dataset
            var data = $.map(frequencies, function(value, index) {
                return [value.frequency];
            });
            var bucketNames = $.map(frequencies, function(value, index) {
                return [value.range];
            });
            drawHistogram(data,bucketNames,"#histogram1");
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
            var summary = "Mean: " + jsonObj.mean + "&emsp;&emsp;&emsp; Median: " + jsonObj.median + "&emsp;&emsp;&emsp; Std: " + jsonObj.std + "&emsp;&emsp;&emsp; Skewness: " + jsonObj.skewness;
            $("#histogram2Title").empty();
            $("#histogram2Title").append(numFeature2);
            $("#numFeature2Summary").empty();
            $("#numFeature2Summary").append(summary);
            var frequencies = jsonObj.frequencies;       
            // transform dataset
            var data = $.map(frequencies, function(value, index) {
                return [value.frequency];
            });
            var bucketNames = $.map(frequencies, function(value, index) {
                return [value.range];
            });
            drawHistogram(data,bucketNames,"#histogram2");
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

function drawHistogram(data,bucketNames,cssClass){
    var hg = histogramUsingCalculatedFrequencies()
        .data(data)
        .bucketNames(bucketNames)
        .width(600)
        .height(125)
        .yLabel('Frequencies')
        .displayAxises(true);
    $(cssClass).empty(); 
    d3.select(cssClass).call(hg);
}