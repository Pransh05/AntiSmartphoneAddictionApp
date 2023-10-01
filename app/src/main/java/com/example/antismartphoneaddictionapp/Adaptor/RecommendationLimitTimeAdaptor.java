package com.example.antismartphoneaddictionapp.Adaptor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Models.LimitTime;
import com.example.antismartphoneaddictionapp.R;

import java.util.List;

public class RecommendationLimitTimeAdaptor extends RecyclerView.Adapter<RecommendationLimitTimeAdaptor.ViewHolder> {

    private final Context context;
    private List<LimitTime> timeLimitArrayList = null;

    public RecommendationLimitTimeAdaptor(List<LimitTime> timeLimitArrayList, Context context) {
        this.timeLimitArrayList = timeLimitArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public RecommendationLimitTimeAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.recommendation_limit_time_content,
                parent, false);
        return new ViewHolder(item);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecommendationLimitTimeAdaptor.ViewHolder holder, int position) {
        try {
            LimitTime timeLimit = timeLimitArrayList.get(position);
            PackageManager packageManager;
            String appName = null;
            try {
                packageManager = context.getPackageManager();
                Drawable icon = packageManager.getApplicationIcon(timeLimit.getPackageName());
                holder.iv_app_icon.setImageDrawable(icon);

                final ApplicationInfo info = packageManager.getApplicationInfo(timeLimit.getPackageName(), 0);
                appName = (String) packageManager.getApplicationLabel(info);

            } catch (Exception e) {
                e.printStackTrace();
            }

            String exceededTime = timeLimit.getExceededTime();
            String limitTime = timeLimit.getTimeLimit();
            appName = appName != null ? appName : timeLimit.getPackageName();
            holder.txtAppName.setText(appName != null ? appName : "Unknown");
            holder.txtLimitTime.setText(limitTime == null ? "Unknown" : limitTime);
            holder.txtExceededTime.setText(exceededTime == null ? "Unknown" : exceededTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return timeLimitArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtAppName,txtLimitTime, txtExceededTime;
        ImageView iv_app_icon;
        CardView appCard;


        public ViewHolder(View view) {
            super(view);
            this.txtAppName = view.findViewById(R.id.txtAppName);
            this.txtLimitTime = view.findViewById(R.id.txtLimitTime);
            this.txtExceededTime = view.findViewById(R.id.txtExceededTime);
            this.iv_app_icon = view.findViewById(R.id.iv_app_icon);
            this.appCard = view.findViewById(R.id.appCard);
        }
    }

}
