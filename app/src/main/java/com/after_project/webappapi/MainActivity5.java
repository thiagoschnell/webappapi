package com.after_project.webappapi;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;
public class MainActivity5 extends AppCompatActivity {
    private MutableLiveData<WebAppApiDataWrapper> liveData = null;
    static String className = MainActivity5.class.getSimpleName();
    private AppMessageReceiver appMessageReceiver;
    private AppMessage appmessage;
    private Boolean extras_parallel = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);
        //getExtras
        {
            if (getIntent().getExtras() != null) {
                extras_parallel = getIntent().getBooleanExtra("Parallel",false);
            }
        }
        appmessage = new AppMessage(this);
        //AppMessageReceiver for MainActivity5
        {
            appMessageReceiver = new AppMessageReceiver(new AppMessageReceiver.ReceiverCallback() {
                @Override
                public void onReceiveMessage(int param, String event, String data) {
                    switch (event) {
                        case "request_customer_profile": {
                                Add_Loading_Text("AppMessage id(" + param +") received success");
                            break;
                        }
                    }
                }
            });
            appmessage.registerReceiver(className,appMessageReceiver);
        }
        //WebApp
        {
            try{
                liveData = new MutableLiveData<>();
                MyApp.getInstance().getWebApp().setLiveData(liveData);
                JSONObject joptions = new JSONObject(WebApp.REQUEST_JSON_OPTIONS_SYNC);
                JSONObject jcallback = new JSONObject() {{
                    put("receiverName",MainActivity5.className);
                    put("param",0);
                    put("event","request_customer_profile");
                }};
                if(!extras_parallel) {
                    MyApp.getInstance().getWebApp().api.newTask(new WebAppApiCustomTask2(101)).prepare("customer_profile.json", joptions, jcallback).execute();
                    joptions = new JSONObject(WebApp.REQUEST_JSON_OPTIONS_SYNC);
                    MyApp.getInstance().getWebApp().api.newTask(new WebAppApiCustomTask2(202)).prepare("customer_profile.json", joptions, jcallback).execute();
                    joptions = new JSONObject(WebApp.REQUEST_JSON_OPTIONS_SYNC);
                    MyApp.getInstance().getWebApp().api.newTask(new WebAppApiCustomTask2(303)).prepare("customer_profile.json", joptions, jcallback).execute();
                }else{
                    MyApp.getInstance().getWebApp().api.newTask(new WebAppApiCustomTask2(101)).prepare("customer_profile.json", joptions, jcallback).executeParallel();
                    joptions = new JSONObject(WebApp.REQUEST_JSON_OPTIONS_SYNC);
                    MyApp.getInstance().getWebApp().api.newTask(new WebAppApiCustomTask2(202)).prepare("customer_profile.json", joptions, jcallback).executeParallel();
                    joptions = new JSONObject(WebApp.REQUEST_JSON_OPTIONS_SYNC);
                    MyApp.getInstance().getWebApp().api.newTask(new WebAppApiCustomTask2(303)).prepare("customer_profile.json", joptions, jcallback).executeParallel();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        appmessage.unregisterReceiver(appMessageReceiver);
        MyApp.getInstance().getWebApp().setLiveData(null);
    }
    private class WebAppApiCustomTask2 extends WebAppApiTask{
        WebAppApiCustomTask2(int id){
            super(id);
            setWebAppApiRequest(new WebAppApiRequest(){
                @Override
                public void onRequestApi(String api_url, JSONObject options, JSONObject callback) {
                    String js = "$.fn.requestUrl('"+api_url+"',"+options+","+callback+")";
                    MyApp.getInstance().getWebApp().runJavaScript(js);
                }
            });
            set_liveData(liveData);
            setObserver(MainActivity5.this,new WebAppApiResponse2LiveData(String.valueOf(id), new WebAppApiResponse2() {
                @Override
                public void onSuccess(String s) {
                    if(!s.equals("null")){
                        try{
                            JsonObject json = JsonParser.parseString((String)s).getAsJsonObject();
                            JsonObject cb = json.get("cb").getAsJsonObject();
                            if(cb.get("id").getAsInt() == id){
                                liveData.removeObserver(observer);
                                if (json.getAsJsonObject("error").has("xhr")) {
                                    Add_Loading_Text("Task task id(" +id+") connection error");
                                } else if (json.getAsJsonObject("error").has("message")) {
                                    Add_Loading_Text("Task id(" +id+") script error");
                                } else {
                                    Add_Loading_Text("Task id(" +id+") received success");
                                    MyApp.getInstance().getAppMessage().sendSync(className,
                                            id,
                                            cb.get("event").getAsString(),
                                            json.get("data").toString());
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                    }
                }
            }) );
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try
            {
                MyApp.getInstance().getWebApp().evalJavaScript("result = Object(); result.jquery = typeof $ !== 'undefined'; result.script = typeof $.fn.requestUrl !== 'undefined'; result;",
                        new ValueCallback() {
                            @Override
                            public void onReceiveValue(Object value) {
                                try{
                                    JsonObject jo = JsonParser.parseString((String) value).getAsJsonObject();
                                    if(jo.get("jquery").getAsBoolean()){
                                        //Android app Assets jquery-3.6.1.min.js loaded success
                                        Add_Loading_Text("Assets jquery-3.6.1.min.js load success");
                                        if(jo.get("script").getAsBoolean()){
                                            //Android app Assets c.js loaded success
                                            Add_Loading_Text("Assets c.js load success");
                                        }else{
                                            // Assets c.js load error
                                            Add_Loading_Text("Assets c.js load error");
                                            cancel(true);
                                        }
                                    }else{
                                        //Assets jquery-3.6.1.min.js load error
                                        Add_Loading_Text("Assets jquery-3.6.1.min.js load error");
                                        cancel(true);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
            }catch (Exception e){
                try {
                    webAppApiRequest.onRequestApiException(e);
                }catch (Exception ee){
                    ee.printStackTrace();
                }finally {
                    cancel(true);
                }
            }
        }
    }
    void Add_Loading_Text(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.MainActivity5LoadingText))
                        .append("\n" + text);
            }
        });
    }
}