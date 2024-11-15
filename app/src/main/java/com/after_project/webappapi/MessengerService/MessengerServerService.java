package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import androidx.lifecycle.Observer;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class MessengerServerService extends MessengerService {
    protected static final int MSG_CLIENT_CONNECTED = 10;
    protected static final int MSG_CLIENT_DISCONNECTED = 11;
    protected static final int MSG_WEBAPP_LOADED = 23;
    protected static final int MSG_WEBAPP_ERROR = 25;
    private static int reserved_id = 0;
    static class Request  {
        int myid  = -1;
        String url = null;
        int getMyid() {
            return myid;
        }
        String getUrl(){
            return url;
        }
        Observer observer ;
        Request(){
            reserved_id++;
            myid = new Integer(reserved_id);
        }
    }
    final public static String TAG = MessengerServerService.class.getSimpleName();
    private WebApp webApp = null;
    protected static final int MSG_WEBAPP_REQUEST_ASYNC = 100;
    protected static final int MSG_WEBAPP_REQUEST_SYNC = 102;
    protected static final int MSG_WEBAPP_RESPONSE = 104;
    private int maxConnectionClients = 0;
    @Override
    public IBinder onBind(Intent intent) {
        if(intent.getExtras()!=null) {
            if(intent.getExtras().containsKey("maxConnectionClients")){
                this.maxConnectionClients = intent.getExtras().getInt("maxConnectionClients");
            }
        }
        return super.onBind(intent);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        {
            String[] allowedDomains = {
                    // [START] CORS domains
                    // [END] CORS domains
                    // [START] Website domain
                    getResources().getString(R.string.websiteMainDomain)
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
                webApp.loadDataWithBaseUrl("https://"+getResources().getString(R.string.websiteMainDomain)+"/",
                        new RawResource(getAssets(),"home.html"),
                        new WebAppCallback() {
                            @Override
                            public void onLoadFinish(WebView view, String url) {
                                webApp.detachWebAppCallback();
                                sendMessageToClient(Message.obtain(null,MSG_WEBAPP_LOADED));
                                webApp.api.setWebAppApiResponse(new WebAppApiResponse(){
                                    @Override
                                    public void onReceiveResponse(String response) {
                                        {
                                            try {
                                                Message newMsg = Message.obtain(null, MSG_WEBAPP_RESPONSE);
                                                Bundle b = new Bundle();
                                                JsonObject responseJsonObject = JsonParser.parseString((String) response).getAsJsonObject();
                                                JsonObject cb = responseJsonObject.get("cb").getAsJsonObject();
                                                responseJsonObject.addProperty("myid",cb.get("myid").getAsString());
                                                cb.remove("myid");
                                                if(cb.isEmpty()){
                                                    responseJsonObject.remove("cb");
                                                }
                                                b.putString("data", responseJsonObject.toString());
                                                newMsg.setData(b);
                                                sendMessageToClient(newMsg);
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            }
                            @Override
                            public void onLoadError(WebView view,
                                    /*RequiresApi(api >= 21)*/WebResourceRequest request, WebResourceErrorCompat error,
                                    /*RequiresApi(api >=19)*/ int errorCode, String description, String failingUrl)
                            {
                                webApp.detachWebAppCallback();
                                sendMessageToClient(Message.obtain(null,MSG_WEBAPP_ERROR));
                            }
                        }, WebApp.WEBAPP_MODE_CALLBACK_RESPONSE_ONLY);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onReceiveMessage(Message msg) {
        switch (msg.what){
            case MSG_REGISTER_CLIENT:{
                if(mClients.size() < maxConnectionClients) {
                    mClients.add(msg.replyTo);
                    sendMessageToClient(Message.obtain(null,MSG_CLIENT_CONNECTED));
                }
                break;
            }
            case MSG_UNREGISTER_CLIENT:{
                sendMessageToClient(Message.obtain(null,MSG_CLIENT_DISCONNECTED));
                mClients.remove(msg.replyTo);
                break;
            }
            default:{
                {
                    final String myid= String.valueOf(msg.getData().getInt("myid",-1));
                    final String url = msg.getData().getString("url",null);
                    if (msg.what == MSG_WEBAPP_REQUEST_ASYNC) {
                        //[start MSG_REQUEST_ASYNC]
                        webApp.runJavaScript("url('" + url + "',{ 'async': true},{'myid':'"+myid+"'}).addAndroidCallback().get()");
                        //[end MSG_REQUEST_ASYNC]
                    }else
                    if (msg.what == MSG_WEBAPP_REQUEST_SYNC) {
                        //[start MSG_REQUEST_SYNC]
                        {
                            webApp.evalJavaScript(
                                    "url('" + url + "',{ 'async': false}).get().response().data",
                                    new ValueCallback() {
                                        @Override
                                        public void onReceiveValue(Object response) {
                                            try {
                                                Message newMsg = Message.obtain(null, MSG_WEBAPP_RESPONSE);
                                                Bundle b = new Bundle();
                                                JsonObject responseJsonObject = JsonParser.parseString((String) response).getAsJsonObject();
                                                responseJsonObject.addProperty("myid",myid);
                                                b.putString("data", responseJsonObject.toString());
                                                newMsg.setData(b);
                                                sendMessageToClient(newMsg);
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        };
                                    });
                        }
                        //[end MSG_REQUEST_SYNC]
                    }
                }
            }
        }
    }
    private void sendMessageToClient(Message msg) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                Message message = new Message();
                message.copyFrom(msg);
                mClients.get(i).send(message);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
