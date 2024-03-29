package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class AppMessageReceiver extends BroadcastReceiver{
   private ReceiverCallback receiverCallback;
    AppMessageReceiver(ReceiverCallback receiverCallback){
       this.receiverCallback = receiverCallback;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        receiverCallback.onReceiveMessage(
                intent.getIntExtra("param", -1),
                intent.getStringExtra("event"),
                intent.getStringExtra("data")
        );
    }
    protected interface ReceiverCallback {
        void onReceiveMessage(int param, String event, String data);
    }
}