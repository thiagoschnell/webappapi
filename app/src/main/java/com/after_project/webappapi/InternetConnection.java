package com.after_project.webappapi;
// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Data;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class InternetConnection {
    @RequiresApi(24)
    protected MutableLiveData mutableLiveDataAvailable = new MutableLiveData<Data>();
    protected MutableLiveData mutableLiveDataOffline = new MutableLiveData<Data>();
    protected MutableLiveData mutableLiveDataOnline = new MutableLiveData<Data>();
    private boolean available = false;
    private boolean online = false;
    private boolean offline = false;
    private boolean capabilities = false;
    private TaskTimeout taskTimeout = null;
    private int schedulerCount = 0;
    private int schedulerIndex = 0;
    private ScheduledExecutorService schedulerThread;
    private ConnectivityManager connectivityManager = null;
    private Context context = null;
    private InternetConnectionInterface sharedCallback = null;
    private InternetConnectionInterface mode1Callback = null;
    private InternetConnectionInterface mode2Callback = null;
    private InternetConnectionInterface mode3Callback = null;
    private ConnectivityManager.NetworkCallback mode2NetworkCallback = null;
    private ConnectivityManager.NetworkCallback mode3NetworkCallback = null;
    private  BroadcastReceiver broadcastReceiver = null;
    @RequiresApi(24)
    protected boolean isAvailable(){
        return available;
    }
    protected boolean isOnline(){
        return online;
    }
    protected boolean isOffline(){
        return offline;
    }
    private void setAvailable(boolean available){
        this.available = available;
        mutableLiveDataAvailable.postValue(available);
    }
    private void setOnline(boolean online){
        this.online = online;
        mutableLiveDataOnline.postValue(online);
    }
    private void setOffline(boolean offline){
        this.offline = offline;
        mutableLiveDataOffline.postValue(offline);
    }
    InternetConnection(Context context){
        this.context = context;
        schedulerThread = Executors.newSingleThreadScheduledExecutor();
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        initSharedCallback();
        taskTimeout = new TaskTimeout();
        taskTimeout.execute();
    }
    @RequiresApi(19)
    protected static final int FLAG_SET_MODE_1 = 1; //mode1 broadcastreceiver for android api 19 to 21
    @RequiresApi(21)
    protected static final int FLAG_SET_MODE_2 = 1<<1; //mode2 registerNetworkCallback for android api 21 to 24
    @RequiresApi(24)
    protected static final int FLAG_SET_MODE_3 = 1<<2; //mode3 registerDefaultNetworkCallback for android api 24 to latest
    @RequiresApi(24)
    protected static final int FLAG_SET_MODE_ALL = 1<<3;
    protected static final int FLAG_INIT_MODE_1 = 1<<4;
    protected static final int FLAG_INIT_MODE_2 = 1<<5;
    protected static final int FLAG_INIT_MODE_3 = 1<<6;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag=true,value={FLAG_SET_MODE_1,FLAG_SET_MODE_2,FLAG_SET_MODE_3,FLAG_SET_MODE_ALL,FLAG_INIT_MODE_1,FLAG_INIT_MODE_2,FLAG_INIT_MODE_3})
    @interface Flags{}
    private void setFlags(@Flags int flags) {
        if ((flags&FLAG_SET_MODE_1) != 0){
            mode1Callback = sharedCallback;
        }
        if ((flags&FLAG_SET_MODE_2) != 0){
            mode2Callback = sharedCallback;
        }
        if ((flags&FLAG_SET_MODE_3) != 0){
            mode3Callback = sharedCallback;
        }
        if ((flags&FLAG_SET_MODE_ALL) != 0){
            mode1Callback = sharedCallback;
            mode2Callback = sharedCallback;
            mode3Callback = sharedCallback;
        }
        if ((flags&FLAG_INIT_MODE_1) != 0){
            initMode1();
        }
        if ((flags&FLAG_INIT_MODE_2) != 0){
            initMode2();
        }
        if ((flags&FLAG_INIT_MODE_3) != 0){
            initMode3();
        }
    }
    protected void setMode(@Flags int flags){
        mode1Callback=null;
        mode2Callback=null;
        mode3Callback=null;
        setFlags(flags);
    }
    private interface InternetConnectionInterface{
        void onAvailable(Network network);
        void onLost(Network network);
        void onLosing(@NonNull Network network, int maxMsToLive);
        void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities);
        void onUnavailable();
        void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties);
        void onBlockedStatusChanged(@NonNull Network network, boolean blocked);
        void onReceive(Context context, Intent intent);
    }
    protected void onDestroy(){
        if(broadcastReceiver!=null){
            context.unregisterReceiver(broadcastReceiver);
        }
        if (Build.VERSION.SDK_INT >= LOLLIPOP) {
            if(mode2NetworkCallback!=null){
                connectivityManager.unregisterNetworkCallback(mode2NetworkCallback);
            }
            if(mode3NetworkCallback!=null){
                connectivityManager.unregisterNetworkCallback(mode3NetworkCallback);
            }
        }
        schedulerThread.shutdownNow();
        taskTimeout.cancel(true);
        connectivityManager=null;
    }
    private void initMode1(){
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(mode1Callback!=null){
                        mode1Callback.onReceive(context,intent);
                    }
                }
            };
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
            intentFilter.addAction(ConnectivityManager.EXTRA_REASON);
            intentFilter.addAction(ConnectivityManager.EXTRA_EXTRA_INFO);
            intentFilter.addAction(ConnectivityManager.EXTRA_IS_FAILOVER);
            intentFilter.addAction(ConnectivityManager.EXTRA_NETWORK_TYPE);
            intentFilter.addAction(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
            intentFilter.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK);
            intentFilter.addAction(WifiManager.EXTRA_PREVIOUS_WIFI_STATE);
            intentFilter.addAction(WifiManager.EXTRA_WIFI_STATE);
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    intentFilter.addAction(ConnectivityManager.EXTRA_NETWORK_REQUEST);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    intentFilter.addAction(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intentFilter.addAction(WifiManager.EXTRA_RESULTS_UPDATED);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    intentFilter.addAction(WifiManager.EXTRA_SCAN_AVAILABLE);
                }
            }
            intentFilter.addAction(WifiManager.EXTRA_NETWORK_INFO);
            intentFilter.addAction(WifiManager.EXTRA_SUPPLICANT_CONNECTED);
            intentFilter.addAction(WifiManager.EXTRA_SUPPLICANT_ERROR);
            intentFilter.addAction(WifiManager.EXTRA_WIFI_INFO);
            context.registerReceiver(broadcastReceiver, intentFilter);
        }
    }
    private void initMode3(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mode3NetworkCallback =  new ConnectivityManager.NetworkCallback() {
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    if(mode3Callback!=null){
                        mode3Callback.onAvailable(network);
                    }
                }
                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    if(mode3Callback!=null){
                        mode3Callback.onLost(network);
                    }
                }
                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    if(mode3Callback!=null){
                        mode3Callback.onCapabilitiesChanged(network, networkCapabilities);
                    }
                }
                @Override
                public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                    if(mode3Callback!=null){
                        mode3Callback.onLinkPropertiesChanged(network, linkProperties);
                    }
                }
            };
            connectivityManager.registerDefaultNetworkCallback(mode3NetworkCallback);
        }
    }
    private void initMode2(){
        if (Build.VERSION.SDK_INT >= LOLLIPOP) {
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addCapability(NET_CAPABILITY_INTERNET)
                    // .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                    .addTransportType( Build.VERSION.SDK_INT >= O? NetworkCapabilities.TRANSPORT_WIFI_AWARE: NetworkCapabilities.TRANSPORT_WIFI )
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
                    .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                   // .addTransportType(NetworkCapabilities.TRANSPORT_USB)
                    .build();
            mode2NetworkCallback =   new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    if(mode2Callback!=null){
                        mode2Callback.onAvailable(network);
                    }
                }
                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    if(mode2Callback!=null){
                        mode2Callback.onLost(network);
                    }
                }
                @Override
                public void onLosing(@NonNull Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    if(mode2Callback!=null){
                        mode2Callback.onLosing(network, maxMsToLive);
                    }
                }
                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    if(mode2Callback!=null){
                        mode2Callback.onUnavailable();
                    }
                }
                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    if(mode2Callback!=null){
                        mode2Callback.onCapabilitiesChanged(network, networkCapabilities);
                    }
                }
                @Override
                public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                    if(mode2Callback!=null){
                        mode2Callback.onLinkPropertiesChanged(network, linkProperties);
                    }
                }
                @Override
                public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                    super.onBlockedStatusChanged(network, blocked);
                    if(mode2Callback!=null){
                        mode2Callback.onBlockedStatusChanged(network, blocked);
                    }
                }
            };
            connectivityManager.registerNetworkCallback(networkRequest,mode2NetworkCallback);
        }
    }
    private boolean hostAvailable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 2000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    void schedule() {
        {
            int delay = 0;
            if(schedulerIndex == schedulerCount ){
                if(schedulerIndex == schedulerCount){
                    delay=0;
                }else{
                    delay=3000;
                }
                schedulerCount++;
            }else{
                return;
            }
            schedulerThread.schedule(new Runnable() {
                @Override
                public void run() {
                    int id= schedulerIndex;
                    boolean b = hostAvailable("www.google.com",80);
                    if(b){
                        setAvailable(true);
                        setOnline(true);
                        setOffline(false);
                    }else{
                        setOnline(false);
                        setOffline(true);
                    }
                    schedulerIndex++;
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }
    private void initSharedCallback(){
        sharedCallback = new InternetConnectionInterface() {
            @Override
            public void onAvailable(Network network) {
                taskTimeout.setTimeout(1000);
            }
            @Override
            public void onLost(Network network) {
                taskTimeout.setTimeout(1000);
                setAvailable(false);
            }
            @Override
            public void onLosing(@NonNull Network network, int maxMsToLive) {
            }
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                taskTimeout.setTimeout(1000);
                if (Build.VERSION.SDK_INT >= LOLLIPOP) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork())
                                .hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                            setAvailable(false);
                        } else {
                            setAvailable(true);
                            taskTimeout.setTimeout(1000);
                        }
                        if (offline) {
                            capabilities = false;
                        } else {
                            capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork()).hasCapability(NET_CAPABILITY_INTERNET);
                        }
                    }
                }
            }
            @Override
            public void onUnavailable() {
            }
            @Override
            public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                taskTimeout.setTimeout(1000);
                if(!offline) {
                    if (capabilities) {
                        setAvailable(true);
                        taskTimeout.setTimeout(1000);
                    }
                }
            }
            @Override
            public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
            }
            @Override
            public void onReceive(Context context, Intent intent) {
                taskTimeout.setTimeout(1000);
                {
                    Bundle extras = intent.getExtras();
                    NetworkInfo info = (NetworkInfo) extras.getParcelable("networkInfo");
                    if(info!=null) {
                        if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
                            setAvailable(false);
                            taskTimeout.setTimeout(1000);
                        }
                    }else{
                    }
                }
            }
        };
    }
    private class TaskTimeout extends AsyncTask<Void,Void,String> {
        private long startTime = 0;
        private long timeout = 0;
        private long endTime = 0;
        private long getTickCount(){
            return System.currentTimeMillis();
        }
        public long getTimeout() {
            return timeout;
        }
        public void setTimeout(long timeout) {
            endTime= getTickCount();
            startTime = getTickCount();
            this.timeout = timeout;
        }
        @Override
        protected String doInBackground(Void... voids) {
            while(true){
                int count = 0;
                while(endTime - startTime  < timeout){
                    endTime= getTickCount();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    count++;
                }
                if(timeout!=0){
                    timeout = 0;
                    schedule();
                }else if(timeout==0){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(1==2)break;
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    };
}