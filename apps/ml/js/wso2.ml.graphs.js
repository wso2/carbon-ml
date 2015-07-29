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

/*******************************************************/
/*************** BasePlot class starts******************/

/**BasePlot the parent class of all graph classes.
   It contains a set of properties and methods which 
   are common to all graph classes*/
var BasePlot = function(data) {

    if(!data){
        throw new PlottingError("The dataset is not defined");
    }

    //setting up margins
    this.margin = {
        top: 10,
        right: 50,
        bottom: 57,
        left: 55
    };

    //setting default width and height of the graph's canvas
    this.width = 800;
    this.height = 500;

    //dataset to visualize
    this.data = data;

    //axis's labels with default values 
    this.xLabel = '';
    this.yLabel = '';

    //graph will be drawn inside the this SVG tag
    this.svg = null;        
};

//following are methods of the base class
BasePlot.prototype.setPlotingAreaWidth = function(width){
    if(width <= 0){
        throw new PlottingError("plotting area width should be positive");
    }
    this.width = width;
};

BasePlot.prototype.setPlotingAreaHeight = function(height){
    if(height <= 0){
        throw new PlottingError("plotting area height should be positive");
    }
    this.height = height;
};

/*setting the SVG container inside the 'selection' DOM
  internal method should not call from outside Plot class hierarchy*/
BasePlot.prototype.initializeSVGContainer = function(selection){
    if(selection){
        this.svg = selection.append("svg")
            .attr("width", this.width + this.margin.left + this.margin.right)
            .attr("height", this.height + this.margin.top + this.margin.bottom)
            .append("g")
            .attr("transform", "translate(" + this.margin.left + "," + this.margin.top + ")");
    }else{
        throw new PlottingError("svg container can not be initialized");  
    }
};

/** internal method should not be called from 
    outside Plot class hierarchy*/
BasePlot.prototype.attachXAxis = function(xAxis){
    if(xAxis){ //
        this.svg.append("g")
            .attr("class", "axis text")
            .attr("transform", "translate(0," + this.height + ")")
            .call(xAxis)
            .append("text")
            .attr("class", "axis text")
            .attr("x", this.width)
            .attr("y", 30)
            .style("text-anchor", "end")
            .text(this.xLabel);
    }else{
        throw new PlottingError("xAxis is null or empty");
    }
};

/** internal method should not called from 
    outside Plot class hierarchy*/
BasePlot.prototype.attachYAxis = function(yAxis){
    if(yAxis){
        this.svg.append("g")
            .attr("class", "axis text")
            .call(yAxis)
            .append("text")
            .attr("class", "axis text")
            .attr("transform", "rotate(-90)")
            .attr("y", -50)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text(this.yLabel);
    }else{
        throw new PlottingError("yAxis is null or empty");
    }
};

BasePlot.prototype.setXAxisText = function(text){
    if(text){
        this.xLabel = text;
    }else{
        throw new PlottingError("Label of the X axis can't be null or empty");
    }
};

BasePlot.prototype.setYAxisText = function(text){
    if(text){
        this.yLabel = text;
    }else{
        throw new PlottingError("Label of the Y axis can't be null or empty");
    }
};
/************end of BasePlot class********************/
/*****************************************************/


/*****************************************************/
/********* ScatterPlot class starts*******************/

/** This class is used to generate a scatter plot 
    using a set of data points*/
var ScatterPlot = function(data) {
    // calling the base class 
    BasePlot.call(this, data);

    // properties related to ScatterPlot
    this.markerSize = 3;
    this.colors = d3.scale.category10();
    this.legendEnabled = true;
    this.groupByValues = [];
    this.legendBoxWidth = 10;
    this.legendBoxHeight = 10; 
    this.legendTextXLoc = 20;
    this.legendTextYLoc = 5;
};

ScatterPlot.prototype = Object.create(BasePlot.prototype);
ScatterPlot.prototype.constructor = ScatterPlot;

/** Following method is used to set the size of each 
    point in the scatter plot*/
