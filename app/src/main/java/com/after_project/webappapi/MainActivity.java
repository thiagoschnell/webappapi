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
            appMessage.registerReceiver(MainActivity.className,appMessageReceiver);
        }
        //Button Single Request
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
        //Button On Demand Requests
        {
            Button buttonOpenActivity3 = (Button) findViewById(R.id.ButtonOpenActivity3);
            buttonOpenActivity3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, MainActivity3.class);
                    startActivity(intent);
                }
            });
        }
        //Button On Demand "Parallel" Requests
        {
            Button buttonOpenActivity3Parallel = (Button) findViewById(R.id.ButtonOpenActivity3Parallel);
            buttonOpenActivity3Parallel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, MainActivity3.class);
                    intent.putExtra("Parallel", true);
                    startActivity(intent);
                }
            });
        }
        //Button Reall MyApp
        {
            Button buttonOpenRealApp = (Button) findViewById(R.id.ButtonOpenRealApp);
            buttonOpenRealApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, RealAppMainActivity.class);
                    startActivity(intent);
                }
            });
        }
        //Button LoadDataWithBaseUrl Request
        {
            Button buttonOpenActivity4 = (Button) findViewById(R.id.ButtonOpenActivity4);
            buttonOpenActivity4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, MainActivity4.class);
                    startActivity(intent);
                }
            });
        }
        //Button JobScheduler
        {
            Button buttonOpenActivity4 = (Button) findViewById(R.id.ButtonOpenJobScheduler);
            buttonOpenActivity4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, JobSchedulerMainActivity.class);
                    startActivity(intent);
                }
            });
        }
        //Button Synchronous Request
        {
            Button buttonNewSynchronousRequest = (Button) findViewById(R.id.ButtonNewSynchronousRequest);
            buttonNewSynchronousRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, MainActivity5.class);
                    intent.putExtra("Parallel", false);
                    startActivity(intent);
                }
            });
        }
        //Button ASynchronous Request
        {
            Button buttonNewASynchronousRequest = (Button) findViewById(R.id.ButtonNewASynchronousRequest);
            buttonNewASynchronousRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, MainActivity5.class);
                    intent.putExtra("Parallel", true);
                    startActivity(intent);
                }
            });
        }
        //Button Download Example
        {
            Button buttonOpenDownloadExample = (Button) findViewById(R.id.ButtonOpenDownloadExample);
            buttonOpenDownloadExample.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, DownloadExampleMainActivity.class);
                    startActivity(intent);
                }
            });
        }
        //Button Zip Website To WebView Example
        {
            Button buttonOpenZipWebsiteToWebViewExample = (Button) findViewById(R.id.ButtonOpenZipWebsiteToWebViewExample);
            buttonOpenZipWebsiteToWebViewExample.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ZipWebsiteToWebViewExampleMainActivity.class);
                    startActivity(intent);
                }
            });
        }
        //Button Zip Website To WebView Example With CORS
        {
            Button buttonOpenZipWebsiteToWebViewExampleWithCORS = (Button) findViewById(R.id.ButtonOpenZipWebsiteToWebViewExampleWithCORS);
            buttonOpenZipWebsiteToWebViewExampleWithCORS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ZipWebsiteToWebViewExampleWithCORS.class);
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