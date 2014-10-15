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
/**
 * This files contains some useful JavaScript functions for drawing
 * visualizations using D3.js library
 */
// function for drawing scatter graphs with one or two variables
function scatterPlot() {
    // margins of the graph
    var margin = {
        top: 50,
        right: 50,
        bottom: 50,
        left: 50
    };

    //setting default width and height
    var width = 800;
    var height = 500;

    // setting X and Y scales
    var xScale = d3.scale.linear();
    var yScale = d3.scale.linear();

    // dataset to visualize
    var data = null;
    var xLabel = "x_label";
    var yLabel = "y_label";
    var exploreSingleFeature = true;

    var markerSize = 1;

    var formatValue = d3.format(".2s");

    function chart(selection) {

        if (exploreSingleFeature) {
            data = data.map(function(d, i) {
                return [i, d[0], d[d.length - 1]];
            });
        }

        xScale = d3.scale.linear()
            .domain([d3.min(data, function(d){
                return d[0];
            }),d3.max(data, function(d) {
                return d[0];
            })])
            .range([0, width]);

        yScale = d3.scale.linear()
            .domain([d3.min(data, function(d){
                return d[1]
            }), d3.max(data, function(d) {
                return d[1];
            })])
            .range([height, 0]);

        var svg = selection.append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        var x_axis = d3.svg.axis()
            .ticks(10)
            .tickFormat(function(d) { return formatValue(d)})
            .scale(xScale)
            .orient("bottom");

        var y_axis = d3.svg.axis()
            .scale(yScale)
            .tickFormat(function(d) { return formatValue(d)})
            .orient("left");

        // appending X and Y axises with their respective labels
        svg.append("g")
            .attr("class", "scatter x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(x_axis)
            .append("text")
            .attr("class", "scatter label")
            .attr("x", width)
            .attr("y", +30)
            .style("text-anchor", "end")
            .text(xLabel);

        svg.append("g")
            .attr("class", "scatter y axis")
            .call(y_axis)
            .append("text")
            .attr("class", "scatter label")
            .attr("transform", "rotate(-90)")
            .attr("y", -40)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text(yLabel);

        var color = d3.scale.category10();

        svg.selectAll(".dot")
            .data(data)
            .enter().append("circle")
            .attr("class", "scatter dot")
            .attr("r", markerSize)
            .attr("cx", function(d) {
                return xScale(d[0]);
            })
            .attr("cy", function(d) {
                return yScale(d[1]);
            })
            .style("fill", function(d) {
                return color(d[d.length - 1]); // color code               
            });

        var legend = svg.selectAll(".legend")
            .data(color.domain())
            .enter().append("g")
            .attr("class", "scatter legend")
            .attr("transform", function(d, i) {
                return "translate(0," + i * 20 + ")";
            });

        legend.append("rect")
            .attr("x", width - 25)
            .attr("width", 25)
            .attr("height", 25)
            .style("fill", color);

        legend.append("text")
            .attr("x", width - 30)
            .attr("y", 9)
            .attr("dy", ".35em")
            .style("text-anchor", "end")
            .text(function(d) {
                return d;
            });
    }

    chart.data = function(value) {
        if (!arguments.length) return data;

        data = value;
        return chart;
    };

    chart.width = function(value) {
        if (!arguments.length) return width;
        width = value;
        return chart;
    };

    chart.height = function(value) {
        if (!arguments.length) return height;
        height = value;
        return chart;
    };

    chart.xLabel = function(value) {
        if (!arguments.length) return xLabel;
        xLabel = value;
        return chart;
    };

    chart.yLabel = function(value) {
        if (!arguments.length) return yLabel;
        yLabel = value;
        return chart;
    };

    chart.exploreTwoFeatures = function(value) {
        if (!arguments.length) return exploreSingleFeature;
        exploreSingleFeature = !value;
        return chart;
    };

    chart.markerSize = function(value){
        if(!arguments.length) return markerSize;
        markerSize = value;
        return chart;
    }

    return chart;
}

// function for drawing scatter graphs with one or two variables
function histogram() {

    var margin = {
        top: 50,
        right: 50,
        bottom: 50,
        left: 50
    };
    var width = 800;
    var height = 500;
    var xScale = d3.scale.linear();
    var yScale = d3.scale.linear();
    var data = null;
    var xLabel = "x_label";
    var yLabel = "y_label";

    var numOfBuckets = 10; // default is 10 

    var printFrequenciesOnBar = false;

    function chart(selection) {


        xScale = d3.scale.linear()
            .domain([0, d3.max(data)])
            .range([0, width]);

        var svg = selection.append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        // Generate a histogram using $numOfBuckets of bins.        
        var histogramData = d3.layout.histogram()
            .bins(xScale.ticks(numOfBuckets))
            (data);

        var yScale = d3.scale.linear()
            .domain([0, d3.max(histogramData, function(d) {
                return d.y;
            })])
            .range([height, 0]);

        var bar = svg.selectAll(".bar")
            .data(histogramData)
            .enter().append("g")
            .attr("class", "histogram")
            .attr("transform", function(d) {
                return "translate(" + xScale(d.x) + "," + yScale(d.y) + ")";
            });

        bar.append("rect")
            .attr("x", 1)
            .attr("width", xScale(histogramData[0].dx) - 1)
            .attr("height", function(d) {
                return height - yScale(d.y);
            });

        // A formatter for counts.
        var formatCount = d3.format(",.0f");

        // if printFrequenciesOnBar flag is on, display frequencies on  
        if (printFrequenciesOnBar) {
            bar.append("text")
                .attr("dy", ".75em")
                .attr("y", 6)
                .attr("x", xScale(histogramData[0].dx) / 2)
                .attr("text-anchor", "middle")
                .text(function(d) {
                    return formatCount(d.y);
                });
        }

        var xAxis = d3.svg.axis()
            .ticks(10)
            .scale(xScale)
            .orient("bottom");

        svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis);

        var y_axis = d3.svg.axis()
            .scale(yScale)
            .orient("left");

        // appending X and Y axises with their respective labels
        svg.append("g")
            .attr("class", "scatter y axis")
            .call(y_axis)
            .append("text")
            .attr("class", "scatter label")
            .attr("transform", "rotate(-90)")
            .attr("y", -40)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text(yLabel)

    }

    chart.data = function(value) {
        if (!arguments.length) return data;
        data = value;
        return chart;
    };

    chart.width = function(value) {
        if (!arguments.length) return width;
        width = value;
        return chart;
    };

    chart.height = function(value) {
        if (!arguments.length) return height;
        height = value;
        return chart;
    };

    chart.xLabel = function(value) {
        if (!arguments.length) return xLabel;
        xLabel = value;
        return chart;
    };

    chart.yLabel = function(value) {
        if (!arguments.length) return yLabel;
        yLabel = value;
        return chart;
    };

    chart.printFrequenciesOnBar = function(value) {
        if (!arguments.length) return printFrequenciesOnBar;
        printFrequenciesOnBar = value;
        return chart;
    };

    chart.numOfBuckets = function(value) {
        if (!arguments.length) return numOfBuckets;
        numOfBuckets = value;
        return chart;
    };

    return chart;
}

