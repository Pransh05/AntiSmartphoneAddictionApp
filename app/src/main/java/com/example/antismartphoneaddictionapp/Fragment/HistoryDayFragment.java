package com.example.antismartphoneaddictionapp.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
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

import com.example.antismartphoneaddictionapp.Adaptor.HistoryAdaptor;
import com.example.antismartphoneaddictionapp.Models.Category;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryDayFragment extends Fragment {

    private Context context;
    private static Long tabDateInMillis;
    private final List<Category> categoryList = new ArrayList<>();

    private View view;
    private RecyclerView categoryListRV;
    private TextView tvNoDataFound;

    private Dialog dialog;

    public static HistoryDayFragment newInstance(long dateInMillis) {
        tabDateInMillis = dateInMillis;
        return new HistoryDayFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUiVisibility(false);
        new AsyncGetAllCategory().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_history_day, container, false);
        initializeUI(view);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void initializeUI(View view) {
        categoryListRV = view.findViewById(R.id.categoryListRV);
        tvNoDataFound = view.findViewById(R.id.tvNoDataFound);
    }

    private void setUiVisibility(boolean isDataFound) {
        if (isDataFound) {
            categoryListRV.setVisibility(View.VISIBLE);
            tvNoDataFound.setVisibility(View.GONE);
        } else {
            categoryListRV.setVisibility(View.GONE);
            tvNoDataFound.setVisibility(View.VISIBLE);
        }
    }

    private void setUpRecyclerView() {
        final List<Category> categoryUsageStats = getCategoryUsageStats(categoryList);
        HistoryAdaptor adaptor = new HistoryAdaptor(categoryUsageStats, context);
        categoryListRV.setHasFixedSize(true);
        categoryListRV.setLayoutManager(new LinearLayoutManager(context));
        categoryListRV.setAdapter(adaptor);
        setUiVisibility(true);
    }

    private List<Category> getCategoryUsageStats(List<Category> categoryList) {
        Map<String, Long> categoryStatsMap = new HashMap<>();
        Map<String, Long> categoryPreviousDayStatsMap = new HashMap<>();

        Map<String, UsageStats> usageStatsMap = getUsageStatsMap(context, tabDateInMillis);
        Map<String, UsageStats> previousDayUsageStatsMap = getUsageStatsMap(context, getPreviousDateInMillis(tabDateInMillis));

        for (Category category : categoryList) {
            String packageName = category.getPackageName();
            String categoryKey = category.getCategory();

            if (usageStatsMap.containsKey(packageName)) {
                UsageStats usageStats = usageStatsMap.get(packageName);
                if (usageStats != null) {
                    long totalUsageTime = usageStats.getTotalTimeInForeground();
                    final long time = categoryStatsMap.getOrDefault(categoryKey, 0L) + totalUsageTime;
                    categoryStatsMap.put(categoryKey, time);
                }
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(tabDateInMillis);
            if (getMidnightMillis(calendar) == getCurrentDateInMillis()){
                if (previousDayUsageStatsMap.containsKey(packageName)) {
                    UsageStats previousDayUsageStats = previousDayUsageStatsMap.get(packageName);
                    if (previousDayUsageStats != null) {
                        long previousDayTotalUsageTime = previousDayUsageStats.getTotalTimeInForeground();
                        final long time = categoryPreviousDayStatsMap.getOrDefault(categoryKey, 0L) + previousDayTotalUsageTime;
                        categoryPreviousDayStatsMap.put(categoryKey, time);
                    }
                }
            }
        }

        List<Category> categoryStatsList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : categoryStatsMap.entrySet()) {
            Category categoryStats = new Category();
            categoryStats.setCategory(entry.getKey());
            categoryStats.setTotalUsageTime(entry.getValue());
            if (categoryPreviousDayStatsMap.containsKey(entry.getKey())) {
                categoryStats.setPreviousDayTotalUsageTime(categoryPreviousDayStatsMap.get(entry.getKey()));
            } else {
                categoryStats.setPreviousDayTotalUsageTime(0L);
            }
            categoryStatsList.add(categoryStats);
        }

        return categoryStatsList;
    }


    private Map<String, UsageStats> getUsageStatsMap(Context context, long dateInMillis) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        long startTime = getMidnightMillis(calendar);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endTime = calendar.getTimeInMillis();
        return usageStatsManager.queryAndAggregateUsageStats(startTime, endTime);
    }

    private long getMidnightMillis(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getCurrentDateInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return getMidnightMillis(calendar);
    }

    private long getPreviousDateInMillis(long dateInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return getMidnightMillis(calendar);
    }

    public void updateData(long dateInMillis) {
        tabDateInMillis = dateInMillis;
        new AsyncGetAllCategory().execute();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // The fragment is now visible, update the data
            updateData(tabDateInMillis);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncGetAllCategory extends AsyncTask<String, String, String> {

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
            }
        }

        private void saveDataToEntityAndSetupRecyclerView(JSONObject json) {
            try {
                JSONArray jsonArray = json.getJSONArray("Data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    Category category = new Category();
                    category.setCategoryId(jsonObj.optString("data0"));
                    category.setAppName(jsonObj.optString("data1"));
                    category.setPackageName(jsonObj.optString("data2"));
                    category.setCategory(jsonObj.optString("data3"));
                    categoryList.add(category);
                }
                setUpRecyclerView();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
