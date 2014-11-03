
$('document').ready(function(){
	
	$('#buildModel').addClass('top_Menu_button menuHiligher');

	disableWizardMenu();
});



function disableWizardMenu(){
	var color='#848484';
	$('#evaluate').css('color',color);
	$('#evaluate').removeAttr("href");
};

function loadModelTypes(){
	jagg.sessionAwareJs();
	$('#wizzardSteps').load('modelType.jag');
};
	
function loadQuestions(data){
	jagg.sessionAwareJs();
	$('#wizzardSteps').load('questions.jag',data);
};

function loadRecommendedAlgos(data){
	jagg.sessionAwareJs();
	$('#wizzardSteps').load('recommendedAlgos.jag',data);
};

function loadAllAlgos(){  
	jagg.sessionAwareJs();
	$('#wizzardSteps').load('allAlgos.jag');
};


$('input[name=algorithm]').change(function () {
	jagg.sessionAwareJs();
	var algoName=$('input[name=algorithm]:checked')[0].value;
    $('#hyperParameters').load('hyperParameters.jag','algorithm='+algoName);
});

$('#algorithms_continue').click(function () {
	jagg.sessionAwareJs();
	var algoName=$('input[name=algorithm]:checked')[0];
    var parameters=$(".parameter_name");
    var values=$(".parameter_value>input");
    var response=$('.responseDropdown option:selected').text();
    var trainDataRatio=$("#trainRatio").slider("value")/100;
	var i=0;
	if(algoName==undefined){
		alert("Please select an algorithm before continue.");
	}else{
		var hyperParametersData='algoName='+algoName.value+'&trainDataRatio='+trainDataRatio;
		if(response!=""){
			hyperParametersData=hyperParametersData+'&response='+response;
		}
		while(parameters[i]!=undefined){
			hyperParametersData=hyperParametersData+'&'+parameters[i].id+'='+values[i].value;
			i++;
		}
		$.ajax({
				url : "./submit.jag",
				type : 'POST',
				data : hyperParametersData,
				success : function(data, textStatus, jqXHR) {
					// TODO
				},
				error : function(jqXHR, textStatus, errorThrown) {
					// TODO: redirect to error page 
				}
			});

	}
});

$('.modelTypeButton').click(function () {
	jagg.sessionAwareJs();
	//get the id of the clicked button
    var modelType=this.id;
	var data='modelType='+modelType;
	loadQuestions(data);
});

$('#questions_continue').click(function () {
	jagg.sessionAwareJs();
	var interpretability=$('#interpretability>option:checked')[0].value;
    var datasetSize=$('#datasetSize>option:checked')[0].value;
    var textual=$('#textual>option:checked')[0].value;
	var data = 'interpretability='+interpretability+'&datasetSize='+datasetSize+'&textual='+textual;
	loadRecommendedAlgos(data);
});