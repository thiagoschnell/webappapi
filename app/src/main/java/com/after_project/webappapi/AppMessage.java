package com.after_project.webappapi;

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
    void registerReceiver( String receiverName, BroadcastReceiver receiver){
        LocalBroadcastManager.getInstance(mContext)
                .registerReceiver(receiver, new IntentFilter(receiverName));
    }
    private void send(String receiverName, int param, String event, String data, Boolean sync){
        Intent intent = new Intent(receiverName);
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