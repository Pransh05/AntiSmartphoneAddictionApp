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

import com.example.antismartphoneaddictionapp.Activity.AddLimitTimeActivity;
import com.example.antismartphoneaddictionapp.Models.LimitTime;
import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.utils.Constants;
import com.example.antismartphoneaddictionapp.utils.Helper;

import java.io.Serializable;
import java.util.List;

public class LimitTimeAdaptor extends RecyclerView.Adapter<LimitTimeAdaptor.ViewHolder> {

    private final Context context;
    private List<LimitTime> timeLimitArrayList = null;

    public LimitTimeAdaptor(List<LimitTime> timeLimitArrayList, Context context) {
        this.timeLimitArrayList = timeLimitArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public LimitTimeAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.limit_time_content, parent, false);
        return new ViewHolder(item);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull LimitTimeAdaptor.ViewHolder holder, int position) {
        LimitTime timeLimit = timeLimitArrayList.get(position);
        PackageManager packageManager;
        String appName = null;
        try {
            packageManager = context.getPackageManager();
            Drawable icon = packageManager.getApplicationIcon(timeLimit.getPackageName());
            holder.iv_app_icon.setImageDrawable(icon);

            final ApplicationInfo info = packageManager.getApplicationInfo(timeLimit.getPackageName(), 0);
            appName = (String) packageManager.getApplicationLabel(info);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        holder.txtAppName.setText(appName != null ? appName : timeLimit.getPackageName());
        holder.txtLimitTime.setText("Limit: " + timeLimit.getTimeLimit() + " min");
        holder.txtStatus.setText("Status: " + timeLimit.getStatus());
        holder.itemView.setOnClickListener(v -> {
            Helper.goTo(context, AddLimitTimeActivity.class,
                    Constants.LIMIT_LIME_OBJ, timeLimit);
        });

    }

    @Override
    public int getItemCount() {
        return timeLimitArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtAppName, txtLimitTime, txtStatus;
        ImageView iv_app_icon;
        CardView appCard;


        public ViewHolder(View view) {
            super(view);
            this.txtAppName = view.findViewById(R.id.txtAppName);
            this.txtLimitTime = view.findViewById(R.id.txtLimitTime);
            this.txtStatus = view.findViewById(R.id.txtStatus);
            this.iv_app_icon = view.findViewById(R.id.iv_app_icon);
            this.appCard = view.findViewById(R.id.appCard);
        }
    }

}
