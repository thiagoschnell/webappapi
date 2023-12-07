package com.after_project.webappapi;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import androidx.annotation.IntDef;
import androidx.lifecycle.MutableLiveData;
import androidx.multidex.MultiDexApplication;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import com.google.gson.JsonObject;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
public class MyApp extends MultiDexApplication {
    static String className = MyApp.class.getSimpleName();
    private static MyApp mInstance;
    private WebApp webApp = null;
    public @WebAppStatus int getWebAppStatus() {
        return WebAppStatus;
    }
    public WebApp getWebApp() {
        return webApp;
    }
    private AppMessageReceiver getAppMessageReceiver() {
        if(appMessageReceiver==null){
            appMessageReceiver = new AppMessageReceiver(myApp_AppMessageReceiverCallback);
        }
        return appMessageReceiver;
    }
    synchronized AppMessage getAppMessage() {
        if(appMessage==null){
            appMessage = new AppMessage(this);
        }
        return appMessage;
    }
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({WEBAPP_STATUS_NONE, WEBAPP_STATUS_LOAD_FINISHED, WEBAPP_STATUS_LOAD_ERROR})
    public @interface WebAppStatus {}
    private @WebAppStatus int WebAppStatus;
    public static final int WEBAPP_STATUS_NONE = 0;
    public static final int WEBAPP_STATUS_LOAD_FINISHED = 1;
    public static final int WEBAPP_STATUS_LOAD_ERROR = 2;
    private AppMessage appMessage = null;
    private AppMessageReceiver appMessageReceiver = null;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        //AppMessageReceiver for .MyApp
        {
            getAppMessage().registerReceiver("MyApp",getAppMessageReceiver());
        }
        //WebApp
        {
            WebView webview = new WebView(this);
            webApp = new WebApp(webview,new WebViewAssetLoader.Builder()
                    .setDomain("realappexample.shop")
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                    .build(),
                    WebApp.FLAG_CLEAR_CACHE);
            try {
                //load server_url for get ready the origin
                webApp.loadDataWithBaseUrl("https://realappexample.shop/",
                        new RawResource(getAssets(),"home.html"),
                        new WebAppCallback() {
                            @Override
                            public void onLoadFinish(WebView view, String url) {
                                //load server_url finished
                                webApp.detachWebAppCallback();
                                webApp.api.setWebAppApiResponse(webAppApiResponse);
                                WebAppStatus = WEBAPP_STATUS_LOAD_FINISHED;
                            }
                            @Override
                            public void onLoadError(WebView view,
                                    /*RequiresApi(api >= 21)*/WebResourceRequest request, WebResourceErrorCompat error,
                                    /*RequiresApi(api >=19)*/ int errorCode, String description, String failingUrl)
                            {
                                //load server_url error
                                webApp.detachWebAppCallback();
                                WebAppStatus = WEBAPP_STATUS_LOAD_ERROR;
                            }
                        });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static synchronized MyApp getInstance() {
        return mInstance;
    }
    private AppMessageReceiver.ReceiverCallback myApp_AppMessageReceiverCallback = new AppMessageReceiver.ReceiverCallback() {
        @Override
        public void onReceiveMessage ( int param, String event, String data){
            switch (event) {
                case "test": {
                    break;
                }
            }
        }
    };
    private WebAppApiResponse webAppApiResponse = new WebAppApiResponse(){
        @Override
        public void onResponseApi(String receiverName, int param, String event, String data) {
            getAppMessage().sendTo(receiverName,param,event,data);
        }
        @Override
        public void onResponseApiConnectionError(String receiverName, JsonObject xhrError) {
            getAppMessage().sendTo(receiverName,0,"connection_error",null);
        }
        @Override
        public void onResponseApiScriptError(JsonObject error) {
            throw new Error(error.toString());
        }
    };
}