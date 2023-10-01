package com.example.antismartphoneaddictionapp.Adaptor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Models.AppModel;
import com.example.antismartphoneaddictionapp.R;

import java.util.List;

@SuppressLint({"DefaultLocale", "SetTextI18n"})
public class AppAdaptor extends RecyclerView.Adapter<AppAdaptor.ViewHolder> {

    private final Context context;
    private List<AppModel> appModelArrayList = null;

    public AppAdaptor(List<AppModel> appModelArrayList, Context context) {
        this.appModelArrayList = appModelArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public AppAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.app_content, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull AppAdaptor.ViewHolder holder, int position) {
        AppModel appModel = appModelArrayList.get(position);
        holder.txtAppName.setText(appModel.getAppName());

        try {
            long totalTimeInSeconds = appModel.getTotalTimeInForeground() / 1000;
            int hours = (int) (totalTimeInSeconds / 3600);
            int minutes = (int) ((totalTimeInSeconds % 3600) / 60);
            int seconds = (int) (totalTimeInSeconds % 60);
            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            holder.txtTimeUsed.setText("Time Used: " + formattedTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Drawable icon = context.getPackageManager().getApplicationIcon(appModel.getPackageName());
            holder.iv_app_icon.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return appModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtAppName, txtTimeUsed;
        ImageView iv_app_icon;
        CardView appCard;


        public ViewHolder(View view) {
            super(view);
            this.txtAppName = view.findViewById(R.id.txtAppName);
            this.txtTimeUsed = view.findViewById(R.id.txtTimeUsed);
            this.iv_app_icon = view.findViewById(R.id.iv_app_icon);
            this.appCard = view.findViewById(R.id.appCard);
        }
    }

    public String getAppNameFromPkgName(String Packagename) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(Packagename, PackageManager.GET_META_DATA);
            String appName = (String) packageManager.getApplicationLabel(info);
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

}
