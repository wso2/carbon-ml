/**
 * 
 */
$('document').ready(function(){
	
	$('#exploreData').addClass('top_Menu_button menuHiligher');

	disableWizardMenu();
});

function disableWizardMenu(){
	var color='#848484';
	$('#evaluate').css('color',color);
	$('#evaluate').removeAttr("href");
};