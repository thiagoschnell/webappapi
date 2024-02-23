package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.
import static android.content.Context.ACTIVITY_SERVICE;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
class ServiceUtils{
    protected void StartUpMyService(final Context context, Intent intent) {
            StartService(context,intent,50);
    }
     private boolean StopService(final Context context, Intent intent) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            if (manager != null) {
                context.stopService(intent);
                return true;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    void StopMyService(Context context, Intent intent) {
        int  ServiceRunningResult = isServiceRunningOrServiceInCache(context);
        if (ServiceRunningResult==1) {
            StopService(context,intent);
        }else
        if(ServiceRunningResult == 2) {
            StopService(context,intent);
        }
    }
    private void StartMyService(Context context, Intent intent, int delay) {
        int  ServiceRunningResult = isServiceRunningOrServiceInCache(context);
        if (ServiceRunningResult == 1) {
            return;
        }else if(ServiceRunningResult == 2){
            StartUpMyService(context,intent);
        } else {
            StartService(context,intent,delay);
        }
    }
    protected void StartMyService(Context context, Intent intent){
        StartMyService(context,intent,0);
    }
    private void StartService(final Context context, Intent intent,int delay) {
        if (delay != 0) {
            new ScheduledThreadPoolExecutor(1).schedule(() -> {
                if (intent != null) {
                    context.startService(intent);
                }
            }, delay, TimeUnit.MILLISECONDS);
        }else{
            context.startService(intent);
        }
    }
    synchronized ServiceSchedule schedule (int delay){
        return new ServiceSchedule(delay);
    }
    class ServiceSchedule{
        private int delay = 0;
        ServiceSchedule(int dealy){
            this.delay = dealy;
        }
        void startMyService(Context context, Intent intent){
            StartMyService(context,intent,delay);
        }
    }
    protected int isServiceRunningOrServiceInCache(final Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        if (manager != null && manager.getRunningAppProcesses() != null) {
            if (manager.getRunningAppProcesses().size() > 0) {
                for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                    if (process != null && process.processName != null) {
                         if (process.processName.equalsIgnoreCase(getServicename(context))) {
                            int result = 1;
                            if (process.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                                result = 2;
                            }
                            return result;
                        }
                    }
                }
            }
        }
        return 0;
    }
    private String getServicename(final Context context) {
        return context.getPackageName();
    }
}