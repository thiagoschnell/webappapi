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
    private InternetConnection internetConnection = null;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        internetConnection.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_connection);
        {
            internetConnection = new InternetConnection(this);
            @InternetConnection.Flags int flags = 0;
            flags = InternetConnection.FLAG_SET_MODE_ALL | InternetConnection.FLAG_INIT_MODE_1| InternetConnection.FLAG_INIT_MODE_2 | InternetConnection.FLAG_INIT_MODE_3;
            internetConnection.setMode(flags);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            internetConnection.mutableLiveDataAvailable.observe(this, (state -> {
                ((TextView)findViewById(R.id.InternetConnectionTextviewIsAvailable)).setText("IsAvailable: " + state);
            }));
        } else{
            ((TextView)findViewById(R.id.InternetConnectionTextviewIsAvailable)).setText("IsAvailable: " + " Android version is not supported");
        }
        internetConnection.mutableLiveDataOffline.observe(this, (state -> {
            ((TextView)findViewById(R.id.InternetConnectionTextviewIsOffline)).setText("IsOffline: " + state);
        }));
        internetConnection.mutableLiveDataOnline.observe(this, (state -> {
            ((TextView)findViewById(R.id.InternetConnectionTextviewIsOnline)).setText("IsOnline: " + state);
        }));
        ((Button) findViewById(R.id.InternetConnectionButtonCheckInternetConnectionAndRequestUrl)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!internetConnection.isOnline()){
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