package com.after_project.webappapi;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
public class ServiceExampleMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_examples_main);
        //Api Notification Service
        {
            ((Button)findViewById(R.id.ServiceExampleLayoutButtonApiNotificationService)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.StartMyService(getApplicationContext(),ApiNotificationService.TAG);
                }
            });
        }
        //WebApp Service
        {
            ((Button)findViewById(R.id.ServiceExampleLayoutButtonWebAppService)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ServiceExampleMainActivity.this, WebAppServiceActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}