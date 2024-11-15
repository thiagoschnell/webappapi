package com.after_project.webappapi;
import static com.after_project.webappapi.WebApp.WEBAPP_STATUS_LOAD_FINISHED;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.ValueCallback;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
public class DownloadExampleImageDownloadAndChecksum extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_download_example_image_download_and_checksum);
        if(MyApp.getInstance().getWebApp().getStatus() == WEBAPP_STATUS_LOAD_FINISHED) {
            try
            {
                Add_Loading_Text("Downloading image...");
                MyApp.getInstance().getWebApp().evalJavaScript("request_download('https://"+getResources().getString(R.string.websiteMainDomain)+"/i-logo.png')",
                        new ValueCallback() {
                            @Override
                            public void onReceiveValue(Object value) {
                                try{
                                    JsonObject json = JsonParser.parseString((String)value).getAsJsonObject();
                                    JsonElement data = json.get("data");
                                    if(data != null){
                                        Add_Loading_Text("image download success");
                                        byte[] image_downloaded = Base64.decode(data.getAsString(), Base64.DEFAULT);
                                        {
                                            Add_Loading_Text("starting checksum ...");
                                            FileManager fileManager = new FileManager();
                                            Add_Loading_Text("verifying download image...");
                                            final String my_private_checksum_md5 = "47fa66b670bb6d8d1749a25bb15cf877";
                                            String download_checksum_md5 = fileManager.getChecksum("MD5",image_downloaded);
                                            if(download_checksum_md5.equals(my_private_checksum_md5)){
                                                Add_Loading_Text("checksum validated success\n Done.");
                                            }else{
                                                Add_Loading_Text("checksum not valid.");
                                            }
                                        }
                                    }else{
                                        Add_Loading_Text("image download error");
                                        JsonElement error = json.get("error");
                                        if(error != null){
                                            Add_Loading_Text(error.getAsString());
                                        }else {
                                            Toast.makeText(DownloadExampleImageDownloadAndChecksum.this, "download url error", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            Add_Loading_Text("Webapp is not loaded.");
        }
    }
    void Add_Loading_Text(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.DownloadExampleImageDownloadAndChecksumLoadingText))
                        .append("\n" + text);
            }
        });
    }
    private File getFileObject(){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File directory = contextWrapper.getDir(getFilesDir().getName(), Context.MODE_PRIVATE);
        /**
         *  Note that files created through a File object will only be accessible by your own application;
         *  you can only set the mode of the entire directory, not of individual files.
         * Apps require no extra permissions to read or write to the returned path, since this path lives in their private storage.
         */
        return directory;
    }
}