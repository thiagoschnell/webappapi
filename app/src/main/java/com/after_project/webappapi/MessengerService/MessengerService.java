package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import java.util.ArrayList;
public class MessengerService extends Service {
    protected ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    onReceiveMessage(msg);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    onReceiveMessage(msg);
                    break;
                default:
                    onReceiveMessage(msg);
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }
    protected void onReceiveMessage(Message msg){
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
