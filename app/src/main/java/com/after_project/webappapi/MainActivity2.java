package com.after_project.webappapi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
public class MainActivity2 extends AppCompatActivity {
    static String className = MainActivity2.class.getSimpleName();
    private AppMessageReceiver mainActivity2AppMessageReceiver;
    private AppMessage mainActivity2AppMessage;
    private WebApp mainActivity2WebApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mainActivity2AppMessage = new AppMessage(this);
        //AppMessageReceiver for MainActivity2
        {
            mainActivity2AppMessageReceiver = new AppMessageReceiver(mainActivity2AppMessageReceiverCallback);
            mainActivity2AppMessage.registerReceiver(MainActivity2.className,mainActivity2AppMessageReceiver.receiver);
        }
        //WebApp
        {
            WebView webview = new WebView(this);
            mainActivity2WebApp = new WebApp(webview,  new WebViewAssetLoader.Builder()
                    .setDomain("webappapi-server.azurewebsites.net")
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                    .build());
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
                                    "api.php", //  your api_url , can be "api.php" or full url "https://webappapi-server.azurewebsites.net/api.php"
                                    WebApp.DEFAULT_REQUEST_CONFIG_OPTIONS,
                                    new JSONObject() {{
                                        put("receiverName",MainActivity2.className); //can also change the receiverName to MainActivity.class
                                        put("param",0);
                                        put("event","my_request_event_name");
                                    }},new IWebAppApi(){
                                        @Override
                                        public Boolean onInterceptRequestApi() {
                                            {
                                                cancelRequest = null;
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
                                                                        cancelRequest = false;
                                                                    }else{
                                                                        // Assets c.js load error
                                                                        Add_Loading_Text("\n Assets c.js load error");
                                                                        cancelRequest = true;
                                                                    }
                                                                }
                                                            });
                                                        }else{
                                                            //Assets jquery-3.6.1.min.js load error
                                                            Add_Loading_Text("\n Assets jquery-3.6.1.min.js load error");
                                                            cancelRequest = true;
                                                        }
                                                    }
                                                });
                                            }
                                            return false;
                                        }
                                        @Override
                                        public void onRequestCanceled() {
                                            Add_Loading_Text("request canceled");
                                            Toast.makeText(MainActivity2.this,"Request Api has canceled.",Toast.LENGTH_LONG).show();
                                        }
                                        @Override
                                        public void onResponseApiSuccess(String receiverName, int param, String event, String data) {
                                            mainActivity2AppMessage.sendTo(receiverName,param,event,data);
                                        }
                                        @Override
                                        public void onResponseApiErrorConnection() {
                                            Add_Loading_Text("connection error");
                                        }
                                        @Override
                                        public void onResponseApiErrorScript() {
                                            Add_Loading_Text("script error");
                                        }
                                    });
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
    }
    private AppMessageReceiver.ReceiverCallback mainActivity2AppMessageReceiverCallback = new AppMessageReceiver.ReceiverCallback() {
        @Override
        public void onReceiveMessage ( int param, String event, String data){
            switch (event) {
                case "my_request_event_name": {
                    System.out.println("my_request_event_name data " + data);
                    Add_Loading_Text("\n mainActivity2 Broadcast Message Received: my_request_event_name \n Done.");
                    break;
                }
                case "test": {
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