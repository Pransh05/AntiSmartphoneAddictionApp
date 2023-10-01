package com.example.antismartphoneaddictionapp;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Adaptor.AppAdaptor;
import com.example.antismartphoneaddictionapp.Models.AppModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private UsageStatsManager mUsageStatsManager;
    private PackageManager mPm;

    private AppNameComparator mAppLabelComparator;
    private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
    private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();

    ArrayList<AppModel> appModelArrayList = new ArrayList<>();

    RecyclerView appListRV;
    TextView totalTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

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

    @SuppressLint("WrongConstant")
    private void initUI() {
        getSupportActionBar().hide();

        appListRV = findViewById(R.id.appListRV);
        totalTime = findViewById(R.id.totalTime);

        mPm = getPackageManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mUsageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        } else {
            mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
        }


        askPermission();

        long hour_in_mil = 1000 * 60 * 60; // In Milliseconds
        long end_time = System.currentTimeMillis();
        long start_time = end_time - hour_in_mil;



        Intent serviceIntent = new Intent(this, BackgroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUsage();
        showList();
    }

    void askPermission() {
        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) this
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), this.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (this.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }

        if (!granted) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
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

                String timeUsed = String.valueOf(DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
                if (!timeUsed.equalsIgnoreCase("00:00")) {
//                    Log.d("TAG", mAppLabelMap.get(pkgStats.getPackageName()) + "_________" + pkgStats.getTotalTimeInForeground());
                    appModelArrayList.add(new AppModel(mAppLabelMap.get(pkgStats.getPackageName()), pkgStats.getPackageName(), pkgStats.getTotalTimeInForeground()));

                }
                totalTimeList = totalTimeList + pkgStats.getTotalTimeInForeground();
            } else {
                Log.w("TAG", "No usage stats info for package:" + i);
            }
        }

        AppAdaptor appointmentAdaptor = new AppAdaptor(appModelArrayList, this);
        appListRV.setHasFixedSize(true);
        appListRV.setLayoutManager(new LinearLayoutManager(this));
        appListRV.setAdapter(appointmentAdaptor);
        totalTime.setText(DateUtils.formatElapsedTime(totalTimeList / 1000) + " Hours");
    }

}