package com.example.antismartphoneaddictionapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.antismartphoneaddictionapp.Models.LocalAppModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    public static String CHANNEL_ID = "";
    Handler handler = new Handler();
    Context mContext;
    private TimerTask timerTask;
    private Timer timer;
    DatabaseHandler db = new DatabaseHandler(this);
    private UsageStatsManager mUsageStatsManager;
    private PackageManager mPm;

    private AppNameComparator mAppLabelComparator;
    private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
    private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();

    public static class AppNameComparator implements Comparator<UsageStats> {
        private Map<String, String> mAppLabelList;

        AppNameComparator(Map<String, String> appList) {
            mAppLabelList = appList;
        }

        @Override
        public final int compare(UsageStats a, UsageStats b) {
            String alabel = mAppLabelList.get(a.getPackageName());
            String blabel = mAppLabelList.get(b.getPackageName());
            return alabel.compareTo(blabel);
        }
    }

    public static void createNotificationChannel(@NonNull Context context, @NonNull String CHANNEL_ID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channelname";
            String description = "Channel desription";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
                Log.d("NotificationLog", "NotificationManagerNull");
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startServices();
        mContext = this;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new TimerTask() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void run() {
                        Log.d("TAG", "BG RUNNING");
                        mPm = getPackageManager();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            mUsageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
                        } else {
                            mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
                        }
                        getUsage();
                        showList();
                    }
                });
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 20000);
    }

    private void startServices() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        createNotificationChannel(getApplicationContext(), CHANNEL_ID);

        Notification notification = new NotificationCompat
                .Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("TEST")
                .setContentIntent(pendingIntent)
                //.setPriority(Notification.PRIORITY_MIN)
                .setAutoCancel(false)
                .build();

        startForeground(123, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void createNotificationChannel_new(@NonNull Context context, @NonNull String CHANNEL_ID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification";
            String description = "channeldescription";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
                Log.d("NotificationLog", "NotificationManagerNull");
            }
        }
    }

    public void showNotification(String title, String message) {
        StrictMode.VmPolicy.Builder sb = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(sb.build());
        sb.detectFileUriExposure();

        Random rand = new Random();
        int randomValue = rand.nextInt(2000);
        Intent intentViewDocs = new Intent(BackgroundService.this, MainActivity.class);
        intentViewDocs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent viewDownloadPI = PendingIntent.getActivity(BackgroundService.this, randomValue,
                intentViewDocs, 0);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel_new(BackgroundService.this, "Notifiation");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(BackgroundService.this, "Notifiation");

        builder.setContentIntent(viewDownloadPI)
                .setSmallIcon(R.drawable.logo)
                .setWhen(System.currentTimeMillis())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentTitle(title)
                .setAutoCancel(false);

        Notification n = builder.build();
        nm.notify("NotiTag", randomValue, n);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        timer.cancel();
    }

    void getUsage() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);

        final List<UsageStats> stats =
                mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                        cal.getTimeInMillis(), System.currentTimeMillis());
        ArrayMap<String, UsageStats> map = new ArrayMap<>();
        final int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {
            final android.app.usage.UsageStats pkgStats = stats.get(i);
            try {
                ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                String label = appInfo.loadLabel(mPm).toString();
                mAppLabelMap.put(pkgStats.getPackageName(), label);

                UsageStats existingStats =
                        map.get(pkgStats.getPackageName());
                if (existingStats == null) {
                    map.put(pkgStats.getPackageName(), pkgStats);
                } else {
                    existingStats.add(pkgStats);
                }

            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        mPackageStats.addAll(map.values());
        mAppLabelComparator = new AppNameComparator(mAppLabelMap);

    }

    @SuppressLint("SetTextI18n")
    void showList() {
        mPm = getApplication().getPackageManager();
        long totalTimeList = 0;
        for (int i = 0; i < mPackageStats.size(); i++) {
            UsageStats pkgStats = mPackageStats.get(i);
            if (pkgStats != null) {
                if (pkgStats.getTotalTimeInForeground() > 7200000) {
                    ArrayList<LocalAppModel> appModels = db.getAllApps();
                    if (appModels.isEmpty()) {
                        showNotification("OVER USAGE DETECTED", "PLEASE STOP USING " + mAppLabelMap.get(pkgStats.getPackageName()).toUpperCase());
                        db.addApp(new LocalAppModel(pkgStats.getPackageName(), getFormattedDate()));
                    } else {
                        for (LocalAppModel appModel : appModels) {
                            if(appModel.getPackageName().equalsIgnoreCase(pkgStats.getPackageName())) {
                                if (!getFormattedDate().equalsIgnoreCase(appModel.getDateTime())) {
                                    showNotification("OVER USAGE DETECTED", "PLEASE STOP USING " + mAppLabelMap.get(pkgStats.getPackageName()).toUpperCase());
                                    db.updateApp(new LocalAppModel(pkgStats.getPackageName(), getFormattedDate()));
                                }
                            }
                        }
                    }
                }
                totalTimeList = totalTimeList + pkgStats.getTotalTimeInForeground();
            } else {
                Log.w("TAG", "No usage stats info for package:" + i);
            }
        }
    }

    public static String getFormattedDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

}
