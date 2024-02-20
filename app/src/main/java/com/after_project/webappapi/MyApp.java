package com.after_project.webappapi;
import static com.after_project.webappapi.MessengerServerService.MSG_WEBAPP_LOADED;
import static com.after_project.webappapi.MessengerServerService.MSG_WEBAPP_REQUEST_ASYNC;
import static com.after_project.webappapi.MessengerServerService.MSG_WEBAPP_REQUEST_SYNC;
import static com.after_project.webappapi.MessengerServerService.MSG_WEBAPP_RESPONSE;
import android.content.Intent;
import android.os.Message;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import androidx.work.Data;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class MyApp extends MessengerClient{
    static String className = MyApp.class.getSimpleName();
    private static MyApp mInstance;
    private WebApp webApp = null;
    protected class Messenger{
        protected void send(Message msg, int myid, String url) throws Exception {
            sendMsgRequest(msg,myid,url);
        }
    }
    protected Messenger getMessenger(){
        return new Messenger();
    }
    protected MutableLiveData AppLiveData = new MutableLiveData<Data>();
    private MutableLiveData WebAppLiveData = new MutableLiveData<Data>();
    protected MutableLiveData getWebAppLiveData() {
        return WebAppLiveData;
    }
    protected WebApp getWebApp() {
        return webApp;
    }
    private AppMessageReceiver getAppMessageReceiver() {
        if(appMessageReceiver==null){
            appMessageReceiver = new AppMessageReceiver(myApp_AppMessageReceiverCallback);
        }
        return appMessageReceiver;
    }
    protected synchronized AppMessage getAppMessage() {
        if(appMessage==null){
            appMessage = new AppMessage(this);
        }
        return appMessage;
    }
    private AppMessage appMessage = null;
    private AppMessageReceiver appMessageReceiver = null;
    private com.after_project.webappapi.ServiceUtils serviceUtils = null;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        //MessengerService
        {
            getServiceUtils().StopMyService(this,new Intent(this,MessengerServerService.class));
            getServiceUtils().StartMyService(this,new Intent(this,MessengerServerService.class));
            try {
                menssengerClientCallback = new MessengerClient.MenssengerClientCallback() {
                    @Override
                    public void onMessageHandle(Message msg) {
                        switch (msg.what) {
                            case MSG_WEBAPP_LOADED:{
                                {
                                    com.after_project.webappapi.WebAppServiceActivity.onResponseCallback responseCallback = new com.after_project.webappapi.WebAppServiceActivity.onResponseCallback() {
                                        @Override
                                        public void onResponse(String data) {
                                            // todo response data
                                        }
                                    };
                                    String url = "https://realappexample.shop/userNotifications.json";
                                }
                                break;
                            }
                            case MSG_WEBAPP_RESPONSE:
                                if(msg.getData()!=null){
                                    if(msg.getData().containsKey("data")) {
                                        String data = msg.getData().getString("data").toString();
                                        MyApp.getInstance().getWebAppLiveData().setValue(data);
                                    }else{
                                    }
                                }
                                break;
                        }
                    }
                };
                MessengerClientConnect();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        //AppMessageReceiver for .MyApp
        {
            getAppMessage().registerReceiver("MyApp",getAppMessageReceiver());
        }
        //WebApp
        {
            String[] allowedDomains = {
                // [START] CORS domains //"webappapi-server.azurewebsites.net",
                        "server.realappexample.shop",
                // [END] CORS domains
                // [START] Website domain
                        "realappexample.shop"
                // [END] Website domain
            };
            WebView webview = new WebView(this);
            webApp = new WebApp(webview,new WebViewAssetLoader.Builder()
                    .setDomain(allowedDomains[allowedDomains.length-1])
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                    .build(),
                    allowedDomains,
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
                            }
                            @Override
                            public void onLoadError(WebView view,
                                    /*RequiresApi(api >= 21)*/WebResourceRequest request, WebResourceErrorCompat error,
                                    /*RequiresApi(api >=19)*/ int errorCode, String description, String failingUrl)
                            {
                                //load server_url error
                                webApp.detachWebAppCallback();
                            }
                        });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    protected MessengerServerServiceCustomRequest newMessengerRequest(String url, onResponseCallback responseCallback) throws Exception{
        MessengerServerServiceCustomRequest request = new MessengerServerServiceCustomRequest(url, responseCallback);
        request.async = true;
        return request;
    }
    protected MessengerServerServiceCustomRequest newMessengerRequestSynchronous(String url, onResponseCallback responseCallback) throws Exception{
        MessengerServerServiceCustomRequest request = new MessengerServerServiceCustomRequest(url, responseCallback);
        request.async = false;
        return request;
    }
    protected class MessengerServerServiceCustomRequest extends MessengerServerService.Request {
        boolean async = false;
        private MessengerServerServiceCustomRequest self = this;
        MessengerServerServiceCustomRequest(String url,onResponseCallback responseCallback ){
            super();
            {
                this.url = url;
                observer = new Observer() {
                    @Override
                    public void onChanged(Object o) {
                        try {
                            JsonObject responseJsonObject = JsonParser.parseString((String) o).getAsJsonObject();
                            if(responseJsonObject.get("myid").getAsInt()==getMyid()){
                                {
                                    if(responseJsonObject.has("data")){
                                        // todo success
                                        responseCallback.onResponse(self, responseJsonObject.get("data").toString());
                                    }else if (responseJsonObject.getAsJsonObject("error").has("xhr")) {
                                        //todo xhr error
                                        responseCallback.onError(self, WebApp.RESPONSE_CONNECTION_ERROR);
                                    }else {
                                        //todo javascript error
                                        responseCallback.onError(self, WebApp.RESPONSE_JAVASCRIPT_ERROR);
                                    }
                                }
                                getWebAppLiveData().removeObserver(observer);
                            }else{
                            }
                        }catch (Exception e ){
                            e.printStackTrace();
                        }
                    }
                };
                getWebAppLiveData().observe(ProcessLifecycleOwner.get(),observer);
            }
        }
    }
    protected interface onResponseCallback {
        void onResponse(MessengerServerServiceCustomRequest request, String data);
        void onError(MessengerServerServiceCustomRequest request, @WebApp.ResponseError int responseError);
    }
    //App LiveData Listener
    {
        AppLiveData.observe(ProcessLifecycleOwner.get(), new Observer() {
            @Override
            public void onChanged(Object o) {
                JsonObject json = JsonParser.parseString((String) o).getAsJsonObject();
                try {
                    if(!json.get("async").getAsBoolean()) {
                        getMessenger().send(Message.obtain(null, MSG_WEBAPP_REQUEST_SYNC), json.get("myid").getAsInt(), json.get("url").getAsString());
                    }else{
                        getMessenger().send(Message.obtain(null, MSG_WEBAPP_REQUEST_ASYNC), json.get("myid").getAsInt(), json.get("url").getAsString());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    protected com.after_project.webappapi.ServiceUtils getServiceUtils(){
        if(serviceUtils==null){
            serviceUtils = new com.after_project.webappapi.ServiceUtils();
        }
        return serviceUtils;
    }
    protected static synchronized MyApp getInstance() {
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