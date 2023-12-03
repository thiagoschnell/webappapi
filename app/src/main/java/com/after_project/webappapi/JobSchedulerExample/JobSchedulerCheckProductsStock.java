package com.after_project.webappapi;
import static com.after_project.webappapi.WebApp.REQUEST_JSON_OPTIONS_SYNC;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
public class JobSchedulerCheckProductsStock extends AppCompatActivity {
    static String className = JobSchedulerCheckProductsStock.class.getSimpleName();
    private AppMessageReceiver appMessageReceiver;
    private AppMessage appmessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_scheduler_check_products_stock);
        appmessage = new AppMessage(this);
        //AppMessageReceiver for JobSchedulerCheckProductsStock
        {
            appMessageReceiver = new AppMessageReceiver(appMessageReceiverCallback);
            appmessage.registerReceiver(className,appMessageReceiver);
        }
        if (getIntent().getExtras() != null ) {
            try{
                JsonObject jsonProducts = JsonParser.parseString(getIntent().getStringExtra("jsonProducts")).getAsJsonObject();
                if(MyApp.getInstance().getWebAppStatus() == MyApp.WEBAPP_STATUS_LOAD_FINISHED){
                    OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorkerParam.class)
                            .setInitialDelay(100, TimeUnit.MILLISECONDS)
                            .setInputData(
                                    new Data.Builder()
                                            .putString("jsonProducts", jsonProducts.toString())
                                            .build()
                            )
                            .build();
                    WorkManager.getInstance(this).beginWith(work).enqueue();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this,"Error jsonProducts",Toast.LENGTH_LONG).show();
        }
    }
    public static class MyWorkerParam extends Worker {
        public MyWorkerParam(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }
        @SuppressLint("WrongThread")
        @NonNull
        @Override
        public Result doWork() {
           try{
               JsonObject jsonProducts = JsonParser.parseString(getInputData().getString("jsonProducts")).getAsJsonObject();
               JsonArray products_list = jsonProducts.getAsJsonArray("products_list");
               for (int i=0; i<products_list.size(); i++){
                   JsonObject jsonProductObject = products_list.get(i).getAsJsonObject();
                   String product_id = jsonProductObject.get("product_id").getAsString();
                   String product_name = jsonProductObject.get("product_name").getAsString();
                   String amount = jsonProductObject.get("amount").getAsString();
                   {
                       MyApp.getInstance().getWebApp().api.newTask(new WebAppApiTask(new WebAppApiRequest(){
                                   @Override
                                   public void onRequestApi(String api_url, JSONObject options, JSONObject callback) {
                                       String js = "request_url('"+api_url+"',"+options+","+callback+")";
                                       MyApp.getInstance().getWebApp().evalJavaScript(js, new ValueCallback() {
                                           @Override
                                           public void onReceiveValue(Object value) {
                                               if(!value.equals("null")){
                                                   try{
                                                       JsonObject json = JsonParser.parseString((String)value).getAsJsonObject();
                                                       if(json.has("data")) {
                                                           JsonObject jsonProduct = json.get("data").getAsJsonObject();
                                                           MyApp.getInstance().getAppMessage().sendSync(JobSchedulerCheckProductsStock.className,
                                                                   Integer.parseInt(product_id),
                                                                   "ui-update_product_stock",
                                                                   jsonProduct.get("amount").getAsString());
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
                               .prepare("product"+product_id+".json",
                                       new JSONObject(REQUEST_JSON_OPTIONS_SYNC),
                                       new JSONObject() {{
                                           put("receiverName",JobSchedulerCheckProductsStock.className);
                                           put("param",-1);
                                           put("event","");
                                       }})
                               .execute();
                   }
               }
               return Result.success();
           }catch (Exception e){
               return Result.failure();
           }
        }
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
                case "ui-update_product_stock": {
                    Add_Loading_Text("Product " + param + " have " + data + " in stock");
                    break;
                }
                case "connection_error":{
                    Add_Loading_Text("connection error");
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
                ((TextView) findViewById(R.id.JobSchedulerCheckProductsStockText))
                        .append("\n" + text);
            }
        });
    }
}