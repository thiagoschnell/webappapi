package com.after_project.webappapi;
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
public class DownloadExampleImageDownload extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_example_image_download);
        ImageView imageview = (ImageView) findViewById(R.id.DownloadExampleImageDownloadImageView);
        try
        {
            Add_Loading_Text("Downloading image...");
            MyApp.getInstance().getWebApp().evalJavaScript("request_download('https://realappexample.shop/i-logo.png')",
                    new ValueCallback() {
                        @Override
                        public void onReceiveValue(Object value) {
                            try{
                                JsonObject json = JsonParser.parseString((String)value).getAsJsonObject();
                                JsonElement data = json.get("data");
                                if(data != null){
                                    Add_Loading_Text("image download success");
                                    byte[] image_downloaded = Base64.decode(data.getAsString(), Base64.DEFAULT);
                                    imageview.setImageBitmap(BitmapFactory.decodeByteArray(image_downloaded, 0, image_downloaded.length));
                                }else{
                                    Add_Loading_Text("image download error");
                                    JsonElement error = json.get("error");
                                    if(error != null){
                                        Add_Loading_Text(error.getAsString());
                                    }else {
                                        Toast.makeText(DownloadExampleImageDownload.this, "download url error", Toast.LENGTH_LONG).show();
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
    }
    void Add_Loading_Text(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.DownloadExampleImageDownloadLoadingText))
                        .append("\n" + text);
            }
        });
    }
}