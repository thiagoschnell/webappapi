package com.after_project.webappapi;
import static com.after_project.webappapi.MyApp.WEBAPP_STATUS_LOAD_ERROR;
import static com.after_project.webappapi.MyApp.WEBAPP_STATUS_LOAD_FINISHED;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Random;
public class ApiNotificationService extends Service {
    final public static String TAG = ApiNotificationService.class.getSimpleName();
    private @MyApp.WebAppStatus int webAppStatus;
    private WebApp webApp = null;
    private boolean keepRunningAfterAppClosed = true;
    private final String GROUP_KEY = "GK";
    private final String CHANNEL_ID = "channel_id";
    private final int SUMMARY_ID = 100;
    private ArrayList receivedUserNotifications = new ArrayList();
    private NotificationManager notificationManager = null;
    private Notification summaryNotification = null;
    private int generate_random(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }
    private NotificationManager getNotificationManager(){
        if(notificationManager!=null){
            return notificationManager;
        };
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                notificationManager.createNotificationChannel(channel);
            }
        }
        return notificationManager;
    }
    private void showUserNotification(String title,String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_action_chat)
                .setAutoCancel(true)
                .setChannelId(CHANNEL_ID)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentText(message)
                .setContentTitle(title)
                .setGroup(GROUP_KEY);
        getNotificationManager().notify(generate_random(999,99999), nb.build());
    }
    private Notification getSummaryNotification(){
        if(summaryNotification!=null){
            return summaryNotification;
        };
         summaryNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setDefaults(Notification.DEFAULT_ALL)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true).build();
        return summaryNotification;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
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
           if(timeout!=0){
               endTime= getTickCount();
               startTime = getTickCount();
               this.timeout = timeout;
           }
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
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Handler handler = new Handler();
            //WebApp
            {
                String[] allowedDomains = {
                        // [START] CORS domains
                        // [END] CORS domains
                        // [START] Website domain
                            "realappexample.shop"
                        // [END] Website domain
                };
                WebView webview = new WebView(this);
                webApp = new WebApp(webview,new WebViewAssetLoader.Builder()
                        .setDomain(allowedDomains[allowedDomains.length-1])
                        .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                        .build(),
                        allowedDomains,
                        WebApp.FLAG_CLEAR_CACHE);
                try {
                    webApp.loadDataWithBaseUrl("https://realappexample.shop/",
                            new RawResource(getAssets(),"home.html"),
                            new WebAppCallback() {
                                @Override
                                public void onLoadFinish(WebView view, String url) {
                                    webApp.detachWebAppCallback();
                                    webAppStatus = WEBAPP_STATUS_LOAD_FINISHED;
                                }
                                @Override
                                public void onLoadError(WebView view,
                                        /*RequiresApi(api >= 21)*/WebResourceRequest request, WebResourceErrorCompat error,
                                        /*RequiresApi(api >=19)*/ int errorCode, String description, String failingUrl)
                                {
                                    webApp.detachWebAppCallback();
                                    webAppStatus = WEBAPP_STATUS_LOAD_ERROR;
                                }
                            });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            new Thread(() -> {
                while (webAppStatus == MyApp.WEBAPP_STATUS_NONE){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                switch (webAppStatus){
                    case WEBAPP_STATUS_LOAD_FINISHED:{
                        {
                            Thread backgroundThread = new Thread(() -> {
                                TaskTimeout taskTimeout = new TaskTimeout();
                                taskTimeout.execute();
                                int delay= 9999999;//until receive response
                                int timeout = 1500;//after receive response, wait more 1,5ms
                                while(true){
                                    if(taskTimeout.getTimeout()==0) {
                                        taskTimeout.setTimeout(delay);
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                {
                                                    webApp
                                                            .evalJavaScript(
                                                                    "url('" + "https://realappexample.shop/userNotifications.json" + "').get().response().data",
                                                                    new ValueCallback() {
                                                                        @Override
                                                                        public void onReceiveValue(Object response) {
                                                                            try {
                                                                                JsonObject responseJsonObject = JsonParser.parseString((String) response).getAsJsonObject();
                                                                                JsonArray dataJsonArray = responseJsonObject.getAsJsonArray("data");
                                                                                for( JsonElement jsonElement  : dataJsonArray.asList()) {
                                                                                    String name = jsonElement.getAsJsonObject().get("name").getAsString();
                                                                                    String title = jsonElement.getAsJsonObject().get("title").getAsString();
                                                                                    String message = jsonElement.getAsJsonObject().get("message").getAsString();
                                                                                    if(!receivedUserNotifications.contains(name)){
                                                                                        receivedUserNotifications.add(name);
                                                                                        showUserNotification(title, message);
                                                                                    }
                                                                                }
                                                                                if(summaryNotification==null){
                                                                                    notificationManager.notify(SUMMARY_ID, getSummaryNotification());
                                                                                }
                                                                                taskTimeout.setTimeout(timeout);
                                                                            } catch (Exception e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    });
                                                }
                                            }
                                        });
                                    }
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                            backgroundThread.start();
                        }
                        break;
                    }
                    case WEBAPP_STATUS_LOAD_ERROR:{
                        //todo load error
                        break;
                    }
                }
            })
            {
            }.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onDestroy() {
        if(keepRunningAfterAppClosed) {
            final Context context = getBaseContext();
                new Thread() {
                    @Override
                    public void run() {
                        Utils.returnUpMyService(context, TAG);
                    }
                }.start();
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}