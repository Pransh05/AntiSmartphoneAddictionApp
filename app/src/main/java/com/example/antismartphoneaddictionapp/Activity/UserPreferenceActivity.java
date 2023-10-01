package com.example.antismartphoneaddictionapp.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Adaptor.SelectPreferenceAdaptor;
import com.example.antismartphoneaddictionapp.Interface.ISelectPreferenceChange;
import com.example.antismartphoneaddictionapp.Models.Preference;
import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.Services.Authentication;
import com.example.antismartphoneaddictionapp.utils.Constants;
import com.example.antismartphoneaddictionapp.utils.DialogUtils;
import com.example.antismartphoneaddictionapp.utils.Helper;
import com.example.antismartphoneaddictionapp.utils.SharedPref;
import com.example.antismartphoneaddictionapp.web_services.JSONParse;
import com.example.antismartphoneaddictionapp.web_services.RestAPI;
import com.example.antismartphoneaddictionapp.web_services.Utility;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserPreferenceActivity extends AppCompatActivity implements View.OnClickListener, ISelectPreferenceChange {

    private RelativeLayout rlMainLayout;
    private LinearLayout llButtonLayout;
    private RecyclerView preferenceRV;
    private TextView tvNoDataFound;
    private Button btnSubmit;

    private Context context;
    private Dialog dialog;
    private RestAPI restAPI;
    private JSONParse jsonParse;
    private String userId;
    private List<Preference> preferenceList;

    private MaterialCardView alertCardUserNameLayout;
    private EditText alertEtUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preference);
        Objects.requireNonNull(getSupportActionBar()).hide();
        initUI();
        setListeners();
        initObj();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUiVisibility(false);
        new AsyncCheckIfDeviceRegistered().execute(Helper.getUniqueIdForDevice(context));
    }

    private void initUI() {
        rlMainLayout = findViewById(R.id.rlMainLayout);
        llButtonLayout = findViewById(R.id.llButtonLayout);
        preferenceRV = findViewById(R.id.preferenceRV);
        tvNoDataFound = findViewById(R.id.tvNoDataFound);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setListeners() {
        btnSubmit.setOnClickListener(this);
    }

    private void initObj() {
        context = this;
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        preferenceList = new ArrayList<>();
    }

    private void setUiVisibility(boolean isDataFound) {
        if (isDataFound) {
            preferenceRV.setVisibility(View.VISIBLE);
            llButtonLayout.setVisibility(View.VISIBLE);
            tvNoDataFound.setVisibility(View.GONE);
        } else {
            preferenceRV.setVisibility(View.GONE);
            llButtonLayout.setVisibility(View.GONE);
            tvNoDataFound.setVisibility(View.VISIBLE);
        }
    }

    private void openUserNameAlertDialog() {
        try {
            DialogUtils.dismissDialog(dialog);
            if (dialog != null) dialog.cancel();
            AlertDialog.Builder alertBuilder = null;
            try {
                View alertView = ((Activity) context).getLayoutInflater()
                        .inflate(R.layout.alert_name_dialog, null);
                alertBuilder = new AlertDialog.Builder(context);
                if (alertView.getParent() != null)
                    ((ViewGroup) alertView.getParent()).removeView(alertView);
                alertBuilder.setView(alertView);
                initUserNameAlertDialog(alertView);
                dialog = alertBuilder.create();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            if (!dialog.isShowing()) if (alertBuilder != null) {
                dialog = alertBuilder.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUserNameAlertDialog(View alertView) {
        alertCardUserNameLayout = alertView.findViewById(R.id.alertCardUserNameLayout);
        alertEtUserName = alertView.findViewById(R.id.alertEtUserName);
        Button alertBtnSubmit = alertView.findViewById(R.id.alertBtnSubmit);
        alertBtnSubmit.setOnClickListener(this);
    }

    @Override
    public void onChangeSelectPreference(int index, Preference preference) {
        preferenceList.set(index, preference);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.alertBtnSubmit) {
            onClickAlertBtnSubmit();
        } else if (v.getId() == R.id.btnSubmit) {
            onClickBtnSubmit();
        }
    }

    private void onClickAlertBtnSubmit() {
        try {
            if (Helper.isEmptyFieldValidation(alertEtUserName)) {
                DialogUtils.dismissDialog(dialog);
                String userName = alertEtUserName.getText().toString().trim();
                userName = Authentication.EncryptMessage(userName);
                new AsyncRegisterDevice().execute(userName, Helper.getUniqueIdForDevice(context));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Helper.makeSnackBar(alertCardUserNameLayout, Constants.SOMETHING_WENT_WRONG);
        }
    }

    private void onClickBtnSubmit() {
        try {
            ArrayList<String> selectedPreferenceIdList = new ArrayList<>();
            for (Preference preference : preferenceList) {
                if (preference.isPreferenceSelected()) {
                    selectedPreferenceIdList.add(preference.getPreferenceId());
                }
            }
            if (selectedPreferenceIdList.size() > 0) {
                new AsyncCreateUserPreference(selectedPreferenceIdList).execute(userId);
            } else {
                Helper.makeSnackBar(rlMainLayout, "Select Preference");
            }
        } catch (Exception e) {
            DialogUtils.dismissDialog(dialog);
            e.printStackTrace();
        }
    }


    private void setUpRecyclerView() {
        SelectPreferenceAdaptor adaptor = new SelectPreferenceAdaptor(preferenceList, this, this);
        preferenceRV.setHasFixedSize(true);
        preferenceRV.setLayoutManager(new LinearLayoutManager(this));
        preferenceRV.setAdapter(adaptor);
        setUiVisibility(true);
    }

    private void saveDataToEntityAndSetupRecyclerView(JSONObject json) {
        try {
            JSONArray jsonArray = json.getJSONArray("Data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                Preference preference = new Preference();
                preference.setPreferenceId(jsonObj.optString("data0"));
                preference.setPreferenceName(jsonObj.optString("data1"));
                preferenceList.add(preference);
            }
            setUpRecyclerView();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncCheckIfDeviceRegistered extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = restAPI.CheckIfDeviceExist(strings[0]);
                a = jsonParse.parse(json);
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
                    if (jsonString.compareToIgnoreCase("ok") == 0) {
                        JSONArray jsonArray = json.getJSONArray("Data");
                        JSONObject jsonObj = jsonArray.getJSONObject(0);
                        userId = jsonObj.getString("data0");
                        SharedPref.setUserName(context, jsonObj.getString("data1"));
                        new AsyncGetAllPreference().execute();
                    } else {
                        openUserNameAlertDialog();
                    }
                }

            } catch (Exception e) {
                Helper.makeSnackBar(rlMainLayout, Constants.SOMETHING_WENT_WRONG);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncRegisterDevice extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = restAPI.CreateDevice(strings[0], strings[1]);
                a = jsonParse.parse(json);
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
                    if (jsonString.compareToIgnoreCase("ok") == 0) {
                        JSONArray jsonArray = json.getJSONArray("Data");
                        JSONObject jsonObj = jsonArray.getJSONObject(0);
                        userId = jsonObj.getString("data0");
                        new AsyncGetAllPreference().execute();
                    } else {
                        openUserNameAlertDialog();
                    }
                }
            } catch (Exception e) {
                Helper.makeSnackBar(rlMainLayout, Constants.SOMETHING_WENT_WRONG);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncGetAllPreference extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = restAPI.GetAllPreference();
                a = jsonParse.parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            DialogUtils.dismissDialog(dialog);
            preferenceList.clear();
            try {
                if (Utility.checkConnection(s)) {
                    Pair<String, String> pair = Utility.GetErrorMessage(s);
                    Utility.ShowAlertDialog(context, pair.first, pair.second, false);
                } else {
                    JSONObject json = new JSONObject(s);
                    String jsonString = json.getString("status");
                    if (jsonString.compareToIgnoreCase("ok") == 0) {
                        saveDataToEntityAndSetupRecyclerView(json);
                    } else if (jsonString.compareToIgnoreCase("no") != 0) {
                        Helper.makeSnackBar(rlMainLayout, Constants.SOMETHING_WENT_WRONG);
                    }

                }
            } catch (Exception e) {
                Helper.makeSnackBar(rlMainLayout, Constants.SOMETHING_WENT_WRONG);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncCreateUserPreference extends AsyncTask<String, String, String> {

        private ArrayList<String> selectedPreferenceListId;

        public AsyncCreateUserPreference(ArrayList<String> selectedPreferenceListId) {
            this.selectedPreferenceListId = selectedPreferenceListId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = restAPI.CreateUserPreferences(strings[0], selectedPreferenceListId);
                a = jsonParse.parse(json);
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
                        SharedPref.setUserId(context, userId);
                        int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_NEW_TASK;
                        Helper.goToWithFlags(context, MainActivity.class, flags);
                        finish();
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