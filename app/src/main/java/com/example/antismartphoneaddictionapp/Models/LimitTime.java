package com.example.antismartphoneaddictionapp.Models;

import java.io.Serializable;

public class LimitTime implements Serializable {
    private String userId,packageName,timeLimit,status;
    private String exceededTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExceededTime() {
        return exceededTime;
    }

    public void setExceededTime(String exceededTime) {
        this.exceededTime = exceededTime;
    }
}
