package com.after_project.webappapi;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
public class DownloadExampleMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_example_main);
        //Download Image
        {
            ((Button)findViewById(R.id.DownloadExampleLayoutButtonImageDownload)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DownloadExampleMainActivity.this, DownloadExampleImageDownload.class);
                    startActivity(intent);
                }
            });
        }
        //Download Image and Save
        {
            ((Button)findViewById(R.id.DownloadExampleLayoutButtonImageDownloadAndSave)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DownloadExampleMainActivity.this, DownloadExampleImageDownloadAndSave.class);
                    startActivity(intent);
                }
            });
        }
    }
}