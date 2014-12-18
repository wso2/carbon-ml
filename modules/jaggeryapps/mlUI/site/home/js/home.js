
/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

$('document').ready(function() {
    $('#home').addClass('top_Menu_button menuHiligher');

    $('.delete_project').click(function() {
        jagg.sessionAwareJs();
        var projectId = $('#' + this.id + 'workflow0ProjectHiddenField').val();
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
                            data : 'projectId=' + projectId,
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
        var workflowId=$('#' + this.id + 'HiddenField').val();
        var projectId=$('#' + this.id + 'ProjectHiddenField').val();
        $.ajax({
            url : "./ajax/openProject.jag",
            type : 'POST',
            data : 'projectId=' + projectId + '&workflowId=' + workflowId + '&action=openWorkflow',
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
        var workflowId=$('#' + this.id + 'HiddenField').val();
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
                            data : 'workflowId=' + workflowId,
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
        var projectId=$('#' + this.id + 'workflow0ProjectHiddenField').val();

        $(function() {
            $('<div id="dialog-confirm" title="Create New Workflow"><p>Workflow Name:<input type="text" name="workflowName" id="workflowName"/></p></div>').dialog({
                resizable: true,
                height:220,
                width: 500,
                modal: true,
                buttons: {
                    Create: function() {
                        $( this ).dialog("close");
                        var workflowName = $('#workflowName').val();
                        
                        $.ajax({
                            url : "./ajax/openProject.jag",
                            type : 'POST',
                            data : 'projectId=' + projectId + '&action=newWorkflow&workflowName=' + workflowName,
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

function newProject(currentProjectId) {
    jagg.sessionAwareJs();
    window.location.href = "../importData/importDataset.jag";
};

function disableWizardMenu() {
    var color='#848484';
    $('#exploreData').css('color',color);
    $('#exploreData').removeAttr("href");
    $('#buildModel').css('color',color);
    $('#buildModel').removeAttr("href");
    $('#evaluate').css('color',color);
    $('#evaluate').removeAttr("href");
};

function enableWizardMenu() {
    $('#evaluate').css('color',"#848484");
    $('#evaluate').removeAttr("href");
};