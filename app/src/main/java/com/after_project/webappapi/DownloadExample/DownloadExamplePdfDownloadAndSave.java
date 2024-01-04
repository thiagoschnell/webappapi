package com.after_project.webappapi;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.webkit.ValueCallback;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileOutputStream;
public class DownloadExamplePdfDownloadAndSave extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_example_pdf_download_and_save);
        if(MyApp.getInstance().getWebAppStatus() == MyApp.WEBAPP_STATUS_LOAD_FINISHED) {
            Add_Loading_Text("Downloading pdf...");
            {
                try {
                    MyApp.getInstance().getWebApp().evalJavaScript("request_download('https://realappexample.shop/SampleFile.pdf')",
                            new ValueCallback() {
                                @Override
                                public void onReceiveValue(Object value) {
                                    try {
                                        JsonObject json = JsonParser.parseString((String) value).getAsJsonObject();
                                        JsonElement data = json.get("data");
                                        if (data != null) {
                                            Add_Loading_Text("pdf download success");
                                            byte[] downloaded_bytes = Base64.decode(data.getAsString(), Base64.DEFAULT);
                                            Add_Loading_Text("Saving pdf...");
                                            {
                                                FileManager fileManager = new FileManager();
                                                openPdf(fileManager,downloaded_bytes);
                                            }
                                        } else {
                                            Add_Loading_Text("pdf download error");
                                            JsonElement error = json.get("error");
                                            if (error != null && !error.isJsonNull()) {
                                                Add_Loading_Text(error.getAsString());
                                            } else {
                                                Toast.makeText(DownloadExamplePdfDownloadAndSave.this, "download url error", Toast.LENGTH_LONG).show();
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
        }else{
            Add_Loading_Text("Webapp is not loaded.");
        }
    }
    @RequiresApi(19)
    private void openPdf(FileManager fileManager, byte[] bytes) throws Exception{
        //https://developer.android.com/reference/android/content/Context#getExternalFilesDir(java.lang.String)
        // Create a path where we will place our picture in our own private
        // pictures directory.  Note that we don't really need to place a
        // picture in DIRECTORY_PICTURES, since the media scanner will see
        // all media in these directories; this may be useful with other
        // media types such as DIRECTORY_MUSIC however to help it classify
        // your media for display to the user.
        File filepath_downloads_owner = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        //if you got uninstalled and then reinstall the file access still be normally.
        //https://developer.android.com/reference/android/os/Environment.html#getExternalStorageDirectory()
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDir().
        File filepath_downloads_public = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //if you save the file with filepath_downloads_public
        // And your app got uninstalled this will no longer available to be read using File and basic access permissions.
        File file = new File(filepath_downloads_public,"MySavedFile.pdf");
        try{
            Add_Loading_Text("open file...");
            new FileOutputStream(file);
        }catch (Exception e){
            // error no file access permission
            e.printStackTrace();
            Add_Loading_Text("file access denied...please check first storage permissions, isExternalStorageAvailable, isExternalStorageReadOnly, handle storage mounted etc");
            return;
        }
        Add_Loading_Text("save pdf to the file...");
        fileManager.saveFile(file, bytes);
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, com.after_project.webappapi.BuildConfig.APPLICATION_ID + ".provider", file);
            intent.setDataAndType(uri,getContentResolver().getType(uri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //intent.setPackage("com.google.android.apps.docs");//Google Drive app //specific package name to open pdf file
            try {
                Add_Loading_Text("opening pdf...");
                startActivity(intent);
                Add_Loading_Text("Done.");
            }catch (Exception e){
                Toast.makeText(DownloadExamplePdfDownloadAndSave.this,"no app found to open pdf file",Toast.LENGTH_LONG).show();
            }
        }
    }
    void Add_Loading_Text(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.DownloadExamplePdfDownloadAndSaveLoadingText))
                        .append("\n" + text);
            }
        });
    }
}