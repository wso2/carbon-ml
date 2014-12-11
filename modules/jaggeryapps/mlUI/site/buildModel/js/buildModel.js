$('document').ready(function () {
    $('#buildModel').addClass('top_Menu_button menuHiligher');
    disableWizardMenu();
});

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
        alert("Please select an algorithm before continue.");
    } else {
        var hyperParametersData = 'algorithmName=' + algoName.value + '&trainDataFraction=' + trainDataFraction;
        if (response != "") {
            hyperParametersData = hyperParametersData + '&responseVariable=' + response;
        }
        while (parameters[i] != undefined) {
            hyperParametersData = hyperParametersData + '&' + parameters[i].id + '=' + values[i].value;
            i++;
        }
        var isModelExecStarted = 'false';
        $.ajax({
            url : "./ajax/queryModelExecStart.jag",
            type : "POST",
            async : "false",
            success : function(data) { isModelExecStarted = data;},
            error : function(){/*TODO:*/ }
        });

        //  model building process starts
        if(isModelExecStarted === 'false'){
            $.ajax({
                url: "./ajax/submit.jag",
                type: 'POST',
                data: hyperParametersData,
                error: function (jqXHR, textStatus, errorThrown) {
                    // TODO: redirect to error page
                }
            });
            $('.wizzardSteps').html("Model building starts...."); 
        }
        
        // continuously polling for the model output      
        var isModelExecFinished = 'false';
        function poll(){            
            $.ajax({ 
                url: "./ajax/queryModelExecEnd.jag", 
                type: 'POST',
                async : "false",
                success: function(data){
                    isModelExecFinished = data;
                }, 
                complete: function(){                    
                    if(isModelExecFinished === 'true'){                        
                        $('#wizzardSteps').html("Result is 101% accuracy"); 
                        return; 
                    } else{
                        $('#wizzardSteps').html("Model building starts....");                         
                        setTimeout(poll, 20*1000); // setting polling interval to 20 seconds
                    }
                }, 
                timeout: 30*1000}); // setting connection timeout to 30 seconds         
        }; 
        // calling poll function       
        poll();
    }
});
