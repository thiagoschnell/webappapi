package com.after_project.webappapi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
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
            appmessage.registerReceiver(RealAppMainActivity.className,appMessageReceiver);
        }
        //WebApp
        {
            WebView webview = new WebView(this);
            webapp = new WebApp(webview,  new WebViewAssetLoader.Builder()

                    .setDomain("realappexample.shop")//site domain here

                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                    .build());
            try {

                webapp.load("https://realappexample.shop/index.html", //server url here
                        new WebAppCallback() {
                    @Override
                    public void onLoadFinish(WebView view, String url) {
                        webapp.detachWebAppCallback();
                        try {
                            webapp.api.setWebAppApiResponse(webAppApiResponse);
                            webapp.api.newTask(new WebAppApiTask(webAppApiRequest)).
                                    prepare("https://realappexample.shop/customer_profile.json", //api url to return Customer profile
                                            new JSONObject(WebApp.DEFAULT_REQUEST_JSON_OPTIONS),
                                            new JSONObject() {{
                                                put("receiverName",RealAppMainActivity.className);
                                                put("param",0);
                                                put("event","request_customer_profile");
                                            }})
                                    .execute();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public void onLoadError(WebView view, WebResourceRequest request, WebResourceErrorCompat error) {
                        webapp.detachWebAppCallback();
                        Add_Loading_Text("\n load error.");
                    }
                    //@Override
                    //public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    //    handleSslError(error);
                    //    handler.proceed();
                    //}
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
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
                case "request_customer_profile": {
                    try
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
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case "get_my_purchases": {
                    try {
                        webapp.api.newTask(new WebAppApiTask(webAppApiRequest)).
                                prepare("https://realappexample.shop/purchases.json", //api url to return Purchases
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
                    break;
                }
                case "get_shop_products": {
                    try {
                        webapp.api.newTask(new WebAppApiTask(webAppApiRequest)).
                                prepare("https://realappexample.shop/products.json", //api url to return Products list
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
        public void onResponseApiConnectionError(String receiverName, JsonObject xhrError) {
           appmessage.sendTo(receiverName,0,"connection_error",null);
        }
        @Override
        public void onResponseApiScriptError(JsonObject error) {
            Snackbar.make(findViewById(android.R.id.content), "Script error", Snackbar.LENGTH_INDEFINITE)
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
    void handleSslError(SslError error){
        switch (error.getPrimaryError()) {
            case SslError.SSL_UNTRUSTED:
                System.out.println( "onReceivedSslError: The certificate authority is not trusted.");
                break;
            case SslError.SSL_EXPIRED:
                System.out.println( "onReceivedSslError: The certificate has expired.");
                break;
            case SslError.SSL_IDMISMATCH:
                System.out.println( "onReceivedSslError: The certificate Hostname mismatch.");
                break;
            case SslError.SSL_NOTYETVALID:
                System.out.println( "onReceivedSslError: The certificate is not yet valid.");
                break;
        }
    }
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
