package com.example.antismartphoneaddictionapp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.Services.BackgroundService;
import com.example.antismartphoneaddictionapp.utils.Helper;
import com.example.antismartphoneaddictionapp.utils.SharedPref;

import java.util.Objects;


public class SplashActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Objects.requireNonNull(getSupportActionBar()).hide();
        context = this;
        navigateToOtherScreen();
    }

    private void navigateToOtherScreen() {
        new Handler().postDelayed(() -> {
            int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK;
            if (SharedPref.checkDeviceRegisteredAndGetUserId(context) != null) {
                Helper.goToWithFlags(context, MainActivity.class, flags);
                finish();
            } else {
                Helper.goToWithFlags(context, UserPreferenceActivity.class, flags);
                finish();
            }
        }, 2500);
    }
}