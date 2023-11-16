package com.after_project.webappapi;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class RealAppCustomerProfileActivity extends AppCompatActivity {
    private JsonObject customer_profile_json;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null) {
            setContentView(R.layout.activity_real_app_customer_profile);
            customer_profile_json = JsonParser.parseString(getIntent().getStringExtra("customer_profile")).getAsJsonObject();
            ((TextView)findViewById(R.id.RealAppMyProfileLayoutTextviewCustomerName))
                    .setText(getResources().getString(R.string.customer_profile_name, customer_profile_json.get("customer_name")));
            ((TextView)findViewById(R.id.RealAppMyProfileLayoutTextviewCustomerEmail))
                    .setText(getResources().getString(R.string.customer_profile_email, customer_profile_json.get("customer_email")));
            ((TextView)findViewById(R.id.RealAppMyProfileLayoutTextviewCustomerPhone))
                    .setText(getResources().getString(R.string.customer_profile_phone, customer_profile_json.get("customer_phone")));
        }else{
            Toast.makeText(this,"Error customer_profile",Toast.LENGTH_LONG).show();
        }
    }
}