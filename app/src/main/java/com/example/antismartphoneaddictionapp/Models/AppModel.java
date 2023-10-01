package com.example.antismartphoneaddictionapp.Models;

import androidx.annotation.NonNull;

public class AppModel {
    private String appName;
    private String packageName;
    private long totalTimeInForeground;

    public AppModel(String appName, String packageName, long totalTimeInForeground) {
        this.appName = appName;
        this.packageName = packageName;
        this.totalTimeInForeground = totalTimeInForeground;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getTotalTimeInForeground() {
        return totalTimeInForeground;
    }

    public void setTotalTimeInForeground(long totalTimeInForeground) {
        this.totalTimeInForeground = totalTimeInForeground;
    }

    @NonNull
    @Override
    public String toString() {
        return appName;
    }
}
