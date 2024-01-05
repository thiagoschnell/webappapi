package com.after_project.webappapi;
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
import java.io.FileOutputStream;
import java.util.ArrayList;
public class DownloadExampleStreamDownload extends AppCompatActivity {
    private Boolean download_error = false;
    private FileOutputStream fos = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_example_stream_download);
        ImageView imageview = (ImageView) findViewById(R.id.DownloadExampleStreamDownloadImageView);
        try {
            FileManager fileManager = new FileManager();
            File fileObject = getFileObject();
            Add_Loading_Text("get file...");
            File image_file = fileManager.getFile(fileObject, "my_downloads/new.png");
            fos = new FileOutputStream(image_file,false);
            Add_Loading_Text("get chunks...");
            ArrayList<String> rangeBytesChunks =  getRangeBytes(5000,17834); //getRangeBytes array
            int chunk_count = 0;
            for(String rangeBytes : rangeBytesChunks) {
                String js = "download('https://realappexample.shop/new.png',{'Accept':'*/*','Range':'"+rangeBytes+"'})";
                if(download_error){
                    fos.close();
                    break;
                }
                {
                    Add_Loading_Text("Downloading image chunk("+chunk_count+")...");
                    int finalChunk_count = chunk_count;
                    MyApp.getInstance().getWebApp().evalJavaScript(js,
                            new ValueCallback() {
                                @Override
                                public void onReceiveValue(Object value) {
                                    try {
                                        JsonObject json = JsonParser.parseString((String) value).getAsJsonObject();
                                        JsonElement data = json.get("data");
                                        if (data != null) {
                                            int chunk_total = finalChunk_count+1;
                                            Add_Loading_Text("stream download progress " + (chunk_total * 100/rangeBytesChunks.size()) + "%");
                                            try {
                                                fos.write(Base64.decode(data.getAsString(), Base64.DEFAULT));
                                                if(finalChunk_count == rangeBytesChunks.size()-1){
                                                    Add_Loading_Text("viewing..");
                                                    fos.close();
                                                    byte[] b = fileManager.getFileBytes(image_file);
                                                    imageview.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
                                                }
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        } else {
                                            download_error = true;
                                            Add_Loading_Text("chunk("+ finalChunk_count +") download error");
                                            JsonElement error = json.get("error");
                                            if (error != null) {
                                                Add_Loading_Text(error.getAsString());
                                            } else {
                                                Toast.makeText(DownloadExampleStreamDownload.this, "stream download error", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    } catch (Exception e) {
                                        download_error = true;
                                        e.printStackTrace();
                                    }
                                }
                            });
                }
                chunk_count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     *
     * @param chuck Split value,  ex: split an file with size of 5000 in chuck by 1000 each, its will go split into 5 chucks.
     * @param file_size Content-Length of the file to download.
     * @return
     */
    private ArrayList<String> getRangeBytes(int chuck, int file_size){
        int pos2 = chuck;
        int pos1 = 0;
        ArrayList<String> array = new ArrayList<String>();
        while(pos1 < file_size){
            array.add("bytes=" + pos1 + "-" + (pos2 >= file_size-1?"":pos2));
            pos2 += chuck+1;
            pos1 = pos2 - (chuck);
        }
        return array;
    }
    void Add_Loading_Text(String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.DownloadExampleStreamDownloadLoadingText))
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