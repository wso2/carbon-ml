
$('document').ready(function(){
	$('#home').addClass('top_Menu_button menuHiligher');

	$('.delete_project').click(function() {
		var projectId=$('#'+this.id+'hiddenField').val();
		$(function() {
		    $('<div id="dialog-confirm" title="Confirm"><p>Are you sure want to delete this project?</p></div>').dialog({
		    	resizable: true,
		    	height:220,
		    	width: 500,
		    	modal: true,
		    	buttons: {
		    		Yes: function() {
		    			$( this ).dialog("close");
		    			$.ajax({
							url : "./deleteProject.jag",
							type : 'POST',
							data : 'projectId='+projectId,
							success : function(data, textStatus, jqXHR) {
								window.location.reload();
							},
							error : function(jqXHR, textStatus, errorThrown) {
								// TODO: redirect to error page 
							}
						});
		    		},
		    		No: function() {
		    			$( this ).dialog("close");
		    		}
		      	}
		    });
  		});
	});

	$('.open_project').click(function() {
		var projectId=$('#'+this.id+'hiddenField').val();
		$.ajax({
			url : "./openProject.jag",
			type : 'POST',
			data : 'projectId='+projectId,
			success : function(data, textStatus, jqXHR) {
				window.location.href = "../importData/datatable.jag";
			},
			error : function(jqXHR, textStatus, errorThrown) {
				// TODO: redirect to error page 
			}
		});
	});

});

function newProject(currentProjectId){
	if(currentProjectId!=""){
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
							data : 'projectId='+currentProjectId,
							success : function(data, textStatus, jqXHR) {
								window.location.href = "../importData/importDataset.jag";
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