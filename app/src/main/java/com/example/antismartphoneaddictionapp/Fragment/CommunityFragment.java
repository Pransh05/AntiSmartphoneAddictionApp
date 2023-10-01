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

import com.example.antismartphoneaddictionapp.Adaptor.CommunityAdaptor;
import com.example.antismartphoneaddictionapp.Models.Community;
import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.utils.Constants;
import com.example.antismartphoneaddictionapp.utils.DialogUtils;
import com.example.antismartphoneaddictionapp.utils.Helper;
import com.example.antismartphoneaddictionapp.web_services.JSONParse;
import com.example.antismartphoneaddictionapp.web_services.RestAPI;
import com.example.antismartphoneaddictionapp.web_services.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private View view;
    private RecyclerView communityListRV;
    private TextView tvNoDataFound;

    private Context context;
    private Dialog dialog;
    private List<Community> communityList = new ArrayList<>();

    public CommunityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_community, container, false);
        initializeUI(view);
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
        new AsyncGetCommunity().execute();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setUiVisibility(false);
            new AsyncGetCommunity().execute();
        }
    }

    private void initializeUI(View view) {
        communityListRV = view.findViewById(R.id.communityListRV);
        tvNoDataFound = view.findViewById(R.id.tvNoDataFound);
    }

    private void setUiVisibility(boolean isDataFound) {
        if (isDataFound) {
            communityListRV.setVisibility(View.VISIBLE);
            tvNoDataFound.setVisibility(View.GONE);
        } else {
            communityListRV.setVisibility(View.GONE);
            tvNoDataFound.setVisibility(View.VISIBLE);
        }
    }

    private void setUpRecyclerView() {
        CommunityAdaptor adaptor = new CommunityAdaptor(communityList, context);
        communityListRV.setHasFixedSize(true);
        communityListRV.setLayoutManager(new LinearLayoutManager(context));
        communityListRV.setAdapter(adaptor);
        setUiVisibility(true);
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncGetCommunity extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = new RestAPI().GetCommunity();
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
            communityList.clear();
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
                    Community community = new Community();
                    community.setCommunityId(jsonObj.optString("data0"));
                    community.setCommunityName(jsonObj.optString("data1"));
                    community.setDescription(jsonObj.optString("data2"));
                    communityList.add(community);
                }
                setUpRecyclerView();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
