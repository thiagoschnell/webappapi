package com.after_project.webappapi;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.webkit.ValueCallback;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
public class DownloadExamplePdfDownload extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_example_pdf_download);
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
                                                StorageManager storageManager = new StorageManager();
                                                File fileObject = getFileObject();
                                                Add_Loading_Text("get internal file...");
                                                File pdf_file = storageManager.getFile(fileObject, "my_downloads/SampleFile.pdf");
                                                Add_Loading_Text("save pdf to the file...");
                                                storageManager.saveFile(pdf_file, downloaded_bytes);
                                                {
                                                    /*
                                                    Below example is just for example purposes to display the pdf image
                                                     */
                                                    Add_Loading_Text("rendering pdf to bitmap...");
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                                        ImageView imageView = findViewById(R.id.DownloadExamplePdfDownloadImageView);
                                                        Bitmap bitmap = pdfToBitmap(pdf_file);
                                                        imageView.setImageBitmap(bitmap);
                                                        Add_Loading_Text("Done.");
                                                    }else{
                                                        Add_Loading_Text("Requires Api 21 to render");
                                                    }
                                                }
                                            }
                                        } else {
                                            Add_Loading_Text("pdf download error");
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
        }else{
            Add_Loading_Text("Webapp is not loaded.");
        }
    }
    @RequiresApi(21)
    private Bitmap pdfToBitmap(File file) throws IOException {
        ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        PdfRenderer renderer = null;
        renderer = new PdfRenderer(fd);
        Bitmap bitmap = Bitmap.createBitmap(222, 222, Bitmap.Config.ARGB_4444);
        PdfRenderer.Page page = null;
        page = renderer.openPage(1);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
       return bitmap;
    };
    void Add_Loading_Text(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.DownloadExamplePdfDownloadLoadingText))
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