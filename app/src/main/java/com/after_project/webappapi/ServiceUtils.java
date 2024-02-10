package com.after_project.webappapi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
class Utils {
    static void returnUpMyService(final Context context, String serviceTag) {
        if (killServiceIfRun(context)) {
            startServiceOn(context,serviceTag);
        }
    }
    private static boolean killServiceIfRun(final Context context) {
        boolean isRunning = isMyServiceRunning(context);
        if (!isRunning) { return true; }
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) {
                manager.killBackgroundProcesses(getServicename(context));
                return true;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private static boolean isServiceInCache(final Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null && manager.getRunningAppProcesses() != null) {
            if (manager.getRunningAppProcesses().size() > 0) {
                for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                    if (process.processName != null) {
                        if (process.processName.equalsIgnoreCase(getServicename(context))) {
                            if (process.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    static void StartMyService(Context context,String serviceTag) {
        if (isMyServiceRunning(context) && !isServiceInCache(context)) {
            return;
        }
        if (isServiceInCache(context)) {
            returnUpMyService(context,serviceTag);
        } else {
            startServiceOn(context,serviceTag);
        }
    }
    private static void startServiceOn(final Context context,String serviceTag) {
        new ScheduledThreadPoolExecutor(1).schedule(() -> {
            Intent launchIntent = null;
            if(serviceTag.equals(ApiNotificationService.TAG)){
                ApiNotificationService service = new ApiNotificationService();
                launchIntent = new Intent(context, service.getClass());
            }
            if(launchIntent!=null){
                context.startService(launchIntent);
            }
        }, 50, TimeUnit.MILLISECONDS);
    }
    private static boolean isMyServiceRunning(final Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null && manager.getRunningAppProcesses() != null) {
            if (manager.getRunningAppProcesses().size() > 0) {
                for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                    if (process != null && process.processName != null && process.processName.equalsIgnoreCase(getServicename(context))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private static String getServicename(final Context context) {
        return context.getPackageName() + ":serviceNonStoppable";
    }
}