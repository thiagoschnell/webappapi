package com.after_project.webappapi;
// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
public class AppMessage {
    private Context mContext;
    AppMessage(Context context){
        mContext = context;
    }
    void unregisterReceiver(BroadcastReceiver receiver){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
    }
    private String action(String action){
        return String.format("%s.APP_MESSAGE_%s",com.after_project.webappapi.BuildConfig.APPLICATION_ID,action.toUpperCase());
    }
    void registerReceiver( String receiverName, BroadcastReceiver receiver){
        LocalBroadcastManager.getInstance(mContext)
                .registerReceiver(receiver, new IntentFilter(action(receiverName)));
    }
    private void send(String receiverName, int param, String event, String data, Boolean sync){
        Intent intent = new Intent(action(receiverName));
        intent.putExtra("param", param);
        intent.putExtra("event", event);
        intent.putExtra("data", data);
        if(sync){
            LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
        }else{
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }
    protected void sendSync(String receiverName, int param, String event, String data){
        send(receiverName,param,event,data,true);
    }
    protected void sendTo(String receiverName, int param, String event, String data){
        send(receiverName,param,event,data,false);
    }
}