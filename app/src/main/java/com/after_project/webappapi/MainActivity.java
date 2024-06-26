package com.after_project.webappapi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
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
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder
                                .setMessage("We are running out of Credits\n We don't know when this example and the server will come back online")
                                .setIcon(android.R.drawable.ic_dialog_alert);
                        builder.setPositiveButton("OK",(dialog, which) -> {});
                        AlertDialog alert = builder.create();
                        alert.show();
                    }

                    //Intent intent = new Intent(MainActivity.this, ZipWebsiteToWebViewExampleWithCORS.class);
                    //startActivity(intent);
                }
            });
        }
        //Button Internet Connection
        {
            Button buttonOpenInternetConnection = (Button) findViewById(R.id.ButtonOpenInternetConnection);
            buttonOpenInternetConnection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, InternetConnectionActivity.class);
                    startActivity(intent);
                }
            });
        }
        //Button Service Example
        {
            Button buttonOpenServiceExample = (Button) findViewById(R.id.ButtonOpenServiceExample);
            buttonOpenServiceExample.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ServiceExampleMainActivity.class);
                    startActivity(intent);
                }
            });
        }
        //Button Ultimate WebApp Example
        {
            Button buttonOpenServiceExample = (Button) findViewById(R.id.ButtonOpenUltimateWebAppExample);
            buttonOpenServiceExample.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, UltimateRealAppMainActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        final Context context = this;
        final Intent intent = new Intent(this,MessengerServerService.class);
        new Thread() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    MyApp.getInstance().setLifeRegistryDestroyed();
                });
                MyApp.getInstance().disconnectMessengerClient();
                context.stopService(intent);
            }
        }.start();
    }
    private AppMessageReceiver.ReceiverCallback appMessageReceiverCallback = new AppMessageReceiver.ReceiverCallback() {
        @Override
        public void onReceiveMessage ( int param, String event, String data){
        }
    };
}