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