package com.example.antismartphoneaddictionapp.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Activity.AddLimitTimeActivity;
import com.example.antismartphoneaddictionapp.Adaptor.LimitTimeAdaptor;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LimitTimeFragment extends Fragment implements View.OnClickListener {

    private View view;
    private RecyclerView limitTimeListRV;
    private TextView tvNoDataFound;
    private ExtendedFloatingActionButton extendedFbLimitTime;

    private Context context;
    private Dialog dialog;
    private List<LimitTime> limitTimeList = new ArrayList<>();

    public LimitTimeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_limit_time, container, false);
        initializeUI(view);
        initListeners();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUiVisibility(false);
        new AsyncGetTimeLimit().execute(SharedPref.checkDeviceRegisteredAndGetUserId(context));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setUiVisibility(false);
            new AsyncGetTimeLimit().execute(SharedPref.checkDeviceRegisteredAndGetUserId(context));
        }
    }

    private void initializeUI(View view) {
        limitTimeListRV = view.findViewById(R.id.limitTimeListRV);
        tvNoDataFound = view.findViewById(R.id.tvNoDataFound);
        extendedFbLimitTime = view.findViewById(R.id.extendedFbLimitTime);
    }

    private void initListeners() {
        extendedFbLimitTime.setOnClickListener(this);
    }

    private void setUiVisibility(boolean isDataFound) {
        if (isDataFound) {
            limitTimeListRV.setVisibility(View.VISIBLE);
            tvNoDataFound.setVisibility(View.GONE);
        } else {
            limitTimeListRV.setVisibility(View.GONE);
            tvNoDataFound.setVisibility(View.VISIBLE);
        }
    }

    private void setUpRecyclerView() {
        LimitTimeAdaptor adaptor = new LimitTimeAdaptor(limitTimeList, context);
        limitTimeListRV.setHasFixedSize(true);
        limitTimeListRV.setLayoutManager(new LinearLayoutManager(context));
        limitTimeListRV.setAdapter(adaptor);
        setUiVisibility(true);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.extendedFbLimitTime) {
            List<String> packageList = new ArrayList<>();
            for (LimitTime limitTime : limitTimeList) {
                packageList.add(limitTime.getPackageName());
            }
            Helper.goTo(context, AddLimitTimeActivity.class,
                    Constants.LIMIT_LIME_LIST, (Serializable) packageList);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncGetTimeLimit extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = new RestAPI().GetLimitScreenByUid(strings[0]);
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
            limitTimeList.clear();
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
                        Helper.makeSnackBar(view, Constants.SOMETHING_WENT_WRONG);
                    }
                }
            } catch (Exception e) {
                Helper.makeSnackBar(view, Constants.SOMETHING_WENT_WRONG);
            }
        }

        private void saveDataToEntityAndSetupRecyclerView(JSONObject json) {
            try {
                JSONArray jsonArray = json.getJSONArray("Data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    LimitTime limitTime = new LimitTime();
                    limitTime.setUserId(jsonObj.optString("data0"));
                    limitTime.setPackageName(Authentication.DecryptMessage(jsonObj.optString("data1")));
                    limitTime.setTimeLimit(Authentication.DecryptMessage(jsonObj.optString("data2")));
                    limitTime.setStatus(jsonObj.optString("data3"));
                    limitTimeList.add(limitTime);
                }
                setUpRecyclerView();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
