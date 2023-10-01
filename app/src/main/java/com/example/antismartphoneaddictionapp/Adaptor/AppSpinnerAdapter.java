package com.example.antismartphoneaddictionapp.Adaptor;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.antismartphoneaddictionapp.Models.AppModel;
import com.example.antismartphoneaddictionapp.R;

import java.util.List;

public class AppSpinnerAdapter extends ArrayAdapter<AppModel> {
    private final Context context;
    private final List<AppModel> installedApps;

    public AppSpinnerAdapter(Context context, int resource, List<AppModel> installedApps) {
        super(context, resource, installedApps);
        this.context = context;
        this.installedApps = installedApps;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.app_spinner_content, parent, false);
        }
        AppModel appModel = installedApps.get(position);
        ImageView appIconImageView = convertView.findViewById(R.id.appIconImageView);
        Drawable appIcon = getPackageIcon(appModel.getPackageName());
        appIconImageView.setImageDrawable(appIcon);

        TextView appNameTextView = convertView.findViewById(R.id.appNameTextView);
        appNameTextView.setText(appModel.getAppName());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    private Drawable getPackageIcon(String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return appInfo.loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
