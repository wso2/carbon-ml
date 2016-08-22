/*
 * Copyright (c) 2005-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

$(document).ready(function(e){
	
	
    //var serverUrl = window.location.origin;

    //var projectName = getParameterByName('projectName');
    //var analysisName = getParameterByName('analysisName');
    //var analysisId = getParameterByName('analysisId');
    //var datasetId = getParameterByName('datasetId');
    //var datasetId = getParameterByName('datasetId');

	draggableInputNo=0;
	draggableHiddenNo=0;
	draggableOutputNo=0;

	$(function() {
		/*
		 * set droppable as a droppable container
		 */
		$( "#droppable" ).droppable({

			drop: function(event, ui) {

				$element=ui.helper.clone();

				if(ui.draggable.attr('id')=='draggableInput'){
					draggableInputNo++;
					$element.attr("id",'draggableInput'+draggableInputNo);
					$element.appendTo(this);
					settingsInput($element);
				}
				else if(ui.draggable.attr('id')=='draggableHidden'){
					draggableHiddenNo++;
					$element.attr("id",'draggableHidden'+draggableHiddenNo);
					$element.appendTo(this);
					settingHidden($element);
				}

				else if(ui.draggable.attr('id')=='draggableOutput'){
					draggableOutputNo++;
					$element.attr("id",'draggableOutput'+draggableOutputNo);
					$element.appendTo(this);
					settingoutput($element);
				}   

				else if(ui.draggable.attr('id')=='nodes'){
					nodeNo++;
					$element.attr("id",'node'+nodeNo);
					$element.appendTo(this);
				}   
				/*
				 * Draw connections using jsPlumb
				 */  
				jsPlumb.ready(function() {
					var common = {
							connector: ["Straight"],
							anchor: ["Left", "Right"],
							//anchor:"AutoDefault",
							endpoint:[ "Dot", { radius:5 } ]
					};

					/*
					 * Connect Input layer to Hidden Layer 1
					 */ 
					jsPlumb.connect({

						source:"draggableInput1",
						target:"draggableHidden1",
						detachable:false,
						paintStyle:{ strokeStyle:"lightgray", lineWidth:2 },
						endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
						overlays:[ 
						          ["Arrow" , { width:12, length:12, location:0.67 }]
						          ]
					}, common);

					/*
					 * Connect Hidden layer 1 to Hidden Layer 2
					 */ 
					jsPlumb.connect({

						source:"draggableHidden1",
						target:"draggableHidden2",
						detachable:false,
						paintStyle:{ strokeStyle:"lightgray", lineWidth:3 },
						endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
						overlays:[ 
						          ["Arrow" , { width:12, length:12, location:0.67 }]
						          ]
					}, common);

					/*
					 * Connect Hidden layer 2 to Hidden Layer 3
					 */ 
					jsPlumb.connect({

						source:"draggableHidden2",
						target:"draggableHidden3",
						detachable:false,
						paintStyle:{ strokeStyle:"lightgray", lineWidth:3 },
						endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
						overlays:[ 
						          ["Arrow" , { width:12, length:12, location:0.67 }]
						          ]
					}, common);

					/*
					 * Connect Hidden layer 3 to Hidden Layer 4
					 */ 
					jsPlumb.connect({

						source:"draggableHidden3",
						target:"draggableHidden4",
						detachable:false,
						paintStyle:{ strokeStyle:"lightgray", lineWidth:3 },
						endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
						overlays:[ 
						          ["Arrow" , { width:12, length:12, location:0.67 }]
						          ]
					}, common);

					/*
					 * Connect Hidden layer 4 to Hidden Layer 5
					 */ 
					jsPlumb.connect({

						source:"draggableHidden4",
						target:"draggableHidden5",
						detachable:false,
						paintStyle:{ strokeStyle:"lightgray", lineWidth:3 },
						endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
						overlays:[ 
						          ["Arrow" , { width:12, length:12, location:0.67 }]
						          ]
					}, common);

					/*
					 * Connect Hidden layer5 to Hidden Layer 6
					 */ 
					jsPlumb.connect({

						source:"draggableHidden5",
						target:"draggableHidden6",
						detachable:false,
						paintStyle:{ strokeStyle:"lightgray", lineWidth:3 },
						endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
						overlays:[ 
						          ["Arrow" , { width:12, length:12, location:0.67 }]
						          ]
					}, common);

					/*
					 * Connect Hidden layer 6 to Hidden Layer 7
					 */ 
					jsPlumb.connect({

						source:"draggableHidden6",
						target:"draggableHidden7",
						detachable:false,
						paintStyle:{ strokeStyle:"lightgray", lineWidth:3 },
						endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
						overlays:[ 
						          ["Arrow" , { width:12, length:12, location:0.67 }]
						          ]
					}, common);

					/*
					 * Connect Hidden layer 7 to Hidden Layer 8
					 */ 
					jsPlumb.connect({

						source:"draggableHidden7",
						target:"draggableHidden8",
						detachable:false,
						paintStyle:{ strokeStyle:"lightgray", lineWidth:3 },
						endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
						overlays:[ 
						          ["Arrow" , { width:12, length:12, location:0.67 }]
						          ]
					}, common);

					/*
					 * Connect Hidden layer 8 to Hidden Layer 9
					 */ 
					jsPlumb.connect({

						source:"draggableHidden8",
						target:"draggableHidden9",
						detachable:false,
						paintStyle:{ strokeStyle:"lightgray", lineWidth:3 },
						endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
						overlays:[ 
						          ["Arrow" , { width:12, length:12, location:0.67 }]
						          ]
					}, common);

					/*
					 * Connect Hidden layer 9 to Hidden Layer 10
					 */ 
					jsPlumb.connect({

						source:"draggableHidden9",
						target:"draggableHidden10",
						detachable:false,
						paintStyle:{ strokeStyle:"lightgray", lineWidth:3 },
						endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
						overlays:[ 
						          ["Arrow" , { width:12, length:12, location:0.67 }]
						          ]
					}, common);

					/*
					 * Connect last Hidden layer to Output Layer 1
					 */ 
					jsPlumb.connect({

						source:"draggableHidden"+draggableHiddenNo,
						target:"draggableOutput1",
						detachable:false,
						paintStyle:{ strokeStyle:"lightgray", lineWidth:3 },
						endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
						overlays:[ 
						          ["Arrow" , { width:12, length:12, location:0.67 }]
						          ]
					}, common);

				});

				/*
				 * set cloned elements to draggable and selectable
				 */ 
				$element.draggable();
				$element.selectable();
			}

		});

		/*
		 * Set dragableInput as a draggable object
		 */ 
		$(".draggableInput").draggable({
			containment: '#droppable',
			cursor: 'move',
			helper: draggableInputHelper,
		});

		/*
		 * Set dragableIHidden as a draggable object
		 */
		$(".draggableHidden").draggable({
			containment: '#droppable',
			cursor: 'move',
			helper: draggableHiddenHelper,
		});

		/*
		 * Set dragableOutput as a draggable object
		 */
		$( ".draggableOutput" ).draggable({
			containment: '#droppable',
			cursor: 'move',
			helper: draggableOutputHelper
		});	 

		/*
		 * Set the title of the diagram
		 */
		$("#NameDiagram").each(function () {
			var label = $(this);
			label.after("<input type = 'text' style = 'display:none' />");
			var textbox = $(this).next();
			var id = this.id.split('_')[this.id.split('_').length - 1];
			textbox[0].name = id.replace("lbl", "txt");
			textbox.val(label.html());
			//When Label is clicked, hide Label and show TextBox.
			label.click(function () {
				$(this).hide();
				$(this).next().show();
			});

			//When focus is lost from TextBox, hide TextBox and show Label.
			textbox.focusout(function () {
				$(this).hide();
				$(this).prev().html($(this).val());
				$(this).prev().show();
			});
		});

		function settingsInput(ui){
			$x=ui.attr('id');
			//curentInputlayerID=$x;
			$("#draggableInput1").dblclick(function(){
				$("#inputsettingModel").modal('show');
			});
		}

		function settingHidden(ui){
			$x=ui.attr('id');
			//curentHiddenlayerID=$x;
			$("#"+$x).dblclick(function(){
				$("#hiddensettingModel").modal('show');
			});
		}

		function settingoutput(ui){
			$x=ui.attr('id');
			//curentOutputlayerID=$x;
			$("#draggableOutput1").dblclick(function(){
				$("#outputsettingModel").modal('show');
			});
		}

	});

	/*
	 * draggableInputHelper method
	 */ 
	function draggableInputHelper(event){
		return '<div id="draggableInput'+draggableInputNo+'" class="draggableInputHelper" ></div>'
	}

	/*
	 * draggableHiddenHelper method
	 */ 
	function draggableHiddenHelper(event){
		return '<div id="draggableHidden'+draggableHiddenNo+'" class="draggableHiddenHelper" ></div>'
	}

	/*
	 * draggableOutputHelper method
	 */ 
	function draggableOutputHelper(event){
		return '<div id="draggableOutput'+draggableOutputNo+'" class="draggableOutputHelper" ></div>'
	}
	
	

});













