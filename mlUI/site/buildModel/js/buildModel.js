
$(document).ready(function(){
    loadModelTypes();
});

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

function loadHyperParameters(){  
	$('#wizzardSteps').load('hyperParameters.jag');
};
