package com.after_project.webappapi;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class ZipWebsiteToWebViewExampleWebviewActivity extends AppCompatActivity {
    private WebView webview = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zip_website_to_web_view_example_webview);
        if (getIntent().getExtras() != null) {
            webview = (WebView)findViewById(R.id.webview);
            WebSettings settings = webview.getSettings();
            //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                webview.clearCache(true);
                webview.clearHistory();
            //}
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            settings.setAllowFileAccess(true);
            settings.setJavaScriptEnabled(true);
            webview.addJavascriptInterface(new WebviewJavaScriptInterface(), "android");
            webview.loadUrl(getIntent().getStringExtra("loadWebsiteUrl"));
        }else{
            Toast.makeText(this,"Error customer_profile",Toast.LENGTH_LONG).show();
        }
    }
    private class WebviewJavaScriptInterface {
        @JavascriptInterface
        public void get(String requestID, String requestURL) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyApp.getInstance()
                            .getWebApp()
                            .evalJavaScript(
                                    "url('"+requestURL+"').addAndroidCallback().get().response().data",
                                    new ValueCallback() {
                                        @Override
                                        public void onReceiveValue(Object response) {
                                            try {
                                                JsonObject responseJsonObject = JsonParser.parseString((String) response).getAsJsonObject();
                                                webview.loadUrl("javascript: getRequestID(" + requestID + ").response(" + responseJsonObject.toString() + ").trigger();");
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                }
            });
        }
    }
}