// This method generates a histogram from an array of frequencies
// for instance [10,15,25,50,55,48,10,5] with an array of
// bin names such as ['1','2','3'] 
function histogramUsingCalculatedFrequencies() {

    var margin = {
        top: 50,
        right: 50,
        bottom: 50,
        left: 50
    };
    var width = 800;
    var height = 500;
    var xScale = null;
    var yScale = null;
    var data = null;
    var bucketNames = null;
    var xLabel = "x_label";
    var yLabel = "y_label";

    var barPadding = 1; // padding between two bars
    var displayAxises = true; // this flag is used to enable/disable axises  
    var barColor = '#4682F4'

    var formatValue = d3.format(".2s");

    function chart(selection) {

        yScale = d3.scale.linear()
            .domain([0, d3.max(data, function(d) {
                return d;
            })])
            .range([0, height]);

        var svg = selection.append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        svg.selectAll("rect")
            .data(data)
            .enter()
            .append("rect")
            .attr("x", function(d, i) {
                return i * (width / data.length);
            })
            .attr("y", function(d) {
                return height - yScale(d);
            })
            .attr("width", width / data.length - barPadding)
            .attr("height", function(d) {
                return yScale(d);
            })
            .attr('fill', barColor);


        var yScaleForAxis = d3.scale.linear()
            .domain([0, d3.max(data, function(d) {
                return d;
            })])
            .range([height, 0]);

        if (displayAxises) {            
            var y_axis = d3.svg.axis()
                .scale(yScaleForAxis)
                .orient("left")
                .tickFormat(function(d) { return formatValue(d)});

            svg.append("g")
                .attr("class", "scatter y axis")
                .call(y_axis)
                .append("text")
                .attr("class", "scatter label")
                .attr("transform", "rotate(-90)")
                .attr("y", -40)
                .attr("dy", ".71em")
                .style("text-anchor", "end")
                .text(yLabel)

            var xScale = d3.scale.ordinal()
                .domain(bucketNames)
                .rangeBands([0, width]);

            var xAxis = d3.svg.axis()
                .scale(xScale)
                .orient("bottom")
                .ticks(4);

            svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis);
        }
    }

    chart.data = function(value) {
        if (!arguments.length) return data;
        data = value;
        return chart;
    };

    chart.bucketNames = function(value) {
        if (!arguments.length) return data;
        bucketNames = value;
        return chart;
    }

    chart.width = function(value) {
        if (!arguments.length) return width;
        width = value;
        return chart;
    };

    chart.height = function(value) {
        if (!arguments.length) return height;
        height = value;
        return chart;
    };

    chart.xLabel = function(value) {
        if (!arguments.length) return xLabel;
        xLabel = value;
        return chart;
    };

    chart.yLabel = function(value) {
        if (!arguments.length) return yLabel;
        yLabel = value;
        return chart;
    };

    chart.printFrequenciesOnBar = function(value) {
        if (!arguments.length) return printFrequenciesOnBar;
        printFrequenciesOnBar = value;
        return chart;
    };

    chart.barPadding = function(value) {
        if (!arguments.length) return htmlTag;
        barPadding = value;
        return chart;
    }

    chart.displayAxises = function(value) {
        if (!arguments.length) return displayAxises;
        displayAxises = value;
        return chart;
    }

    chart.barColor = function(value) {
        if (!arguments.length) return barColor;
        barColor = value;
        return chart;
    }
    
    return chart;
}