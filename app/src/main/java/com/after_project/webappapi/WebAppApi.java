// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
package com.after_project.webappapi;
import android.os.AsyncTask;
import com.google.gson.JsonObject;
import org.json.JSONObject;
interface WebAppApiRequestInterface {
    void onRequestApi(String api_url, JSONObject options, JSONObject callback);
    void onRequestCanceled();
    void onRequestApiException(Exception e);
}
interface WebAppApiResponseInterface {
    void onResponseApi(String receiverName, int param, String event, String data);
    void onResponseApiConnectionError(String receiverName, JsonObject xhrError);
    void onResponseApiScriptError(JsonObject error);
    void onResponseApiException(Exception e);
}
class WebAppApiTask extends AsyncTask  {
    private String api_url;
    private JSONObject options;
    private JSONObject callback;
    WebAppApiRequest webAppApiRequest = null;
    WebAppApiTask(){
    }
    WebAppApiTask(WebAppApiRequest webAppApiRequest){
        this.webAppApiRequest = webAppApiRequest;
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
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        try{
            webAppApiRequest.onRequestApi(api_url,options,callback);
        }catch (Exception e){
            try {
                webAppApiRequest.onRequestApiException(e);
            }catch (Exception ee){
                ee.printStackTrace();
            }finally {
                onCancelled();
            }
        }
    }
    @Override
    protected Object doInBackground(Object[] objects) {
        {}
        return null;
    }
    @Override
    protected void onCancelled(){
        super.onCancelled();
        try{
            webAppApiRequest.onRequestCanceled();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
class WebAppApiRequest  implements WebAppApiRequestInterface{
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
    public void onResponseApiConnectionError(String receiverName, JsonObject xhrError) {
    }
    @Override
    public void onResponseApiScriptError(JsonObject error) {
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