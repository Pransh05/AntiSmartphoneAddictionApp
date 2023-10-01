package com.example.antismartphoneaddictionapp.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Adaptor.CommunityChatAdaptor;
import com.example.antismartphoneaddictionapp.Models.CommunityChat;
import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.Services.Authentication;
import com.example.antismartphoneaddictionapp.utils.Constants;
import com.example.antismartphoneaddictionapp.utils.DialogUtils;
import com.example.antismartphoneaddictionapp.utils.Helper;
import com.example.antismartphoneaddictionapp.utils.SharedPref;
import com.example.antismartphoneaddictionapp.web_services.JSONParse;
import com.example.antismartphoneaddictionapp.web_services.RestAPI;
import com.example.antismartphoneaddictionapp.web_services.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommunityChatActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout rlMainLayout;
    private RecyclerView communityChatRV;
    private TextView tvNoDataFound;

    private EditText etMessage;
    private ImageButton ibSendMessage;

    private Context context;
    private Dialog dialog;
    private RestAPI restAPI;
    private JSONParse jsonParse;
    private String communityId;

    private CommunityChatAdaptor communityChatAdaptor;
    private List<CommunityChat> communityChatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_chat);

        initToolbar();
        initUI();
        setListeners();
        initObj();
        loadIntentData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUiVisibility(false);
        new AsyncGetAllCommunityChat().execute(communityId);
    }

    private void initToolbar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("Community Chat");
        }
    }

    private void initUI() {
        rlMainLayout = findViewById(R.id.rlMainLayout);
        communityChatRV = findViewById(R.id.communityChatRV);
        tvNoDataFound = findViewById(R.id.tvNoDataFound);
        etMessage = findViewById(R.id.etMessage);
        ibSendMessage = findViewById(R.id.ibSendMessage);
    }

    private void setListeners() {
        ibSendMessage.setOnClickListener(this);
    }

    private void initObj() {
        context = this;
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        communityChatList = new ArrayList<>();
    }

    private void loadIntentData() {
        communityId = getIntent().getStringExtra(Constants.COMMUNITY_ID);
    }

    private void setUiVisibility(boolean isDataFound) {
        if (isDataFound) {
            communityChatRV.setVisibility(View.VISIBLE);
            tvNoDataFound.setVisibility(View.GONE);
        } else {
            communityChatRV.setVisibility(View.GONE);
            tvNoDataFound.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ibSendMessage) {
            onClickIbSendMessage();
        }
    }

    private void onClickIbSendMessage() {
        try {
            if (!etMessage.getText().toString().trim().isEmpty()) {
                String[] strings = Helper.currentDateTime();
                String userId = SharedPref.checkDeviceRegisteredAndGetUserId(context);
                String chatMessage = Authentication.EncryptMessage(etMessage.getText().toString().trim());
                String dateTime = strings[0];
                new AsyncCreateCommunityChat().execute(communityId, userId, chatMessage, dateTime);
            } else {
                Helper.makeSnackBar(rlMainLayout, "Message is empty");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setUpRecyclerView() {
        if (communityChatAdaptor != null) {
            communityChatAdaptor.updateChatList(communityChatList);
        } else {
            communityChatAdaptor = new CommunityChatAdaptor(communityChatList, this);
            communityChatRV.setHasFixedSize(true);
            communityChatRV.setLayoutManager(new LinearLayoutManager(this));
            communityChatRV.setAdapter(communityChatAdaptor);
        }
        setUiVisibility(true);
        communityChatAdaptor.notifyDataSetChanged();
    }

    private void saveDataToEntityAndSetupRecyclerView(JSONObject json) {
        try {
            JSONArray jsonArray = json.getJSONArray("Data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                CommunityChat chat = new CommunityChat();
                chat.setCommunityId(jsonObj.optString("data0"));
                chat.setUserId(jsonObj.optString("data1"));
                chat.setMessage(Authentication.DecryptMessage(jsonObj.optString("data2")));
                chat.setDateTime(jsonObj.optString("data3"));
                communityChatList.add(chat);
            }
            setUpRecyclerView();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncGetAllCommunityChat extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setUiVisibility(false);
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = restAPI.GetCommunityChats(communityId);
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
            communityChatList.clear();
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
    private class AsyncCreateCommunityChat extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String ans;
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject json = restAPI.CreateCommunityChat(strings[0],
                        strings[1], strings[2], strings[3]);
                ans = jsonParse.parse(json);
            } catch (Exception e) {
                ans = e.getMessage();
            }
            return ans;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                DialogUtils.dismissDialog(dialog);
                if (Utility.checkConnection(s)) {
                    Pair<String, String> pair = Utility.GetErrorMessage(s);
                    Utility.ShowAlertDialog(context, pair.first, pair.second, false);
                } else {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");
                    if (StatusValue.compareTo("true") == 0) {
                        etMessage.setText("");
                        onResume();
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