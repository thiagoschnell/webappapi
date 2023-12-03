// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
$(function(){
    $.StackError = function(e) {
        try{
            if (!e.stack) {
                return "   stack No support"
            }else{
                var stack = e.stack.toString().split(/\r\n|\n/);
                return stack[1].replace(/:/g, ' ');
            }
        } catch(e){
            return "   stack undefined";
        }
    }
    $.ReferenceError = function(e){
        return "ReferenceError: " + e.message + $.StackError(e);
    }
    //Error support 4.4
})
function request_url(url,options,vardata) {
    try {
        result = Object();
        result.cb = vardata;
        result.request_url = url;
        result.error = Object();
        if (typeof $ !== 'undefined') {
        options.timeout = 3000;
            $.ajax(url,options).done(function (data) {
                try {
                    result.data = typeof data === 'object'? data : JSON.parse(data);
                    android.response_url(JSON.stringify(result));
                } catch (e) {
                    result.error.message = $.ReferenceError(e);
                    android.response_url(JSON.stringify(result));
                }
            })
            .fail(function( jqXHR, textStatus, errorThrown ){
                try{
                result.error.xhr = jqXHR;
                android.response_url(JSON.stringify(result));
                } catch (e) {
                    result.error.message = $.ReferenceError(e);
                    android.response_url(JSON.stringify(result));
                }
            })
        return result;
        }
        else {
            throw new Error('$ is not function');
        }
    } catch (e) {
        result.error.message = $.ReferenceError(e);
        android.response_url(JSON.stringify(result));
    }
}