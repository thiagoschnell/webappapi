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
    private MutableLiveData<WebAppApiDataWrapper> liveData = null;
    WebAppApi api = new WebAppApi();
    WebApp(WebView webView1, WebViewAssetLoader webViewAssetLoader){
        this.webView = webView1;
        LocalContentWebViewClient LC = new LocalContentWebViewClient(webViewAssetLoader);
        this.webView.setWebViewClient(LC);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.webView.addJavascriptInterface(new WebAppJavaScriptInterface(), "android");
    }
    void clearCacheRamOnly(){
        this.webView.clearCache(false);
    }
    void clearCache(){
        this.webView.clearCache(true);
    }
    void clearHistory(){
        this.webView.clearHistory();
    }
    void clearSslPreferences(){
        this.webView.clearSslPreferences();
    }
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
    protected void setLiveData(MutableLiveData<WebAppApiDataWrapper> liveData){
        this.liveData = liveData;
    }
    WebApp(WebView webView1, WebViewAssetLoader webViewAssetLoader, @Flags int flags){
        this(webView1,webViewAssetLoader);
        setFlags(flags);
    }
    public static final int FLAG_CLEAR_CACHE_RAM_ONLY = 1; // clear RAM cache ; Note that the cache is per-application, so this will clear the cache for all WebViews used.
    public static final int FLAG_CLEAR_CACHE = 1<<1; // Clears the resource cache ; Note that the cache is per-application, so this will clear the cache for all WebViews used.
    public static final int FLAG_CLEAR_HISTORY = 1<<2; // Clear its internal back/forward list.
    public static final int FLAG_CLEAR_SSL_PREFERENCES = 1<<3; // Clears the SSL preferences table stored in response to proceeding with SSL certificate errors.
    public static final int FLAG_CLEAR = 1<<4;
    @IntDef(
            flag=true,
            value={
                    FLAG_CLEAR_CACHE_RAM_ONLY,
                    FLAG_CLEAR_CACHE,
                    FLAG_CLEAR_HISTORY,
                    FLAG_CLEAR_SSL_PREFERENCES,
                    FLAG_CLEAR
            }
    )
    @Retention(RetentionPolicy.SOURCE)
    public @interface Flags{}
    static final String DEFAULT_REQUEST_API_OPTIONS = "{'type':'POST', 'headers':{}}";
    static final String DEFAULT_REQUEST_JSON_OPTIONS = "{'type':'GET','dataType':'json'}";
    static final String REQUEST_API_OPTIONS_SERIALIZE_EXAMPLE = "{'type':'POST', 'data': { 'get_param': 'value' }, 'headers':{}}";
    static final String REQUEST_JSON_OPTIONS_SYNC = "{'type':'GET','dataType':'json', 'async': false}";
    private WebView webView;
    private WebAppCallback webAppCallback;
    void evalJavaScript(String js, ValueCallback valueCallback){
        webView.evaluateJavascript(js,valueCallback);
    }
    void runJavaScript(String url){
        webView.loadUrl("javascript: " + url);
    }
    void detachWebAppCallback(){
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
    void abort(){
        webView.stopLoading();
        detachWebAppCallback();
    }
    void load(String server_url, WebAppCallback wb){
        this.webAppCallback = wb;
        webView.loadUrl(server_url);
    }
    void loadDataWithBaseUrl(String server_url, RawResource rawResource, WebAppCallback wb){
        //String exampleString = "<html><head><script src=\"/assets/jquery-3.6.1.min.js\"></script><script src=\"/assets/c.js\"></script></head><body ></body></html>";
        this.webAppCallback = wb;
        webView.loadDataWithBaseURL(server_url,
                rawResource.toString(), "text/html", "UTF-8",null);
    }
    void load(String server_url){
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