package com.example.antismartphoneaddictionapp.Models;

public class Category {
    private String categoryId,appName,packageName,category,recommendation;
    private long totalUsageTime,previousDayTotalUsageTime;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public long getTotalUsageTime() {
        return totalUsageTime;
    }

    public void setTotalUsageTime(long totalUsageTime) {
        this.totalUsageTime = totalUsageTime;
    }

    public long getPreviousDayTotalUsageTime() {
        return previousDayTotalUsageTime;
    }

    public void setPreviousDayTotalUsageTime(long previousDayTotalUsageTime) {
        this.previousDayTotalUsageTime = previousDayTotalUsageTime;
    }
}
