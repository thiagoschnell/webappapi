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
public class ZipWebsiteToWebViewExampleWithCORS extends AppCompatActivity {
    private FileManager fileManager = null;
    private File fileObject = null;
    private File websiteFilePath = null;
    private String websiteDirName = "my_website_with_cors";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zip_website_to_web_view_example_with_cors);
        {
            fileManager = new FileManager();
            fileObject = getFileObject();
            websiteFilePath = fileManager.getDir(fileObject, websiteDirName);

            //Indentify strings as domain that you don't have at ignoreJavascriptStrings ArrayList in JavaScriptInputSecurity.java, it will resulting the WebApp to cancel .evalJavascript() or .runJavaScript().
            //To start filtering JavaScriptInputSecurity use .enableJavaScriptInputSecurity();
            MyApp.getInstance().getWebApp().enableJavaScriptInputSecurity();

            //if not previous added the domain to allowedDomains list then just call .ignoreDomain(),
            // prefer to add the allowed domain by constructor or use bellow example
            //MyApp.getInstance().getWebApp().ignoreDomain("server.realappexample.shop");

            // ATTENTION : This examples use apache MultiViews link: https://httpd.apache.org/docs/2.2/content-negotiation.html#multiviews
            // NOTE : by enabling server MultiViews not required use .ignoreDomain("patch.zip"); .ignoreDomain("update.zip"); .ignoreDomain("products.json"); .ignoreDomain("purchases.json"); .ignoreDomain("profile.json");  as previous example in ZipWebsiteToWebViewExampleMainActivity.java
            // because MultiViews auto detect the file extension by name
            //if there have two or more files with same name need add type of the file in the request header "Accept".
        }
        //Unzip Website from Resource
        ((Button)findViewById(R.id.ZipWebsiteToWebViewExampleWithCORSLayoutButtonUnZipFromResource))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            unZipWebsiteFromResource(R.raw.websitewithcors);
                            startWebviewActivity();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        //Patch website files (download and install)
        ((Button)findViewById(R.id.ZipWebsiteToWebViewExampleWithCORSLayoutButtonDownloadPatchAndInstall))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            unZipWebsiteFromResource(R.raw.websitewithcors);
                            // Download and install the patch.zip and wait its finish
                            // then if all done with success the RequestAndWait.onComplete() method will be call to startWebviewActivity().
                            {
                                new RequestDownloadAndWait("https://server.realappexample.shop/patch",
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
                                                        Toast.makeText(ZipWebsiteToWebViewExampleWithCORS.this, "download url error", Toast.LENGTH_LONG).show();
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
        ((Button)findViewById(R.id.ZipWebsiteToWebViewExampleWithCORSLayoutButtonDownloadUpdateAndInstall))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            deleteFolder(websiteFilePath);
                            // Download and install the update.zip and wait its finish
                            // then if all done with success the RequestAndWait.onComplete() method will be call to startWebviewActivity().
                            {
                                new RequestDownloadAndWait("https://server.realappexample.shop/update",
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
                                                        Toast.makeText(ZipWebsiteToWebViewExampleWithCORS.this, "download url error", Toast.LENGTH_LONG).show();
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
        Intent intent = new Intent(this, com.after_project.webappapi.ZipWebsiteToWebViewExampleWebviewActivity.class);
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