
$(document).ready(function(){
    loadModelTypes();
});

$('#buildModel').addClass('top_Menu_button menuHiligher');

function loadModelTypes(){
	$('#wizzardSteps').load('modelType.jag');
	$('#continue').attr("style","display:none");
	$('#back').attr("style","display:none");
};
	
function loadQuestions(){  
	$('#wizzardSteps').load('questions.jag');
	$('#back').attr("style","display:true");
	$('#continue').attr("style","display:true");
	$('#continue').click(function(){
		loadRecommendedAlgos();
	});
	$('#back').click(function(){
		loadModelTypes();
	});
};

function loadRecommendedAlgos(){  
	$('#wizzardSteps').load('recommendedAlgos.jag');
	//$('#back').attr("onclick","loadQuestions();");
	//$('#continue').attr("onclick","loadHyperParameters();");
	$('#continue').click(function(){
		loadHyperParameters();
	});
	$('#back').click(function(){
		loadQuestions();
	});
};

function loadAllAlgos(){  
	$('#wizzardSteps').load('allAlgos.jag');
	//$('#back').attr("onclick","loadQuestions();");
	//$('#continue').attr("onclick","loadHyperParameters();");
	$('#continue').click(function(){
		loadHyperParameters();
	});
	$('#back').click(function(){
		loadQuestions();
	});
};

function loadHyperParameters(){  
	$('#wizzardSteps').load('hyperParameters.jag');
	//$('#continue').attr("onclick","#");
	//$('#back').attr("onclick","loadRecommendedAlgos();");
	$('#continue').click(function(){
		
	});
	$('#back').click(function(){
		loadRecommendedAlgos();
	});
};
