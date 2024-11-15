package com.after_project.webappapi;
import static com.after_project.webappapi.WebApp.WEBAPP_STATUS_LOAD_ERROR;
import static com.after_project.webappapi.WebApp.WEBAPP_STATUS_LOAD_FINISHED;
import static com.after_project.webappapi.WebApp.WEBAPP_STATUS_NONE;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
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
import java.util.List;
import java.util.Random;
public class ApiNotificationService extends Service {
    final public static String TAG = ApiNotificationService.class.getSimpleName();
    private WebApp webApp = null;
    private boolean keepRunningAfterAppClosed = true;
    private final String GROUP_KEY = "GK";
    private final String CHANNEL_ID = "channel_id";
    private final int SUMMARY_ID = 100;
    private int last_receivedCount = 0;
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
    private Notification getSummaryNotification(NotificationCompat.Style style){
        androidx.core.app.NotificationCompat.Builder nb = new NotificationCompat.Builder(this, CHANNEL_ID);
        nb
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setDefaults(Notification.DEFAULT_ALL)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true);
        if(style!=null) {
            nb.setStyle(style);
        }
        return nb.build();
    }
    private Notification getSummaryNotification(){
        if(summaryNotification!=null){
            return summaryNotification;
        };
        summaryNotification = getSummaryNotification(null);
        return summaryNotification;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
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
                        getResources().getString(R.string.websiteMainDomain)
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
                    webApp.loadDataWithBaseUrl("https://"+getResources().getString(R.string.websiteMainDomain)+"/",
                            new RawResource(getAssets(),"home.html"),
                            new WebAppCallback() {
                                @Override
                                public void onLoadFinish(WebView view, String url) {
                                    webApp.detachWebAppCallback();
                                }
                                @Override
                                public void onLoadError(WebView view,
                                        /*RequiresApi(api >= 21)*/WebResourceRequest request, WebResourceErrorCompat error,
                                        /*RequiresApi(api >=19)*/ int errorCode, String description, String failingUrl)
                                {
                                    webApp.detachWebAppCallback();
                                }
                            });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            new Thread(() -> {
                TaskTimeout taskTimeout = new TaskTimeout();
                taskTimeout.setTimeout(5000);
                taskTimeout.execute();
                while (webApp.getStatus() == WEBAPP_STATUS_NONE){
                    try {
                        if(taskTimeout.getTimeout()==0){
                            throw new Exception("WebApp load timeout");
                        }
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                switch (webApp.getStatus()){
                    case WEBAPP_STATUS_LOAD_FINISHED:{
                        {
                            Thread backgroundThread = new Thread(() -> {
                                taskTimeout.reset();
                                int delay= 9999999;//until receive response
                                int timeout = 1500;//after receive response, wait more 1,5s
                                while(true){
                                    if(taskTimeout.getTimeout()==0) {
                                        taskTimeout.setTimeout(delay);
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                webApp.evalJavaScript(
                                                        "url('" + "https://"+getResources().getString(R.string.websiteMainDomain)+"/userNotifications.json" + "').get().response().data",
                                                        new ValueCallback() {
                                                            @Override
                                                            public void onReceiveValue(Object response) {
                                                                try {
                                                                    if(response==null){
                                                                        //response is null
                                                                        return;
                                                                    }
                                                                    JsonObject responseJsonObject = JsonParser.parseString((String) response).getAsJsonObject();
                                                                    if(responseJsonObject.has("data")){
                                                                        //continue your routine
                                                                    }else if (responseJsonObject.getAsJsonObject("error").has("xhr")) {
                                                                        //"Connection Error";
                                                                        return;
                                                                    }else {
                                                                        //"Script Error";
                                                                        return;
                                                                    }
                                                                    JsonArray dataJsonArray = responseJsonObject.getAsJsonArray("data");
                                                                    List<JsonElement> lastest_receivedUserNotifications = new ArrayList<JsonElement>();
                                                                    for( JsonElement jsonElement  : dataJsonArray.asList()) {
                                                                        String name = jsonElement.getAsJsonObject().get("name").getAsString();
                                                                        String title = jsonElement.getAsJsonObject().get("title").getAsString();
                                                                        String message = jsonElement.getAsJsonObject().get("message").getAsString();
                                                                        if(!receivedUserNotifications.contains(name)){
                                                                            receivedUserNotifications.add(name);
                                                                            //start api < 24
                                                                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                                                                lastest_receivedUserNotifications.add(jsonElement);
                                                                            }
                                                                            //end api < 24
                                                                            showUserNotification(title, message);
                                                                        }
                                                                    }
                                                                    if(!receivedUserNotifications.isEmpty()){
                                                                        boolean createSummary = true;
                                                                        //start api < 24
                                                                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                                                            if ( (receivedUserNotifications.size() + last_receivedCount > 1 )
                                                                                    &&
                                                                                    receivedUserNotifications.size() - last_receivedCount > 0) {
                                                                                NotificationCompat.Style style = new NotificationCompat.InboxStyle();
                                                                                for (JsonElement jsonElement : lastest_receivedUserNotifications) {
                                                                                    String title = jsonElement.getAsJsonObject().get("title").getAsString();
                                                                                    String message = jsonElement.getAsJsonObject().get("message").getAsString();
                                                                                    ((NotificationCompat.InboxStyle) style).addLine(title + "    " + message);
                                                                                }
                                                                                int lines_total=5;
                                                                                if(lastest_receivedUserNotifications.size()<lines_total){
                                                                                    int lines_count=0;
                                                                                    for( JsonElement jsonElement  : dataJsonArray.asList()) {
                                                                                        String title = jsonElement.getAsJsonObject().get("title").getAsString();
                                                                                        String message = jsonElement.getAsJsonObject().get("message").getAsString();
                                                                                        if(!lastest_receivedUserNotifications.contains(jsonElement)) {
                                                                                            ((NotificationCompat.InboxStyle) style).addLine(title + "    " + message);
                                                                                        }if(lines_count==lines_total){
                                                                                            break;
                                                                                        }
                                                                                        lines_count++;
                                                                                    }
                                                                                }
                                                                                if(createSummary) {
                                                                                    //when api is 19 then cancelAll to summary work properly
                                                                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                                                                        notificationManager.cancelAll();
                                                                                    }
                                                                                    notificationManager.notify(SUMMARY_ID, getSummaryNotification(style));
                                                                                }
                                                                            }
                                                                            last_receivedCount = receivedUserNotifications.size();
                                                                        }
                                                                        //end api < 24
                                                                        //start api > 24
                                                                        else {
                                                                            if(createSummary)
                                                                                if(summaryNotification==null){
                                                                                    notificationManager.notify(SUMMARY_ID, getSummaryNotification());
                                                                                }
                                                                        }
                                                                        //end api > 24
                                                                    }
                                                                    taskTimeout.setTimeout(timeout);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
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
            final Intent intent = new Intent(context, ApiNotificationService.class);
            new Thread() {
                @Override
                public void run() {
                    MyApp.getInstance().getServiceUtils().StartUpMyService(context, intent);
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