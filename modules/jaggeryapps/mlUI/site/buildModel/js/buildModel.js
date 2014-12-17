    function disableWizardMenu() {
    var color = '#848484';
    $('#evaluate').css('color', color);
    $('#evaluate').removeAttr("href");
};

function loadAlgoTypes() {
    jagg.sessionAwareJs();
    $('#wizzardSteps').load('algoType.jag');
};

function loadQuestions(data) {
    jagg.sessionAwareJs();
    $('#wizzardSteps').load('questions.jag', data);
};

function loadRecommendedAlgos(data) {
    jagg.sessionAwareJs();
    $('#wizzardSteps').load('recommendedAlgos.jag', data);
};

function loadAllAlgos() {
    jagg.sessionAwareJs();
    $('#wizzardSteps').load('allAlgos.jag');
};


$('input[name=algorithm]').change(function () {
    jagg.sessionAwareJs();
    var algoName = $('input[name=algorithm]:checked')[0].value;
    $('#hyperParameters').load('hyperParameters.jag', 'algorithm=' + algoName);
    $('.algoSelectionErrorMessage').css('display','none');
});

$('.algoTypeButton').click(function () {
    jagg.sessionAwareJs();
    //get the id of the clicked button
    var algoType = this.id;
    var data = 'algoType=' + algoType;
    loadQuestions(data);
});

$('#questions_continue').click(function () {
    jagg.sessionAwareJs();
    var interpretability = $('#interpretability>option:checked')[0].value;
    var datasetSize = $('#datasetSize>option:checked')[0].value;
    var textual = $('#textual>option:checked')[0].value;
    var binary = $('#binary>option:checked')[0].value;
    var data = 'interpretability=' + interpretability + '&datasetSize=' + datasetSize + '&binary=' + binary + '&textual=' + textual;
    loadRecommendedAlgos(data);
});


$('#algorithms_continue').click(function () {
    jagg.sessionAwareJs();
    var algoName = $('input[name=algorithm]:checked')[0];
    var parameters = $(".parameter_name");
    var values = $(".parameter_value>input");
    var response = $('.responseDropdown option:selected').text();
    var trainDataFraction = $("#trainRatio").slider("value") / 100;
    var i = 0;
    if (algoName == undefined) {
        $('.algoSelectionErrorMessage').css('display', 'block');
    } else {
        var modelParameters = {};
        modelParameters.algorithmName = algoName.value;
        modelParameters.trainDataFraction = trainDataFraction;
        if (response != "") {
            modelParameters.responseVariable = response;
        }
        var hyperParameters = [];
        while (parameters[i] != undefined) {
            var parameter = [];
            parameter.push(parameters[i].id);
            parameter.push(values[i].value);
            hyperParameters.push(parameter);
            i++;
        }
        modelParameters.hyperParameters = hyperParameters;

        var isModelExecStarted = 'false';
        $.ajax({
            url : "./ajax/queryModelExecStart.jag",
            type : "POST",
            async : false,
            success : function(data) { isModelExecStarted = data;},
            error : function(){/*TODO:*/ }
        });

        //  model building process starts
        if(isModelExecStarted === 'false'){
            $.ajax({
                url: "./ajax/submit.jag",
            	type: 'POST',
            	dataType: "json",
            	data: {'modelParameters': JSON.stringify(modelParameters)},
                error: function (jqXHR, textStatus, errorThrown) {
                    var message = "An error has occurred with submitting model data";
                }
            });
            $('.wizzardSteps').html("Model building starts...."); 
        }

        // continuously polling with poll       
        poll();
    }
});

var isModelExecFinished = false;
var isResultSuccess = false; 

function poll(){              
    $.ajax({ 
        url: "./ajax/queryModelExecEnd.jag", 
        type: 'POST',
        async : false,
        success: function(data){
            isModelExecFinished = (data === "true");
        }, 
        complete: function(){                    
            if(isModelExecFinished){
                var graph = null;
                $('#wizzardSteps').empty();                        
                $('#wizzardSteps').append('<div class="rocCurve" ></div>'); 
                    $.ajax({
                        url : './ajax/getResults.jag',
                        type : 'POST',
                        async : false,
                        success : function(data){
                            if(!data){
                               //TODO: write a proper error message in new UI
                               $('#wizzardSteps').text("An error has occurred while "+
                                "building the model"); 
                            }else{
                                result = $.parseJSON(data);
                                var rocData = $.parseJSON(result.roc);
                                graph = new ROCGraph(rocData);
                                graph.setXAxisText("False positive rate");
                                graph.setYAxisText("True positive rate");
                                graph.setLegendName("roc curve (area="+result.auc+")");
                                graph.setLineWidth(2);
                                graph.setPlotingAreaWidth(400);
                                graph.setPlotingAreaHeight(300);
                                graph.setLineColor("#cf1fff");
                                graph.plot(d3.select(".rocCurve"));

                                isResultSuccess = true;
                            }
                        },
                        error : function (){
                            var message = "An error has occurred while fetching results";
                            //TODO: handle this with new UI...                            
                        }
                    });
                    if(isResultSuccess){
                        $('#wizzardSteps').append(
                            '<div class="cutOff"> '+
                            '<input id="cutOffSlider" type="range" min="0" max="1.0" step="0.01" />'+
                            '<span class="cutoffDisplay"></span>'+
                            '</div>')
                        $('#wizzardSteps').append('<div class="confutionMatrix"></div>');
                    
                        $('#cutOffSlider').on('change', function(){
                            var cutoffVal = $(this).val();
                            $('.cutoffDisplay').text(cutoffVal);
                            $.ajax({
                                url : './ajax/getconfusionMatrix.jag',
                                type : 'POST',
                                async : false,
                                data : {threshold: cutoffVal},
                                success :  function(data){
                                    var cm = $.parseJSON(data);
                                    var table = "<table>" +
                                        "<tr><td></td><td> </td><td>Actual</td><td></td><tr>"+
                                        "<tr><td></td><td> </td><td>True</td><td>False</td><tr>"+
                                        "<tr><td>Predicted: </td><td>True</td><td>"+cm.tp+"</td><td>"+cm.fp+"</td><tr>"+
                                        "<tr><td></td><td>False</td><td>"+cm.fn+"</td><td>"+cm.tn+"</td><tr>"+
                                         "</table>";
                                    $('.confutionMatrix').html(table);
                                    graph.drawCutoffProbMarker((cm.fp/(cm.tn+cm.fp)),(cm.tp/(cm.tp+cm.fn)),5);
                                },
                                error : function(){
                                    var message = "An error has occurred while fetching confution matrix";
                                    //TODO: handle this error in new UI
                                }
                            });
                        });                   
                        return; 
                    }
                } else{
                    // TODO: print proper error message in the new UI
                    $('#wizzardSteps').html("Model building starts....");                         
                    setTimeout(poll, 20*1000); // setting polling interval to 20 seconds
                }
            }, 
        timeout: 30*1000}); // setting connection timeout to 30 seconds         
}; 
