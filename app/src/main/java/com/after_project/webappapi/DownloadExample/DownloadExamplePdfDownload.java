package com.after_project.webappapi;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.webkit.ValueCallback;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
public class DownloadExamplePdfDownload extends AppCompatActivity {
    final int PERMISSION_REQUEST_CODE = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_example_pdf_download);
        if(MyApp.getInstance().getWebAppStatus() == MyApp.WEBAPP_STATUS_LOAD_FINISHED) {
            Boolean permission= isStoragePermissionGranted();
            if(!permission){
                Toast.makeText(this,"you need grant permission to continue",Toast.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
            }else{
                Add_Loading_Text("permission success");
                handleAfterPermissionGranted();
            }
        }else{
            Add_Loading_Text("Webapp is not loaded.");
        }
    }
    private void handleAfterPermissionGranted(){
        Add_Loading_Text("isExternalStorageAvailable is " + isExternalStorageAvailable());
        Add_Loading_Text("isExternalStorageReadOnly is " + isExternalStorageReadOnly());
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Add_Loading_Text("unable to continue.");
        }
        else {
            Add_Loading_Text("Downloading pdf...");
            {
                try {
                    MyApp.getInstance().getWebApp().evalJavaScript("request_download('https://realappexample.shop/SampleFile.pdf')",
                            new ValueCallback() {
                                @Override
                                public void onReceiveValue(Object value) {
                                    try {
                                        String filename = "SampleFile.pdf";
                                        String filepath = "MyFileStorage";
                                        File myExternalFile;
                                        JsonObject json = JsonParser.parseString((String) value).getAsJsonObject();
                                        JsonElement data = json.get("data");
                                        if (data != null) {
                                            Add_Loading_Text("pdf download success");
                                            byte[] downloaded_bytes = Base64.decode(data.getAsString(), Base64.DEFAULT);
                                            Add_Loading_Text("Saving pdf...");
                                            {
                                                StorageManager storageManager = new StorageManager();
                                                Add_Loading_Text("get file...");
                                                //myExternalFile = new File(getExternalFilesDir(filepath), filename);
                                                myExternalFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                                                Add_Loading_Text("save pdf to file...");
                                                storageManager.saveFile(myExternalFile, downloaded_bytes);
                                                Add_Loading_Text("opening pdf...");
                                                {
                                                    Intent intent =  new Intent(Intent.ACTION_VIEW);
                                                    intent.setPackage("com.google.android.apps.docs"); //specific package name to open pdf file
                                                    intent.setDataAndType(Uri.fromFile(myExternalFile), "application/pdf");
                                                    try {
                                                        startActivity(intent);
                                                        Add_Loading_Text("Done.");
                                                    }catch (Exception e){
                                                        Add_Loading_Text("no app found to read the pdf");
                                                        Snackbar.make(findViewById(android.R.id.content), "Please install Google Drive App.", Snackbar.LENGTH_INDEFINITE).show();
                                                    }
                                                }
                                            }
                                        } else {
                                            Add_Loading_Text("image download error");
                                            JsonElement error = json.get("error");
                                            if (error != null && !error.isJsonNull()) {
                                                Add_Loading_Text(error.getAsString());
                                            } else {
                                                Toast.makeText(DownloadExamplePdfDownload.this, "download url error", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }
    private boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (Build.VERSION.SDK_INT >= 23) {
                    for (int i = 0; i < permissions.length; i++) {
                        String permission = permissions[i];
                        int grantResult = grantResults[i];
                        if (permission.equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                                Add_Loading_Text("WRITE_EXTERNAL_STORAGE permission success");
                                handleAfterPermissionGranted();
                            } else {
                                Add_Loading_Text("WRITE_EXTERNAL_STORAGE permission denied");
                            }
                        }
                    }
                }
        }
    }
    void Add_Loading_Text(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.DownloadExamplePdfDownloadLoadingText))
                        .append("\n" + text);
            }
        });
    }
}