package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import androidx.annotation.IntDef;
import androidx.lifecycle.Observer;
import com.google.gson.Gson;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
public class AppMessenger extends AbstractMessengerConnection{
    private Integer connectionId = null;
    @Override
    Integer getConnectionId() {
        return connectionId;
    }
    private static int reserved_request_id = 0;
    static class Request  {
        private boolean async = false;
        private int requestId  = -1;
        private String requestUrl = null;
        int getRequestId() {
            return requestId;
        }
        void setAsync(boolean async) {
            this.async = async;
        }
        String getRequestUrl(){
            return requestUrl;
        }
        boolean getAsync(){
            return async;
        }
        Observer observer ;
        Request(String url){
            reserved_request_id++;
            this.requestId = new Integer(reserved_request_id);
            this.requestUrl = url;
        }
    }
    private static int reserved_client_id = 0;
    protected static final int MESSENGER_CLIENT_STATUS_BINDING = 1;
    protected static final int MESSENGER_CLIENT_STATUS_CONNECTED = 2;
    protected static final int MESSENGER_CLIENT_STATUS_CONNECTING = 3;
    protected static final int MESSENGER_CLIENT_STATUS_DISCONNECTED = 4;
    protected static final int MESSENGER_CLIENT_STATUS_DISCONNECTING = 5;
    protected static final int MESSENGER_CLIENT_STATUS_UNBINDING = 6;
    protected static final int MESSENGER_CLIENT_STATUS_CLOSE = 7;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MESSENGER_CLIENT_STATUS_CONNECTED, MESSENGER_CLIENT_STATUS_DISCONNECTED, MESSENGER_CLIENT_STATUS_BINDING, MESSENGER_CLIENT_STATUS_UNBINDING, MESSENGER_CLIENT_STATUS_CLOSE})
    private @interface MessengerClientStatus {}
    private @MessengerClientStatus int mStatus;
    protected @MessengerClientStatus int getStatus() {
        return mStatus;
    }
    private android.os.Messenger mService = null;
    private boolean mIsBound;
    private Context mContext;
    private AppMenssengerCallback mCallback = null;
    private  AbstractMessengerConnection connection = this;
     AppMessenger(Context context, String name){
        super(context.getClass().getSimpleName(),new Integer(reserved_client_id), name);
        reserved_client_id++;
        mContext = context;
    }
    AppMessenger(Context context) {
        super(context.getClass().getSimpleName(),new Integer(reserved_client_id),  new String("messengerclient_" + new Integer(reserved_client_id)) );
        reserved_client_id++;
        mContext = context;
    }
    protected void setCallback(AppMenssengerCallback callback){
        mCallback = callback;
    }
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Messenger.MSG_CLIENT_CONNECTION_STATE_CHANGE:{
                    final String status = msg.getData().getString("connectionStatus");
                    final String state =  msg.getData().getString("connectionState");
                    if(mCallback!=null) {
                        mCallback.onConnectionChanges(MessengerConnectionManager.ConnectionStateValueOf(state),
                                MessengerConnectionManager.ConnectionStatusValueOf(status),
                               getConnectionTextStatus(MessengerConnectionManager.ConnectionStatusValueOf(status)));
                    }
                    break;
                }
                case Messenger.MSG_CLIENT_CONNECTED:{
                    if(mStatus == MESSENGER_CLIENT_STATUS_CONNECTED){
                        return;
                    }
                    try {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(msg.getData().containsKey("connectionId")){
                        {
                            final int connectionId_final = new Integer(msg.getData().getInt("connectionId",-1));
                            if(msg.getData().containsKey("hashcode")){
                                {
                                    if(connection.hashCode() ==  msg.getData().getInt("hashcode")){
                                       connectionId = connectionId_final;
                                   }else{
                                    }
                                }
                            }else{
                                return;
                            }
                        }
                    }else{
                        return;
                    }
                    mStatus = MESSENGER_CLIENT_STATUS_CONNECTED;
                    if(mCallback!=null) {
                        mCallback.onConnected(msg);
                    }
                    break;
                }
                case Messenger.MSG_CLIENT_DISCONNECTED:{
                    if(mStatus == MESSENGER_CLIENT_STATUS_DISCONNECTED){
                        return;
                    }
                    mStatus = MESSENGER_CLIENT_STATUS_DISCONNECTED;
                    if(mCallback!=null){
                        mCallback.onDisconnected(msg);
                    }
                    break;
                }
                default:
                    if(mCallback!=null){
                        mCallback.onReceiveMessage(msg);
                    }
            }
        }
    }
    private final android.os.Messenger mMessenger = new android.os.Messenger(new IncomingHandler());
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mService = new android.os.Messenger(service);
            if(mCallback!=null){
                mCallback.onAttached();
            }
            {
                Message msg = Message.obtain(null,
                        Messenger.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
               try{
                   String gsonConfig = new Gson().toJson(connection, MessengerConnectionImpl.class);
                msg.getData().putString("config", gsonConfig);
                msg.getData().putInt("hashcode", connection.hashCode());
                sendMessageToServer(msg);
               }catch (Exception e){
                   e.printStackTrace();
               }
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mStatus = MESSENGER_CLIENT_STATUS_CLOSE;
            if(mCallback!=null){
                mCallback.onClose();
            }
        }
    };
    private void send(Message msg) throws Exception {
        if (mIsBound) {
            if (mService != null) {
                msg.getData().putInt("connectionId",connection.getConnectionId());
                {
                    //for test only
                    if(msg.what==Messenger.MSG_WEBAPP_TEST)
                        msg.replyTo = mMessenger;
                }
                mService.send(msg);
            }
        }
    }
    void sendMsgTest(Message msg) throws Exception{
        send(msg);
    }
    protected void sendMsgRequest(Message msg, Request request, MultiClientOptions multiClientOptions) throws Exception {
        if(getStatus()!=MESSENGER_CLIENT_STATUS_CONNECTED){
            return;
        }
        String jsonRequest = new Gson().toJson(request,Request.class);
        String jsonMultiClientOptions = new Gson().toJson(multiClientOptions,MultiClientOptions.class);
        msg.getData().putString("request", jsonRequest);
        msg.getData().putString("multiClientOptions", jsonMultiClientOptions);
        msg.getData().putInt("connectionId", connection.getConnectionId());
        send(msg);
    }
    private void sendMessageToServer(Message msg){
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    protected void connect() throws ClientIsBinding,ClientIsConnecting,ClientAlreadyConnected,ClientConnectException {
        if(Integer.valueOf(getStatus()) != 0) {
            if(getStatus() < MESSENGER_CLIENT_STATUS_CONNECTED) {
                throw new ClientIsBinding("Client is binding status(" + getStatus() + ")");
            }else if(getStatus() == MESSENGER_CLIENT_STATUS_CONNECTING) {
                throw new ClientIsConnecting("Client is connecting status(" + getStatus() + ")");
            }
            throw new ClientAlreadyConnected("Client cannot connect status(" + getStatus() + ")");
        }
        mStatus = MESSENGER_CLIENT_STATUS_CONNECTING;
        try{
            try {
                if (mContext.bindService(new Intent(mContext, Messenger.class), mConnection, Context.BIND_AUTO_CREATE)) {
                    mIsBound = true;
                }
            }finally {
                if(mIsBound){
                    mStatus = MESSENGER_CLIENT_STATUS_BINDING;
                    if (mCallback != null) {
                        mCallback.onBinding();
                    }
                }
            }
        }catch (Exception e){
            throw new ClientConnectException(e);
        }
    }
    protected void disconnect() throws ClientIsUnbinding,ClientIsDisconnecting,ClientAlreadyDisconnected, ClientDisconnectException{
        if(getStatus() >= MESSENGER_CLIENT_STATUS_DISCONNECTED) {
            if(getStatus() == MESSENGER_CLIENT_STATUS_UNBINDING) {
                throw new ClientIsUnbinding("Client is unbinding status(" + getStatus() + ")");
            }else if(getStatus() == MESSENGER_CLIENT_STATUS_DISCONNECTING) {
                throw new ClientIsDisconnecting("Client is disconnecting status(" + getStatus() + ")");
            }
            throw new ClientAlreadyDisconnected("Client already disconnected status(" + getStatus() + ")");
        }
        mStatus = MESSENGER_CLIENT_STATUS_DISCONNECTING;
        try {
            if (mIsBound) {
                if (mService != null) {
                    Message msg = Message.obtain(null,
                            Messenger.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    if(connectionId!=null) {
                        msg.getData().putInt("connectionId", connectionId);
                    }
                    sendMessageToServer(msg);
                }
                try {
                    mContext.unbindService(mConnection);
                } finally {
                    mIsBound = false;
                    mStatus = MESSENGER_CLIENT_STATUS_UNBINDING;
                    if (mCallback != null) {
                        mCallback.onUnbinding();
                    }
                }
            }
        }catch (Exception e){
            throw new ClientDisconnectException(e);
        }
    }
    private String getConnectionTextStatus(MessengerConnection.ConnectionStatus status) {
        String errorMessage;
        switch (status) {
            case ERROR_MAX_CONNECTION_EXCEEDED: {
                errorMessage = "Maximum connections exceeded";
                break;
            }
            case ERROR_CONNECTION_UNAVAILABLE: {
                errorMessage = "Maximum connections must higher than zero to start accepting connections.";
                break;
            }
            case ERROR_CONNECTION_MULTCLIENT_PARALLEL_LIMIT_EXCEEDED: {
                errorMessage = "Connection multiClient available but the parallel connection limit has been exceeded";
                break;
            }
            case ERROR_CONNECTION_NORMAL_PARALLEL_LIMIT_EXCEEDED: {
                errorMessage = "Connection normal available but the parallel connection limit has been exceeded";
                break;
            }
            case ERROR_CONNECTION_MULTCLIENT_NO_MATCHS_FOUND: {
                errorMessage = "Connection multiClient available, but rules have no matches";
                break;
            }
            case ERROR_CONNECTION_NORMAL_NO_MATCHS_FOUND: {
                errorMessage = "Connection normal available, but rules have no matches";
                break;
            }
            case ERROR_CONNECTION_MULTCLIENT_ALREADY_CONNECTED: {
                errorMessage = "MultiClient already connected.";
                break;
            }
            case ERROR_CONNECTION_NORMAL_ALREADY_CONNECTED: {
                errorMessage = "Connection normal already connected.";
                break;
            }
            case ERROR_CONNECTION_TAG_NOT_REGISTERED: {
                errorMessage = "Not encountered tag registry. please go to MessengerConnectionManager.java and move to superclass MessengerConnectionManager() and add a new ConnectionPolicy like ex: connectionPolicies.add(new ConnectionPolicy(UltimateRealAppMyPurchasesActivity.class, ConnectionType....";
                break;
            }
            case ERROR_CONNECTION_RULES_INCOMPLETE_MATCH_ARGUMENTS: {
                errorMessage = "The ConnectionRulesMatch requires more than one arguments";
                break;
            }
            case CONNECTION_CLIENT_SUCCESS: {
                errorMessage = "Connection client success.";
                break;
            }
            case CONNECTION_MULTICLIENT_SUCCESS: {
                errorMessage = "Connection multi client success.";
                break;
            }
            default:
                errorMessage = "unkown";
        }
        return errorMessage;
    }
}
class AppMenssengerCallback implements AppMenssengerInterface {
    @Override
    public void onConnected(Message msg) {
    }
    @Override
    public void onConnectionChanges(MessengerConnection.ConnectionState state, MessengerConnection.ConnectionStatus status, String textStatus) {
    }
    @Override
    public void onAttached() {
    }
    @Override
    public void onClose() {
    }
    @Override
    public void onBinding() {
    }
    @Override
    public void onDisconnected(Message msg) {
    }
    @Override
    public void onUnbinding() {
    }
    @Override
    public void onReceiveMessage(Message msg) {
    }
}
interface AppMenssengerInterface {
    void onConnected(Message msg);
    void onConnectionChanges(MessengerConnection.ConnectionState state, MessengerConnection.ConnectionStatus status, String textStatus);
    void onAttached();
    void onClose();
    void onBinding();
    void onDisconnected(Message msg);
    void onUnbinding();
    void onReceiveMessage(Message msg);
}
class ClientIsUnbinding extends Exception{
    public ClientIsUnbinding(String msg) {
        super(msg);
    }
}
class ClientIsBinding extends Exception{
    public ClientIsBinding(String msg) {
        super(msg);
    }
}
class ClientIsConnecting extends Exception{
    public ClientIsConnecting(String msg) {
        super(msg);
    }
}
class ClientIsDisconnecting extends Exception{
    public ClientIsDisconnecting(String msg) {
        super(msg);
    }
}
class ClientAlreadyConnected extends Exception{
    public ClientAlreadyConnected(String msg) {
        super(msg);
    }
}
class ClientAlreadyDisconnected extends Exception{
    public ClientAlreadyDisconnected(String msg) {
        super(msg);
    }
}
class ClientDisconnectException extends Exception{
    public ClientDisconnectException(Throwable cause) {
        super(cause);
    }
}
class ClientConnectException extends Exception{
    public ClientConnectException(Throwable cause) {
        super(cause);
    }
}