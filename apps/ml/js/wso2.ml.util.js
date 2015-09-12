/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
    results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function getBaseUrl(fullUrl){
	var parts = fullUrl.split('/');
	return parts[0] + '//' + parts[2];
}
// function to build notifications
function buildNotification(message, notificationType) {
    var builtNotification;
    if(notificationType == 'warning') {
        builtNotification = 
        '<div class="alert alert-warning alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<img src="../../images/icons/ico-error.png" class="indi" />' +
            message +
        '</div>';            
    }
    else if(notificationType == 'info') {
        builtNotification = 
        '<div class="alert alert-info alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<img src="../../images/icons/ico-alert.png" class="indi" />' +
            message +
        '</div>';            
    }
    else if(notificationType == 'success') {
        builtNotification = 
        '<div class="alert alert-success alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<img src="../../images/icons/ico-success.png" class="indi" />' +            
            message +
        '</div>';            
    }
    return builtNotification;
}
// function to handle notifications
function handleNotification(notificationText, notificationArea, notificationType) {
    $(notificationArea).html(buildNotification(notificationText, notificationType));
}

/* pagination plugin */
$.fn.pageMe = function(opts){
    var $this = this,
        defaults = {
            perPage: 7,
            showPrevNext: false,
            numbersPerPage: 1,
            hidePageNumbers: false
        },
        settings = $.extend(defaults, opts);

    var listElement = $this;
    var perPage = settings.perPage;
    var children = listElement.children();
    var pager = $('.pagination');

    if (typeof settings.childSelector!="undefined") {
        children = listElement.find(settings.childSelector);
    }

    if (typeof settings.pagerSelector!="undefined") {
        pager = $(settings.pagerSelector);
    }

    var numItems = children.size();
    //alert(numItems);
    var numPages = Math.ceil(numItems/perPage);

    var curr = 0;
    pager.data("curr",curr);

    if (settings.showPrevNext){
        $('<li><a href="#" class="prev_link">«</a></li>').appendTo(pager);
    }

    while(numPages > curr && (settings.hidePageNumbers==false)){
        $('<li><a href="#" class="page_link">'+(curr+1)+'</a></li>').appendTo(pager);
        curr++;
    }

    if (settings.numbersPerPage>1) {
        $('.page_link').hide();
        $('.page_link').slice(pager.data("curr"), settings.numbersPerPage).show();
    }

    if (settings.showPrevNext){
        $('<li><a href="#" class="next_link">»</a></li>').appendTo(pager);
    }

    pager.find('.page_link:first').addClass('active');
    pager.find('.prev_link').hide();
    if (numPages<=1) {
        pager.find('.next_link').hide();
    }
    pager.children().eq(0).addClass("active");

    children.hide();
    children.slice(0, perPage).show();

    pager.find('li .page_link').click(function(){
        var clickedPage = $(this).html().valueOf()-1;
        goTo(clickedPage,perPage);
        return false;
    });
    pager.find('li .prev_link').click(function(){
        previous();
        return false;
    });
    pager.find('li .next_link').click(function(){
        next();
        return false;
    });


    function previous(){
        var goToPage = parseInt(pager.data("curr")) - 1;
        goTo(goToPage);
    }

    function next(){
        goToPage = parseInt(pager.data("curr")) + 1;
        goTo(goToPage);
    }

    function goTo(page){
        var startAt = page * perPage,
            endOn = startAt + perPage;

        children.css('display','none').slice(startAt, endOn).show();

        if (page>=1) {
            pager.find('.prev_link').show();
        }
        else {
            pager.find('.prev_link').hide();
        }

        if (page<(numPages-1)) {
            pager.find('.next_link').show();
        }
        else {
            pager.find('.next_link').hide();
        }

        pager.data("curr",page);

        if (settings.numbersPerPage>1) {
            $('.page_link').hide();
            $('.page_link').slice(page, settings.numbersPerPage+page).show();
        }

        pager.children().removeClass("active");
        pager.children().eq(page+1).addClass("active");

    }
};
/* end plugin */

// function to sanitize HTML
var tagBody = '(?:[^"\'>]|"[^"]*"|\'[^\']*\')*';

var tagOrComment = new RegExp(
    '<(?:'
    // Comment body.
    + '!--(?:(?:-*[^->])*--+|-?)'
    // Special "raw text" elements whose content should be elided.
    + '|script\\b' + tagBody + '>[\\s\\S]*?</script\\s*'
    + '|style\\b' + tagBody + '>[\\s\\S]*?</style\\s*'
    // Regular name
    + '|/?[a-z]'
    + tagBody
    + ')>',
    'gi');

function sanitize(html) {
    var oldHtml;
    if(typeof html == 'string' || html instanceof String) {        
        do {
        oldHtml = html;
        html = html.replace(tagOrComment, '');
        } while (html !== oldHtml);
        return html.replace(/</g, '&lt;');
    }
    return html;
}    

// fix for vibrate issue on navfix
/*function scrollVibrateFix() {
    if (($(document).height() > $('body').height()) && (($(document).height() - $('body').height()) < 100)) {
        $('body > .container').css('padding-bottom', ($('#nav').height() + $('header').height()));
    } else {
        $('body > .container').css('padding-bottom', $('#nav').height() + 15);
    }
}*/