ScatterPlot.prototype.setMarkerSize = function(markerSize){

    if(markerSize <= 0){
        throw new PlottingError("markerSize should be positive");
    }
    this.markerSize = markerSize;    
};

/** Following method is used to set colors used
    in the scatter plot*/
ScatterPlot.prototype.setColors = function(colors){

    if(colors.length < 1){
        throw new PlottingError("At least one color should be defined");
    }
    this.colors = d3.scale.ordinal().range(colors);
};

/** Following method is used to set legend used
    in the scatter plot*/
ScatterPlot.prototype.setLegend = function(legendEnabled){

    if(legendEnabled == false){
        this.legendEnabled = legendEnabled;
    }
};

/** Following method is used to set group by values (values of the categorical 
    feature e.g. 'Correct', 'Incorrect') in the scatter plot*/
ScatterPlot.prototype.setGroupByValues = function(groupByValues){
    
    this.groupByValues = groupByValues;
};

/**Main function of the ScatterPlot class.
   Once called graph will be drawn inside the SVG container*/
ScatterPlot.prototype.plot = function(selection) {
    if(!selection){
       throw new PlottingError("DOM element can't be null or empty"); 
    }

    // setting up the SVG container
    this.initializeSVGContainer(selection);

    //setting up X and Y scales appropriate to Scatter Plots 
    var xScale = d3.scale.linear()
        .domain([d3.min(this.data, function(d) {
            return d[0];
        }), d3.max(this.data, function(d) {
            return d[0];
        })])
        .range([0, this.width]);

    var yScale = d3.scale.linear()
        .domain([d3.min(this.data, function(d) {
            return d[1];
        }), d3.max(this.data, function(d) {
            return d[1];
        })])
        .range([this.height, 0]);

    // setting up X and Y axis, appropriate to Scatter Plots
    var xAxis = d3.svg.axis()
        .ticks(10)
        .tickFormat(function(d) {
            return (d3.format(".2s"))(d);
        })
        .scale(xScale)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(yScale)
        .tickFormat(function(d) {
            return (d3.format(".2s"))(d);
        })
        .orient("left");

    // updating X and Y axises
    this.attachXAxis(xAxis);
    this.attachYAxis(yAxis);

    var color = this.colors;
    var groupByValues = this.groupByValues;

    // get color values from D3 color scale e.g. ['#286c4f', '#c02e1d']
    var colorValues = color.range();

    // drawing dots, each dot represents a single data point
    this.svg.selectAll("circle")
        .data(this.data)
        .enter().append("circle")
        .attr("r", this.markerSize)
        .attr("cx", function(d) {
            return xScale(d[0]);
        })
        .attr("cy", function(d) {
            return yScale(d[1]);
        })
        .style("fill", function(d) {
            // group by values set (when both group by values and colors are set, those will be mapped in passed order)
            // e.g. 'Correct' -> '#286c4f', 'Incorrect' -> '#c02e1d'
            if(groupByValues.length > 0) {
                var groupByValuesIndex = groupByValues.indexOf(d[2]);
                return colorValues[groupByValuesIndex];
            }
            return color(d[d.length - 1]); // color code
        })
        .style("opacity", 0.8);

    // setting the legend at top left corner of the graph if legend is enabled
    if(this.legendEnabled == true) {

        var colorDomain;
        // if group by values are set, use those values explicitly for legend e.g. ['Correct', 'Incorrect']
        if(groupByValues.length > 0) {
            colorDomain = groupByValues;
        }
        else {
            colorDomain = color.domain();   
        }

        var legend = this.svg.selectAll(".basegraph")
            .data(colorDomain)
            .enter().append("g")
            .attr("class", "basegraph")
            .attr("transform", function(d, i) {
                return "translate(0," + i * 20 + ")";
            });

        legend.append("rect")
            .attr("x", this.width - this.legendBoxWidth)
            .attr("width", this.legendBoxWidth)
            .attr("height", this.legendBoxHeight)
            .style("fill", color);

        legend.append("text")
            .attr("x", this.width - this.legendTextXLoc)
            .attr("y", this.legendTextYLoc)
            .attr("dy", ".35em")
            .style("text-anchor", "end")
            .text(function(d) {
                return d;
            });        
    }
};
/*******************end of ScatterPlot class*************/
/********************************************************/

