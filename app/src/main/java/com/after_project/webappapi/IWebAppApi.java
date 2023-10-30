package com.after_project.webappapi;
interface WebAppApiInterface {
    Boolean onInterceptRequestApi(String url);
    void onBeforeRequest();
    void onRequestCanceled();
    void onRequestApiException(Exception e);
    void onResponseApiSuccess(String receiverName, int param, String event, String data);
    void onResponseApiErrorConnection();
    void onResponseApiErrorScript();
    void onResponseApiException(Exception e);
}
public abstract class IWebAppApi implements WebAppApiInterface {
    Boolean cancelRequest = false;
    @Override
    public Boolean onInterceptRequestApi(String url) {
        return false;
    }
    @Override
    public void onBeforeRequest() {
    }
    @Override
    public void onRequestCanceled() {
    }
    @Override
    public void onRequestApiException(Exception e) {
    }
    @Override
    public void onResponseApiSuccess(String receiverName, int param, String event, String data) {
    }
    @Override
    public void onResponseApiErrorConnection() {
    }
    @Override
    public void onResponseApiErrorScript() {
    }
    @Override
    public void onResponseApiException(Exception e) {
    }
}
