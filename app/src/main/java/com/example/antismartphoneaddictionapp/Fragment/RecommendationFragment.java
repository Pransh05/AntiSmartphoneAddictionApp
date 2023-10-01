package com.example.antismartphoneaddictionapp.Fragment;

import static android.content.Context.USAGE_STATS_SERVICE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
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

import com.example.antismartphoneaddictionapp.Adaptor.RecommendationAdaptor;
import com.example.antismartphoneaddictionapp.Adaptor.RecommendationLimitTimeAdaptor;
import com.example.antismartphoneaddictionapp.Models.Category;
import com.example.antismartphoneaddictionapp.Models.LimitTime;
import com.example.antismartphoneaddictionapp.Models.Recommendation;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("DefaultLocale")
public class RecommendationFragment extends Fragment {

    // Declare class variables
    private View view;
    private RecyclerView limitTimeAppListRV, recommendationListRV;
    private TextView limitTimeAppListTvNoDataFound, recommendationListTvNoDataFound;

    private Context context;
    private Dialog dialog;
    private UsageStatsManager mUsageStatsManager;

    private String userId;
    private List<Category> categoryList = new ArrayList<>();
    private List<Recommendation> recommendationList = new ArrayList<>();
    private List<LimitTime> limitTimeList = new ArrayList<>();
    private Map<String, LimitTime> exceededLimitTimeMap = new HashMap<>();

