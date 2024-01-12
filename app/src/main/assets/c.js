// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
$(function(){
//StackError support android >= API 4.4
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
   $.Base64Encode = function(str){
       var CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
       var out = "", i = 0, len = str.length, c1, c2, c3;
       while (i < len) {
           c1 = str.charCodeAt(i++) & 0xff;
           if (i == len) {out += CHARS.charAt(c1 >> 2);out += CHARS.charAt((c1 & 0x3) << 4);out += "==";break;}
           c2 = str.charCodeAt(i++);
           if (i == len) {out += CHARS.charAt(c1 >> 2);out += CHARS.charAt(((c1 & 0x3)<< 4) | ((c2 & 0xF0) >> 4));out += CHARS.charAt((c2 & 0xF) << 2);out += "=";break;}
           c3 = str.charCodeAt(i++);out += CHARS.charAt(c1 >> 2);out += CHARS.charAt(((c1 & 0x3) << 4) | ((c2 & 0xF0) >> 4));out += CHARS.charAt(((c2 & 0xF) << 2) | ((c3 & 0xC0) >> 6));out += CHARS.charAt(c3 & 0x3F);
       }
       return out;
   }
   $.fn = {
       requestUrl: function(url,options,vardata){
            getRequestUrl(url,options,vardata,undefined);
       }
   }
   function getRequestUrl(url,options,vardata,cb){
       var defaultOptions = {
           'async': true,
           'timeout': 3000
         };
        var requestOptions = $.extend(
           defaultOptions,
           options
         );
         try {
           var result = new Object();
           result.cb = vardata;
           result.request_url = url;
           result.error = Object();
           result.options= requestOptions;
           if (typeof $ !== 'undefined') {
             $.ajax(url,requestOptions)
               .done(function (data) {
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
           }
           else {
               throw new Error('$ is not function');
           }
       } catch (e) {
           result.error.message = $.ReferenceError(e);
           android.response_url(JSON.stringify(result));
       }
       if (typeof cb !== 'undefined') {
           cb(result);
       }
   }
   $.deprecated = {
       request_url: function(url,options,vardata){
          if(!options.async){
                  var f;
                  getRequestUrl(url,options,vardata,function(data){
                      f = data;
                  })
                  return f;
              }else{
                  $.fn.requestUrl(url,options,vardata);
              }
       }
   }
})
function download(download_url,download_headers){
    var result = new Object();
    jQuery.ajax({
        url:download_url,
        cache:false, //Note: Setting cache to false will only work correctly with HEAD and GET requests.
        dataType: 'text',
        headers:download_headers,
        async:false,
        timeout: 3000,
        xhr:function(){
            var xhr = new XMLHttpRequest();
            xhr.overrideMimeType("text/plain; charset=x-user-defined");
            return xhr;
        },
        success: function(data){
            result.data= $.Base64Encode(data);
        },
        error:function( jqXHR, textStatus, errorThrown){
            result.error = JSON.stringify(errorThrown);
        }
    });
    return result;
}
function request_download(download_url){
   return download(download_url,{})
}
function url(url,options,vardata){
    var vardata = vardata;
    var result =  new Object();
    result.cb = vardata;
    result.request_url = url;
    result.error = Object();
    result.toString = function(){
      return JSON.stringify(result);
    }
 try{
    var ajax = $.ajax(url,$.extend({
       'async': false,
       'timeout': 3000,
       'cache':false, //Note: Setting cache to false will only work correctly with HEAD and GET requests.
       'dataType':'json'
     },options));
    var exception = function(e){
        result.error.message = $.ReferenceError(e);
    }
    var androidCallback =false;
    this.addAndroidCallback = function(){
            androidCallback = true;
            return this;
    }
    this.get = function(){
    try{
        this.response = function(){
            this.data = result;
            this.string = result.toString();
            return this;
        }
        handle(ajax)
    }catch(e){
        exception(e)
    }
        return this;
    }
    var handle = function(){
       try{
         ajax.done(function(data){
                    try {
                        result.data = typeof data === 'object'? data : JSON.parse(data);
                    } catch (e) {
                        exception(e)
                    }
                })
                .fail(function(jqXHR,textStatus,errorThrown){
                   try{
                       result.error.xhr = jqXHR;
                       } catch (e) {
                           exception(e)
                       }
                })
                .always(function(){
                    if(androidCallback){
                        android.response_url(JSON.stringify(result));
                    }
                })
       }catch (e){
        exception(e)
       }
    }
 } catch (e) {
    exception(e)
 }
 return this;
}