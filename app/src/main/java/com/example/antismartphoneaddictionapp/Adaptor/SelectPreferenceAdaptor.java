package com.example.antismartphoneaddictionapp.Adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Interface.ISelectPreferenceChange;
import com.example.antismartphoneaddictionapp.Models.Preference;
import com.example.antismartphoneaddictionapp.R;

import java.util.List;

public class SelectPreferenceAdaptor extends RecyclerView.Adapter<SelectPreferenceAdaptor.ViewHolder> {

    private final Context context;
    private final ISelectPreferenceChange selectPreferenceChange;
    private List<Preference> preferenceArrayList;

    public SelectPreferenceAdaptor(List<Preference> preferenceArrayList, Context context
            , ISelectPreferenceChange selectPreferenceChange) {
        this.preferenceArrayList = preferenceArrayList;
        this.context = context;
        this.selectPreferenceChange = selectPreferenceChange;
    }


    @NonNull
    @Override
    public SelectPreferenceAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.select_preference_content, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectPreferenceAdaptor.ViewHolder holder, int position) {
        Preference preference = preferenceArrayList.get(position);
        holder.txtPreferenceName.setText(preference.getPreferenceName());
        holder.cbPreference.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preference.setPreferenceSelected(isChecked);
            selectPreferenceChange.onChangeSelectPreference(position,preference);
        });

    }

    @Override
    public int getItemCount() {
        return preferenceArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPreferenceName ;
        CardView appCard;
        CheckBox cbPreference;


        public ViewHolder(View view) {
            super(view);
            this.txtPreferenceName = view.findViewById(R.id.txtPreferenceName);
            this.appCard = view.findViewById(R.id.appCard);
            this.cbPreference = view.findViewById(R.id.cbPreference);
        }
    }

}
