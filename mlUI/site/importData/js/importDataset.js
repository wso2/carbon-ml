$('document').ready(function() {

	// highlight "import data" menu
	$('#importData').addClass('top_Menu_button menuHiligher');

	$("#dataImportForm").submit(function(e) {
		var fileName = $('#datasetName').val();
		var projectName = $('#projectName').val();

		if(!projectName){
			$('#validatorMsg').html("<span class=\"errorMessage\">Project name is empty!</span>");
		}else if(!fileName) {
			// if dataset name is empty, an error messages is displayed 
			$('#validatorMsg').html("<span class=\"errorMessage\">Data Source is empty!</span>");
		} else {
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
	});

});
