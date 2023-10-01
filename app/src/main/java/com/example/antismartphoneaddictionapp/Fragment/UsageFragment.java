package com.example.antismartphoneaddictionapp.Fragment;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Adaptor.AppAdaptor;
import com.example.antismartphoneaddictionapp.Services.BackgroundService;
import com.example.antismartphoneaddictionapp.Models.AppModel;
import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.utils.SharedPref;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class UsageFragment extends Fragment {

    private Context mContext;
    private UsageStatsManager mUsageStatsManager;
    private PackageManager mPackageManager;
    private AppAdaptor mAppAdapter;

    private final Map<String, String> mAppLabelMap = new ArrayMap<>();
    private final List<AppModel> mAppList = new ArrayList<>();
    private final HashSet<String> uniquePackages = new HashSet<>(); // To avoid duplicate entries

    private RecyclerView mAppListRecyclerView;
    private TextView title, mTotalTimeTextView;

    public UsageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usage, container, false);
        initializeUI(view);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            updateUsageData();
            showUsageList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void initializeUI(View view) {
        title = view.findViewById(R.id.title);
        mAppListRecyclerView = view.findViewById(R.id.appListRV);
        mTotalTimeTextView = view.findViewById(R.id.totalTime);

        title.setText("Welcome Back, " + SharedPref.getUserName(mContext));
        mPackageManager = mContext.getPackageManager();
        mUsageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);

        askPermission();

        long startTime = getMidnightMillis();
        long endTime = System.currentTimeMillis();

        mAppAdapter = new AppAdaptor(mAppList, mContext);
        mAppListRecyclerView.setHasFixedSize(true);
        mAppListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAppListRecyclerView.setAdapter(mAppAdapter);
    }

    private void askPermission() {
        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), mContext.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (mContext.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }

        if (!granted) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
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

    private void updateUsageData() {
        long startTime = getMidnightMillis();
        long endTime = System.currentTimeMillis();

        UsageStatsManager usageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        mAppLabelMap.clear();
        mAppList.clear();
        uniquePackages.clear();

        for (UsageStats usageStats : usageStatsList) {
            try {
                String packageName = usageStats.getPackageName();

                if (!packageName.equals(mContext.getPackageName())) {
                    // Check if the package name is already in the HashSet
                    if (uniquePackages.contains(packageName)) {
                        // If it's a duplicate, update the total time
                        for (AppModel appModel : mAppList) {
                            if (appModel.getPackageName().equals(packageName)) {
                                appModel.setTotalTimeInForeground(appModel.getTotalTimeInForeground() + usageStats.getTotalTimeInForeground());
                                break;
                            }
                        }
                    } else {
                        uniquePackages.add(packageName);

                        ApplicationInfo appInfo = mPackageManager.getApplicationInfo(packageName, 0);
                        String label = appInfo.loadLabel(mPackageManager).toString();
                        mAppLabelMap.put(packageName, label);

                        String timeUsed = String.valueOf(DateUtils.formatElapsedTime(usageStats.getTotalTimeInForeground() / 1000));
                        if (!timeUsed.equalsIgnoreCase("00:00")) {
                            mAppList.add(new AppModel(label, packageName, usageStats.getTotalTimeInForeground()));
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private void showUsageList() {

        long totalTimeList = 0;
        for (AppModel appModel : mAppList) {
            totalTimeList += appModel.getTotalTimeInForeground();
        }
        Collections.sort(mAppList, Comparator.comparingLong(AppModel::getTotalTimeInForeground));
        Collections.reverse(mAppList);
        mTotalTimeTextView.setText(DateUtils.formatElapsedTime(totalTimeList / 1000) + " Hours");
        mAppAdapter.notifyDataSetChanged();
    }
}
