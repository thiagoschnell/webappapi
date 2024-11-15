package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.
import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.SystemClock;
import android.os.ext.SdkExtensions;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
@RequiresApi(23)
public class InternetConnection2 {
    private Boolean isOnline = false;
    private boolean isOffline = false;
    long disconnectTime = 0;
    private MutableLiveData networkStateLive = new MutableLiveData<Boolean>();
    private ConnectivityManager connectivityManager = null;
    private ConnectivityManager.NetworkCallback networkCallback = null;
    private NetworkRequest networkRequest = null;
    private boolean connected = false;
    InternetConnection2(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkRequest  = getNetworkRequest();
    }
    @RequiresApi(23)
    void initNetworkStateHandle(){
        new Thread(() -> {
            setConnected(isInternetAccessSuccess());
            registerNetworkStateChanges(networkRequest);
        }).start();
    }
    void stopNetworkStateHandle(){
        unregisterNetworkStateChanges(networkRequest);
    }
    private void handleCapabilitiesChanged( NetworkCapabilities networkCapabilities){
        if(networkCapabilities==null) return;
        if (connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork())
                .hasCapability(NET_CAPABILITY_VALIDATED)) {
            if(connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork())
                    .hasCapability(NET_CAPABILITY_INTERNET)){
                {
                    handleOnLineState();
                }
            }
        }
    }
    private NetworkRequest getNetworkRequest(){
        NetworkRequest.Builder networkRequest ;
        networkRequest = new NetworkRequest.Builder();
        networkRequest.addCapability(NET_CAPABILITY_INTERNET);
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            }
            networkRequest.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
            networkRequest.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            networkRequest.addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH);
            networkRequest.addTransportType(NetworkCapabilities.TRANSPORT_VPN);
            if (Build.VERSION.SDK_INT >= O) {
                networkRequest.addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                networkRequest.addTransportType(NetworkCapabilities.TRANSPORT_LOWPAN);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                networkRequest.addTransportType(NetworkCapabilities.TRANSPORT_USB);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= 7) {
                networkRequest.addTransportType(NetworkCapabilities.TRANSPORT_THREAD);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= 12) {
                networkRequest.addTransportType(NetworkCapabilities.TRANSPORT_SATELLITE);
            }
        }
        return networkRequest.build();
    }
    private void registerNetworkStateChanges(NetworkRequest networkRequest){
        ConnectivityManager.NetworkCallback networkCallback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback =  new ConnectivityManager.NetworkCallback() {
                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    handleOfflineState();
                }
                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    handleCapabilitiesChanged(networkCapabilities);
                }
            };
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }else
        if (Build.VERSION.SDK_INT >= LOLLIPOP) {
            networkCallback =   new ConnectivityManager.NetworkCallback() {
                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    handleOfflineState();
                }
                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    handleCapabilitiesChanged(networkCapabilities);
                }
            };
            connectivityManager.registerNetworkCallback(networkRequest,networkCallback);
        }
    }
    private void unregisterNetworkStateChanges(NetworkRequest networkRequest){
        if(networkCallback!=null){
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
    private void handleOnLineState() {
        if (isOnline != null && isOnline) return;
        {
            isOnline = true;
            isOffline = false;
            setConnected(true);
            networkStateLive.postValue(true);
        }
    }
    private void handleOfflineState() {
        if (isOffline) return;
        {
            isOffline = true;
            isOnline = false;
            setConnected(false);
            networkStateLive.postValue(false);
        }
        new Thread(() -> {
            disconnectTime = System.currentTimeMillis();
            int count = 1;
            while (isOffline) {
                int interval = 3333;
                int inc = count * (interval * count / 10);
                SystemClock.sleep(interval + inc);
                if (count > 10) break;
                if (isOffline) {
                    if (isInternetAccessSuccess()) {
                        isOffline = false;
                        isOnline = null;
                        setConnected(true);
                        networkStateLive.postValue(true);
                    } else {
                    }
                }
                count++;
            }
        }).start();
    }
    private boolean isInternetAccessSuccess(){
        return hostAvailable("www.google.com", 80);
    }
    private boolean hostAvailable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 4000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    protected MutableLiveData getNetworkStateLive() {
        return networkStateLive;
    }
    public long getDisconnectedTime() {
        if(disconnectTime > 0 ){
            return System.currentTimeMillis() - disconnectTime;
        }
        return disconnectTime;
    }
    public long getDisconnectTime() {
        return disconnectTime;
    }
    public void setConnected(boolean connected) {
        if(connected){
            disconnectTime = 0;
        }else{
            disconnectTime = System.currentTimeMillis();
        }
        this.connected = connected;
    }
    public boolean isConnected() {
        return connected;
    }
}
