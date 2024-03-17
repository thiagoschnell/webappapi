package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.
import static com.after_project.webappapi.MessengerConnection.ConnectionState.CONNECTION_STATE_NOT_ADDED;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
public class Messenger extends Service {
    protected static final int MSG_REGISTER_CLIENT = 1;
    protected static final int MSG_UNREGISTER_CLIENT = 2;
    protected static final int MSG_CLIENT_CONNECTED = 10;
    protected static final int MSG_CLIENT_DISCONNECTED = 11;
    protected static final int MSG_CLIENT_CONNECTION_STATE_CHANGE = 12;
    protected static final int MSG_WEBAPP_LOADED = 23;
    protected static final int MSG_WEBAPP_ERROR = 25;
    protected static final int MSG_WEBAPP_RESPONSE = 27;
    protected static final int MSG_WEBAPP_TEST = 30;
    protected static final int MSG_WEBAPP_REQUEST_ASYNC = 31;
    protected static final int MSG_WEBAPP_REQUEST_SYNC = 32;
    private ShuffleCrypt crypt ;
    private WebApp webApp = null;
    private MessengerConnectionManager messengerConnectionManager = null;
    private Map<android.os.Messenger, MessengerConnection> mClients = new HashMap<>();
    private final android.os.Messenger mMessenger = new android.os.Messenger(new IncomingHandler());
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try{
                switch (msg.what){
                    case MSG_REGISTER_CLIENT:{
                        final MessengerConnection reqquestConnection = new Gson().fromJson(JsonParser.parseString(msg.getData().getString("config")).getAsJsonObject(),MessengerConnection.class);
                            messengerConnectionManager.addConnection(reqquestConnection, msg, new MessengerConnection.MessengerConnectionCallback() {
                                @Override
                                public void onConnectionSuccess(MessengerConnection connection, MessengerConnection.ConnectionStatus connectionStatus) {
                                    addClient(connection, msg.replyTo);
                                    Message message = Message.obtain(null,MSG_CLIENT_CONNECTED);
                                    message.getData().putInt("connectionId",connection.getConnectionId());
                                    message.getData().putInt("hashcode",msg.getData().getInt("hashcode"));
                                    try {
                                        replyMsg(message,msg.replyTo);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                    {
                                        setConnectionChanges(msg,connection.getConnectionState(),connection.getConnectionStatus());
                                    }
                                    {
                                        new Thread(() -> {
                                            while (webApp.getStatus() == WebApp.WEBAPP_STATUS_NONE){
                                                try {
                                                    Thread.sleep(300);
                                                } catch (InterruptedException e) {
                                                    throw new RuntimeException(e);
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                            {
                                                try {
                                                    getConnection(connection.getConnectionId()).getKey().send(Message.obtain(null,
                                                            webApp.getStatus()==WebApp.WEBAPP_STATUS_LOAD_FINISHED? MSG_WEBAPP_LOADED : MSG_WEBAPP_ERROR));
                                                } catch (RemoteException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();
                                    }
                                }
                                @Override
                                public void onConnectionError(MessengerConnection.ConnectionStatus connectionStatus) {
                                    setConnectionChanges(msg, CONNECTION_STATE_NOT_ADDED, connectionStatus);
                                }
                            });
                        break;
                    }
                    case MSG_UNREGISTER_CLIENT:{
                        removeClient(msg.replyTo);
                        {
                        }
                        replyMsg(Message.obtain(null,MSG_CLIENT_DISCONNECTED), msg.replyTo);
                        if(msg.getData().containsKey("connectionId")) {
                            JsonObject jsonObjectChanges = messengerConnectionManager.endConnection(msg.getData().getInt("connectionId"));
                            setConnectionChanges(msg, jsonObjectChanges.get("connectionState").getAsString(), jsonObjectChanges.get("connectionStatus").getAsString());
                        }
                        break;
                    }
                    default:{
                        onReceiveMessage(msg);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void setConnectionChanges(final android.os.Message msg, final MessengerConnection.ConnectionState state, final MessengerConnection.ConnectionStatus status){
        setConnectionChanges(msg,String.valueOf(state),String.valueOf(status));
    }
    private void setConnectionChanges(final android.os.Message msg, final String state, final String status){
        {
            Message message = Message.obtain(null,MSG_CLIENT_CONNECTION_STATE_CHANGE);
            message.getData().putString("connectionStatus", status);
            message.getData().putString("connectionState", state);
            try{
                replyMsg(message,msg.replyTo);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }
    private void onReceiveMessage(Message msg){
        {
            if (msg.what == MSG_WEBAPP_TEST) {
                try {
                    msg.replyTo.send(Message.obtain(null,MSG_WEBAPP_TEST));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }else
            if (msg.what == MSG_WEBAPP_REQUEST_ASYNC) {
                try {
                    //[start] mount
                    Link link = new Link(msg);
                    //[end] mount
                    //[start]  Encryption ShuffleCrypt.java
                    String enc = crypt.encryptStringToBase64( link.toJson().toString() );
                    //[end]  Encryption ShuffleCrypt.java
                    JsonObject jsonObjectCb = new JsonObject();
                    jsonObjectCb.addProperty("msg", enc);
                    webApp.runJavaScript("url('" + link.request.getRequestUrl() + "',{ 'async': true},"+jsonObjectCb.toString().replace("\"","'")+").addAndroidCallback().get()");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else
            if (msg.what == MSG_WEBAPP_REQUEST_SYNC) {
                {
                    //[start] mount
                   try{
                       final Link link = new Link(msg);
                       //[end] mount
                       webApp.evalJavaScript(
                               "url('" + link.request.getRequestUrl() + "',{ 'async': false}).get().response().data",
                               new ValueCallback() {
                                   @Override
                                   public void onReceiveValue(Object response) {
                                       try {
                                           sendMessageToWebAppClient(link,(String)response);
                                       }catch (Exception e){
                                           e.printStackTrace();
                                       }
                                   };
                               });
                   }catch (Exception e){
                       e.printStackTrace();
                   }
                }
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        {
            try {
                crypt = new ShuffleCrypt(999);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        {
            try{
                if(messengerConnectionManager==null){
                    messengerConnectionManager = new MessengerConnectionManager();
                }
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson2 = gsonBuilder.create();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        {
            String[] allowedDomains = {
                    "realappexample.shop"
            };
            WebView webview = new WebView(this);
            webApp = new WebApp(webview,new WebViewAssetLoader.Builder()
                    .setDomain(allowedDomains[allowedDomains.length-1])
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                    .build(),
                    allowedDomains,
                    WebApp.FLAG_CLEAR_CACHE);
            try {
                webApp.loadDataWithBaseUrl("https://realappexample.shop/",
                        new RawResource(getAssets(),"home.html"),
                        new WebAppCallback() {
                            @Override
                            public void onLoadFinish(WebView view, String url) {
                                webApp.detachWebAppCallback();
                                {
                                }
                                webApp.api.setWebAppApiResponse(new WebAppApiResponse(){
                                    @Override
                                    public void onReceiveResponse(String response) {
                                        {
                                            try {
                                                JsonObject responseJsonObject = JsonParser.parseString((String) response).getAsJsonObject();
                                                JsonObject jsonObjectCb = responseJsonObject.get("cb").getAsJsonObject();
                                                String jsonMsg = jsonObjectCb.get("msg").toString();
                                                //[start]  Decode Encryption ShuffleCrypt.java
                                                String dec = crypt.decryptFromBase64String(jsonMsg);
                                                //[end]  Decode Encryption ShuffleCrypt.java
                                                JsonObject jsonObjectMsg = JsonParser.parseString(dec).getAsJsonObject();
                                                jsonObjectCb.remove("msg");
                                                if(jsonObjectCb.isEmpty()){
                                                    responseJsonObject.remove("cb");
                                                }
                                                final Link link = new Link(jsonObjectMsg);
                                                sendMessageToWebAppClient(link, responseJsonObject.toString());
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            }
                            @Override
                            public void onLoadError(WebView view,
                                    WebResourceRequest request, WebResourceErrorCompat error,
                                     int errorCode, String description, String failingUrl)
                            {
                                webApp.detachWebAppCallback();
                            }
                        }, WebApp.WEBAPP_MODE_CALLBACK_RESPONSE_ONLY);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    private class Link{
        private String link = null;
        private JsonObject jsonObject = null;
        private Integer connectionId = null;
        private AppMessenger.Request request = null;
        private MultiClientOptions multiClientOptions = null;
        JsonObject toJson(){
            return jsonObject;
        }
        Link(JsonObject jsonObject) throws Exception{
            this.jsonObject = new JsonObject();
            connectionId = jsonObject.get("connectionId").getAsInt();
            if(jsonObject.has("multiClientRequestOptions")) {
                multiClientOptions = new Gson().fromJson(jsonObject.get("multiClientRequestOptions").getAsString(),MultiClientOptions.class);
                jsonObject.addProperty("multiClientRequestOptions", new Gson().toJson(multiClientOptions, MultiClientOptions.class));
            }
            request = new Gson().fromJson(jsonObject.get("request").getAsString(), AppMessenger.Request.class);
            jsonObject.addProperty("request", new Gson().toJson(request,AppMessenger.Request.class));
            jsonObject.addProperty("connectionId", connectionId);
        }
        Link(final android.os.Message msg) throws Exception{
            jsonObject = new JsonObject();
            connectionId = msg.getData().getInt("connectionId");
            if(msg.getData().containsKey("multiClientRequestOptions")) {
                multiClientOptions = new Gson().fromJson(msg.getData().getString("multiClientRequestOptions"),MultiClientOptions.class);
                jsonObject.addProperty("multiClientRequestOptions", new Gson().toJson(multiClientOptions, MultiClientOptions.class));
            }
            request = new Gson().fromJson(msg.getData().getString("request"), AppMessenger.Request.class);
            jsonObject.addProperty("request", new Gson().toJson(request,AppMessenger.Request.class));
            jsonObject.addProperty("connectionId", connectionId);
        }
        @NonNull
        @Override
        public String toString() {
            return link;
        }
    }
    private void sendMessageToWebAppClient(final Link link, final String response) throws Exception{
        Message message = Message.obtain(null, MSG_WEBAPP_RESPONSE);
        Bundle b = new Bundle();
        b.putString("data",response);
        b.putString("request",new Gson().toJson(link.request,AppMessenger.Request.class));
        message.setData(b);
        sendMessageToClient(message, link);
    }
    private void addClient(final MessengerConnection connection, final android.os.Messenger msg){
        mClients.put(msg,connection);
    }
    private void removeClient(final android.os.Messenger msg){
        mClients.remove(msg);
    }
    private int getClientsCount(){
        return mClients.size();
    }
    private boolean containsClient(final android.os.Messenger client){
        return mClients.containsKey(client);
    }
    private Map.Entry<android.os.Messenger,MessengerConnection> getConnection(int connectionId){
        Iterator<Map.Entry<android.os.Messenger,MessengerConnection>> myVeryOwnIterator = mClients.entrySet().iterator();
        HashMap.Entry<android.os.Messenger,MessengerConnection> entry = null;
        while(myVeryOwnIterator.hasNext()) {
            entry = myVeryOwnIterator.next();
            if(entry.getValue().getConnectionId()==connectionId){
                break;
            }
        }
        return entry;
    }
    private void sendMessageToClient(android.os.Message msg, Link link) throws Exception {
        final HashMap.Entry<android.os.Messenger,MessengerConnection> entry = getConnection(link.connectionId);
        if (!entry.getValue().isMultiClient()) {
            if(entry!=null) {
                Message message = new Message();
                message.copyFrom(msg);
                entry.getKey().send(message);
            }
        } else {
            if(link.multiClientOptions==null){
            }else if(link.multiClientOptions!=null){
                sendMessageToClients(msg, link.multiClientOptions);
            }
        }
    }
    private void sendMessageToClients(final android.os.Message msg, final MultiClientOptions multiClientRequestOptions) throws Exception{
        Iterator<Map.Entry<android.os.Messenger,MessengerConnection>> myVeryOwnIterator = mClients.entrySet().iterator();
        while(myVeryOwnIterator.hasNext()) {
            HashMap.Entry<android.os.Messenger,MessengerConnection> entry = myVeryOwnIterator.next();
            Message message = new Message();
            message.copyFrom(msg);
            for(RequestRulesMatch requestRulesMatch : multiClientRequestOptions.requestRulesMatches){
                if (requestRulesMatch.rulesMatchType.equals(RulesMatchType.MATCH_TYPE_NAME)) {
                    if (!requestRulesMatch.matchWith(requestRulesMatch.toArray(), entry.getValue().getName())) {
                    } else {
                        entry.getKey().send(message);
                    }
                }
                if (requestRulesMatch.rulesMatchType.equals(RulesMatchType.MATCH_TYPE_TAG)) {
                    if (!requestRulesMatch.matchWith(requestRulesMatch.toArray(), entry.getValue().getTag())) {
                    } else {
                        entry.getKey().send(message);
                    }
                }
            }
        }
    }
    private void replyMsg(final Message msg, final android.os.Messenger messenger) throws Exception {
        Message message = new Message();
        message.copyFrom(msg);
        messenger.send(message);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
class MultiClientOptions{
    RequestRulesMatch[] requestRulesMatches = null;
    MultiClientOptions(RequestRulesMatch... requestRulesMatches ){
        this.requestRulesMatches = requestRulesMatches;
    }
}
class RequestRulesMatch extends RulesMatch {
    RequestRulesMatch(String...stringsToMatch){
        super(stringsToMatch);
    }
}
class RequestMatchByTags extends RequestRulesMatch {
    RequestMatchByTags(String...matchs){
        super(matchs);
        this.rulesMatchType = RulesMatchType.MATCH_TYPE_TAG;
    }
}
class RequestMatchByNames extends RequestRulesMatch {
    RequestMatchByNames(String...matchs){
        super(matchs);
        this.rulesMatchType = RulesMatchType.MATCH_TYPE_NAME;
    }
}
enum RulesMatchType{
    MATCH_TYPE_NAME,
    MATCH_TYPE_TAG
}
abstract class AbstractRulesMatch{
    RulesMatchType rulesMatchType = null;
    abstract String[] toArray();
}
class RulesMatch extends AbstractRulesMatch{
    private String[] stringsToMatch = null;
    RulesMatch(String...stringsToMatch){
        this.stringsToMatch = stringsToMatch;
    }
    String[] getStringsToMatch() {
        return stringsToMatch;
    }
    @Override
    String[] toArray() {
        return stringsToMatch;
    }
    boolean matchWith(String[] strings, String with){
        return Arrays.asList(strings).indexOf(with) > -1;
    }
}
class MessengerConnectionImpl{
    private String tag = null;
    private Integer clientId = null;
    private String name = null;
    Integer getClientId() {
        return clientId;
    }
    String getTag() {
        return tag;
    }
    String getName() {
        return name;
    }
    MessengerConnectionImpl(@NonNull String tag, @NonNull Integer clientId, @NonNull String name) {
        this.tag = tag;
        this.name = name;
        this.clientId = clientId;
    }
}
abstract class AbstractMessengerConnection extends MessengerConnectionImpl{
     AbstractMessengerConnection(@NonNull String tag, @NonNull Integer clientId, @NonNull String name) {
         super(tag, clientId, name);
     }
     abstract Integer getConnectionId();
}
class MessengerConnection extends AbstractMessengerConnection {
    private Integer connectionId = null;
    @Override
    Integer getConnectionId() {
        return connectionId;
    }
    enum ConnectionState{
        CONNECTION_STATE_OK,
        CONNECTION_STATE_CANCELLED,
        CONNECTION_STATE_ENDED,
        CONNECTION_STATE_ADDED,
        CONNECTION_STATE_NOT_ADDED,
        //CONNECTION_STATE_REMOVED
    }
    enum ConnectionStatus{
        ERROR_MAX_CONNECTION_EXCEEDED,
        ERROR_CONNECTION_UNAVAILABLE,
        ERROR_CONNECTION_MULTCLIENT_PARALLEL_LIMIT_EXCEEDED,
        ERROR_CONNECTION_NORMAL_PARALLEL_LIMIT_EXCEEDED,
        ERROR_CONNECTION_MULTCLIENT_NO_MATCHS_FOUND,
        ERROR_CONNECTION_NORMAL_NO_MATCHS_FOUND,
        ERROR_CONNECTION_MULTCLIENT_ALREADY_CONNECTED,
        ERROR_CONNECTION_NORMAL_ALREADY_CONNECTED,
        ERROR_CONNECTION_TAG_NOT_REGISTERED,
        ERROR_CONNECTION_RULES_INCOMPLETE_MATCH_ARGUMENTS,
        CONNECTION_CLIENT_SUCCESS,
        CONNECTION_MULTICLIENT_SUCCESS,
        CONNECTION_CLIENT_DISCONNECTED,
        ERROR_UNKOWN
    }
    private ConnectionStatus connectionStatus = null;
    private ConnectionState connectionState = null;
    private Boolean isMultiClient = null;
    private Boolean isParallel = null;
    MessengerConnection(@NonNull String tag, @NonNull Integer clientId, @NonNull String name, Integer connectionId, boolean isMultiClient, boolean isParallel) {
        super(tag,clientId,name);
        this.connectionId = connectionId;
        this.isMultiClient = isMultiClient;
        this.isParallel = isParallel;
    }
    protected boolean isMultiClient() {
        return isMultiClient;
    }
    protected Boolean isParallel() {
        return isParallel;
    }
    protected ConnectionState getConnectionState() {
        return connectionState;
    }
    protected ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }
    protected interface MessengerConnectionCallback {
        void onConnectionSuccess(MessengerConnection connection, ConnectionStatus connectionStatus);
        void onConnectionError(ConnectionStatus connectionStatus);
    }
}