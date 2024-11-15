package com.after_project.webappapi;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
public class MessengerServiceActivity extends AppCompatActivity {
    int current_button = 0;
    int last_button = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger_service);
        /**
         *  In high demand requests you should use more timeout, the default timeout value is 3000.
         */
        //Button Send sync
        {
            ((Button)findViewById(R.id.MessengerServiceLayoutButtonSendSync)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        current_button = v.getId();
                        if(last_button==0 || last_button != current_button){
                            last_button = current_button;
                            ((TextView) findViewById(R.id.MessengerServiceLayoutTextviewLoadingText)).setText("");
                        }
                        MyApp.onResponseCallback responseCallback = new MyApp.onResponseCallback() {
                            @Override
                            public void onResponse(MyApp.MessengerServerServiceCustomRequest request, String data) {
                                // todo response data
                                ((TextView) findViewById(R.id.MessengerServiceLayoutTextviewLoadingText)).append("\nRequest "+(request.async?"Async":"Sync")+" id(" + request.getMyid() + ") success");
                            }
                            @Override
                            public void onError(MyApp.MessengerServerServiceCustomRequest request, @WebApp.ResponseError int responseError) {
                                ((TextView) findViewById(R.id.MessengerServiceLayoutTextviewLoadingText)).append("\nRequest "+(request.async?"Async":"Sync")+" id(" + request.getMyid() + ") " + getErrorMessage(responseError));
                            }
                        };
                        sendSync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                        sendSync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                        sendSync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                        sendSync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                        sendSync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                        sendSync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        //Button Send Async
        {
            ((Button)findViewById(R.id.MessengerServiceLayoutButtonSendAsync)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        current_button = v.getId();
                        if(last_button==0 || last_button != current_button){
                            last_button = current_button;
                            ((TextView) findViewById(R.id.MessengerServiceLayoutTextviewLoadingText)).setText("");
                        }
                        MyApp.onResponseCallback responseCallback = new MyApp.onResponseCallback() {
                            @Override
                            public void onResponse(MyApp.MessengerServerServiceCustomRequest request, String data) {
                                // todo response data
                                ((TextView) findViewById(R.id.MessengerServiceLayoutTextviewLoadingText)).append("\nRequest "+(request.async?"Async":"Sync")+" id(" + request.getMyid() + ") success");
                            }
                            @Override
                            public void onError(MyApp.MessengerServerServiceCustomRequest request, @WebApp.ResponseError int responseError) {
                                // todo error
                                ((TextView) findViewById(R.id.MessengerServiceLayoutTextviewLoadingText)).append("\nRequest "+(request.async?"Async":"Sync")+" id(" + request.getMyid() + ") " + getErrorMessage(responseError));
                            }
                        };
                        sendAsync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                        sendAsync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                        sendAsync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                        sendAsync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                        sendAsync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                        sendAsync("https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json", responseCallback);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
    private String getErrorMessage(@WebApp.ResponseError int responseError){
        switch (responseError){
            case WebApp.RESPONSE_CONNECTION_ERROR:{
                return "Connection Error";
            }
            case WebApp.RESPONSE_JAVASCRIPT_ERROR:{
                return "Javascript Error";
            }
            default:{
                return "Unknown";
            }
        }
    }
    private void sendAsync(String url, MyApp.onResponseCallback onResponseCallback) throws Exception {
        MyApp.MessengerServerServiceCustomRequest request= MyApp.getInstance().newMessengerRequest(url,onResponseCallback);
        MyApp.getInstance().AppLiveData.setValue(new JSONObject(){{
            put("url",request.getUrl());
            put("myid",request.getMyid());
            put("async",request.async);
        }}.toString());
        ((TextView) findViewById(R.id.MessengerServiceLayoutTextviewLoadingText)).append("\nRequest "+(request.async?"Async":"Sync")+" id(" + request.getMyid() + ") pending");
    }
    private void sendSync(String url, MyApp.onResponseCallback onResponseCallback) throws Exception {
        MyApp.MessengerServerServiceCustomRequest request= MyApp.getInstance().newMessengerRequestSynchronous(url,onResponseCallback);
        MyApp.getInstance().AppLiveData.setValue(new JSONObject(){{
            put("url",request.getUrl());
            put("myid",request.getMyid());
            put("async",request.async);
        }}.toString());
        ((TextView) findViewById(R.id.MessengerServiceLayoutTextviewLoadingText)).append("\nRequest "+(request.async?"Async":"Sync")+" id(" + request.getMyid() + ") pending");
    }
}
