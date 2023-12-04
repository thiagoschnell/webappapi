package com.after_project.webappapi;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.BackoffPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
public class JobSchedulerCheckPurchaseStatus extends AppCompatActivity {
    static String className = JobSchedulerCheckPurchaseStatus.class.getSimpleName();
    private AppMessageReceiver appMessageReceiver;
    private AppMessage appmessage;
    private WorkManager mWorkManager;
    private OneTimeWorkRequest work;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_scheduler_check_purchase_status);
        appmessage = new AppMessage(this);
        //AppMessageReceiver for JobSchedulerCheckPurchaseStatus
        {
            appMessageReceiver = new AppMessageReceiver(appMessageReceiverCallback);
            appmessage.registerReceiver(className,appMessageReceiver);
        }
        mWorkManager = WorkManager.getInstance();
        Operation operation = jobScheduler();
        operation.getState().observe(ProcessLifecycleOwner.get(), (state -> {
            if (state instanceof Operation.State.SUCCESS) {
                //Operation successfully
                Add_Loading_Text("Worker success");
                handleJobScheduler();
            } else if (state instanceof Operation.State.FAILURE) {
                // Operarion failure
                Add_Loading_Text("Worker failed");
            }
        }));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        appmessage.unregisterReceiver(appMessageReceiver);
        mWorkManager.cancelAllWorkByTag(CheckPurchaseStatusWorker.TAG);
    }
    private Operation jobScheduler(){
        work = new OneTimeWorkRequest.Builder(CheckPurchaseStatusWorker.class)
                .setBackoffCriteria(BackoffPolicy.LINEAR,10000L,TimeUnit.MILLISECONDS)
                .addTag(CheckPurchaseStatusWorker.TAG)
                .build();
        return mWorkManager.enqueueUniqueWork(className, ExistingWorkPolicy.REPLACE, work);
    }
    private void handleJobScheduler(){
        mWorkManager.getWorkInfoByIdLiveData(work.getId())
                .observe(ProcessLifecycleOwner.get(), (state -> {
                    if(state!=null) {
                        if (state.getState().equals(WorkInfo.State.ENQUEUED)) {
                            try{
                                MyApp.getInstance().getWebApp().api.newTask(new WebAppApiTask(new WebAppApiRequest(){
                                            @Override
                                            public void onRequestApi(String api_url, JSONObject options, JSONObject callback) {
                                                String js = "request_url('"+api_url+"',"+options+","+callback+")";
                                                MyApp.getInstance().getWebApp().runJavaScript(js);
                                            }
                                        }))
                                        .prepare("https://realappexample.shop/purchases.json",
                                        new JSONObject(WebApp.DEFAULT_REQUEST_JSON_OPTIONS),
                                        new JSONObject() {{
                                            put("receiverName",JobSchedulerCheckPurchaseStatus.className);
                                            put("param",0);
                                            put("event","request_purchase_status");
                                        }})
                                        .execute();
                            }catch (Exception e){
                                throw new RuntimeException(e);
                            }
                            //[start cancel]
                                //mWorkManager.cancelWorkById(work.getId());
                            //[end cancel]
                        } else if (state.getState().equals(WorkInfo.State.CANCELLED)) {
                            Add_Loading_Text("Worker cancelled");
                        }
                    }
                }));
    }
    public static class CheckPurchaseStatusWorker extends Worker {
        static String TAG = CheckPurchaseStatusWorker.class.getSimpleName();
        public CheckPurchaseStatusWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }
        @NonNull
        @Override
        public Result doWork() {
            return Result.retry();
        }
    }
    private AppMessageReceiver.ReceiverCallback appMessageReceiverCallback = new AppMessageReceiver.ReceiverCallback() {
        @Override
        public void onReceiveMessage ( int param, String event, String data){
            switch (event) {
                case "request_purchase_status": {
                    try
                    {
                        JsonObject my_purchases_json = JsonParser.parseString(data).getAsJsonObject();
                        {
                            JsonArray purchases_list = my_purchases_json.getAsJsonArray("purchases_list");
                            for (int i=0; i<purchases_list.size(); i++){
                                JsonObject jsonPurchaseObject = purchases_list.get(i).getAsJsonObject();
                                String product_name = jsonPurchaseObject.get("product_name").getAsString();
                                String purchase_status = jsonPurchaseObject.get("purchase_status").getAsString();
                                String purchase_stage = jsonPurchaseObject.get("purchase_stage").getAsString();
                                Add_Loading_Text("\nYour purchase of " + product_name + " status is " +purchase_status + " and stay in " + purchase_stage);
                            }
                        }
                    }catch (Exception e){
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
    void Add_Loading_Text(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.JobSchedulerCheckPurchaseStatusText))
                        .append("\n" + text);
            }
        });
    }
}