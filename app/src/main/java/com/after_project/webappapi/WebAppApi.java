package com.after_project.webappapi;
import android.os.AsyncTask;
import android.os.SystemClock;
import org.json.JSONObject;
interface WebAppApiRequestInterface {
    Boolean onInterceptRequestApi(String url);
    void onRequestApi(String api_url, JSONObject options, JSONObject callback);
    void onRequestCanceled();
    void onRequestApiException(Exception e);
}
interface WebAppApiResponseInterface {
    void onResponseApi(String receiverName, int param, String event, String data);
    void onResponseApiConnectionError();
    void onResponseApiScriptError();
    void onResponseApiException(Exception e);
}
interface WebAppApiTaskCallback{
    void onPreExecute();
}
class WebAppApiTask extends AsyncTask  {
    private String api_url;
    private JSONObject options;
    private JSONObject callback;
    private WebAppApiTaskCallback webAppApiTaskCallback;
    WebAppApiRequest webAppApiRequest = null;
    WebAppApiTask(){
    }
    WebAppApiTask(WebAppApiRequest webAppApiRequest){
        this.webAppApiRequest = webAppApiRequest;
    }

    public void setWebAppApiTaskCallback(WebAppApiTaskCallback webAppApiTaskCallback) {
        this.webAppApiTaskCallback = webAppApiTaskCallback;
    }

    void setWebAppApiRequest(WebAppApiRequest webAppApiRequest) {
        this.webAppApiRequest = webAppApiRequest;
    }
    synchronized WebAppApiTask prepare(String api_url, JSONObject options, JSONObject callback) {
        this.api_url = api_url;
        this.options = options;
        this.callback = callback;
        return this;
    }
    Boolean cancelRequest = false;
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if(cancelRequest){
            webAppApiRequest.onRequestCanceled();
        }else{
            try {
                webAppApiRequest.onRequestApi(api_url,options,callback);
            } catch (Exception e) {
                e.printStackTrace();
                webAppApiRequest.onRequestApiException(e);
            }
        }
    }
    @Override
    protected Object doInBackground(Object[] objects) {
        int count = 0;
        while (cancelRequest==null){
            SystemClock.sleep(300);
            if(count>=5){
                cancelRequest = true;
            }
            count++;
        }
        return null;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(webAppApiTaskCallback!= null){
            webAppApiTaskCallback.onPreExecute();
        }else{
            Boolean b = webAppApiRequest.onInterceptRequestApi(api_url);
            if(cancelRequest!=null){
                cancelRequest = b;
            }
        }
    }
}
class WebAppApiRequest  implements WebAppApiRequestInterface{
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
}
class WebAppApiResponse implements WebAppApiResponseInterface {
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
public class WebAppApi  {
    private WebAppApiResponse webAppApiResponse = null;
    private WebAppApiRequest webAppApiRequest = null;
    void setWebAppApiRequest(WebAppApiRequest webAppApiRequest) {
        this.webAppApiRequest = webAppApiRequest;
    }
    void setWebAppApiResponse(WebAppApiResponse webAppApiResponse) {
        this.webAppApiResponse = webAppApiResponse;
    }
    synchronized WebAppApiTask newTask(WebAppApiTask webAppApiTask){
        return webAppApiTask;
    }
    synchronized WebAppApiResponse response (){
        return this.webAppApiResponse;
    }
}
