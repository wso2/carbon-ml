$('document').ready(function() {

    // highlight "import data" menu
    $('#importData').addClass('top_Menu_button menuHiligher');

    // disable the evaluation link on wizard menu
    disableEvaluation();

    $("#dataImportForm").submit(function(e) {
        var isAllEntered = true;
        
        var projectName = $('#projectName').val();
        var projectDescrition = $('#projectDescription').val();
        var workflowName = $('#workflowName').val();
        var dataFileName = $('#datasetName').val();
        
        if(!projectName){
            $('#projectNameValidator').html("<span class=\"errorMessage\">Project name is required</span>");
            isAllEntered = false;
        }

        if(!projectDescrition){
            $('#projectDescriptionValidator').html("<span class=\"errorMessage\">Project description is required</span>");
            isAllEntered = false;
        }
        
        if(!workflowName){
            $('#workflowNameValidator').html("<span class=\"errorMessage\">Workflow name is required</span>");
            isAllEntered = false;
        }

        if(!dataFileName){
	        $('#datasetNameValidator').html("<span class=\"errorMessage\">Dataset is required</span>");
            isAllEntered = false;
        } else if(!isValidFormat(dataFileName)){
            $('#datasetNameValidator').empty().html("<span class=\"errorMessage\">Incorrect file format selected</span>");
            isAllEntered = false;
        }

        if(isAllEntered){
            var formObj = $(this);
            var formURL = formObj.attr("action");
            var formData = new FormData(this);

            $.ajax({
                url : formURL,
                type : 'POST',
                data : formData,
                mimeType : "multipart/form-data",
                contentType : false,
                cache : false,
                processData : false,
                success : function(data, textStatus, jqXHR) {
                    window.location.href = "./datatable.jag";
                },
                error : function(jqXHR, textStatus, errorThrown) {
                    // TODO: redirect to error page 
                }
            });
        }

        e.preventDefault(); //Prevent Default action.

    });

    $('#datasetOpen').click(function() {
        $('#validatorMsg').text("");
        $('#uploadDataset').click();

    });

    $("#uploadDataset").change(function() {
        $("#datasetName").val($('#uploadDataset').val());
        $('#datasetNameValidator').empty();
    });

    $('#projectName').keypress(function (event){
        $('#projectNameValidator').empty();
    });

    $('#projectDescription').keypress(function(event){
       $('#projectDescriptionValidator').empty();
    });

    $('#workflowName').keypress(function(event){
       $('#workflowNameValidator').empty();
    });
});

function hasValidFileFormat(fileName) {
    var result = fileName.match(/^.+\.csv$/g);
    if(result) {
        return true;
    } else {
        return false;
    }
}

function disableEvaluation() {
    var color='#848484';
    $('#evaluate').css('color',color);
    $('#evaluate').removeAttr("href");
};

function disableWizardMenu() {
    var color='#848484';
    $('#exploreData').css('color',color);
    $('#exploreData').removeAttr("href");
    $('#buildModel').css('color',color);
    $('#buildModel').removeAttr("href");
};

// Preserve pagination when refreshing datatable 
$.fn.dataTableExt.oApi.fnStandingRedraw = function(oSettings) {
    if(oSettings.oFeatures.bServerSide === false){
        var before = oSettings._iDisplayStart;
        oSettings.oApi._fnReDraw(oSettings);
        oSettings._iDisplayStart = before;
        oSettings.oApi._fnCalculateEnd(oSettings);
    }
    //draw the 'current' page
    oSettings.oApi._fnDraw(oSettings);
};


