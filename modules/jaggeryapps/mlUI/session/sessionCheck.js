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