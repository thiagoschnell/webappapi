package com.after_project.webappapi;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class RealAppShopActivity extends AppCompatActivity {
    private AppMessage appmessage;
    private AppMessageReceiver appMessageReceiver;
    static String className = RealAppShopActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_app_shop);
        appmessage = new AppMessage(this);
        //AppMessageReceiver for RealAppShopActivity
        {
            appMessageReceiver = new AppMessageReceiver(new AppMessageReceiver.ReceiverCallback() {
                @Override
                public void onReceiveMessage(int param, String event, String data) {
                    switch (event){
                        case "request_shop_products": {
                            try
                            {
                                JsonObject shop_products_json = JsonParser.parseString(data).getAsJsonObject();
                                {
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
                            }catch (Exception e){
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
                                                Toast.makeText(RealAppShopActivity.this,"To Do on Retry connection error",Toast.LENGTH_LONG).show();
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
            appmessage.registerReceiver(RealAppShopActivity.className,appMessageReceiver);
            appmessage.sendTo(RealAppMainActivity.className,0,"get_shop_products",null);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        appmessage.unregisterReceiver(appMessageReceiver);
    }
}