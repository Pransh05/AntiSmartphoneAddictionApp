package com.example.antismartphoneaddictionapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.antismartphoneaddictionapp.Services.Authentication;

public class SharedPref {

    private static final String SHARED_PREF = "AntiSmartphoneAddiction";
    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";

    public static void setUserId(Context con, String value) {
        SharedPreferences.Editor editor = sharedPreferences(con).edit();
        editor.putString(USER_ID, value);
        editor.apply();
    }

    public static void setUserName(Context con, String value) {
        SharedPreferences.Editor editor = sharedPreferences(con).edit();
        editor.putString(USER_NAME, value);
        editor.apply();
    }

    public static String checkDeviceRegisteredAndGetUserId(Context con) {
        String userId = sharedPreferences(con).getString(USER_ID, "");
        if (userId != null && !userId.isEmpty()) {
            return userId;
        }
        return null;
    }

    public static String getUserName(Context con) {
        String userName = sharedPreferences(con).getString(USER_NAME, "");
        return Authentication.DecryptMessage(userName);
    }

    public static SharedPreferences sharedPreferences(Context con) {
        return con.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
    }

    public static void deleteAll(Context context) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SHARED_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefLogin.edit();
        editor.clear();
        editor.apply();
    }

}