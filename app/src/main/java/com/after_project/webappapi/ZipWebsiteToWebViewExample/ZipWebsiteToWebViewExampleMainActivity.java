package com.after_project.webappapi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
public class ZipWebsiteToWebViewExampleMainActivity extends AppCompatActivity {
    private FileManager fileManager = null;
    private File fileObject = null;
    private File websiteFilePath = null;
    private String websiteDirName = "my_website";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zip_web_view_example_main);
        {
            fileManager = new FileManager();
            fileObject = getFileObject();
            websiteFilePath = fileManager.getDir(fileObject, websiteDirName);


            //[START JavaScriptInputSecurity]

                MyApp.getInstance().getWebApp().enableJavaScriptInputSecurity(true);
                MyApp.getInstance().getWebApp().ignoreDomain("patch.zip"); //request file to download will identified as domain in JavaScriptInputSecurity.java
                MyApp.getInstance().getWebApp().ignoreDomain("update.zip"); //request file to download will identified as domain in JavaScriptInputSecurity.java
                MyApp.getInstance().getWebApp().ignoreDomain("products.json"); //incoming request from website will identified as domain in JavaScriptInputSecurity.java
                MyApp.getInstance().getWebApp().ignoreDomain("purchases.json"); //incoming request from website will identified as domain in JavaScriptInputSecurity.java
                MyApp.getInstance().getWebApp().ignoreDomain("profile.json"); //incoming request from website will identified as domain in JavaScriptInputSecurity.java

            //[END JavaScriptInputSecurity]

        }
        //Unzip Website from Resource
        ((Button)findViewById(R.id.ZipWebsiteToWebViewExampleLayoutButtonUnZipFromResource))
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    unZipWebsiteFromResource(R.raw.website);
                    startWebviewActivity();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //Patch website files (download and install)
        ((Button)findViewById(R.id.ZipWebsiteToWebViewExampleLayoutButtonDownloadPatchAndInstall))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            unZipWebsiteFromResource(R.raw.website);
                            // Download and install the patch.zip and wait its finish
                            // then if all done with success the RequestAndWait.onComplete() method will be call to startWebviewActivity().
                            {
                                new RequestDownloadAndWait("https://realappexample.shop/patch.zip",
                                        new RequestAndWait.RequestURLAndWaitCallback() {
                                            @Override
                                            public void onComplete() {
                                                startWebviewActivity();
                                            }
                                            @Override
                                            public void onRequestURLError(Exception e) {
                                            }
                                            @Override
                                            public @RequestAndWait.ResultStatus int onReceiveResponse(String value) {
                                                try {
                                                    JsonObject json = JsonParser.parseString((String) value).getAsJsonObject();
                                                    JsonElement data = json.get("data");
                                                    if (data != null) {
                                                        byte[] downloaded_bytes = Base64.decode(data.getAsString(), Base64.DEFAULT);
                                                        fileManager.unzip(downloaded_bytes,websiteFilePath);
                                                    } else {
                                                        JsonElement error = json.get("error");
                                                        if (error != null && !error.isJsonNull()) {
                                                        }
                                                        Toast.makeText(ZipWebsiteToWebViewExampleMainActivity.this, "download url error", Toast.LENGTH_LONG).show();
                                                    }
                                                    return RequestAndWait.RESULT_STATUS_SUCCESS;
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    return RequestAndWait.RESULT_STATUS_ERROR;
                                                }
                                            }
                                            @Override
                                            public void onRequestURL(RequestAndWait.RequestThread requestThread) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        requestThread.proc();
                                                    }
                                                });
                                            }
                                        });
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        //Install new version from update files\n (download and install)
        ((Button)findViewById(R.id.ZipWebsiteToWebViewExampleLayoutButtonDownloadUpdateAndInstall))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            deleteFolder(websiteFilePath);
                            // Download and install the update.zip and wait its finish
                            // then if all done with success the RequestAndWait.onComplete() method will be call to startWebviewActivity().
                            {
                                new RequestDownloadAndWait("https://realappexample.shop/update.zip",
                                        new RequestAndWait.RequestURLAndWaitCallback() {
                                            @Override
                                            public void onComplete() {
                                                startWebviewActivity();
                                            }
                                            @Override
                                            public void onRequestURLError(Exception e) {
                                            }
                                            @Override
                                            public @RequestAndWait.ResultStatus int onReceiveResponse(String value) {
                                                try {
                                                    JsonObject json = JsonParser.parseString((String) value).getAsJsonObject();
                                                    JsonElement data = json.get("data");
                                                    if (data != null) {
                                                        byte[] downloaded_bytes = Base64.decode(data.getAsString(), Base64.DEFAULT);
                                                        fileManager.unzip(downloaded_bytes,websiteFilePath);
                                                    } else {
                                                        JsonElement error = json.get("error");
                                                        if (error != null && !error.isJsonNull()) {
                                                        }
                                                        Toast.makeText(ZipWebsiteToWebViewExampleMainActivity.this, "download url error", Toast.LENGTH_LONG).show();
                                                    }
                                                    return RequestAndWait.RESULT_STATUS_SUCCESS;
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    return RequestAndWait.RESULT_STATUS_ERROR;
                                                }
                                            }
                                            @Override
                                            public void onRequestURL(RequestAndWait.RequestThread requestThread) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        requestThread.proc();
                                                    }
                                                });
                                            }
                                        });
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }
    public void deleteFolder(File folder) throws IOException {
        if (folder.isDirectory()) {
            for (File ct : folder.listFiles()){
                deleteFolder(ct);
            }
        }
        if (!folder.delete()) {
            throw new FileNotFoundException("Unable to delete: " + folder);
        }
    }
    private void unZipWebsiteFromResource(int resource_id) throws Exception{
        InputStream is = getResources().openRawResource(resource_id);
        fileManager.unzip(is, websiteFilePath);
    }
    private void startWebviewActivity(){
        Intent intent = new Intent(this, ZipWebsiteToWebViewExampleWebviewActivity.class);
        intent.putExtra("loadWebsiteUrl", "file://"+getFileObject().getAbsolutePath()+"/"+websiteDirName+"/index.html");
        startActivity(intent);
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApp.getInstance().getWebApp().stopJavaScriptInputSecurity();
    }
}