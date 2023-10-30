package com.after_project.webappapi;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.webkit.JavascriptInterface;
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
import org.json.JSONException;
import org.json.JSONObject;
public class WebApp {
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
    private WebViewClientCallback webViewClientCallback;
    private IWebAppApi webAppApiCallback;
    CorsApi corsApi = new CorsApi();
    void evalJavaScript(String js, ValueCallback valueCallback){
        webView.evaluateJavascript(js,valueCallback);
    }
    void runJavaScript(String url){
        webView.loadUrl("javascript: " + url);
    }
    void detachWebAppCallback(){
        webViewClientCallback = null;
    }
    class CorsApi{
        protected void request(String api_url,String options,JSONObject callback,IWebAppApi iWebAppApi) throws Exception{
            webAppApiCallback = iWebAppApi;
            {
                if(iWebAppApi!=null){
                    AsyncTask<Void, Void, String > task = new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... params) {
                            int count = 0;
                            while (webAppApiCallback.cancelRequest==null){
                                SystemClock.sleep(300);
                                if(count>=5){
                                    webAppApiCallback.cancelRequest = true;
                                }
                                count++;
                            }
                            return null;
                        }
                        @Override
                        protected void onPostExecute(String token) {
                            if(!webAppApiCallback.cancelRequest){
                                String js = null;
                                try {
                                    js = "request_url('" + api_url + "',$.parseJSON( '" + new JSONObject(options) + "' ) ,$.parseJSON( '" + callback + "' ))";
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    if(iWebAppApi!=null){
                                        iWebAppApi.onRequestApiException(e);
                                    }
                                }
                                runJavaScript(js);
                            }else{
                                webAppApiCallback.onRequestCanceled();
                            }
                        }
                        @Override
                        protected void onPreExecute() {
                            Boolean b = iWebAppApi.onInterceptRequestApi();
                            if(webAppApiCallback.cancelRequest!=null){
                                webAppApiCallback.cancelRequest = b;
                            }
                        }
                    };
                    task.execute();// if you want parallel execution use task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    }
    private class LocalContentWebViewClient extends androidx.webkit.WebViewClientCompat {
        private final androidx.webkit.WebViewAssetLoader mAssetLoader;
        LocalContentWebViewClient(WebViewAssetLoader assetLoader) {
            mAssetLoader = assetLoader;
        }
        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(webViewClientCallback != null){
                Boolean r = webViewClientCallback.onshouldOverrideUrlLoading(view,url);
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
            if(webViewClientCallback != null) webViewClientCallback.onLoadError(view, request, error);
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(webViewClientCallback != null) webViewClientCallback.onLoadFinish(view,url);
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
    }
    void abort(){
        webView.stopLoading();
        detachWebAppCallback();
    }
    void load(String server_url,WebViewClientCallback webViewClientCallback){
        this.webViewClientCallback = webViewClientCallback;
        webView.loadUrl(server_url);
    }
    void load(String server_url){
        load(server_url, null);    }
    private class WebAppInterface {
        WebAppInterface() {
        }
        @JavascriptInterface
        public void response_url(String response) {
            try {
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                if(json.getAsJsonObject("error").has("xhr")){
                    webAppApiCallback.onResponseApiErrorConnection();
                }
                else if (json.getAsJsonObject("error").has("message")){
                    webAppApiCallback.onResponseApiErrorScript();
                }
                else {
                    JsonObject cb = JsonParser.parseString(json.get("cb").getAsString()).getAsJsonObject();
                    webAppApiCallback.onResponseApiSuccess(
                                cb.get("receiverName").getAsString(),
                                cb.get("param").getAsInt(),
                                cb.get("event").getAsString(),
                                json.get("data").getAsString()
                            );
                }
            } catch (Exception e) {
                e.printStackTrace();
                webAppApiCallback.onResponseApiException(e);
            }
        }
    }
    protected interface WebViewClientCallback {
        void onLoadFinish(WebView view, String url);
        Boolean onshouldOverrideUrlLoading(WebView view, String url);
        void onLoadError(WebView view, WebResourceRequest request, WebResourceErrorCompat error);
    }
}
