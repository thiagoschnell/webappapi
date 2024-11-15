package com.after_project.webappapi;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class UltimateRealAppShopActivity extends AppCompatActivity {
    private AppMessenger appMessenger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ultimate_real_app_shop);
        ((Button)findViewById(R.id.UltimateRealAppShopLayoutButtonShowProducts)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    AppMessenger.Request  request = new AppMessenger.Request("https://"+getResources().getString(R.string.websiteMainDomain)+"/products.json");
                    request.setAsync(false);
                    MultiClientOptions multiClientRequestOptions = null;
                    try {
                        appMessenger.sendMsgRequest(Message.obtain(null, Messenger.MSG_WEBAPP_REQUEST_ASYNC),request,multiClientRequestOptions);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        ultShopMessenger(UltimateRealAppShopActivity.this,"ultshop");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(appMessenger.getStatus()==AppMessenger.MESSENGER_CLIENT_STATUS_CONNECTED){
            try {
                appMessenger.disconnect();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void ultShopMessenger(Context context, String name){
        if(appMessenger==null)
        {
            appMessenger = new AppMessenger(context, name);
            try {
                appMessenger.setCallback(new AppMenssengerCallback() {
                    @Override
                    public void onConnectionChanges(MessengerConnection.ConnectionState state, MessengerConnection.ConnectionStatus status, String textStatus) {
                    }
                    @Override
                    public void onConnected(Message msg) {
                    }
                    @Override
                    public void onReceiveMessage(Message msg) {
                        switch (msg.what){
                            case Messenger.MSG_WEBAPP_ERROR:{
                                break;
                            }
                            case Messenger.MSG_WEBAPP_LOADED:{
                                break;
                            }
                            case Messenger.MSG_WEBAPP_RESPONSE: {
                                String response = msg.getData().getString("data");
                                JsonObject jsonObjectResponse= JsonParser.parseString(response).getAsJsonObject();
                                if(jsonObjectResponse.has("data")){
                                    // todo success
                                    {
                                        JsonObject shop_products_json = jsonObjectResponse.get("data").getAsJsonObject();
                                        JsonArray products_list = shop_products_json.getAsJsonArray("products_list");
                                        for (int i=0; i<products_list.size(); i++){
                                            JsonObject jsonProductObject = products_list.get(i).getAsJsonObject();
                                            String product_id = jsonProductObject.get("product_id").getAsString();
                                            String product_name = jsonProductObject.get("product_name").getAsString();
                                            String amount = jsonProductObject.get("amount").getAsString();
                                            ((TextView)findViewById(R.id.RealAppShopLayoutTextviewProductsList))
                                                    .append(getResources().getString(R.string.shop_product_list_append_text,product_id,product_name,amount));
                                        }
                                    }
                                }else if (jsonObjectResponse.getAsJsonObject("error").has("xhr")) {
                                    //todo xhr error
                                    Toast.makeText(UltimateRealAppShopActivity.this,"Connection Error",Toast.LENGTH_LONG).show();
                                }else {
                                    //todo javascript error
                                    Toast.makeText(UltimateRealAppShopActivity.this,"Javascript Error",Toast.LENGTH_LONG).show();
                                }
                                break;
                            }
                            default:
                                super.onReceiveMessage(msg);
                        }
                    }
                });
                appMessenger.connect();
            } catch (Exception e) {
                if(e instanceof ClientIsBinding){
                }else if(e instanceof ClientIsConnecting){
                }else if(e instanceof ClientAlreadyConnected){
                }else if(e instanceof ClientConnectException){
                }
                e.printStackTrace();
            }
        }
    }
}