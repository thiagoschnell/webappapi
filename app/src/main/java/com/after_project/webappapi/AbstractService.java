package com.after_project.webappapi;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import java.util.ArrayList;
public abstract class AbstractService extends Service {
    static final int MSG_REGISTER_CLIENT = 9991;
    static final int MSG_UNREGISTER_CLIENT = 9992;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                default:
                    onReceiveMessage(msg);
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        onStartService();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        onStopService();
    }
    protected void send(Message msg) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                mClients.get(i).send(msg);
            }
            catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }
    public abstract void onStartService();
    public abstract void onStopService();
    public abstract void onReceiveMessage(Message msg);
}