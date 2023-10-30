package com.after_project.webappapi;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import androidx.webkit.WebResourceErrorCompat;
interface IWebAppClient {
    void onLoadFinish(WebView view, String url);
    Boolean onshouldOverrideUrlLoading(WebView view, String url);
    void onLoadError(WebView view, WebResourceRequest request, WebResourceErrorCompat error);
    void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);
}
public class WebAppClient implements IWebAppClient{
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
