package com.after_project.webappapi;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class UltimateRealAppCustomerProfileActivity extends AppCompatActivity {
    private AppMessenger appMessenger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ultProfileMessenger(this,"ultprofile");
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
    private void ultProfileMessenger(Context context, String name){
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
                        {
                            AppMessenger.Request  request = new AppMessenger.Request("https://realappexample.shop/customer_profile.json");
                            request.setAsync(false);
                            MultiClientOptions multiClientRequestOptions = new MultiClientOptions(new RequestMatchByNames("ultprofile","ultmain"), new RequestMatchByTags("tag1,tag2"));
                            try {
                                appMessenger.sendMsgRequest(Message.obtain(null, Messenger.MSG_WEBAPP_REQUEST_ASYNC),request,multiClientRequestOptions);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
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
                                JsonObject json =  jsonObjectResponse.get("data").getAsJsonObject();
                                {
                                    setContentView(R.layout.activity_real_app_customer_profile);
                                    JsonObject customer_profile_json = JsonParser.parseString(json.toString()).getAsJsonObject();
                                    ((TextView)findViewById(R.id.RealAppMyProfileLayoutTextviewCustomerName))
                                            .setText(getResources().getString(R.string.customer_profile_name, customer_profile_json.get("customer_name")));
                                    ((TextView)findViewById(R.id.RealAppMyProfileLayoutTextviewCustomerEmail))
                                            .setText(getResources().getString(R.string.customer_profile_email, customer_profile_json.get("customer_email")));
                                    ((TextView)findViewById(R.id.RealAppMyProfileLayoutTextviewCustomerPhone))
                                            .setText(getResources().getString(R.string.customer_profile_phone, customer_profile_json.get("customer_phone")));
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