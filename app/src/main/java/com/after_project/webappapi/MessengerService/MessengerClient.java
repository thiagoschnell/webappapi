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
import android.os.Messenger;
import android.os.RemoteException;
import androidx.annotation.IntDef;
import androidx.multidex.MultiDexApplication;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
public class MessengerClient extends MultiDexApplication {
    protected static final int MESSENGER_CLIENT_STATUS_CONNECTED = 1;
    protected static final int MESSENGER_CLIENT_STATUS_DISCONNECTED = 2;
    protected static final int MESSENGER_CLIENT_STATUS_BINDING = 3;
    protected static final int MESSENGER_CLIENT_STATUS_UNBINDING = 4;
    protected static final int MESSENGER_CLIENT_STATUS_CLOSE = 5;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MESSENGER_CLIENT_STATUS_CONNECTED, MESSENGER_CLIENT_STATUS_DISCONNECTED, MESSENGER_CLIENT_STATUS_BINDING, MESSENGER_CLIENT_STATUS_UNBINDING, MESSENGER_CLIENT_STATUS_CLOSE})
    private @interface MessengerClientStatus {}
    private @MessengerClientStatus int messengerClientStatus;
    protected @MessengerClientStatus int getMessengerClientStatus() {
        return messengerClientStatus;
    }
    private Messenger mService = null;
    private boolean mIsBound;
    MenssengerClientCallback menssengerClientCallback = null;
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessengerServerService.MSG_CLIENT_CONNECTED:{
                    messengerClientStatus = MESSENGER_CLIENT_STATUS_CONNECTED;
                    if(menssengerClientCallback!=null) {
                        menssengerClientCallback.onConnected();
                    }
                    break;
                }
                case MessengerServerService.MSG_CLIENT_DISCONNECTED:{
                    messengerClientStatus = MESSENGER_CLIENT_STATUS_DISCONNECTED;
                    if(menssengerClientCallback!=null){
                        menssengerClientCallback.onDisconnected();
                    }
                    break;
                }
                case MessengerServerService.MSG_WEBAPP_RESPONSE:
                case MessengerServerService.MSG_WEBAPP_LOADED:
                case MessengerServerService.MSG_WEBAPP_ERROR:{
                    if(menssengerClientCallback!=null) {
                        menssengerClientCallback.onMessageHandle(msg);
                    }
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    }
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mService = new Messenger(service);
            if(menssengerClientCallback!=null){
                menssengerClientCallback.onAttached();
            }
            {
                Message msg = Message.obtain(null,
                        MessengerService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                sendMessageToServer(msg);
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            messengerClientStatus = MESSENGER_CLIENT_STATUS_CLOSE;
            if(menssengerClientCallback!=null){
                menssengerClientCallback.onClose();
            }
        }
    };
    protected void sendMsgRequest(Message msg, int _myid, String _url) throws Exception {
        if (mIsBound) {
            if (mService != null) {
                msg.getData().putInt("myid", _myid);
                msg.getData().putString("url",_url);
                mService.send(msg);
            }
        }
    }
    private void sendMessageToServer(Message msg){
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    protected void MessengerClientConnect() throws Exception {
        Intent i = new Intent(this,
                MessengerServerService.class);
        i.putExtra("maxConnectionClients",1);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        messengerClientStatus = MESSENGER_CLIENT_STATUS_BINDING;
        if(menssengerClientCallback!=null){
            menssengerClientCallback.onBinding();
        }
    }
    protected void MessengerClientDisconnect() {
        if (mIsBound) {
            if (mService != null) {
                Message msg = Message.obtain(null,
                        MessengerService.MSG_UNREGISTER_CLIENT);
                msg.replyTo = mMessenger;
                sendMessageToServer(msg);
            }
            try {
                unbindService(mConnection);
            }catch (Exception e){
                e.printStackTrace();
            }
            finally {
                mIsBound = false;
                messengerClientStatus = MESSENGER_CLIENT_STATUS_UNBINDING;
                if(menssengerClientCallback!=null){
                    menssengerClientCallback.onUnbinding();
                }
            }
        }
    }
    protected class MenssengerClientCallback implements MenssengerServiceClientCallback {
        @Override
        public void onConnected() {
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
        public void onDisconnected() {
        }
        @Override
        public void onUnbinding() {
        }
        @Override
        public void onMessageHandle(Message msg) {
        }
    }
    private interface MenssengerServiceClientCallback {
        void onConnected();
        void onAttached();
        void onClose();
        void onBinding();
        void onDisconnected();
        void onUnbinding();
        void onMessageHandle(Message msg);
    }
}