package com.after_project.webappapi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;
public class MainActivity3 extends AppCompatActivity {
    static String className = MainActivity3.class.getSimpleName();
    private AppMessageReceiver mainActivity3AppMessageReceiver;
    private AppMessage mainActivity3AppMessage;
    private WebApp mainActivity3WebApp;
    private Boolean extra_parallel = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        //getExtras
        {
            if (getIntent().getExtras() != null) {
                extra_parallel = getIntent().getBooleanExtra("Parallel",false);
            }
        }
        mainActivity3AppMessage = new AppMessage(this);
        //AppMessageReceiver for MainActivity3
        {
            mainActivity3AppMessageReceiver = new AppMessageReceiver(mainActivity3AppMessageReceiverCallback);
            mainActivity3AppMessage.registerReceiver(MainActivity3.className,mainActivity3AppMessageReceiver);
        }
        //WebApp
        {
            WebView webview = new WebView(this);
            mainActivity3WebApp = new WebApp(webview,  new WebViewAssetLoader.Builder()
                    .setDomain("webappapi-server.azurewebsites.net")
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                    .build());
            try {
                Add_Loading_Text("\n Starting load server url ...");
                //load server_url for get ready the origin
                mainActivity3WebApp.load("https://webappapi-server.azurewebsites.net/index.html",new WebAppCallback() {
                    @Override
                    public void onLoadFinish(WebView view, String url) {
                        //load server_url finished
                        Add_Loading_Text("\n load finished.");
                        mainActivity3WebApp.detachWebAppCallback();
                        try {
                            JSONObject joptions = new JSONObject(WebApp.DEFAULT_REQUEST_API_OPTIONS);
                            joptions.put("async",false);
                            JSONObject jcallback = new JSONObject() {{
                                put("receiverName",MainActivity3.className);
                                put("param",0);
                                put("event","my_request_event_name");
                            }};
                            mainActivity3WebApp.api.setWebAppApiResponse(webAppApiResponse);
                            WebAppApiTask webAppApiTask1 = new WebAppApiTask(){{
                                setWebAppApiTaskCallback(new WebAppApiTaskCallback() {
                                    @Override
                                    public void onPreExecute() {
                                        cancelRequest = null;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mainActivity3WebApp.evalJavaScript("result = Object(); result.jquery = typeof $ !== 'undefined'; result.script = typeof request_url !== 'undefined'; result;",
                                                        new ValueCallback() {
                                                            @Override
                                                            public void onReceiveValue(Object value) {
                                                                {
                                                                    JsonObject jo = JsonParser.parseString((String) value).getAsJsonObject();
                                                                    if(jo.get("jquery").getAsBoolean()){
                                                                        //Android app Assets jquery-3.6.1.min.js loaded success
                                                                        Add_Loading_Text("Assets jquery-3.6.1.min.js load success");
                                                                        if(jo.get("script").getAsBoolean()){
                                                                            //Android app Assets c.js loaded success
                                                                            Add_Loading_Text("Assets c.js load success");
                                                                            cancelRequest = false;
                                                                        }else{
                                                                            // Assets c.js load error
                                                                            Add_Loading_Text("Assets c.js load error");
                                                                            cancelRequest = true;
                                                                        }
                                                                    }else{
                                                                        //Assets jquery-3.6.1.min.js load error
                                                                        Add_Loading_Text("Assets jquery-3.6.1.min.js load error");
                                                                        cancelRequest = true;
                                                                    }
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                                    }
                                });
                            }};
                            webAppApiTask1.setWebAppApiRequest(webAppApiRequest);
                            webAppApiTask1.prepare("api.php", joptions,jcallback).execute(AsyncTask.THREAD_POOL_EXECUTOR);
                           if(!extra_parallel){
                                //On Demand Requests
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=0", joptions,jcallback).execute();
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=1", joptions,jcallback).execute();
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=2", joptions,jcallback).execute();
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=3", joptions,jcallback).execute();
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=4", joptions,jcallback).execute();
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=5", joptions,jcallback).execute();
                           }else{
                               //On Demand "Parallel" Requests
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=0", joptions,jcallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=1", joptions,jcallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=2", joptions,jcallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=3", joptions,jcallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=4", joptions,jcallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                               mainActivity3WebApp.api.newTask(new WebAppApiTask(webAppApiRequest)).prepare("api.php?id=5", joptions,jcallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                           }
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
                        mainActivity3WebApp.detachWebAppCallback();
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
        mainActivity3AppMessage.unregisterReceiver(mainActivity3AppMessageReceiver);
    }
    private AppMessageReceiver.ReceiverCallback mainActivity3AppMessageReceiverCallback = new AppMessageReceiver.ReceiverCallback() {
        @Override
        public void onReceiveMessage ( int param, String event, String data){
            switch (event) {
                case "my_request_event_name": {
                    System.out.println("my_request_event_name data " + data);
                    JSONObject json_data = null;
                    try {
                        json_data = new JSONObject(data);
                        Add_Loading_Text("\n mainActivity3 Broadcast Message Received: my_request_event_name id=" + json_data.get("id").toString()  +  " \n Done.");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
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
            Toast.makeText(MainActivity3.this,"Request Api has canceled.",Toast.LENGTH_LONG).show();
        }
        @Override
        public void onRequestApi(String api_url, JSONObject options, JSONObject callback) {
            String js = "request_url('"+api_url+"',"+options+","+callback+")";
            mainActivity3WebApp.evalJavaScript(js, new ValueCallback() {
                @Override
                public void onReceiveValue(Object value) {
                    //To do
                    // on request completed
                }
            });
        }
    };
    private WebAppApiResponse webAppApiResponse = new WebAppApiResponse(){
        @Override
        public void onResponseApi(String receiverName, int param, String event, String data) {
            mainActivity3AppMessage.sendTo(receiverName,param,event,data);
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
                ((TextView) findViewById(R.id.MainActivity3LoadingText))
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