    public RecommendationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recommendation, container, false);
        initializeUI(view);
        userId = SharedPref.checkDeviceRegisteredAndGetUserId(context);
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
        setUiVisibility();
        new AsyncGetAllCategoryThenLimitTime().execute();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setUiVisibility();
            new AsyncGetAllCategoryThenLimitTime().execute();
        }
    }

    // Initialize UI components
    private void initializeUI(View view) {
        limitTimeAppListRV = view.findViewById(R.id.limitTimeAppListRV);
        recommendationListRV = view.findViewById(R.id.recommendationListRV);
        limitTimeAppListTvNoDataFound = view.findViewById(R.id.limitTimeAppListTvNoDataFound);
        recommendationListTvNoDataFound = view.findViewById(R.id.recommendationListTvNoDataFound);
    }

    // Update UI visibility based on data
    private void setUiVisibility() {
        limitTimeAppListRV.setVisibility(limitTimeList.isEmpty() ? View.GONE : View.VISIBLE);
        limitTimeAppListTvNoDataFound.setVisibility(limitTimeList.isEmpty() ? View.VISIBLE : View.GONE);

        recommendationListRV.setVisibility(recommendationList.isEmpty() ? View.GONE : View.VISIBLE);
        recommendationListTvNoDataFound.setVisibility(recommendationList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Set up the LimitTime RecyclerView
    private void setUpLimitTimeRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        RecommendationLimitTimeAdaptor adaptor = new RecommendationLimitTimeAdaptor(new ArrayList<>(exceededLimitTimeMap.values()), context);
        limitTimeAppListRV.setHasFixedSize(true);
        limitTimeAppListRV.setLayoutManager(manager);
        limitTimeAppListRV.setAdapter(adaptor);
        setUiVisibility();
    }

    // Set up the Recommendation RecyclerView
    private void setUpRecommendationRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(context);
        RecommendationAdaptor adaptor = new RecommendationAdaptor(recommendationList, context);
        recommendationListRV.setLayoutManager(manager);
        recommendationListRV.setAdapter(adaptor);
        setUiVisibility();
    }

    // Initialize the UsageStatsManager
    private void proceedLimitTimeData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mUsageStatsManager = (UsageStatsManager) context.getSystemService(USAGE_STATS_SERVICE);
        } else {
            mUsageStatsManager = (UsageStatsManager) context.getSystemService("usagestats");
        }
    }

    // Check app usage and set up RecyclerView
    @SuppressLint("SetTextI18n")
    private void checkUsageAndSetupRecyclerView() {
        boolean isLimitTimeExceededForAnyApp = false;
        List<String> exceededLimitTimePackageNameList = new ArrayList<>();

        try {
            recommendationList.clear();
            exceededLimitTimeMap.clear();

            for (LimitTime limitTime : limitTimeList) {
                String packageName = limitTime.getPackageName();
                double limitInMinutes = Double.parseDouble(limitTime.getTimeLimit());
                long limitInMillis = (long) (limitInMinutes * 60 * 1000);
                long startTime = getMidnightMillis();
                long endTime = System.currentTimeMillis();
                List<UsageStats> stats = mUsageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_BEST, startTime, endTime);

                for (UsageStats usageStats : stats) {
                    if (packageName.equals(usageStats.getPackageName())) {
                        long totalTimeInForeground = usageStats.getTotalTimeInForeground();
                        if (totalTimeInForeground > limitInMillis) {
                            isLimitTimeExceededForAnyApp = true;
                            exceededLimitTimePackageNameList.add(packageName);

                            long millis = totalTimeInForeground - limitInMillis;
                            double exceedTimeMinutes = ((double) millis) / 60000.0;

                            String time = String.format("%.1f min", limitInMinutes);
                            String exceedTime = String.format("%.1f min", exceedTimeMinutes);

                            if (exceededLimitTimeMap.containsKey(packageName)) {
                                LimitTime existingLimitTime = exceededLimitTimeMap.get(packageName);
                                existingLimitTime.setExceededTime(exceedTime);
                                existingLimitTime.setTimeLimit(time);
                            } else {
                                LimitTime exceededLimitTime = new LimitTime();
                                exceededLimitTime.setPackageName(packageName);
                                exceededLimitTime.setTimeLimit(time);
                                exceededLimitTime.setExceededTime(exceedTime);
                                exceededLimitTimeMap.put(packageName, exceededLimitTime);
                            }
                        }
                    }
                }
            }


            if (isLimitTimeExceededForAnyApp) {
                recommendationList.clear();
                for (Category category : categoryList) {
                    for (String packageName : exceededLimitTimePackageNameList) {
                        if (category.getPackageName().equals(packageName)) {
                            boolean isCategoryAdded = false;
                            for (Recommendation recommendation : recommendationList) {
                                if (recommendation.getPreferenceName().equals(category.getCategory())) {
                                    isCategoryAdded = true;
                                    break;
                                }
                            }
                            if (!isCategoryAdded) {
                                Recommendation recommendation = new Recommendation();
                                recommendation.setPreferenceName(category.getCategory());
                                recommendation.setRecommendation(category.getRecommendation());
                                recommendationList.add(recommendation);
                            }
                        }
                    }
                }
                new AsyncGetRecommendation().execute(userId);
            } else {
                recommendationListTvNoDataFound.setText("No App Limit Exceeded");
            }

            setUpLimitTimeRecyclerView();
        } catch (Exception e) {
            Helper.makeSnackBar(view, Constants.SOMETHING_WENT_WRONG);
        }
    }

    // Get midnight milliseconds
    private long getMidnightMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // AsyncTask for getting time limit data
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
                        saveDataToEntity(json);
                        checkUsageAndSetupRecyclerView();
                    } else if (jsonString.compareToIgnoreCase("no") != 0) {
                        Helper.makeSnackBar(view, Constants.SOMETHING_WENT_WRONG);
                    }
                }
            } catch (Exception e) {
                Helper.makeSnackBar(view, Constants.SOMETHING_WENT_WRONG);
            }
        }

        private void saveDataToEntity(JSONObject json) {
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
                if (limitTimeList.size() > 0) {
                    proceedLimitTimeData();
                } else {
                    Helper.makeSnackBar(view, "No Limit Time Data Found");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    // AsyncTask for getting recommendations
    @SuppressLint("StaticFieldLeak")
    private class AsyncGetRecommendation extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = new RestAPI().GetAllRecommendationByUserId(strings[0]);
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
                    if (jsonString.compareToIgnoreCase("ok") == 0) {
                        saveRecommendationDataToEntity(json);
                    } else if (jsonString.compareToIgnoreCase("no") != 0) {
                        Helper.makeSnackBar(view, Constants.SOMETHING_WENT_WRONG);
                    }
                }
            } catch (Exception e) {
                Helper.makeSnackBar(view, Constants.SOMETHING_WENT_WRONG);
            }
        }

        private void saveRecommendationDataToEntity(JSONObject json) {
            try {
                JSONArray jsonArray = json.getJSONArray("Data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    Recommendation recommendation = new Recommendation();
                    recommendation.setPreferenceId(jsonObj.optString("data0"));
                    recommendation.setPreferenceName(jsonObj.optString("data1"));
                    recommendation.setRecommendation(jsonObj.optString("data2"));
                    recommendationList.add(recommendation);
                }
                setUpRecommendationRecyclerView();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    // AsyncTask for getting category data
    @SuppressLint("StaticFieldLeak")
    private class AsyncGetAllCategoryThenLimitTime extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = DialogUtils.showLoadingDialog(context, Constants.LOADING, dialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = new RestAPI().GetAllCategory();
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
            categoryList.clear();
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
            } finally {
                new AsyncGetTimeLimit().execute(SharedPref.checkDeviceRegisteredAndGetUserId(context));
            }
        }

        private void saveDataToEntityAndSetupRecyclerView(JSONObject json) {
            try {
                JSONArray jsonArray = json.getJSONArray("Data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    boolean isExist = false;
                    for (Category category1 : categoryList) {
                        if (category1.getCategory().equals(jsonObj.optString("data0"))) {
                            isExist = true;
                        }
                    }
                    if (!isExist) {
                        Category category = new Category();
                        category.setCategoryId(jsonObj.optString("data0"));
                        category.setAppName(jsonObj.optString("data1"));
                        category.setPackageName(jsonObj.optString("data2"));
                        category.setCategory(jsonObj.optString("data3"));
                        category.setRecommendation(jsonObj.optString("data4"));
                        categoryList.add(category);
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
