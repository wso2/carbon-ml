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
$('document').ready(function () {
    $('#exploreData').addClass('top_Menu_button menuHiligher');

    disableWizardMenu();

    // redraw upon feature list selections
    $(".numFeaturesIndependentDropdown, .numFeaturesDependentDropdown, .catFeaturesDropdown").change(function () {
        drawPlotsAjax();
    });

    // draw plots at the loading event
    drawPlotsAjax();
});

function disableWizardMenu() {
    var color = '#848484	';
    $('#evaluate').css('color', color);
    $('#evaluate').removeAttr("href");
};

function drawPlotsAjax() {
    var numFeatureIndependent = $(".numFeaturesIndependentDropdown").val();
    var numFeatureDependent = $(".numFeaturesDependentDropdown").val();
    var catFeature = $(".catFeaturesDropdown").val();

    $.ajax({
               type: "POST",
               url: "./ajax/fetchSamplePoints.jag",
               data: {
                   'numFeatureIndependent': numFeatureIndependent,
                   'numFeatureDependent': numFeatureDependent,
                   'catFeature': catFeature
               },
               success: function (data) {
                   drawScatterPlot(JSON.parse(data), "#scatter", numFeatureIndependent, numFeatureDependent);
                   $("#scatterPlotTitle").html(numFeatureIndependent + " Vs. " + numFeatureDependent);
               },
               error: function () {
                   var message = "An error occurred while fetching sample points";
                   //TODO: handle this with new UI...
               }
           });

    $.ajax({
               type: "POST",
               url: "./ajax/fetchSummaryStatistics.jag",
               data: {
                   'numFeature': numFeatureIndependent
               },
               success: function (data) {
                   var jsonObj = JSON.parse(data);
                   var summary = "Mean: " + jsonObj[0].mean + "&emsp;&emsp;&emsp;  Median: " + jsonObj[0].median + "&emsp;&emsp;&emsp; Std: " + jsonObj[0].std + "&emsp;&emsp;&emsp; Skewness: " + jsonObj[0].skewness;
                   $("#histogramIndependentTitle").html(numFeatureIndependent);
                   $("#numFeatureIndependentSummary").html(summary);

                   // transform dataset
                   drawHistogram(jsonObj, "#histogramIndependent");
               },
               error: function () {
                   var message = "An error occurred while fetching summary statistics";
                   //TODO: handle this with new UI...
               }
           });

    $.ajax({
               type: "POST",
               url: "./ajax/fetchSummaryStatistics.jag",
               data: {
                   'numFeature': numFeatureDependent
               },
               success: function (data) {
                   var jsonObj = JSON.parse(data);
                   var summary = "Mean: " + jsonObj[0].mean + "&emsp;&emsp;&emsp; Median: " + jsonObj[0].median + "&emsp;&emsp;&emsp; Std: " + jsonObj[0].std + "&emsp;&emsp;&emsp; Skewness: " + jsonObj[0].skewness;
                   $("#histogramDependentTitle").html(numFeatureDependent);
                   $("#numFeatureDependentSummary").html(summary);

                   // transform dataset
                   drawHistogram(jsonObj, "#histogramDependent");
               },
               error: function () {
                   var message = "An error occurred while fetching summary statistics";
                   //TODO: handle this with new UI...
               }
           });
}

// drawing a simple scatter graph
function drawScatterPlot(data, cssClass, xLabel, yLabel) {
    $(cssClass).empty();
    var scatter = new ScatterPlot(data);

    scatter.setPlotingAreaWidth(600);
    scatter.setPlotingAreaHeight(350);
    scatter.setMarkerSize("2");
    scatter.setXAxisText(xLabel);
    scatter.setYAxisText(yLabel);
    scatter.plot(d3.select(cssClass));
};

function drawHistogram(data, divID) {
    $(divID + ' svg').empty();

    nv.addGraph(function () {

        var chart = nv.models.linePlusBarChart()
                .margin({top: 30, right: 60, bottom: 50, left: 70})
                .x(function (d, i) {
                       return i
                   })
                .y(function (d) {
                       return d[1]
                   })
                .color(["#C99614"])
            ;

        chart.xAxis
            .showMaxMin(false)
            .tickFormat(function (d) {
                            return data[0].values[d][0];
                        });

        chart.y1Axis
            .tickFormat(d3.format(',f'));

        chart.y2Axis
            .tickFormat(function (d) {
                            return '$' + d3.format(',f')(d)
                        });


        chart.bars.forceY([0]);

        d3.select(divID + ' svg')
            .datum(data)
            .transition().duration(500)
            .call(chart);

        nv.utils.windowResize(chart.update);

        return chart;
    });
}
