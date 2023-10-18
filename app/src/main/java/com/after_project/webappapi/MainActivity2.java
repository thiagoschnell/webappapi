package com.after_project.webappapi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.TextView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
public class MainActivity2 extends AppCompatActivity {
    static String className = MainActivity2.class.getSimpleName();
    private AppMessageReceiver mainActivity2AppMessageReceiver;
    private AppMessageReceiver mainActivity2WebAppMessageReceiver;
    private AppMessage mainActivity2AppMessage;
    private WebApp mainActivity2WebApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mainActivity2AppMessage = new AppMessage(this);
        //create AppMessageReceiver for MainActivity2 and set Receiver Callback as mainActivity2AppMessageReceiverCallback then registerReceiver
        {
            mainActivity2AppMessageReceiver = new AppMessageReceiver(mainActivity2AppMessageReceiverCallback);
            mainActivity2AppMessage.registerReceiver(MainActivity2.className,mainActivity2AppMessageReceiver.receiver);
        }
        //create AppMessageReceiver for WebApp and set Receiver Callback as mainActivity2WebAppMessageReceiverCallback then registerReceiver
        {
            mainActivity2WebAppMessageReceiver = new AppMessageReceiver(mainActivity2WebAppMessageReceiverCallback);
            mainActivity2AppMessage.registerReceiver("App",mainActivity2WebAppMessageReceiver.receiver);
        }
        //WebApp
        {
            WebView webview = new WebView(this);
            //Create mainActivity2WebApp with WebViewAssetLoader.Builder for mix of in-app content and content from the internet,
            //this will go make https://webappapi-server.azurewebsites.net/index.html access your Android App Assets directory.
            // where the index.html source code There are two scripts that have path to "/assets/...
            // <html>...
            //  <script src="/assets/jquery-3.6.1.min.js"></script>
            //  <script src="/assets/c.js"></script>
            //..
            //</html>
            mainActivity2WebApp = new WebApp(webview,  new WebViewAssetLoader.Builder()
                    .setDomain("webappapi-server.azurewebsites.net")
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                    .build());
            mainActivity2WebApp.setWebAppMessageReceiverCallback(mainActivity2WebAppMessageReceiverCallback);
            try {
                Add_Loading_Text("\n Starting load server url ...");
                //load server_url for get ready the origin
                mainActivity2WebApp.load("https://webappapi-server.azurewebsites.net/index.html",new WebApp.WebViewClientCallback() {
                    @Override
                    public void onLoadFinish(WebView view, String url) {
                        //load server_url finished
                        Add_Loading_Text("\n load finished.");
                        mainActivity2WebApp.detachWebAppCallback();
                        try {
                            mainActivity2WebApp.corsApi.request(
                                    "config.php", //  your api_url , can be config.php or full url https://webappapi-server.azurewebsites.net/config.php
                                    WebApp.DEFAULT_REQUEST_CONFIG_OPTIONS,
                                    new JSONObject() {{
                                        //can also change the receiverName to MainActivity.class
                                        put("receiverName",MainActivity2.className);
                                        put("param",0);
                                        put("event","request_url_main_config");
                                    }} );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public Boolean onshouldOverrideUrlLoading(WebView view, String url) {
                        // return false|true or null default
                        return null;
                    }
                    @Override
                    public void onLoadError(WebView view, WebResourceRequest request, WebResourceErrorCompat error) {
                        //load server_url error
                        mainActivity2WebApp.detachWebAppCallback();
                        Add_Loading_Text("\n load error.");
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainActivity2AppMessage.unregisterReceiver(mainActivity2AppMessageReceiver.receiver);
        mainActivity2AppMessage.unregisterReceiver(mainActivity2WebAppMessageReceiver.receiver);
    }
    private AppMessageReceiver.ReceiverCallback mainActivity2AppMessageReceiverCallback = new AppMessageReceiver.ReceiverCallback() {
        @Override
        public void onReceiveMessage ( int param, String event, String data){
            switch (event) {
                case "request_url_main_config": {
                    System.out.println("request_url_main_config data " + data);
                    Add_Loading_Text("\n mainActivity2 Broadcast Message Received: request_url_main_config \n Done.");
                    break;
                }
                case "test": {
                    break;
                }
            }
        }
    };
    private AppMessageReceiver.ReceiverCallback mainActivity2WebAppMessageReceiverCallback = new AppMessageReceiver.ReceiverCallback() {
        @Override
        public void onReceiveMessage ( int param, String event, String data){
            switch (event) {
                case "request_url": {
                    Add_Loading_Text("\n WebApp Broadcast Message Received: request_url");

                    {
                        mainActivity2WebApp.evalJavaScript("typeof $", new ValueCallback() {
                            @Override
                            public void onReceiveValue(Object value) {
                                if(value.toString().equals("\"function\"")){
                                    //Android app Assets jquery-3.6.1.min.js loaded success
                                    Add_Loading_Text("\n Assets jquery-3.6.1.min.js load success");
                                    mainActivity2WebApp.evalJavaScript("typeof request_url", new ValueCallback() {
                                        @Override
                                        public void onReceiveValue(Object value) {
                                            if(value.toString().equals("\"function\"")){
                                                //Android app Assets c.js loaded success
                                                Add_Loading_Text("\n Assets c.js load success");
                                            }else{
                                                // Assets c.js load error
                                                Add_Loading_Text("\n Assets c.js load error");
                                            }
                                        }
                                    });
                                }else{
                                    //Assets jquery-3.6.1.min.js load error
                                    Add_Loading_Text("\n Assets jquery-3.6.1.min.js load error");
                                }
                            }
                        });
                    }

                    try {
                        JsonObject json = JsonParser.parseString(data).getAsJsonObject();
                        String js = "request_url('" + json.get("url").getAsString() + "',$.parseJSON( '" + json.getAsJsonObject("options").toString() + "' ) ,$.parseJSON( '" + json.getAsJsonObject("callback") + "' ))";
                        mainActivity2WebApp.runJavaScript(js);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "response_url" : {
                    {
                        Add_Loading_Text("\n WebApp Broadcast Message Received: response_url");

                        try {
                            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
                            if(json.getAsJsonObject("error").has("xhr")){
                                // connection error
                                Add_Loading_Text("connection error");
                            }
                            else if (json.getAsJsonObject("error").has("message")){
                                //   script error
                                Add_Loading_Text("js error");
                            }
                            else {
                                JsonObject cb = JsonParser.parseString(json.get("cb").getAsString()).getAsJsonObject();
                                mainActivity2AppMessage.sendTo(
                                        cb.get("receiverName").getAsString(),
                                        cb.get("param").getAsInt(),
                                        cb.get("event").getAsString(),
                                        json.get("data").getAsString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
            }
        }
    };
    void Add_Loading_Text(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.MainActivity2LoadingText))
                        .append("\n" + text);
            }
        });
    }
}