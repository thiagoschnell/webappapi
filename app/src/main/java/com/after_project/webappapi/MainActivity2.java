package com.after_project.webappapi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonObject;
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
            mainActivity2AppMessage.registerReceiver(MainActivity2.className,mainActivity2AppMessageReceiver);
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
                mainActivity2WebApp.load("https://webappapi-server.azurewebsites.net/index.html",new WebAppCallback() {
                    @Override
                    public void onLoadFinish(WebView view, String url) {
                        //load server_url finished
                        Add_Loading_Text("\n load finished.");
                        mainActivity2WebApp.detachWebAppCallback();
                        try {
                            mainActivity2WebApp.api.setWebAppApiResponse(webAppApiResponse);
                            mainActivity2WebApp.api.newTask(new WebAppApiTask(webAppApiRequest))
                                    .prepare("api.php",
                                            new JSONObject(WebApp.DEFAULT_REQUEST_API_OPTIONS),
                                            new JSONObject() {{
                                                put("receiverName",MainActivity2.className); //can also change the receiverName to MainActivity.className
                                                put("param",0);
                                                put("event","my_request_event_name");
                                            }})
                                    .execute();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                        handleSslError(error);
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                            handler.proceed();
                        }
                    }
                    @Override
                    public void onLoadError(WebView view,
                            /*RequiresApi(api >= 21)*/WebResourceRequest request, WebResourceErrorCompat error,
                            /*RequiresApi(api >=19)*/ int errorCode, String description, String failingUrl)
                    {
                        //load server_url error
                        mainActivity2WebApp.detachWebAppCallback();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Add_Loading_Text("\n load error. description: " + error.getDescription() + " url: " + request.getUrl().toString());
                        }else {
                            Add_Loading_Text("\n load error. description: " + description + " url: " + failingUrl);
                        }
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
        mainActivity2AppMessage.unregisterReceiver(mainActivity2AppMessageReceiver);
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
    private WebAppApiRequest webAppApiRequest = new WebAppApiRequest(){
        @Override
        public void onRequestCanceled() {
            Add_Loading_Text("request canceled");
            Toast.makeText(MainActivity2.this,"Request Api has canceled.",Toast.LENGTH_LONG).show();
        }
        @Override
        public void onRequestApi(String api_url, JSONObject options, JSONObject callback) {
            String js = "request_url('"+api_url+"',"+options+","+callback+")";
            mainActivity2WebApp.runJavaScript(js);
        }
    };
    private WebAppApiResponse webAppApiResponse = new WebAppApiResponse(){
        @Override
        public void onResponseApi(String receiverName, int param, String event, String data) {
            mainActivity2AppMessage.sendTo(receiverName,param,event,data);
        }
        @Override
        public void onResponseApiConnectionError(String receiverName, JsonObject xhrError) {
            Add_Loading_Text("connection error");
        }
        @Override
        public void onResponseApiScriptError(JsonObject error) {
            Add_Loading_Text("script error");
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
    void handleSslError(SslError error){
        switch (error.getPrimaryError()) {
            case SslError.SSL_UNTRUSTED:
                System.out.println( "onReceivedSslError: The certificate authority is not trusted.");
                break;
            case SslError.SSL_EXPIRED:
                System.out.println( "onReceivedSslError: The certificate has expired.");
                break;
            case SslError.SSL_IDMISMATCH:
                System.out.println( "onReceivedSslError: The certificate Hostname mismatch.");
                break;
            case SslError.SSL_NOTYETVALID:
                System.out.println( "onReceivedSslError: The certificate is not yet valid.");
                break;
        }
    }
}