
$('document').ready(function(){
	$('#home').addClass('top_Menu_button menuHiligher');
});

function newProject(currentProjectId){  
	if(currentProjectId){
		$(function() {
		    $('<div id="dialog-confirm" title="Confirm"><p>Do you want to save the current project?</p></div>').dialog({
		    	resizable: true,
		    	height:220,
		    	width: 500,
		    	modal: true,
		    	buttons: {
		    		Save: function() {
		    			$( this ).dialog("close");
		    			window.location.href = "../importData/importDataset.jag";
		    		},
		    		Delete: function() {
		    			$( this ).dialog("close");
		    			$.ajax({
							url : "./deleteProject.jag",
							type : 'POST',
							data : 'prijectId='+currentProjectId.toString(),
							success : function(data, textStatus, jqXHR) {
								// TODO
							},
							error : function(jqXHR, textStatus, errorThrown) {
								// TODO: redirect to error page 
							}
						});
		    		},
		    		Cancel: function() {
		    			$( this ).dialog("close");
		    		}
		      	}
		    });
  		});
	}else{
		window.location.href = "../importData/importDataset.jag";
	}
};