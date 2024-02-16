package com.after_project.webappapi;
import static com.after_project.webappapi.WebApp.WEBAPP_STATUS_LOAD_FINISHED;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.ValueCallback;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
public class DownloadExampleImageDownloadAndSave extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_example_image_download_and_save);
        if(MyApp.getInstance().getWebApp().getStatus() == WEBAPP_STATUS_LOAD_FINISHED) {
            ImageView imageview = (ImageView) findViewById(R.id.DownloadExampleImageDownloadAndSaveImageView);
            try {
                Add_Loading_Text("Downloading image...");
                MyApp.getInstance().getWebApp().evalJavaScript("request_download('https://realappexample.shop/i-logo.png')",
                        new ValueCallback() {
                            @Override
                            public void onReceiveValue(Object value) {
                                try {
                                    JsonObject json = JsonParser.parseString((String) value).getAsJsonObject();
                                    JsonElement data = json.get("data");
                                    if (data != null) {
                                        Add_Loading_Text("image download success");
                                        byte[] downloaded_bytes = Base64.decode(data.getAsString(), Base64.DEFAULT);
                                        Add_Loading_Text("Saving image...");
                                        {
                                            FileManager fileManager = new FileManager();
                                            File fileObject = getFileObject();
                                            Add_Loading_Text("get file...");
                                            File image_file = fileManager.getFile(fileObject, "my_downloads/i-logo.png");
                                            Add_Loading_Text("save image to file...");
                                            fileManager.saveFile(image_file, downloaded_bytes);
                                            byte[] image_bytes = fileManager.getFileBytes(image_file);
                                            Add_Loading_Text("set image to imageview...");
                                            imageview.setImageBitmap(BitmapFactory.decodeByteArray(image_bytes, 0, image_bytes.length));
                                            Add_Loading_Text("Done.");
                                        }
                                    } else {
                                        Add_Loading_Text("image download error");
                                        JsonElement error = json.get("error");
                                        if (error != null && !error.isJsonNull()) {
                                            Add_Loading_Text(error.getAsString());
                                        } else {
                                            Toast.makeText(DownloadExampleImageDownloadAndSave.this, "download url error", Toast.LENGTH_LONG).show();
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
        }else{
            Add_Loading_Text("Webapp is not loaded.");
        }
    }
    void Add_Loading_Text(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.DownloadExampleImageDownloadAndSaveLoadingText))
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