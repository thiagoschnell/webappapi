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
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;
public class MainActivity3 extends AppCompatActivity {
    static String className = MainActivity3.class.getSimpleName();
    private AppMessageReceiver mainActivity3AppMessageReceiver;
    private AppMessage mainActivity3AppMessage;
    private WebApp mainActivity3WebApp;
    private Boolean extras_parallel = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Snackbar.make(findViewById(android.R.id.content), R.string.notice_server_php, Snackbar.LENGTH_INDEFINITE).show();
        //getExtras
        {
            if (getIntent().getExtras() != null) {
                extras_parallel = getIntent().getBooleanExtra("Parallel",false);
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
            String[] allowedDomains = {
                    // [START] Additional domains //"realappexample.shop",

                    // [END] Additional domains
                    // [START] Website domain
                            "webappapi-server.azurewebsites.net"
                    // [END] Website domain
            };
            WebView webview = new WebView(this);
            mainActivity3WebApp = new WebApp(webview,new WebViewAssetLoader.Builder()
                    .setDomain(allowedDomains[0])
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                    .build(),
                    allowedDomains,
                    WebApp.FLAG_CLEAR_CACHE);
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
                           if(!extras_parallel){
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
            String js = "$.deprecated.request_url('"+api_url+"',"+options+","+callback+")";
            mainActivity3WebApp.evalJavaScript(js, new ValueCallback() {
                @Override
                public void onReceiveValue(Object value) {
                    //override the WebAppApiResponse
                    {
                        if(!value.equals("null")){
                            try{
                                JsonObject json = JsonParser.parseString((String)value).getAsJsonObject();
                                if(json.has("data")){
                                    JsonObject cb = json.get("cb").getAsJsonObject();
                                    JsonObject data = json.get("data").getAsJsonObject();
                                    mainActivity3AppMessage.sendTo(
                                            cb.get("receiverName").getAsString(),
                                            cb.get("param").getAsInt(),
                                            cb.get("event").getAsString(),
                                            data.toString());
                                }else if (json.getAsJsonObject("error").has("xhr")) {
                                    Add_Loading_Text("connection error");
                                }else {
                                    Add_Loading_Text("script error");
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
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