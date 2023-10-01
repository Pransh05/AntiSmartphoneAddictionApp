package com.example.antismartphoneaddictionapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.example.antismartphoneaddictionapp.Adaptor.AppSpinnerAdapter;
import com.example.antismartphoneaddictionapp.Models.AppModel;
import com.example.antismartphoneaddictionapp.Models.LimitTime;
import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.Services.Authentication;
import com.example.antismartphoneaddictionapp.utils.Constants;
import com.example.antismartphoneaddictionapp.utils.DialogUtils;
import com.example.antismartphoneaddictionapp.utils.Helper;
import com.example.antismartphoneaddictionapp.utils.SharedPref;
import com.example.antismartphoneaddictionapp.web_services.JSONParse;
import com.example.antismartphoneaddictionapp.web_services.RestAPI;
import com.example.antismartphoneaddictionapp.web_services.Utility;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AddLimitTimeActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout rlMainLayout;
    private Spinner spinnerAppName;
    private EditText etLimitTime;
    private SwitchMaterial switchIsEnabled;
    private Button btnSubmit;
    private ImageView ivDelete;

    private Context context;
    private Dialog dialog;
    private PackageManager packageManager;
    private List<AppModel> installedApps;

    private String savedPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_limit_time);

        initToolbar();
        initUI();
        initListeners();
        initObj();
        loadIntentDataThenSpinnerData();
    }

    private void initToolbar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("Manage Limit Time");
        }
    }

    private void initUI() {
        rlMainLayout = findViewById(R.id.rlMainLayout);
        spinnerAppName = findViewById(R.id.spinnerAppName);
        etLimitTime = findViewById(R.id.etLimitTime);
        switchIsEnabled = findViewById(R.id.switchIsEnabled);
        btnSubmit = findViewById(R.id.btnSubmit);
        ivDelete = findViewById(R.id.ivDelete);
    }

    private void initListeners() {
        btnSubmit.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
    }

    private void initObj() {
        context = this;
        packageManager = getPackageManager();
    }

    private void loadIntentDataThenSpinnerData() {
        LimitTime limitTime = (LimitTime) getIntent().getSerializableExtra(Constants.LIMIT_LIME_OBJ);
        if (limitTime != null) {
            spinnerAppName.setEnabled(false);
            ivDelete.setVisibility(View.VISIBLE);
            savedPackageName = limitTime.getPackageName();
            etLimitTime.setText(limitTime.getTimeLimit().trim());
            switchIsEnabled.setChecked(Objects.equals(limitTime.getStatus(), "ENABLED"));
        }
        initLoadSpinnerData(limitTime);
    }

    private void initLoadSpinnerData(LimitTime limitTime) {
        try {
            List<String> packageList = (List<String>) getIntent().getSerializableExtra(Constants.LIMIT_LIME_LIST);
            if (limitTime != null) {
                installedApps = getAllInstalledAppsInfo(packageList, limitTime.getPackageName());
            } else installedApps = getAllInstalledAppsInfo(packageList, null);

            AppSpinnerAdapter adapter = new AppSpinnerAdapter(this, R.layout.app_spinner_content, installedApps);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAppName.setAdapter(adapter);
            if (limitTime != null) spinnerAppName.setSelection(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<AppModel> getAllInstalledAppsInfo(List<String> packageList, String intentPackageName) {
        List<AppModel> appModelList = new ArrayList<>();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(0);
        for (ApplicationInfo app : apps) {
            if ((app.flags & ApplicationInfo.FLAG_INSTALLED) != 0) {
                String appName = app.loadLabel(packageManager).toString();
                String packageName = app.packageName;
                if (!packageName.equals(context.getPackageName())){
                    if (intentPackageName == null || (packageList != null && !packageList.contains(packageName))) {
                        appModelList.add(new AppModel(appName, packageName, 0));
                    } else if (packageName.equals(intentPackageName)) {
                        appModelList.add(new AppModel(appName, packageName, 0));
                    }
                }
            }
        }

        Collections.sort(appModelList, (o1, o2) -> o1.getAppName().compareTo(o2.getAppName()));
        return appModelList;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSubmit) {
            onClickFbLimitTime();
        } else if (v.getId() == R.id.ivDelete) {
            DialogUtils.dismissDialog(dialog);
            dialog = DialogUtils.deleteDialog(this, (dialog, which) -> onClickDelete());
            dialog.show();
        }
    }

    private void onClickFbLimitTime() {
        try {
            if (Helper.isEmptyFieldValidation(etLimitTime)) {
                final AppModel selectedItem = (AppModel) spinnerAppName.getSelectedItem();

                String userId = SharedPref.checkDeviceRegisteredAndGetUserId(context);
                String packageName = selectedItem.getPackageName();
                String timeLimit = etLimitTime.getText().toString().trim();
                String status = switchIsEnabled.isChecked() ? "ENABLED" : "DISABLED";

                timeLimit = Authentication.EncryptMessage(timeLimit);
                packageName = Authentication.EncryptMessage(packageName);
                new AsyncAddUpdateLimitTime().execute(userId, packageName, timeLimit, status);
            }
        } catch (Exception e) {
            Helper.makeSnackBar(rlMainLayout, Constants.SOMETHING_WENT_WRONG);
        }
    }

    private void onClickDelete() {
        try {
            DialogUtils.dismissDialog(dialog);
            final AppModel selectedItem = (AppModel) spinnerAppName.getSelectedItem();
            String userId = SharedPref.checkDeviceRegisteredAndGetUserId(context);
            String packageName = selectedItem == null ? savedPackageName : selectedItem.getPackageName();
            packageName = Authentication.EncryptMessage(packageName);
            new AsyncDeleteLimitTime().execute(userId, packageName);
        } catch (Exception e) {
            Helper.makeSnackBar(rlMainLayout, Constants.SOMETHING_WENT_WRONG);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncAddUpdateLimitTime extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = new RestAPI().CreateOrUpdateLimitScreen(strings[0],
                        strings[1], strings[2], strings[3]);
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
                if (Utility.checkConnection(s)) {
                    Pair<String, String> pair = Utility.GetErrorMessage(s);
                    Utility.ShowAlertDialog(context, pair.first, pair.second, false);
                } else {
                    JSONObject json = new JSONObject(s);
                    String jsonString = json.getString("status");
                    if (jsonString.compareToIgnoreCase("true") == 0 ||
                            jsonString.compareToIgnoreCase("ok") == 0) {
                        DialogUtils.openAlertDialog(context,
                                "Limit Time Added",
                                "OK",
                                false,
                                true).show();
                    } else if (jsonString.compareToIgnoreCase("update") == 0 ||
                            jsonString.compareToIgnoreCase("ok") == 0) {
                        DialogUtils.openAlertDialog(context,
                                "Limit Time Updated",
                                "OK",
                                false,
                                true).show();
                    } else {
                        Helper.makeSnackBar(rlMainLayout, Constants.SOMETHING_WENT_WRONG);
                    }
                }
            } catch (Exception e) {
                Helper.makeSnackBar(rlMainLayout, Constants.SOMETHING_WENT_WRONG);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncDeleteLimitTime extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = new RestAPI().DeleteLimitScreen(strings[0], strings[1]);
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
                if (Utility.checkConnection(s)) {
                    Pair<String, String> pair = Utility.GetErrorMessage(s);
                    Utility.ShowAlertDialog(context, pair.first, pair.second, false);
                } else {
                    JSONObject json = new JSONObject(s);
                    String jsonString = json.getString("status");
                    if (jsonString.compareToIgnoreCase("true") == 0 ||
                            jsonString.compareToIgnoreCase("ok") == 0) {
                        DialogUtils.openAlertDialog(context,
                                "Limit Time Deleted",
                                "OK",
                                false,
                                true).show();
                    } else {
                        Helper.makeSnackBar(rlMainLayout, Constants.SOMETHING_WENT_WRONG);
                    }
                }
            } catch (Exception e) {
                Helper.makeSnackBar(rlMainLayout, Constants.SOMETHING_WENT_WRONG);
            }
        }
    }

}