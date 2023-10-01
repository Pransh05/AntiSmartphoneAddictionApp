package com.example.antismartphoneaddictionapp.Models;

public class LocalAppModel {

    public int id;
    public String packageName,dateTime;

    public LocalAppModel() {
    }

    public LocalAppModel(String packageName, String dateTime) {
        this.packageName = packageName;
        this.dateTime = dateTime;
    }

    public LocalAppModel(int id, String packageName, String dateTime) {
        this.id = id;
        this.packageName = packageName;
        this.dateTime = dateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
