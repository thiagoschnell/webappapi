package com.after_project.webappapi;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class UltimateRealAppMyPurchasesActivity extends AppCompatActivity {
    private AppMessenger appMessenger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_app_my_purchases);
        ultPurchasesMessenger(UltimateRealAppMyPurchasesActivity.this,"ultpurchases");
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
    private void ultPurchasesMessenger(Context context, String name){
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
                                {
                                    AppMessenger.Request  request = new AppMessenger.Request("https://"+getResources().getString(R.string.websiteMainDomain)+"/purchases.json");
                                    request.setAsync(false);
                                    MultiClientOptions multiClientRequestOptions = null;
                                    try {
                                        appMessenger.sendMsgRequest(Message.obtain(null, Messenger.MSG_WEBAPP_REQUEST_ASYNC),request,multiClientRequestOptions);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            }
                            case Messenger.MSG_WEBAPP_RESPONSE: {
                                    String response = msg.getData().getString("data");
                                    JsonObject jsonObjectResponse= JsonParser.parseString(response).getAsJsonObject();
                                if(jsonObjectResponse.has("data")){
                                    // todo success
                                    {
                                        JsonObject my_purchases_json = jsonObjectResponse.get("data").getAsJsonObject();
                                        JsonArray purchases_list = my_purchases_json.getAsJsonArray("purchases_list");
                                        for (int i=0; i<purchases_list.size(); i++){
                                            JsonObject jsonPurchaseObject = purchases_list.get(i).getAsJsonObject();
                                            String product_id = jsonPurchaseObject.get("product_id").getAsString();
                                            String product_name = jsonPurchaseObject.get("product_name").getAsString();
                                            String purchase_quantity = jsonPurchaseObject.get("purchase_quantity").getAsString();
                                            String purchase_status = jsonPurchaseObject.get("purchase_status").getAsString();
                                            String purchase_stage = jsonPurchaseObject.get("purchase_stage").getAsString();
                                            ((TextView)findViewById(R.id.RealAppMyPurchasesLayoutTextviewPurchasesList))
                                                    .append(getResources().getString(R.string.purchase_list_append_text,product_id,product_name,purchase_quantity,purchase_status,purchase_stage));
                                        }
                                    }
                                }else if (jsonObjectResponse.getAsJsonObject("error").has("xhr")) {
                                    //todo xhr error
                                    Toast.makeText(UltimateRealAppMyPurchasesActivity.this,"Connection Error",Toast.LENGTH_LONG).show();
                                }else {
                                    //todo javascript error
                                    Toast.makeText(UltimateRealAppMyPurchasesActivity.this,"Javascript Error",Toast.LENGTH_LONG).show();
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