/********************************************************/
/*********** BasicLineGraph starts *********************/

/** This is the abstract class of all line graphs.
    It contains a set of methods which are common to all
    specialized line graphs such as ROC curve*/ 
var BasicLineGraph = function(data){
    // calling the base class 
    BasePlot.call(this, data);

    this.legendName = '';
    this.lineColor = "#3282BD";
    this.lineWidth = 2;
};

BasicLineGraph.prototype = Object.create(BasePlot.prototype);
BasicLineGraph.prototype.constructor = BasicLineGraph;

BasicLineGraph.prototype.setLegendName = function(legend){
    if(!legend){
        throw new PlottingError("legend can't be null or empty");
    }    
    this.legendName = legend;
};

BasicLineGraph.prototype.setLineColor = function(color){
    if(!color){
        throw new PlottingError("Line color can't be null or empty");
    }    
    this.lineColor = color;
};

BasicLineGraph.prototype.setLineWidth = function(lineWidth){
    if(!lineWidth){
        throw new PlottingError(" Line width can't be null or empty");
    }    
    this.lineWidth = lineWidth;
};
/*********** BasicLineGraph ends ************************/ 
/********************************************************/

/*********** ROC graph starts **************************/
/*******************************************************/
/** This is a concrete class of BasicLineGraph and it is 
    used to generate ROC curve using an array of array of points
    such as [[0.0, 0.0], ..., [1.0, 1.0]]*/
var ROCGraph = function(data){
    // calling the base class 
    BasePlot.call(this, data);

    this.randomGuassingLineColor = "#000000";
    this.xScale = null;
    this.yScale = null;

    this.marker = null;
    this.markerHorizontalIndicator = null;
    this.marketVerticalIndicator = null;
};

ROCGraph.prototype = Object.create(BasicLineGraph.prototype);
ROCGraph.prototype.constructor = ROCGraph;

// cutoff probability and it's X and Y coordinates 
// will be draw on the ROC graph 
ROCGraph.prototype.drawCutoffProbMarker = function(cx, cy, r){
    if(this.marker){
        this.marker.remove();
    }
    this.marker = this.svg.append("circle")
            .attr("cx", xScale(cx))
            .attr("cy", yScale(cy))
            .attr("r", r)
            .attr("fill", "red");

    if(this.markerHorizontalIndicator){
        this.markerHorizontalIndicator.remove();
    }
    this.markerHorizontalIndicator = this.svg.append("line")
        .attr('x1', 0)
        .attr('y1', yScale(cy))
        .attr('x2', xScale(cx))
        .attr('y2', yScale(cy))
        .attr('stroke-width', 1)
        .attr('stroke', '#ff0000')
        .attr("stroke-dasharray", ("5, 5"));
    
    if(this.marketVerticalIndicator){
        this.marketVerticalIndicator.remove();
    }
    this.marketVerticalIndicator = this.svg.append("line")
        .attr('x1', xScale(cx))
        .attr('y1', this.height)
        .attr('x2', xScale(cx))
        .attr('y2', yScale(cy))
        .attr('stroke-width', 1)
        .attr('stroke', '#ff0000')
        .attr("stroke-dasharray", ("5, 5"));
};

