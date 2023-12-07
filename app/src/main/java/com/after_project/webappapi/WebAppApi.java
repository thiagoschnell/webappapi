// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
package com.after_project.webappapi;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.google.gson.JsonObject;
import org.json.JSONException;
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
    private MutableLiveData<WebAppApiDataWrapper> _liveData = null;
    private int id = -1;
    WebAppApiRequest webAppApiRequest = null;
    androidx.lifecycle.Observer observer = null;
    WebAppApiTask(){
    }
    private void setJSONCallback(JSONObject callback){
        this.callback = callback;
    }
    private JSONObject getJSONCallback(){
        return callback;
    }
    WebAppApiTask(int id){
        this.id = id;
    }
    protected void set_liveData(MutableLiveData<WebAppApiDataWrapper> _liveData){
        this._liveData = _liveData;
    }
    protected void setObserver(androidx.lifecycle.LifecycleOwner owner,androidx.lifecycle.Observer observer){
        this.observer = observer;
        this._liveData.observe(owner,observer);
    }
    WebAppApiTask(WebAppApiRequest webAppApiRequest){
        this.webAppApiRequest = webAppApiRequest;
    }
    void setWebAppApiRequest(WebAppApiRequest webAppApiRequest) {
        this.webAppApiRequest = webAppApiRequest;
    }
    synchronized WebAppApiTask prepare(final String api_url,final JSONObject options,final JSONObject callback) {
        try {
            this.api_url = new String(api_url);
            this.options = new JSONObject(options.toString());
            if(callback!=null){
                this.callback = new JSONObject(callback.toString());
                this.callback.put("id",id);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return this;
    }
    void executeParallel(){
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    };
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
/**
 * WebApp Api Response2
 */
class WebAppApiDataWrapper  {
    String data = null;
    int code = 0;
    public void setCode(int code) {
        this.code = code;
    }
    public void setData(String data) {
        this.data = data;
    }
    public Exception getApiException() {
        return null;
    }
    public int getCode() {
        return code;
    }
    public String getErrorMessage() {
        return null;
    }
    public String getData() {
        return data;
    }
}
class WebAppApiResponse2 implements WebAppApiResponse2LiveData.WebAppApiResponse2LiveDataInfo {
    @Override
    public void onSuccess(String s) {
    }
    @Override
    public void onFail(Exception exception) {
    }
    @Override
    public void handleCodes(int code) {
    }
    @Override
    public void onErrorMessage(String message) {
    }
}
class WebAppApiResponse2LiveData implements Observer<WebAppApiDataWrapper> {
    private WebAppApiResponse2LiveDataInfo liveDataInfo;
    String id = null;
    public WebAppApiResponse2LiveData(String id,WebAppApiResponse2LiveDataInfo webAppApiResponse2LiveDataInfo) {
        this.id = id;
        this.liveDataInfo = webAppApiResponse2LiveDataInfo;
    }
    @Override
    public void onChanged(@Nullable WebAppApiDataWrapper tDataWrapper) {
        if (tDataWrapper != null)
            if (tDataWrapper.getApiException() != null)
                liveDataInfo.onFail(tDataWrapper.getApiException());
            else if (tDataWrapper.getCode() != 0)
                liveDataInfo.handleCodes(tDataWrapper.getCode());
            else if(tDataWrapper.getData()!=null)
                liveDataInfo.onSuccess(tDataWrapper.data);
            else {
            }
    }
    public interface WebAppApiResponse2LiveDataInfo {
        void onSuccess(String s);
        void onFail(Exception exception);
        void handleCodes(int code);
        void onErrorMessage(String message);
    }
}
