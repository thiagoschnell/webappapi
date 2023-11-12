package com.after_project.webappapi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;
public class RealAppMainActivity extends AppCompatActivity {
    static String className = RealAppMainActivity.class.getSimpleName();
    private AppMessageReceiver appMessageReceiver;
    private AppMessage appmessage;
    private WebApp webapp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_app_main);
        appmessage = new AppMessage(this);
        //AppMessageReceiver for RealAppMainActivity
        {
            appMessageReceiver = new AppMessageReceiver(appMessageReceiverCallback);
            appmessage.registerReceiver(RealAppMainActivity.className,appMessageReceiver.receiver);
        }
        //WebApp
        {
            WebView webview = new WebView(this);
            webapp = new WebApp(webview,  new WebViewAssetLoader.Builder()
                    .setDomain("webappapi-server.azurewebsites.net")
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                    .build());
            try {
                webapp.load("https://webappapi-server.azurewebsites.net/index.html",new WebAppCallback() {
                    @Override
                    public void onLoadFinish(WebView view, String url) {
                        webapp.detachWebAppCallback();
                        try {
                            webapp.api.setWebAppApiResponse(webAppApiResponse);
                            webapp.api.newTask(new WebAppApiTask(webAppApiRequest)).
                                    prepare("https://webappapi-server.azurewebsites.net/customer_profile.json",
                                            new JSONObject(WebApp.DEFAULT_REQUEST_JSON_OPTIONS),
                                            new JSONObject() {{
                                                put("receiverName",RealAppMainActivity.className); //can also change the receiverName to MainActivity.className
                                                put("param",0);
                                                put("event","request_customer_profile");
                                            }})
                                    .execute();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public void onLoadError(WebView view, WebResourceRequest request, WebResourceErrorCompat error) {
                        webapp.detachWebAppCallback();
                        Add_Loading_Text("\n load error.");
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
        appmessage.unregisterReceiver(appMessageReceiver.receiver);
    }
    private AppMessageReceiver.ReceiverCallback appMessageReceiverCallback = new AppMessageReceiver.ReceiverCallback() {
        @Override
        public void onReceiveMessage ( int param, String event, String data){
            switch (event) {
                case "request_customer_profile": {
                    {
                        ((LinearLayout)findViewById(R.id.RealAppLoadingLayout)).setVisibility(View.GONE);
                        ((LinearLayout)findViewById(R.id.RealAppLayout)).setVisibility(View.VISIBLE);
                        JsonObject json = JsonParser.parseString(data).getAsJsonObject();
                        ((TextView)findViewById(R.id.RealAppLayoutTextviewHello))
                                .setText(getResources().getString(R.string.welcome_messages, json.get("customer_name"), 0));
                        ((Button)findViewById(R.id.RealAppLayoutButtonMyProfile)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(RealAppMainActivity.this, RealAppCustomerProfileActivity.class);
                                intent.putExtra("customer_profile", json.toString());
                                startActivity(intent);
                            }
                        });
                        ((Button)findViewById(R.id.RealAppLayoutButtonMyPurchases)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(RealAppMainActivity.this, RealAppMyPurchasesActivity.class);
                                startActivity(intent);
                            }
                        });
                        ((Button)findViewById(R.id.RealAppLayoutButtonShop)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(RealAppMainActivity.this, RealAppShopActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                    break;
                }
                case "get_my_purchases": {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                webapp.api.newTask(new WebAppApiTask(webAppApiRequest)).
                                        prepare("https://webappapi-server.azurewebsites.net/purchases.json",
                                                new JSONObject(WebApp.DEFAULT_REQUEST_JSON_OPTIONS),
                                                new JSONObject() {{
                                                    put("receiverName",RealAppMyPurchasesActivity.className);
                                                    put("param",0);
                                                    put("event","request_my_purchases");
                                                }})
                                        .execute();
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    break;
                }
                case "get_shop_products": {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                webapp.api.newTask(new WebAppApiTask(webAppApiRequest)).
                                        prepare("https://webappapi-server.azurewebsites.net/products.json",
                                                new JSONObject(WebApp.DEFAULT_REQUEST_JSON_OPTIONS),
                                                new JSONObject() {{
                                                    put("receiverName",RealAppShopActivity.className);
                                                    put("param",0);
                                                    put("event","request_shop_products");
                                                }})
                                        .execute();
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    break;
                }
                case "connection_error":{
                    {
                        Snackbar.make(findViewById(android.R.id.content), "Connetion error", Snackbar.LENGTH_LONG)
                                .setAction("Retry", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(RealAppMainActivity.this,"To Do on Retry connection error",Toast.LENGTH_LONG).show();
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
    private WebAppApiRequest webAppApiRequest = new WebAppApiRequest(){
        @Override
        public void onRequestCanceled() {
            Toast.makeText(RealAppMainActivity.this,"Request Api has canceled.",Toast.LENGTH_LONG).show();
        }
        @Override
        public void onRequestApi(String api_url, JSONObject options, JSONObject callback) {
            String js = "request_url('"+api_url+"',"+options+","+callback+")";
            webapp.runJavaScript(js);
        }
    };
    private WebAppApiResponse webAppApiResponse = new WebAppApiResponse(){
        @Override
        public void onResponseApi(String receiverName, int param, String event, String data) {
            appmessage.sendTo(receiverName,param,event,data);
        }
        @Override
        public void onResponseApiConnectionError(String receiverName) {
           appmessage.sendTo(receiverName,0,"connection_error",null);
        }
        @Override
        public void onResponseApiScriptError() {
            Snackbar.make(findViewById(android.R.id.content), "Script error", Snackbar.LENGTH_LONG)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(RealAppMainActivity.this,"To Do on Retry script error",Toast.LENGTH_LONG).show();
                        }
                    })
                    .setActionTextColor(Color.RED)
                    .show();
        }
    };
    void Add_Loading_Text(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.RealAppLoadingText))
                        .append("\n" + text);
            }
        });
    }
}