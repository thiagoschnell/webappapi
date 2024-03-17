package com.after_project.webappapi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class UltimateRealAppMainActivity extends AppCompatActivity {
    private AppMessenger appMessenger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_app_main);
        ((TextView)findViewById(R.id.RealAppLayoutTextviewTitle)).setText("Ultimate WebApp Example");
        ultMainMessenger(UltimateRealAppMainActivity.this,"ultmain");
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
    private void ultMainMessenger(Context context, String name){
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
                                    ((LinearLayout)findViewById(R.id.RealAppLoadingLayout)).setVisibility(View.GONE);
                                    ((LinearLayout)findViewById(R.id.RealAppLayout)).setVisibility(View.VISIBLE);
                                    ((Button)findViewById(R.id.RealAppLayoutButtonMyProfile)).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(UltimateRealAppMainActivity.this, UltimateRealAppCustomerProfileActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                    ((Button)findViewById(R.id.RealAppLayoutButtonMyPurchases)).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(UltimateRealAppMainActivity.this, UltimateRealAppMyPurchasesActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                    ((Button)findViewById(R.id.RealAppLayoutButtonShop)).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(UltimateRealAppMainActivity.this, UltimateRealAppShopActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                }
                                break;
                            }
                            case Messenger.MSG_WEBAPP_RESPONSE: {
                                String response = msg.getData().getString("data");
                                JsonObject jsonObjectResponse= JsonParser.parseString(response).getAsJsonObject();
                                if(jsonObjectResponse.has("data")){
                                    // todo success
                                    {
                                        JsonObject json =  jsonObjectResponse.get("data").getAsJsonObject();
                                        ((TextView)findViewById(R.id.RealAppLayoutTextviewHello))
                                                .setText(getResources().getString(R.string.welcome_messages, json.get("customer_name"), 0));
                                    }
                                }else if (jsonObjectResponse.getAsJsonObject("error").has("xhr")) {
                                    //todo xhr error
                                    Toast.makeText(UltimateRealAppMainActivity.this,"Connection Error",Toast.LENGTH_LONG).show();
                                }else {
                                    //todo javascript error
                                    Toast.makeText(UltimateRealAppMainActivity.this,"Javascript Error",Toast.LENGTH_LONG).show();
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