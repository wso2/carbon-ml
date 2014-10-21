
/*$(document).ready(function(){
    loadModelTypes();
});*/

$('#buildModel').addClass('top_Menu_button menuHiligher');

function loadModelTypes(){
	$('#wizzardSteps').load('modelType.jag');
};
	
function loadQuestions(){  
	$('#wizzardSteps').load('questions.jag');
};

function loadRecommendedAlgos(){  
	$('#wizzardSteps').load('recommendedAlgos.jag');
};

function loadAllAlgos(){  
	$('#wizzardSteps').load('allAlgos.jag');
};


$('input[name=algorithm]').change(function () {
	var algoName=$('input[name=algorithm]:checked')[0].value;
    $('#hyperParameters').load('hyperParameters.jag?algorithm='+algoName);
});


$('#algorithms_continue').click(function () {
	var algoName=$('input[name=algorithm]:checked')[0].value;
    var parameters=$(".parameter_name");
    var values=$(".parameter_value>input");
	var i=0;
    while(parameters[i]!=undefined){
		console.log(parameters[i].innerHTML+":"+values[i].value);
		i++;
	}
	console.log(algoName);
});


$('#questions_continue').click(function () {
	var interpretability=$('#interpretability>option:checked')[0].value;
    var datasetSize=$('#datasetSize>option:checked')[0].value;
    var textual=$('#textual>option:checked')[0].value;
	console.log(interpretability);
	console.log(datasetSize);
	console.log(textual);
	loadRecommendedAlgos();
});