package com.after_project.webappapi;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class RealAppMyPurchasesActivity extends AppCompatActivity {
    private AppMessage appmessage;
    private AppMessageReceiver appMessageReceiver;
    static String className = RealAppMyPurchasesActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_app_my_purchases);
        appmessage = new AppMessage(this);
        //AppMessageReceiver for RealAppMyPurchasesActivity
        {
            appMessageReceiver = new AppMessageReceiver(new AppMessageReceiver.ReceiverCallback() {
                @Override
                public void onReceiveMessage(int param, String event, String data) {
                    switch (event){
                        case "request_my_purchases": {
                            {
                                JsonObject my_purchases_json = JsonParser.parseString(data).getAsJsonObject();
                                {
                                    JSONArray arr = null;
                                    try {
                                        arr = new JSONArray(my_purchases_json.getAsJsonArray("purchases_list").toString());
                                        for (int i=0; i<arr.length(); i++){
                                            JSONObject jsonProductObject = arr.getJSONObject(i);
                                            String product_id = jsonProductObject.getString("product_id");
                                            String product_name = jsonProductObject.getString("product_name");
                                            String purchase_quantity = jsonProductObject.getString("purchase_quantity");
                                            String purchase_status = jsonProductObject.getString("purchase_status");
                                            String purchase_stage = jsonProductObject.getString("purchase_stage");
                                            ((TextView)findViewById(R.id.RealAppMyPurchasesLayoutTextviewPurchasesList))
                                                    .append(getResources().getString(R.string.purchase_list_append_text,product_id,product_name,purchase_quantity,purchase_status,purchase_stage));
                                        }
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                            break;
                        }
                        case "connection_error":{
                            {
                                Snackbar.make(findViewById(android.R.id.content), "Connetion error", Snackbar.LENGTH_LONG)
                                        .setAction("Retry", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Toast.makeText(RealAppMyPurchasesActivity.this,"To Do on Retry connection error",Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .setActionTextColor(Color.RED)
                                        .show();
                            }
                            break;
                        }
                    }
                }
            });
            appmessage.registerReceiver(RealAppMyPurchasesActivity.className,appMessageReceiver.receiver);
            appmessage.sendTo(RealAppMainActivity.className,0,"get_my_purchases",null);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        appmessage.unregisterReceiver(appMessageReceiver.receiver);
    }
}