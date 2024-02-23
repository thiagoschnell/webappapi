package com.after_project.webappapi;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class WebAppServiceActivity extends AppCompatActivity {
    private ServiceManager WebAppServiceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_app_service);
        WebAppServiceManager =  new ServiceManager(WebAppServiceActivity.this, WebAppService.class, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WebAppService.MSG_RESPONSE:
                        if(msg.getData()!=null){
                            if(msg.getData().containsKey("data")) {
                                String data = msg.getData().getString("data").toString();
                                MyApp.getInstance().getWebAppLiveData().setValue(data);
                            }else{
                            }
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        });
        WebAppServiceManager.start();
        //Button Send sync
        {
            ((Button)findViewById(R.id.WebAppServiceLayoutButtonSendSync)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        onResponseCallback responseCallback = new onResponseCallback() {
                            @Override
                            public void onResponse(String data) {
                                // todo response data
                            }
                        };
                        newWebAppServiceRequestSynchronous("https://realappexample.shop/userNotifications.json", responseCallback);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        //Button Send Async
        {
            ((Button)findViewById(R.id.WebAppServiceLayoutButtonSendAsync)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        onResponseCallback responseCallback = new onResponseCallback() {
                            @Override
                            public void onResponse(String data) {
                                // todo response data
                            }
                        };
                        newWebAppServiceRequest("https://realappexample.shop/userNotifications.json",responseCallback );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
    private void newWebAppServiceRequest(String url, onResponseCallback responseCallback) throws Exception{
        WebAppServiceCustomRequest request = new WebAppServiceCustomRequest(url, responseCallback);
        request.async = true;
        ((TextView) findViewById(R.id.WebAppServiceLayoutTextviewLoadingText)).append("\nRequest "+(request.async?"Async":"Sync")+" id(" + request.getMyid() + ") pending");
        WebAppServiceManager.sendRequest(Message.obtain(null, WebAppService.MSG_REQUEST_ASYNC), request);
    }
    private void newWebAppServiceRequestSynchronous(String url, onResponseCallback responseCallback) throws Exception{
        WebAppServiceCustomRequest request = new WebAppServiceCustomRequest(url, responseCallback);
        request.async = false;
        ((TextView) findViewById(R.id.WebAppServiceLayoutTextviewLoadingText)).append("\nRequest "+(request.async?"Async":"Sync")+" id(" + request.getMyid() + ") pending");
        WebAppServiceManager.sendRequest(Message.obtain(null, WebAppService.MSG_REQUEST_SYNC), request);
    }
    private class WebAppServiceCustomRequest extends WebAppService.Request {
        boolean async = false;
        WebAppServiceCustomRequest(String url,onResponseCallback responseCallback ){
            super();
            {
                this.url = url;
                observer = new Observer() {
                    @Override
                    public void onChanged(Object o) {
                        try {
                            JsonObject responseJsonObject = JsonParser.parseString((String) o).getAsJsonObject();
                            if(responseJsonObject.get("myid").getAsInt()==getMyid()){
                                {
                                    if(responseJsonObject.has("data")){
                                        // todo success
                                        ((TextView) findViewById(R.id.WebAppServiceLayoutTextviewLoadingText)).append("\nRequest "+(async?"Async":"Sync")+" id(" + getMyid() + ") success");
                                        responseCallback.onResponse(responseJsonObject.get("data").toString());
                                    }else if (responseJsonObject.getAsJsonObject("error").has("xhr")) {
                                        //todo xhr error
                                        ((TextView) findViewById(R.id.WebAppServiceLayoutTextviewLoadingText)).append("\nRequest "+(async?"Async":"Sync")+" id(" + getMyid() + ")  \"Connection Error\"");
                                    }else {
                                        //todo javascript error
                                        ((TextView) findViewById(R.id.WebAppServiceLayoutTextviewLoadingText)).append("\nRequest "+(async?"Async":"Sync")+" id(" + getMyid() + ")  \"JavaScript Error\"");
                                    }
                                }
                                MyApp.getInstance().getWebAppLiveData().removeObserver(observer);
                            }else{
                            }
                        }catch (Exception e ){
                            e.printStackTrace();
                        }
                    }
                };
                MyApp.getInstance().getWebAppLiveData().observe(WebAppServiceActivity.this, observer);
            }
        }
    }
    protected interface onResponseCallback {
        void onResponse(String data);
    }
}