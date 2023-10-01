package com.example.antismartphoneaddictionapp.Adaptor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Activity.CommunityChatActivity;
import com.example.antismartphoneaddictionapp.Models.Community;
import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.utils.Constants;
import com.example.antismartphoneaddictionapp.utils.Helper;

import java.io.Serializable;
import java.util.List;

public class CommunityAdaptor extends RecyclerView.Adapter<CommunityAdaptor.ViewHolder> {

    private final Context context;
    private List<Community> communityArrayList = null;

    public CommunityAdaptor(List<Community> communityArrayList, Context context) {
        this.communityArrayList = communityArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public CommunityAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.community_content, parent, false);
        return new ViewHolder(item);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CommunityAdaptor.ViewHolder holder, int position) {
        try {
            Community community = communityArrayList.get(position);
            holder.txtCommunityName.setText(community.getCommunityName());
            holder.txtDescription.setText("Description: " + community.getDescription());
            holder.itemView.setOnClickListener(v -> {
                Helper.goTo(context, CommunityChatActivity.class,
                        Constants.COMMUNITY_ID, community.getCommunityId());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return communityArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCommunityName, txtDescription;

        public ViewHolder(View view) {
            super(view);
            this.txtCommunityName = view.findViewById(R.id.txtCommunityName);
            this.txtDescription = view.findViewById(R.id.txtDescription);
        }
    }

}
