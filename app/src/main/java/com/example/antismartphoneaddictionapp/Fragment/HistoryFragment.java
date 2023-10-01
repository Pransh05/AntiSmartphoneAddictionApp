package com.example.antismartphoneaddictionapp.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.antismartphoneaddictionapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private Context context;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private List<Long> datesInMillis;

    public HistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        initializeUI(view);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void initializeUI(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        datesInMillis = getDatesForLastWeek();
        final FragmentActivity activity = (FragmentActivity) context;
        UsagePagerAdapter pagerAdapter = new UsagePagerAdapter(activity, datesInMillis);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(datesInMillis.size() -1,true);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(datesInMillis.get(position));
            String tabTitle = formatDate(calendar.getTimeInMillis());
            tab.setText(tabTitle);
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                HistoryDayFragment fragment = (HistoryDayFragment) pagerAdapter.createFragment(position);
                fragment.updateData(datesInMillis.get(position));
            }
        });
    }

    private List<Long> getDatesForLastWeek() {
        List<Long> datesInMillis = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            datesInMillis.add(calendar.getTimeInMillis());
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        Collections.reverse(datesInMillis);
        return datesInMillis;
    }

    private String formatDate(long dateInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String month = new SimpleDateFormat("MMM", Locale.ENGLISH).format(calendar.getTime());

        String dayText;
        if (day >= 11 && day <= 13) {
            dayText = day + "th";
        } else {
            int lastDigit = day % 10;
            switch (lastDigit) {
                case 1:
                    dayText = day + "st";
                    break;
                case 2:
                    dayText = day + "nd";
                    break;
                case 3:
                    dayText = day + "rd";
                    break;
                default:
                    dayText = day + "th";
                    break;
            }
        }

        return dayText + " " + month;
    }

    private static class UsagePagerAdapter extends FragmentStateAdapter {

        private final List<Long> datesInMillis;

        UsagePagerAdapter(FragmentActivity fragmentActivity, List<Long> datesInMillis) {
            super(fragmentActivity);
            this.datesInMillis = datesInMillis;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return HistoryDayFragment.newInstance(datesInMillis.get(position));
        }

        @Override
        public int getItemCount() {
            return datesInMillis.size();
        }
    }
}