ROCGraph.prototype.plot = function(selection){
    if(!selection){
       throw new PlottingError("DOM element can't be null or empty"); 
    }

    //setting up the SVG container
    this.initializeSVGContainer(selection); 

    //setting up X and Y scales appropriate to Scatter Plots 
    xScale = d3.scale.linear()
        .domain([d3.min(this.data, function(d) {
            return d[0];
        }), d3.max(this.data, function(d) {
            return d[0];
        })])
        .range([0, this.width]);

    yScale = d3.scale.linear()
        .domain([d3.min(this.data, function(d) {
            return d[1];
        }), d3.max(this.data, function(d) {
            return d[1];
        })])
        .range([this.height, 0]);

    //setting up X and Y axis, appropriate to basic line graph
    var xAxis = d3.svg.axis()
        .ticks(10)
        .tickFormat(function(d){            
            return d3.round(d,2);
        })
        .scale(xScale)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(yScale)
        .tickFormat(function(d) {
            return d3.round(d,2);
        })
        .orient("left");

    // updating X and Y axises
    this.attachXAxis(xAxis);
    this.attachYAxis(yAxis);

    var lineBuilder = d3.svg.line()
                .x(function(d) { return xScale(d[0]); })
                .y(function(d) { return yScale(d[1]); })
                .interpolate("linear");

    var graph = this.svg.append("path")
                .attr("d", lineBuilder(this.data))
                .attr("stroke", this.lineColor)
                .attr("stroke-width", this.lineWidth)
                .attr("fill", "none");
    
    var legend = this.svg.selectAll('.legend')
                .data([1])
                .enter()
                .append('g')
                    .attr('class', 'legend');
    
    legend.append('line')
                .attr('x1', this.width - 200)
                .attr('y1', 8*(this.height/10) -3)
                .attr('x2', this.width - 175)
                .attr('y2', 8*(this.height/10) -3)
                .attr('stroke-width', this.lineWidth)
                .attr('stroke', this.lineColor);
      
    var name = this.legendName;     
    legend.append('text')
                .attr('x', this.width - 160)
                .attr('y', 8*(this.height/10))
                .text(function(d){ return name; });

    var randomGuass = this.svg.append("line")
                .attr('x2', this.width)
                .attr('y2', 0)
                .attr('x1', 0)
                .attr('y1', this.height)
                .attr('stroke-width', 1)
                .attr('stroke', this.randomGuassingLineColor)
                .attr("stroke-dasharray", ("10, 3"));

};
/*********** end of ROC graph *************************/
/*******************************************************/


/*******************************************************/
/******************** BaseHistogram starts *************/
/** This class represents properties common to 
    all histogram classes*/
var BaseHistogram = function(data){
    //calling the base class constructor
    BasePlot.call(this,data);

    this.barPadding = 1;

    //setting the default color for bars
    this.barColor = '#3182BF';    
};

BaseHistogram.prototype = Object.create(BasePlot.prototype);
BaseHistogram.prototype.constructor = BaseHistogram;

BaseHistogram.prototype.setBarColor = function(color){
    if(arguments.length){
        this.barColor = color;
    }
};

/******************** end of BaseHistogram  *************/
/********************************************************/


/********************************************************/
/*****************Histogram class starts*****************/
/**This class generates a histogram using an array of numbers
   such as [1.0, 10.3, 100.2, ....] with a given number of buckets*/
var Histogram = function(data){
    //calling the base class constructor
    BaseHistogram.call(this,data);

    // number of bucket in the histogram
    this.numOfBuckets = 10;
};  

Histogram.prototype = Object.create(BaseHistogram.prototype);
Histogram.prototype.constructor = Histogram;

Histogram.prototype.setNumOfBuckets = function(numOfBuckets) {    
    if(numOfBuckets <= 0){
        throw new PlottingError("numOfBuckets should be positive");
    }
    this.numOfBuckets = numOfBuckets;
};

