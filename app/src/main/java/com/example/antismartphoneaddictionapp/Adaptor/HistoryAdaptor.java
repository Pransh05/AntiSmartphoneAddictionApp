package com.example.antismartphoneaddictionapp.Adaptor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Models.Category;
import com.example.antismartphoneaddictionapp.R;

import java.util.List;

public class HistoryAdaptor extends RecyclerView.Adapter<HistoryAdaptor.ViewHolder> {

    private final Context context;
    private List<Category> categoryList;

    public HistoryAdaptor(List<Category> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
    }


    @NonNull
    @Override
    public HistoryAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.history_content, parent, false);
        return new ViewHolder(item);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HistoryAdaptor.ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.txtCategory.setText(category.getCategory());

        try {
            long totalTimeInSeconds = category.getTotalUsageTime() / 1000;
            int hours = (int) (totalTimeInSeconds / 3600);
            int minutes = (int) ((totalTimeInSeconds % 3600) / 60);
            int seconds = (int) (totalTimeInSeconds % 60);
            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            holder.txtTimeUsed.setText("Time Used: " + formattedTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
         
        if (category.getPreviousDayTotalUsageTime() != 0) {
            if (category.getPreviousDayTotalUsageTime() > category.getTotalUsageTime()) {
                holder.appCard.setCardBackgroundColor(Color.GREEN);
                holder.txtCategory.setTextColor(Color.WHITE);
                holder.txtTimeUsed.setTextColor(Color.WHITE);
            } else if (category.getPreviousDayTotalUsageTime() < category.getTotalUsageTime()) {
                holder.appCard.setCardBackgroundColor(Color.RED);
                holder.txtCategory.setTextColor(Color.WHITE);
                holder.txtTimeUsed.setTextColor(Color.WHITE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategory, txtTimeUsed;
        CardView appCard;


        public ViewHolder(View view) {
            super(view);
            this.txtCategory = view.findViewById(R.id.txtCategory);
            this.txtTimeUsed = view.findViewById(R.id.txtTimeUsed);
            this.appCard = view.findViewById(R.id.appCard);
        }
    }


}
