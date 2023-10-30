package com.after_project.webappapi;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    static String className = MainActivity.class.getSimpleName();
    private AppMessage appMessage;
    private AppMessageReceiver appMessageReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);        
        appMessage = new AppMessage(this);
        //AppMessageReceiver for MainActivity
        {
            appMessageReceiver = new AppMessageReceiver(appMessageReceiverCallback);
            appMessage.registerReceiver(MainActivity.className,appMessageReceiver.receiver);
        }

        {
            Button buttonOpenActivity2 = (Button) findViewById(R.id.ButtonOpenActivity2);
            buttonOpenActivity2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                    startActivity(intent);
                }
            });
        }

    }
    private AppMessageReceiver.ReceiverCallback appMessageReceiverCallback = new AppMessageReceiver.ReceiverCallback() {
        @Override
        public void onReceiveMessage ( int param, String event, String data){
            System.out.println("MainAcitivty onReceiveMessage...");
        }
    };
}