//This is the main function of the Histogram class
//Once called graph will be drawn inside the SVG container 
Histogram.prototype.plot = function(selection) {
    if(!selection){
       throw new PlottingError("DOM element can't be null or empty"); 
    }

    //setting up the SVG container
    this.initializeSVGContainer(selection);

    //creating X Scale, appropriate to Histograms
    var xScale = d3.scale.linear()
        .domain([0, d3.max(this.data)])
        .range([0, this.width]);

    //generating histogram data, using numOfBuckets parameter        
    var histogramData = d3.layout.histogram()
        .bins(xScale.ticks(this.numOfBuckets))
        (this.data);

    //creating Y scale, appropriate to Histograms
    var yScale = d3.scale.linear()
        .domain([0, d3.max(histogramData, function(d) {
            return d.y;
        })])
        .range([this.height, 0]);
    
    //setting up a bars chart, one bar per each bin 
    var that = this;
    var bar = this.svg.selectAll("rect")
        .data(histogramData)
        .enter().append("g")
        .append("rect")
        .attr("x", this.barPadding)
        .attr("width", xScale(histogramData[0].dx) - this.barPadding)
        .attr("height", function(d) {                             
            return that.height - yScale(d.y);
        })
        .attr("transform", function(d) {
            return "translate(" + xScale(d.x) + "," + yScale(d.y) + ")";
        })
        .attr('fill', this.barColor);

    //setting up axis 
    var xAxis = d3.svg.axis()
        .ticks(10)
        .scale(xScale)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(yScale)
        .tickFormat(function(d) {
            return (d3.format(".2s"))(d);
        })
        .orient("left");

    //updating histogram with axis data
    this.attachYAxis(yAxis);
    this.attachXAxis(xAxis);
};
/***********************end of histogram class**********************/
/*******************************************************************/


/******************************************************************/
/************HistogramUsingCalculatedFrequencies class starts******/
/**This class generates a histogram using calculated frequencies
   Data format should be [[10,'Sun'],[20, 'Mon'],[30, 'Tue']]*/
var HistogramUsingCalculatedFrequencies = function(data){
    //calling the base class constructor
    BaseHistogram.call(this, data);    
};

HistogramUsingCalculatedFrequencies.prototype = Object.create(BaseHistogram.prototype);
HistogramUsingCalculatedFrequencies.prototype.constructor = HistogramUsingCalculatedFrequencies;

HistogramUsingCalculatedFrequencies.prototype.plot = function(selection) {
    if(!selection){
       throw new PlottingError("DOM element can't be null or empty"); 
    }

    //setting up SVG container
    this.initializeSVGContainer(selection);

    //setting up Y scale, appropriate to HistogramUsingCalculatedFrequencies 
    var yScale = d3.scale.linear()
        .domain([0, d3.max(this.data, function(d) {
            return d[0];
        })])
       .range([0, this.height]);
    
    var that = this;    
    //draw rectangles, each represents single bucket in the histogram
    var rect = this.svg.selectAll("rect")
            .data(this.data)
            .enter()
            .append("rect")
            .attr("x", function(d, i) {
                return i * (that.width / that.data.length);
            })
            .attr("y", function(d) {                
                return that.height - yScale(d[0]);
            })
            .attr("width", that.width / that.data.length - this.barPadding)
            .attr("height", function(d) {
                return yScale(d[0]);
            })
            .attr('fill', this.barColor);

    var scalingYAxisForDisplaying = d3.scale.linear()
        .domain([0, d3.max(this.data, function(d) {
            return d[0];
        })])
        .range([this.height, 0]);    

    var yAxis = d3.svg.axis()
        .scale(scalingYAxisForDisplaying)
        .orient("left")
        .tickFormat(function(d) { return (d3.format(".2s"))(d);});       
        
    var labels = [];
    for(var i=0; i<this.data.length;i++){
        labels.push(this.data[i][1]);
    }

    //creating xScale and updating xAxis    
    var xScale = d3.scale.ordinal()
        .domain(labels)
        .rangeBands([0,this.width]);

    var xAxis = d3.svg.axis()
        .scale(xScale)
        .orient("bottom")
        .ticks(4);
    
    //updating X axis 
    this.attachXAxis(xAxis);  
    this.attachYAxis(yAxis);            
};
/********end of HistogramUsingCalculatedFrequencies class*****/
/*************************************************************/


/************************************************************/
/*********** PlottingError**********************************/
/**custom error class extends from Error*/
var PlottingError = function (message) {
    this.name = 'PlottingError';
    this.message = message;
    this.stack = (new Error()).stack;
};
PlottingError.prototype = Object.create(Error.prototype);
