package com.example.antismartphoneaddictionapp.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.antismartphoneaddictionapp.Fragment.CommunityFragment;
import com.example.antismartphoneaddictionapp.Fragment.HistoryFragment;
import com.example.antismartphoneaddictionapp.Fragment.LimitTimeFragment;
import com.example.antismartphoneaddictionapp.Fragment.RecommendationFragment;
import com.example.antismartphoneaddictionapp.Fragment.UsageFragment;
import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.Services.BackgroundService;
import com.example.antismartphoneaddictionapp.utils.Constants;
import com.example.antismartphoneaddictionapp.utils.DialogUtils;
import com.example.antismartphoneaddictionapp.utils.Helper;
import com.example.antismartphoneaddictionapp.utils.SharedPref;
import com.example.antismartphoneaddictionapp.web_services.JSONParse;
import com.example.antismartphoneaddictionapp.web_services.RestAPI;
import com.example.antismartphoneaddictionapp.web_services.Utility;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private FrameLayout frameLayout;
    private BottomNavigationView bottomNavigationView;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPref.checkDeviceRegisteredAndGetUserId(this) != null) {
            setContentView(R.layout.activity_main);
            initUI();
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
            new AsyncCheckIfDeviceRegistered().execute(Helper.getUniqueIdForDevice(this));
        } else {
            Helper.goTo(this, UserPreferenceActivity.class);
        }
    }


    private void initUI() {
        Objects.requireNonNull(getSupportActionBar()).hide();
        frameLayout = findViewById(R.id.frameLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.menuUsage);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuUsage:
                setFragment(new UsageFragment());
                break;
            case R.id.menuHistory:
                setFragment(new HistoryFragment());
                break;
            case R.id.menuLimit:
                setFragment(new LimitTimeFragment());
                break;
            case R.id.menuCommunity:
                setFragment(new CommunityFragment());
                break;
            case R.id.menuRecommend:
                setFragment(new RecommendationFragment());
                break;
        }
        return true;
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void closeService() {
        try {
            stopService(new Intent(this, BackgroundService.class));
        } catch (Exception ignored) {

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncCheckIfDeviceRegistered extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = new RestAPI().CheckIfDeviceExist(strings[0]);
                a = new JSONParse().parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            DialogUtils.dismissDialog(dialog);
            try {
                if (!Utility.checkConnection(s)) {
                    JSONObject json = new JSONObject(s);
                    String jsonString = json.getString("status");
                    if (jsonString.compareToIgnoreCase("ok") != 0) {
                        closeService();
                        SharedPref.deleteAll(MainActivity.this);
                        int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_NEW_TASK;
                        Helper.goToWithFlags(MainActivity.this, UserPreferenceActivity.class, flags);
                        finish();
                    } else {
                        JSONArray jsonArray = json.getJSONArray("Data");
                        JSONObject jsonObj = jsonArray.getJSONObject(0);
                        SharedPref.setUserName(MainActivity.this, jsonObj.optString("data1"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}