package com.after_project.webappapi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
public class ServiceManager {
    private Class<? extends AbstractService> mServiceClass;
    private Context mActivity;
    private boolean mIsBound;
    private Messenger mService = null;
    private Handler mIncomingHandler = null;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (mIncomingHandler != null) {
                mIncomingHandler.handleMessage(msg);
            }
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, AbstractService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
    public ServiceManager(Context context, Class<? extends AbstractService> serviceClass, Handler incomingHandler) {
        this.mActivity = context;
        this.mServiceClass = serviceClass;
        this.mIncomingHandler = incomingHandler;
        if (isRunning()) {
            doBindService();
        }
    }
    public void start() {
        doStartService();
        doBindService();
    }
    public void stop() {
        doUnbindService();
        doStopService();
    }
    public void unbind() {
        doUnbindService();
    }
    public boolean isRunning() {
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (mServiceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void send(Message msg) throws RemoteException {
        if (mIsBound) {
            if (mService != null) {
                mService.send(msg);
            }
        }
    }
    public void sendRequest(Message msg, WebAppService.Request request) throws Exception {
        if (mIsBound) {
            if (mService != null) {
                msg.getData().putInt("myid", request.getMyid());
                msg.getData().putString("url",request.getUrl());
                mService.send(msg);
            }
        }
    }
    private void doStartService() {
        mActivity.startService(new Intent(mActivity, mServiceClass));
    }
    private void doStopService() {
        mActivity.stopService(new Intent(mActivity, mServiceClass));
    }
    private void doBindService() {
        mActivity.bindService(new Intent(mActivity, mServiceClass), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }
    private void doUnbindService() {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, AbstractService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
            mActivity.unbindService(mConnection);
            mIsBound = false;
        }
    }
}