/* Draw Data view table */
function drawDataViewTable() {
    $('#datasetTable').dataTable({
        "bServerSide": true,
        "sAjaxSource": "ajax/importDataset.jag",
        "bProcessing": false,
        "bLengthChange": false,
        "bFilter": false,
    });

    $('#datasetTable').on('draw.dt', function() {
        
        $('.summaryStatistics').each(function() {
            var jsonText = $(this).text();
            // TODO: handle JSON parsing errors
            var jsonObj  = JSON.parse(jsonText);            

            // clear text in this cell and draw graphs
            $(this).text("");

            //get feature type
            var closestTr = $(this).closest('tr');
            var FeatureType = closestTr.find('.fieldType option:selected').text();

            var frequencies = jsonObj[0].values;
            
            // transform dataset
            var dataArray = $.map(frequencies, function(value, index) {
                return value[1];
            });
            
            if (FeatureType == 'CATEGORICAL'){          
                
                var w = 40;
                var h = 40;
                var pie = d3.layout.pie();
                
                var outerRadius = w / 2;
                var innerRadius = 0;
                var arc = d3.svg.arc()
                                .innerRadius(innerRadius)
                                .outerRadius(outerRadius);

                  var svg = d3.select(this)
                            .append("svg")
                            .attr("width", w)
                            .attr("height", h);

                  var arcs = svg.selectAll("g.arc")
                        .data(pie(dataArray))
                        .enter()
                        .append("g")
                        .attr("class", "arc")
                        .attr("transform", "translate(" + outerRadius + ", " + outerRadius + ")");

                  //var color = d3.scale.category20();
                   var color = d3.scale.ordinal().range(["#D59C0C", "#3C2B02", '#614705', '#FFD64A', '#7A5C0F', '#FFF869', '#A8801C', '#F0D74C', '#D9AE21', '#FFC400', '#D9A90A', '#BFB011', '#B29E47', '#FFD64A', '#C6B902', '#C68202', '#95773B', '#8F6908', '#4F3903', '#FFDA00']);
                   
                  arcs.append("path")
                    .attr("fill", function(d, i) {
                        return color(i);
                    })
                    .attr("d", arc);
                
            }else{
                
                var w = 200;
                var h = 40;                 
                var barPadding = 1;
                
                // scaling this y-axis using a linear scaler 
                var yScale = d3.scale.linear()
                              .domain([0, d3.max(dataArray, function(d) {
                                 return d;
                               })])
                               .range([0,h]);
                
                var svg = d3.select(this)
                .append("svg")
                .attr("width", w)
                .attr("height", h);
                
                svg.selectAll("rect")
                    .data(dataArray)
                    .enter()
                    .append("rect")
                    .attr("x", function(d, i) {
                        return i * (w / dataArray.length);
                    })
                    .attr("y", function(d) {
                        return h - yScale(d);
                    })
                    .attr("width", w / dataArray.length - barPadding)
                    .attr("height", function(d) {
                        return yScale(d);
                    })
                    .attr("fill", '#D59C0C');
            }
        });
        
        // TODO: AJAX call per change in the data-table is an overhead
        // use the AJAX methods given by the datatable and improve this section
        $('.fieldType').on('change', function(e) {
            var closestTr = $(this).closest('tr');
            var selectedFeature = closestTr.find('.feature').text();
            
            var selectedFeatureType = this.options[e.target.selectedIndex].text;

            $.ajax({
                type: "POST",
                url: "./ajax/updateFeatureType.jag",
                data: {
                    'FEATURE_TYPE': selectedFeatureType,
                    'FEATURE_NAME': selectedFeature
                }
            });
            // refresh datatable, preserving the current page
            $('#datasetTable').dataTable().fnStandingRedraw();
        });

        $('.includeFeature').on('change', function(e) {
            var closestTr = $(this).closest('tr');
            var selectedFeature = closestTr.find('.feature').text();
            var selectionFlag = 'false';

            if (this.checked) {
                selectionFlag = 'true';
            }
            console.log("selectionFlag: "+selectionFlag);
            $.ajax({
                type: "POST",
                url: "./ajax/updateInputSelection.jag",
                data: {
                    'IS_FEATURE_SELECTED': selectionFlag,
                    'FEATURE_NAME': selectedFeature
                }
            });
        });

        $('.imputeMethod').on('change', function(e) {
            var closestTr = $(this).closest('tr');
            var selectedFeature = closestTr.find('.feature').text();
            var imputedMethod = this.options[e.target.selectedIndex].text;

            $.ajax({
                type: "POST",
                url: "./ajax/updateImputeMethod.jag",
                data: {
                    'IMPUTE_OPTION': imputedMethod,
                    'FEATURE_NAME': selectedFeature
                }
            });
        });

    });
};
