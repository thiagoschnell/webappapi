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
var defaultOptions = {
    'async': true,
    'timeout': 3000
  };
var requestOptions = $.extend(
    defaultOptions,
    options
  );
    try {
        result = Object();
        result.cb = vardata;
        result.request_url = url;
        result.error = Object();
        if (typeof $ !== 'undefined') {

            $.ajax(url,options).done(function (data) {
                try {
                    result.data = typeof data === 'object'? data : JSON.parse(data);
                    requestOptions.async&&android.response_url(JSON.stringify(result));
                } catch (e) {
                    result.error.message = $.ReferenceError(e);
                    requestOptions.async&&android.response_url(JSON.stringify(result));
                }
            })
            .fail(function( jqXHR, textStatus, errorThrown ){
                try{
                result.error.xhr = jqXHR;
                requestOptions.async&&android.response_url(JSON.stringify(result));
                } catch (e) {
                    result.error.message = $.ReferenceError(e);
                    requestOptions.async&&android.response_url(JSON.stringify(result));
                }
            })
        }
        else {
            throw new Error('$ is not function');
        }
    } catch (e) {
        result.error.message = $.ReferenceError(e);
        requestOptions.async&&android.response_url(JSON.stringify(result));
    }
    if(requestOptions.async==false)return result;
}