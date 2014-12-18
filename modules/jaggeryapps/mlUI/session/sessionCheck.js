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

/* Session check function for UI button clicks */

var jagg = jagg || {};

(function () {
    jagg.syncPost = function(url, data, callback, type) {
        return jQuery.ajax({
            type: "POST",
            url: url,
            data: data,
            async:false,
            success: callback,
            dataType:"json"
        });
    };

    jagg.isSessionOut=function(){
        var loggedOut=false;
        jagg.syncPost("../../session/sessionCheck.jag", { action:"sessionCheck" },
            function (result) {
                if(result!=null){
                    if (result.message == "sessionOut") {
                        loggedOut = true;
                    }
                }
            }, "json");
        return loggedOut;
    };

    jagg.sessionAwareJs=function() {
        if(jagg.isSessionOut()) {
            window.location.href = "../home/login.jag";
        }
    };
}());