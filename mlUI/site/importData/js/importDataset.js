$('document').ready(function() {

	// highlight "import data" menu
	$('#importData').addClass('top_Menu_button menuHiligher');

	$("#multiform").submit(function(e) {

		var fileName = $('#datasetName').val();

		if (fileName) {
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
					// update the data table 
					$('#dataTable').load('./datatable.jag');
				},
				error : function(jqXHR, textStatus, errorThrown) {
					// TODO: redirect to error page 
				}
			});
		} else {
			// if dataset name is empty, an error messages is displayed 
			$('#validatorMsg').html("<span class=\"errorMessage\">File name is empty!</span>");
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
