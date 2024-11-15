package com.after_project.webappapi;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class InternetConnectionActivity extends AppCompatActivity {
    private InternetConnection2 internetConnection2 = null;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            internetConnection2.stopNetworkStateHandle();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_connection);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            internetConnection2 = new InternetConnection2(this);
            internetConnection2.initNetworkStateHandle();
            internetConnection2.getNetworkStateLive().observe(this, (state -> {
                ((TextView)findViewById(R.id.InternetConnectionTextviewIsAvailable)).setText("");
                ((TextView)findViewById(R.id.InternetConnectionTextviewIsOffline)).setText("IsOffline: " + ((Boolean) state==false?"true":"false"));
                ((TextView)findViewById(R.id.InternetConnectionTextviewIsOnline)).setText("IsOnline: " + ((Boolean) state==true?"true":"false"));
            }));
        }else{
            ((TextView)findViewById(R.id.InternetConnectionTextviewIsAvailable)).setText("Android version is not supported");
            ((TextView)findViewById(R.id.InternetConnectionTextviewIsOffline)).setText("");
            ((TextView)findViewById(R.id.InternetConnectionTextviewIsOnline)).setText("");
        }
        ((Button) findViewById(R.id.InternetConnectionButtonCheckInternetConnectionAndRequestUrl)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(!internetConnection2.isConnected()){
                        String error = "{\"error\":{\"xhr\":{\"readyState\":0,\"status\":0,\"statusText\":\"NetworkError: no internet connection access.\"}}}";
                        JsonObject responseJsonObject = JsonParser.parseString((String) error).getAsJsonObject();
                        parseResponse(responseJsonObject);
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MyApp.getInstance()
                                        .getWebApp()
                                        .evalJavaScript(
                                                "url('https://"+getResources().getString(R.string.websiteMainDomain)+"/products.json').get().response().data",
                                                new ValueCallback() {
                                                    @Override
                                                    public void onReceiveValue(Object response) {
                                                        try {
                                                            JsonObject responseJsonObject = JsonParser.parseString((String) response).getAsJsonObject();
                                                            parseResponse(responseJsonObject);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                            }
                        });
                    }
                }
            }
        });
    }
    private void parseResponse(JsonObject jsonObject){
        String title = "";
        String message = "";
        if(jsonObject.has("data")){
            message = "Response data is \n\n" +  jsonObject.toString();
            title= "Request Success";
        }else if (jsonObject.getAsJsonObject("error").has("xhr")) {
            message = "Please Connect to the Internet";
            title= "Connection Error";
        }else {
            message = "Please go to logs";
            title= "Script Error";
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}