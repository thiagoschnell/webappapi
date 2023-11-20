package com.after_project.webappapi;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;
public class MainActivity4 extends AppCompatActivity {
    static String className = MainActivity4.class.getSimpleName();
    private AppMessageReceiver mainActivity4AppMessageReceiver;
    private AppMessage mainActivity4AppMessage;
    private WebApp mainActivity4WebApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        mainActivity4AppMessage = new AppMessage(this);
        //AppMessageReceiver for MainActivity4
        {
            mainActivity4AppMessageReceiver = new AppMessageReceiver(mainActivity4AppMessageReceiverCallback);
            mainActivity4AppMessage.registerReceiver(MainActivity4.className,mainActivity4AppMessageReceiver);
        }
        //WebApp
        {
            WebView webview = new WebView(this);
            mainActivity4WebApp = new WebApp(webview,  new WebViewAssetLoader.Builder()
                    .setDomain("realappexample.shop")
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                    .build());
            try {
                Add_Loading_Text("\n Starting load server url ...");
                //load server_url for get ready the origin
                mainActivity4WebApp.loadDataWithBaseUrl("https://realappexample.shop/",
                        new RawResource(getAssets(),"home.html"),
                        new WebAppCallback() {
                    @Override
                    public void onLoadFinish(WebView view, String url) {
                        //load server_url finished
                        Add_Loading_Text("\n load finished.");
                        mainActivity4WebApp.detachWebAppCallback();
                        try{
                            mainActivity4WebApp.api.setWebAppApiResponse(webAppApiResponse);
                            WebAppApiTask webAppApiCustomTask = new WebAppApiTask(){{
                                setWebAppApiTaskCallback(new WebAppApiTaskCallback() {
                                    @Override
                                    public void onPreExecute() {
                                        cancelRequest = null;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mainActivity4WebApp.evalJavaScript("result = Object(); result.jquery = typeof $ !== 'undefined'; result.script = typeof request_url !== 'undefined'; result;",
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
                                                                            Add_Loading_Text_Error("Assets c.js load error");
                                                                            cancelRequest = true;
                                                                        }
                                                                    }else{
                                                                        //Assets jquery-3.6.1.min.js load error
                                                                        Add_Loading_Text_Error("Assets jquery-3.6.1.min.js load error");
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
                            webAppApiCustomTask.setWebAppApiRequest(webAppApiRequest);
                            webAppApiCustomTask
                                    .prepare("customer_profile.json",
                                            new JSONObject(WebApp.DEFAULT_REQUEST_JSON_OPTIONS),
                                            new JSONObject() {{
                                                put("receiverName",MainActivity4.className);
                                                put("param",0);
                                                put("event","request_customer_profile");
                                            }})
                                    .execute();
                        }catch (Exception e){
                            throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public void onLoadError(WebView view,
                            /*RequiresApi(api >= 21)*/WebResourceRequest request, WebResourceErrorCompat error,
                            /*RequiresApi(api >=19)*/ int errorCode, String description, String failingUrl)
                    {
                        //load server_url error
                        mainActivity4WebApp.detachWebAppCallback();
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
    private AppMessageReceiver.ReceiverCallback mainActivity4AppMessageReceiverCallback = new AppMessageReceiver.ReceiverCallback() {
        @Override
        public void onReceiveMessage ( int param, String event, String data){
            switch (event) {
                case "request_customer_profile": {
                    System.out.println("request_customer_profile data " + data);
                    JSONObject json_data = null;
                    try {
                        json_data = new JSONObject(data);
                        Add_Loading_Text("\n mainActivity4 Broadcast Message Received: request_customer_profile customer_name=" + json_data.get("customer_name").toString());
                        Add_Loading_Text_Success("Done.");
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
            Add_Loading_Text_Error("request canceled");
            Toast.makeText(MainActivity4.this,"Request Api has canceled.",Toast.LENGTH_LONG).show();
        }
        @Override
        public void onRequestApi(String api_url, JSONObject options, JSONObject callback) {
            String js = "request_url('"+api_url+"',"+options+","+callback+")";
            mainActivity4WebApp.runJavaScript(js);
        }
    };
    private WebAppApiResponse webAppApiResponse = new WebAppApiResponse(){
        @Override
        public void onResponseApi(String receiverName, int param, String event, String data) {
            mainActivity4AppMessage.sendTo(receiverName,param,event,data);
        }
        @Override
        public void onResponseApiConnectionError(String receiverName, JsonObject xhrError) {
            Add_Loading_Text_Error("connection error");
        }
        @Override
        public void onResponseApiScriptError(JsonObject error) {
            Add_Loading_Text_Error("script error");
        }
    };
    void Add_Loading_Text(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.MainActivity4LoadingText))
                        .append("\n" + text);
            }
        });
    }
    void Add_Loading_Text_Error(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.MainActivity4LoadingText))
                        .append(Html.fromHtml(("\n" +"<font color='#FF0000'>"+text+"</font>").replace("\n","<br>")));
            }
        });
    }
    void Add_Loading_Text_Success(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.MainActivity4LoadingText))
                        .append(Html.fromHtml(("\n" +"<font color='#00FF00'>"+text+"</font>").replace("\n","<br>")));
            }
        });
    }
}