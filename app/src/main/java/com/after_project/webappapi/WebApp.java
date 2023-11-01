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
    Boolean onshouldOverrideUrlLoading(WebView view, String url);
    void onLoadError(WebView view, WebResourceRequest request, WebResourceErrorCompat error);
    void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);
}
class WebAppCallback implements WebAppInterface {
    @Override
    public void onLoadFinish(WebView view, String url) {
    }
    @Override
    public Boolean onshouldOverrideUrlLoading(WebView view, String url) {
        return null;
    }
    @Override
    public void onLoadError(WebView view, WebResourceRequest request, WebResourceErrorCompat error) {
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
        this.webView.addJavascriptInterface(new WebAppInterface(), "android");
    }
    static final String DEFAULT_REQUEST_CONFIG_OPTIONS = "{\"type\":\"POST\", \"headers\":{}}";
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
        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(webAppCallback != null){
                Boolean r = webAppCallback.onshouldOverrideUrlLoading(view,url);
                if(r !=null){
                    return r;
                }
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
        @Override
        public void onReceivedError(@NonNull WebView view, @NonNull WebResourceRequest request, @NonNull WebResourceErrorCompat error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                super.onReceivedError(view, request, error);
            }
            if(webAppCallback != null) webAppCallback.onLoadError(view, request, error);
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
    void load(String server_url){
        load(server_url, null);    }
    private class WebAppInterface {
        WebAppInterface() {
        }
        @JavascriptInterface
        public void response_url(String response) {
            if(api.response()!=null)
            try {
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                if(json.getAsJsonObject("error").has("xhr")){
                    api.response().onResponseApiConnectionError();
                }
                else if (json.getAsJsonObject("error").has("message")){
                    api.response().onResponseApiScriptError();
                }
                else {
                    JsonObject cb = JsonParser.parseString(json.get("cb").getAsString()).getAsJsonObject();
                    api.response().onResponseApi(
                                cb.get("receiverName").getAsString(),
                                cb.get("param").getAsInt(),
                                cb.get("event").getAsString(),
                                json.get("data").getAsString()
                            );
                }
            } catch (Exception e) {
                e.printStackTrace();
                api.response().onResponseApiException(e);
            }
        }
    }
}
