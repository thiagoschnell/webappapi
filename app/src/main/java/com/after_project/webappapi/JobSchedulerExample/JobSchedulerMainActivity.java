package com.after_project.webappapi;
import static com.after_project.webappapi.WebApp.REQUEST_JSON_OPTIONS_SYNC;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
public class JobSchedulerMainActivity extends AppCompatActivity {
    static String className = JobSchedulerMainActivity.class.getSimpleName();
    private AppMessageReceiver appMessageReceiver;
    private AppMessage appmessage;
    private JsonObject jsonProducts = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_scheduler_main);
        appmessage = new AppMessage(this);
        //AppMessageReceiver for JobSchedulerMainActivity
        {
            appMessageReceiver = new AppMessageReceiver(appMessageReceiverCallback);
            appmessage.registerReceiver(className,appMessageReceiver);
        }
        try{
            MyApp.getInstance().getWebApp().api.newTask(new WebAppApiTask(
                    new WebAppApiRequest(){
                        @Override
                        public void onRequestApi(String api_url, JSONObject options, JSONObject callback) {
                            String js = "$.deprecated.request_url('"+api_url+"',"+options+","+callback+")";
                            MyApp.getInstance().getWebApp().evalJavaScript(js, new ValueCallback() {
                                @Override
                                public void onReceiveValue(Object value) {
                                    if(!value.equals("null")){
                                        try{
                                            JsonObject json = JsonParser.parseString((String)value).getAsJsonObject();
                                            if(json.has("data")){
                                                jsonProducts = json.get("data").getAsJsonObject();
                                                findViewById(R.id.JobSchedulerLoadingLayout).setVisibility(View.GONE);
                                                findViewById(R.id.JobSchedulerLayout).setVisibility(View.VISIBLE);
                                            }else if (json.getAsJsonObject("error").has("xhr")) {
                                                MyApp.getInstance().getAppMessage().sendTo(className,0,"connection_error","");
                                            }else {
                                                throw new Error(json.getAsJsonObject("error").toString());
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }else{
                                    }
                                }
                            });
                        }
                    }))
                    .prepare("products.json",
                            new JSONObject(REQUEST_JSON_OPTIONS_SYNC),
                            null)
                    .execute();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        ((Button)findViewById(R.id.JobSchedulerLayoutButtonScheduleOneTime)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JobSchedulerMainActivity.this, JobSchedulerCheckProductsStock.class);
                intent.putExtra("jsonProducts", jsonProducts.toString());
                startActivity(intent);
            }
        });
        ((Button)findViewById(R.id.JobSchedulerLayoutButtonScheduleRepeat)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JobSchedulerMainActivity.this, JobSchedulerCheckPurchaseStatus.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        appmessage.unregisterReceiver(appMessageReceiver);
    }
    private AppMessageReceiver.ReceiverCallback appMessageReceiverCallback = new AppMessageReceiver.ReceiverCallback() {
        @Override
        public void onReceiveMessage ( int param, String event, String data){
            switch (event) {
                case "connection_error":{
                    {
                        Snackbar.make(findViewById(android.R.id.content), "Connetion error", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Retry", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(JobSchedulerMainActivity.this,"To Do on Retry connection error",Toast.LENGTH_LONG).show();
                                    }
                                })
                                .setActionTextColor(Color.RED)
                                .show();
                    }
                    break;
                }
                case "test": {
                    break;
                }
            }
        }
    };
}