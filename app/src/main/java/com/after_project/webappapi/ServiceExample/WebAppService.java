package com.after_project.webappapi;
import android.os.Bundle;
import android.os.Message;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import androidx.lifecycle.Observer;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class WebAppService extends AbstractService {
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
    protected static final int MSG_REQUEST_ASYNC = 1;
    protected static final int MSG_REQUEST_SYNC = 2;
    protected static final int MSG_RESPONSE = 3;
    final public static String TAG = WebAppService.class.getSimpleName();
    private WebApp webApp = null;
    @Override
    public void onStartService() {
        //WebApp
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
                                webApp.api.setWebAppApiResponse(new WebAppApiResponse(){
                                    @Override
                                    public void onReceiveResponse(String response) {
                                        {
                                            try {
                                                Message newMsg = Message.obtain(null, MSG_RESPONSE);
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
                                                send(newMsg);
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
                            }
                        }, WebApp.WEBAPP_MODE_CALLBACK_RESPONSE_ONLY);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void onStopService() {
    }
    @Override
    public void onReceiveMessage(Message msg) {
        final String myid= String.valueOf(msg.getData().getInt("myid",-1));
        final String url = msg.getData().getString("url",null);
        if (msg.what == MSG_REQUEST_ASYNC) {
        //[start MSG_REQUEST_ASYNC]
            webApp.runJavaScript("url('" + url + "',{ 'async': true},{'myid':'"+myid+"'}).addAndroidCallback().get()");
        //[end MSG_REQUEST_ASYNC]
        }else
        if (msg.what == MSG_REQUEST_SYNC) {
        //[start MSG_REQUEST_SYNC]
            {
                webApp.evalJavaScript(
                        "url('" + url + "',{ 'async': false}).get().response().data",
                        new ValueCallback() {
                            @Override
                            public void onReceiveValue(Object response) {
                                try {
                                    Message newMsg = Message.obtain(null, MSG_RESPONSE);
                                    Bundle b = new Bundle();
                                    JsonObject responseJsonObject = JsonParser.parseString((String) response).getAsJsonObject();
                                    responseJsonObject.addProperty("myid",myid);
                                    b.putString("data", responseJsonObject.toString());
                                    newMsg.setData(b);
                                    send(newMsg);
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