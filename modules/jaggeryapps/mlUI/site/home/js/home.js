
$('document').ready(function(){
	$('#home').addClass('top_Menu_button menuHiligher');

	$('.delete_project').click(function() {
		jagg.sessionAwareJs();
		var projectId=$('#'+this.id+'workflow0ProjectHiddenField').val();
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
							url : "./ajax/deleteProject.jag",
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

	$('.open_workflow').click(function() {
		jagg.sessionAwareJs();
		var workflowId=$('#'+this.id+'HiddenField').val();
		var projectId=$('#'+this.id+'ProjectHiddenField').val();
		$.ajax({
			url : "./ajax/openProject.jag",
			type : 'POST',
			data : 'projectId='+projectId+'&workflowId='+workflowId+'&action=openWorkflow',
			success : function(data, textStatus, jqXHR) {
				window.location.href = "../importData/datatable.jag";
			},
			error : function(jqXHR, textStatus, errorThrown) {
				// TODO: redirect to error page 
			}
		});
	});

	$('.delete_workflow').click(function() {
		jagg.sessionAwareJs();
		var workflowId=$('#'+this.id+'HiddenField').val();
		$(function() {
		    $('<div id="dialog-confirm" title="Confirm"><p>Are you sure want to delete this workflow?</p></div>').dialog({
		    	resizable: true,
		    	height:220,
		    	width: 500,
		    	modal: true,
		    	buttons: {
		    		Yes: function() {
		    			$( this ).dialog("close");
		    			$.ajax({
							url : "./ajax/deleteWorkflow.jag",
							type : 'POST',
							data : 'workflowId='+workflowId,
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

	$('.new_workflow').click(function() {
		jagg.sessionAwareJs();
		var projectId=$('#'+this.id+'workflow0ProjectHiddenField').val();

		$(function() {
		    $('<div id="dialog-confirm" title="Create New Workflow"><p>Workflow Name:<input type="text" name="workflowName" id="workflowName"/></p></div>').dialog({
		    	resizable: true,
		    	height:220,
		    	width: 500,
		    	modal: true,
		    	buttons: {
		    		Create: function() {
		    			$( this ).dialog("close");
		    			var workflowName=$('#workflowName').val();
		    			
		    			$.ajax({
							url : "./ajax/openProject.jag",
							type : 'POST',
							data : 'projectId='+projectId+'&action=newWorkflow&workflowName='+workflowName,
							success : function(data, textStatus, jqXHR) {
								window.location.href = "../importData/datatable.jag";
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

		
	});
});

function newProject(currentProjectId){
	jagg.sessionAwareJs();
	window.location.href = "../importData/importDataset.jag";
};

function disableWizardMenu(){
	var color='#848484';
	$('#exploreData').css('color',color);
	$('#exploreData').removeAttr("href");
	$('#buildModel').css('color',color);
	$('#buildModel').removeAttr("href");
	$('#evaluate').css('color',color);
	$('#evaluate').removeAttr("href");
};

function enableWizardMenu(){
	$('#evaluate').css('color',"#848484");
	$('#evaluate').removeAttr("href");
};