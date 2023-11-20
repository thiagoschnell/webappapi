// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
package com.after_project.webappapi;
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
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    public void onLoadError(WebView view, WebResourceRequest request, WebResourceErrorCompat error, int errorCode, String description, String failingUrl) {

    }
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
    }
}
public class WebApp {
    WebAppApi api = new WebAppApi();
    WebApp(WebView webView1, WebViewAssetLoader webViewAssetLoader){
        this.webView = webView1;
        LocalContentWebViewClient LC = new LocalContentWebViewClient(webViewAssetLoader);
        this.webView.setWebViewClient(LC);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.webView.clearCache(true);
        this.webView.addJavascriptInterface(new WebAppJavaScriptInterface(), "android");
    }
    static final String DEFAULT_REQUEST_API_OPTIONS = "{'type':'POST', 'headers':{}}";
    static final String DEFAULT_REQUEST_API_OPTIONS_WITH_SERIALIZE = "{'type':'POST', 'data': { 'get_param': 'value' }, 'headers':{}}";
    static final String DEFAULT_REQUEST_JSON_OPTIONS = "{'type':'GET','dataType':'json'}";
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
            System.out.println("onReceivedError LOLLIPOP");
            super.onReceivedError(view,  error.getErrorCode(), error.getDescription().toString(),
                    request.getUrl().toString());
            if(webAppCallback != null) webAppCallback.onLoadError(view, request, error,
                    error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString() );
        }
        @Override
        @SuppressWarnings("deprecation")
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            System.out.println("onReceivedError KITKAT");
            super.onReceivedError(view, errorCode, description, failingUrl);
            if(webAppCallback != null) webAppCallback.onLoadError(view,null,null,
                    errorCode, description, failingUrl);

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
            if(api.response()!=null) {
                try {
                    JsonObject json = JsonParser.parseString(response).getAsJsonObject();
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
                } catch (Exception e) {
                    e.printStackTrace();
                    api.response().onResponseApiException(e);
                }
            }
        }
    }
}
