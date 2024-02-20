package com.after_project.webappapi;
import android.content.Context;
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
                    Context context = getApplicationContext();
                    Intent intent = new Intent(context, ApiNotificationService.class);
                    MyApp.getInstance().getServiceUtils().schedule(50).startMyService(context,intent);
                   // MyApp.getInstance().getServiceUtils().StartMyService(context,intent);
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
        //Messenger Service
        {
            ((Button)findViewById(R.id.ServiceExampleLayoutButtonMessengerService)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ServiceExampleMainActivity.this, MessengerServiceActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}