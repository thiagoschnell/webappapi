package com.after_project.webappapi;
import android.os.AsyncTask;
import android.os.SystemClock;
import org.json.JSONException;
import org.json.JSONObject;
interface WebAppApiInterface {
    Boolean onInterceptRequestApi(String url);
    void onRequestApi(String api_url,JSONObject options, JSONObject callback);
    void onRequestCanceled();
    void onRequestApiException(Exception e);
    void onResponseApi(String receiverName, int param, String event, String data);
    void onResponseApiConnectionError();
    void onResponseApiScriptError();
    void onResponseApiException(Exception e);
}
abstract class WebAppApiCallback implements WebAppApiInterface {
    Boolean cancelRequest = false;
    @Override
    public Boolean onInterceptRequestApi(String url) {
        return false;
    }
    @Override
    public void onRequestApi(String api_url, JSONObject options, JSONObject callback) {
    }
    @Override
    public void onRequestCanceled() {
    }
    @Override
    public void onRequestApiException(Exception e) {
    }
    @Override
    public void onResponseApi(String receiverName, int param, String event, String data) {
    }
    @Override
    public void onResponseApiConnectionError() {
    }
    @Override
    public void onResponseApiScriptError() {
    }
    @Override
    public void onResponseApiException(Exception e) {
    }
}
public class WebAppApi {
    WebAppApiCallback webAppApiCallback;
    protected void request(String api_url, String options, JSONObject callback, WebAppApiCallback webAppApiCallback1) throws Exception{
        this.webAppApiCallback = webAppApiCallback1;
        {
            if(webAppApiCallback1!=null){
                AsyncTask<Void, Void, String > task = new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        int count = 0;
                        while (webAppApiCallback.cancelRequest==null){
                            SystemClock.sleep(300);
                            if(count>=5){
                                webAppApiCallback.cancelRequest = true;
                            }
                            count++;
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(String token) {
                        if(!webAppApiCallback.cancelRequest){
                            try {
                                webAppApiCallback.onRequestApi(api_url,new JSONObject(options),callback);
                            } catch (Exception e) {
                                e.printStackTrace();
                                if(webAppApiCallback1!=null){
                                    webAppApiCallback1.onRequestApiException(e);
                                }
                            }
                        }else{
                            webAppApiCallback.onRequestCanceled();
                        }
                    }
                    @Override
                    protected void onPreExecute() {
                        Boolean b = webAppApiCallback1.onInterceptRequestApi(api_url);
                        if(webAppApiCallback.cancelRequest!=null){
                            webAppApiCallback.cancelRequest = b;
                        }
                    }
                };
                task.execute();// parallel task execution use task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }
}
