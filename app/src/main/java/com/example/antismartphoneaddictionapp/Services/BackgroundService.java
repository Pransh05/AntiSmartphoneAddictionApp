package com.example.antismartphoneaddictionapp.Services;

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
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.antismartphoneaddictionapp.Activity.MainActivity;
import com.example.antismartphoneaddictionapp.Models.LimitTime;
import com.example.antismartphoneaddictionapp.Models.LocalAppModel;
import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.utils.SharedPref;
import com.example.antismartphoneaddictionapp.web_services.JSONParse;
import com.example.antismartphoneaddictionapp.web_services.RestAPI;
import com.example.antismartphoneaddictionapp.web_services.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    public static String CHANNEL_ID = "";
    Handler handler = new Handler();
    Context mContext;
    String userId;
    private TimerTask timerTask;
    private Timer timer;
    private UsageStatsManager mUsageStatsManager;
    private PackageManager mPm;
    private List<LimitTime> limitTimeList;

    private static final String TAG = BackgroundService.class.getSimpleName();

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
        try {
            startServices();
            mContext = this;
            userId= SharedPref.checkDeviceRegisteredAndGetUserId(mContext);
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new TimerTask() {
                        @SuppressLint("WrongConstant")
                        @Override
                        public void run() {
                            Log.d("TAG", "BG RUNNING");
                            new AsyncGetTimeLimit().execute(userId);
                        }
                    });
                }
            };
            timer = new Timer();
            timer.schedule(timerTask, 0, 20000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startServices() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

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
                intentViewDocs, PendingIntent.FLAG_IMMUTABLE);

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

    private void proceedLimitTimeData(List<LimitTime> limitTimeList) {
        mPm = getPackageManager();
        this.limitTimeList = limitTimeList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mUsageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        } else {
            mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
        }
    }

    private long getMidnightMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void checkUsageAndShowNotifications() {
        if (limitTimeList == null) {
            Log.d(TAG, "limitTimeList found null");
            return;
        }

        if (mUsageStatsManager == null) {
            Log.d(TAG, "mUsageStatsManager found null");
            return;
        }

        if (getApplicationContext() == null) {
            Log.d(TAG, "getApplicationContext found null");
            return;
        }

        if (mPm == null) {
            Log.d(TAG, "mPm found null");
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        long currentTimeMillis = System.currentTimeMillis();

        for (LimitTime limitTime : limitTimeList) {
            try {
                String packageName = limitTime.getPackageName();
                double limitInMinutes = Double.parseDouble(limitTime.getTimeLimit());
                long limitInMillis = (long) (limitInMinutes * 60 * 1000);
                long startTime = getMidnightMillis();
                Log.d(TAG, "PACKAGE : " + limitTime.getPackageName());
                Log.d(TAG, "LIMIT IN MINUTES : " + limitInMinutes);
                List<UsageStats> stats = mUsageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_BEST, startTime, currentTimeMillis);

                for (UsageStats usageStats : stats) {
                    if (packageName.equals(usageStats.getPackageName())) {
                        long totalTimeInForeground = usageStats.getTotalTimeInForeground();
                        Log.d(TAG, "totalTimeInForeground : " + totalTimeInForeground);
                        if (totalTimeInForeground > limitInMillis) {
                            Log.d(TAG, "totalTimeInForeground : "
                                    + totalTimeInForeground + ">" + limitInMillis);
                            long lastNotificationTime = sharedPreferences.getLong(packageName, 0);
                            Log.d(TAG, "lastNotificationTime : " + lastNotificationTime);
                            if (currentTimeMillis - lastNotificationTime >= 24 * 60 * 60 * 1000) {
                                String appName = getAppName(packageName);
                                Log.d(TAG, "NOTIFICATION : " + appName);
                                showNotification("Usage Limit Exceeded", "Please stop using " + appName);
                                sharedPreferences.edit().putLong(packageName, currentTimeMillis).apply();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, Arrays.toString(e.getSuppressed()));
            }
        }
    }

    private String getAppName(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return packageName;
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncGetTimeLimit extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                Log.d(TAG, "USER ID: "+userId);
                JSONObject json = new RestAPI().GetEnabledLimitScreenByUid(strings[0]);
                a = new JSONParse().parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                Log.d(TAG, s);
                if (!Utility.checkConnection(s)) {
                    JSONObject json = new JSONObject(s);
                    String jsonString = json.getString("status");
                    if (jsonString.compareToIgnoreCase("ok") == 0) {
                        saveDataToEntity(json);
                        checkUsageAndShowNotifications();
                    } else Log.d(TAG, jsonString);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, Arrays.toString(e.getSuppressed()));
            }
        }

        private void saveDataToEntity(JSONObject json) {
            try {
                List<LimitTime> limitTimeList = new ArrayList<>();
                JSONArray jsonArray = json.getJSONArray("Data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    LimitTime limitTime = new LimitTime();
                    limitTime.setUserId(jsonObj.optString("data0"));
                    limitTime.setPackageName(Authentication.DecryptMessage(jsonObj.optString("data1")));
                    limitTime.setTimeLimit(Authentication.DecryptMessage(jsonObj.optString("data2")));
                    limitTime.setStatus(jsonObj.optString("data3"));
                    limitTimeList.add(limitTime);
                }
                if (limitTimeList.size() > 0) {
                    proceedLimitTimeData(limitTimeList);
                } else Log.d(TAG, "LimitTime List SIZE: 0");
            } catch (Exception exception) {
                exception.printStackTrace();
                Log.d(TAG, Arrays.toString(exception.getSuppressed()));
            }
        }
    }

}
