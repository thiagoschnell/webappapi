package com.after_project.webappapi;
// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
import android.os.AsyncTask;
import android.webkit.ValueCallback;
import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Semaphore;
public class RequestURLAndWait {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RESULT_STATUS_SUCCESS, RESULT_STATUS_ERROR})
    public @interface ResultStatus {}
    public static final int RESULT_STATUS_ERROR = 0;
    public static final int RESULT_STATUS_SUCCESS = 1;
    private String url;
    private Boolean background_error =false;
    private RequestURLAndWaitCallback requestURLAndWaitCallback;
    final Semaphore semaphore = new Semaphore(0);
    RequestURLAndWait(String url, RequestURLAndWaitCallback requestURLAndWaitCallback) throws Exception {
        this.url = url;
        this.requestURLAndWaitCallback = requestURLAndWaitCallback;
        AsyncTask<Void, Void, String > task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    backgroundThread();
                } catch (Exception e) {
                    semaphore.release();
                    background_error = true;
                    if(requestURLAndWaitCallback!=null){
                        requestURLAndWaitCallback.onRequestURLError(e);
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if(!background_error) {
                    if (requestURLAndWaitCallback != null) {
                        requestURLAndWaitCallback.onComplete();
                    }
                }
            }
        };
        task.execute();
    }
    private void backgroundThread() throws Exception{
        if(requestURLAndWaitCallback!=null){
            requestURLAndWaitCallback.onRequestURL(new RequestThread() {
                @Override
                public void proc() {
                    try {
                        if(MyApp.getInstance().getWebAppStatus() == MyApp.WEBAPP_STATUS_LOAD_FINISHED) {
                            try {
                                if(requestURLAndWaitCallback!=null){
                                    MyApp.getInstance().getWebApp().evalJavaScript(
                                            "request_download('"+url+"')",
                                            new ValueCallback() {
                                                @Override
                                                public void onReceiveValue(Object value) {
                                                    if(requestURLAndWaitCallback!=null){
                                                        try {
                                                            @ResultStatus int resultStatus = requestURLAndWaitCallback.onReceiveResponse((String)value);
                                                            if(resultStatus==RESULT_STATUS_ERROR){
                                                                background_error = true;
                                                            }
                                                        }
                                                        finally {
                                                            semaphore.release();
                                                        }
                                                    }
                                                }
                                            });
                                }
                            } catch (Exception e) {
                                semaphore.release();
                                background_error = true;
                                if(requestURLAndWaitCallback!=null){
                                    requestURLAndWaitCallback.onRequestURLError(e);
                                }
                            }
                        }else{
                        }
                    } catch (Exception e) {
                        semaphore.release();
                        background_error = true;
                        if(requestURLAndWaitCallback!=null){
                            requestURLAndWaitCallback.onRequestURLError(e);
                        }
                    }
                }
            });
        }
       semaphore.acquire();
    }
    protected interface RequestThread {
        void proc();
    }
    protected interface RequestURLAndWaitCallback {
        void onComplete();
        void onRequestURLError(Exception e);
        @ResultStatus int onReceiveResponse(String value);
        void onRequestURL(RequestThread requestThread);
    }
}