package com.example.antismartphoneaddictionapp.Adaptor;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Models.Recommendation;
import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.utils.DialogUtils;

import java.util.List;

public class RecommendationAdaptor extends RecyclerView.Adapter<RecommendationAdaptor.ViewHolder> {

    private final Context context;
    private List<Recommendation> recommendationArrayList;
    private Dialog dialog;

    public RecommendationAdaptor(List<Recommendation> recommendationArrayList, Context context) {
        this.recommendationArrayList = recommendationArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public RecommendationAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.recommendation_content, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationAdaptor.ViewHolder holder, int position) {
        Recommendation recommendation = recommendationArrayList.get(position);
        holder.txtName.setText(recommendation.getPreferenceName());
        holder.txtRecommendation.setText(recommendation.getRecommendation());
        holder.itemView.setOnClickListener(v -> {
            DialogUtils.dismissDialog(dialog);
            showAlert(recommendation.getRecommendation());
        });

    }

    private void showAlert(String message) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Description");
            builder.setMessage(message);
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return recommendationArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtRecommendation;


        public ViewHolder(View view) {
            super(view);
            this.txtName = view.findViewById(R.id.txtName);
            this.txtRecommendation = view.findViewById(R.id.txtRecommendation);
        }
    }

}
