package com.after_project.webappapi;
// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
interface WebAppInterface {
    void onLoadFinish(WebView view, String url);
    void onLoadError(WebView view,
            /*RequiresApi(api >= 21)*/WebResourceRequest request, WebResourceErrorCompat error,
            /*RequiresApi(api >=19)*/ int errorCode, String description, String failingUrl);
    void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);
}
class WebAppCallback implements WebAppInterface {
    @Override
    public void onLoadFinish(WebView view, String url) {
    }
    @Override
    public void onLoadError(WebView view, WebResourceRequest request, WebResourceErrorCompat error, int errorCode, String description, String failingUrl){
    }
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
    }
}
public class WebApp {
    protected static final int FLAG_CLEAR_CACHE_RAM_ONLY = 1; // clear RAM cache ; Note that the cache is per-application, so this will clear the cache for all WebViews used.
    protected static final int FLAG_CLEAR_CACHE = 1<<1; // Clears the resource cache ; Note that the cache is per-application, so this will clear the cache for all WebViews used.
    protected static final int FLAG_CLEAR_HISTORY = 1<<2; // Clear its internal back/forward list.
    protected static final int FLAG_CLEAR_SSL_PREFERENCES = 1<<3; // Clears the SSL preferences table stored in response to proceeding with SSL certificate errors.
    protected static final int FLAG_CLEAR = 1<<4;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag=true,value={FLAG_CLEAR_CACHE_RAM_ONLY,FLAG_CLEAR_CACHE,FLAG_CLEAR_HISTORY,FLAG_CLEAR_SSL_PREFERENCES,FLAG_CLEAR})
    private @interface Flags{}
    private void setFlags(@Flags int flags) {
        if ((flags&FLAG_CLEAR_CACHE_RAM_ONLY) != 0){
            clearCacheRamOnly();
        }
        if ((flags&FLAG_CLEAR_CACHE) != 0){
            clearCache();
        }
        if ((flags&FLAG_CLEAR_HISTORY) != 0){
            clearHistory();
        }
        if ((flags&FLAG_CLEAR_SSL_PREFERENCES) != 0){
            clearSslPreferences();
        }
        if ((flags&FLAG_CLEAR) != 0){
        }
    }
    private void clearCacheRamOnly(){
        this.webView.clearCache(false);
    }
    private void clearCache(){
        this.webView.clearCache(true);
    }
    private void clearHistory(){
        this.webView.clearHistory();
    }
    private void clearSslPreferences(){
        this.webView.clearSslPreferences();
    }
    protected static final String DEFAULT_REQUEST_API_OPTIONS = "{'type':'POST', 'headers':{}}";
    protected static final String DEFAULT_REQUEST_JSON_OPTIONS = "{'type':'GET','dataType':'json'}";
    protected static final String REQUEST_API_OPTIONS_SERIALIZE_EXAMPLE = "{'type':'POST', 'data': { 'get_param': 'value' }, 'headers':{}}";
    protected static final String REQUEST_JSON_OPTIONS_SYNC = "{'type':'GET','dataType':'json', 'async': false}";
    private WebView webView;
    private WebAppCallback webAppCallback;
    protected WebAppApi api = new WebAppApi();
    private MutableLiveData<WebAppApiDataWrapper> liveData = null;
    private String[] allowedDomains = null;
    private Boolean javaScriptInputSecurityEnabled = false;
    private JavaScriptInputSecurity javaScriptInputSecurity = null;
    protected void setLiveData(MutableLiveData<WebAppApiDataWrapper> liveData){
        this.liveData = liveData;
    }
    /**
     * JavaScriptInputSecurity.java is powerful tunnel for input Javascript , Its has created to cancel the start of execution javascript string if contains domains that have not allowed.
     * Also JavaScriptInputSecurity works to detect domains,IPv4,functions, and support for prohibit IPv6 in the JavaScript String.
     * To start filtering JavaScriptInputSecurity use .enableJavaScriptInputSecurity();
     */
    protected synchronized WebApp enableJavaScriptInputSecurity(Boolean prohibitSquareBracketsIpv6){
        javaScriptInputSecurityEnabled = true;
        javaScriptInputSecurity.prohibitSquareBracketsIpv6(prohibitSquareBracketsIpv6);
        return this;
    }
    protected void stopJavaScriptInputSecurity(){
        javaScriptInputSecurityEnabled = false;
    }
    /**
     *
     * @param s Add a string identified as domain to the ignoreJavascriptStrings ArrayList.
     *          Its a Extra security layer for webapp execute JavaScript string.
     *          WebApp can now start filtering .evalJavascript() or .runJavaScript() to identify domains that may not const in the allowedDomains list in webapp.
     *          To execute JavaScript functions like fn.RequestUrl() or console.log() those functions will go be identified as domain and you need to go on JavaScriptInputSecurity.java ignoreJavascriptStrings variable to add.
     *          And to execute URL requests as baseurl like "...requestUrl('api.php?id=0')" or
     *          "...request_url('products.json#example')" also identify as domain too.
     *
     */
    protected void ignoreDomain(String s) {
        if(javaScriptInputSecurity!=null) {
            javaScriptInputSecurity.addIgnoreJavascriptString(s);
        }
    }
    WebApp(WebView webView1, WebViewAssetLoader webViewAssetLoader){
        this.webView = webView1;
        LocalContentWebViewClient LC = new LocalContentWebViewClient(webViewAssetLoader);
        this.webView.setWebViewClient(LC);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.webView.addJavascriptInterface(new WebAppJavaScriptInterface(), "android");
    }
    /**Example of use allowedDomains and CORS
     * if you use webApp.load("https://realappexample.shop/") or webApp.loadDataWithBaseUrl("https://realappexample.shop/",
     * and want request apis with urls have domain name "webappapi-server.azurewebsites.net"
     * then add the "webappapi-server.azurewebsites.net" in the allowedDomains at below variable,
     * now go to your server hosting that are using the domain "realappexample.shop"
     * and make this CORS settings:
     *
     * Access-Control-Allow-Origin
     *      ->webappapi-server.azurewebsites.net
     *
     *
     * You may also must add headers in the file you are requesting
     * For example in PHP file, to allow a site at "realappexample.shop" to access the resource using CORS, the header should be:
     *
     * < ?php  header("Access-Control-Allow-Origin: webappapi-server.azurewebsites.net"); ?>
     *
     */
    WebApp(WebView webView1, WebViewAssetLoader webViewAssetLoader,String[] allowedDomains, @Flags int flags){
        this(webView1,webViewAssetLoader);
        setFlags(flags);
        this.allowedDomains = allowedDomains;
        javaScriptInputSecurity = new JavaScriptInputSecurity(allowedDomains);
    }
    protected void evalJavaScript(String js, ValueCallback valueCallback){
        String error_message = null;
        if(javaScriptInputSecurityEnabled){
            if(javaScriptInputSecurity!=null){
                if(javaScriptInputSecurity.isProhibitSquareBracketsIpv6()){
                    if(javaScriptInputSecurity.containsSquareBracketsIpv6InJavaScript(js)){
                        error_message = "Javascript contains prohibited Square Brackets Ipv6.";
                    }
                }else
                if(!javaScriptInputSecurity.isAllowedDomainsInJavaScriptString(js)){
                    error_message = "Javascript contains domains that are not in domains allowed list.";
                }
            }
        }
        if(error_message==null) {
            webView.evaluateJavascript(js, valueCallback);
        }else{
            Toast.makeText(MyApp.getInstance(),error_message,Toast.LENGTH_LONG).show();
        }
    }
    protected void runJavaScript(String js){
        String error_message = null;
        if(javaScriptInputSecurityEnabled){
            if(javaScriptInputSecurity!=null){
                if(javaScriptInputSecurity.isProhibitSquareBracketsIpv6()){
                    if(javaScriptInputSecurity.containsSquareBracketsIpv6InJavaScript(js)){
                        error_message = "Javascript contains prohibited Square Brackets Ipv6.";
                    }
                }else
                if(!javaScriptInputSecurity.isAllowedDomainsInJavaScriptString(js)){
                    error_message = "Javascript contains domains that are not in domains allowed list.";
                }
            }
        }
        if(error_message==null) {
            webView.loadUrl("javascript: " + js);
        }else{
            Toast.makeText(MyApp.getInstance(),error_message,Toast.LENGTH_LONG).show();
        }
    }
    protected void detachWebAppCallback(){
        webAppCallback = null;
    }
    private class LocalContentWebViewClient extends androidx.webkit.WebViewClientCompat {
        private final WebViewAssetLoader mAssetLoader;
        LocalContentWebViewClient(WebViewAssetLoader assetLoader) {
            mAssetLoader = assetLoader;
        }
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceivedError(@NonNull WebView view, @NonNull WebResourceRequest request, @NonNull WebResourceErrorCompat error) {
            super.onReceivedError(view, error.getErrorCode(), error.getDescription().toString(),
                    request.getUrl().toString());
            if(webAppCallback != null){
                webAppCallback.onLoadError(view, request, error,
                        error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString() );
            }
        }
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        @SuppressWarnings("deprecation")
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if(webAppCallback != null){
                webAppCallback.onLoadError(view,null,null,
                        errorCode, description, failingUrl);
            }
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(webAppCallback != null) webAppCallback.onLoadFinish(view,url);
        }
        @Override
        @RequiresApi(21)
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return mAssetLoader.shouldInterceptRequest(request.getUrl());
        }
        @Override
        @SuppressWarnings("deprecation")
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return mAssetLoader.shouldInterceptRequest(Uri.parse(url));
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if(webAppCallback != null){
                webAppCallback.onReceivedSslError(view, handler, error);
            }
        }
    }
    protected void abort(){
        webView.stopLoading();
        detachWebAppCallback();
    }
    protected void load(String server_url, WebAppCallback wb){
        this.webAppCallback = wb;
        webView.loadUrl(server_url);
    }
    protected void loadDataWithBaseUrl(String server_url, RawResource rawResource, WebAppCallback wb){
        this.webAppCallback = wb;
        webView.loadDataWithBaseURL(server_url,
                rawResource.toString(), "text/html", "UTF-8",null);
    }
    protected void load(String server_url){
        load(server_url, null);    }
    private class WebAppJavaScriptInterface {
        WebAppJavaScriptInterface() {
        }
        @JavascriptInterface
        public void response_url(String response) {
            if(liveData!=null){
                WebAppApiDataWrapper dataWrapper = new WebAppApiDataWrapper();
                dataWrapper.setData(response);
                liveData.postValue(dataWrapper);
            }else
            if(api.response()!=null) {
                try {
                    JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                    if(json.has("cb")) {
                        if(!json.get("cb").isJsonNull()) {
                            JsonObject cb = json.get("cb").getAsJsonObject();
                            if (json.getAsJsonObject("error").has("xhr")) {
                                api.response().onResponseApiConnectionError(cb.get("receiverName").getAsString(),
                                        json.getAsJsonObject("error").get("xhr").getAsJsonObject());
                            } else if (json.getAsJsonObject("error").has("message")) {
                                api.response().onResponseApiScriptError(json.getAsJsonObject("error"));
                            } else {
                                JsonObject data = json.get("data").getAsJsonObject();
                                api.response().onResponseApi(
                                        cb.get("receiverName").getAsString(),
                                        cb.get("param").getAsInt(),
                                        cb.get("event").getAsString(),
                                        data.toString()
                                );
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    api.response().onResponseApiException(e);
                }
            }
        }
    }
}
