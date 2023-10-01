package com.example.antismartphoneaddictionapp.Adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antismartphoneaddictionapp.Models.CommunityChat;
import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.utils.SharedPref;

import java.util.List;
import java.util.Objects;

public class CommunityChatAdaptor extends RecyclerView.Adapter<CommunityChatAdaptor.ViewHolder> {

    private Context context;
    private List<CommunityChat> communityChatArrayList;
    String userId;

    public CommunityChatAdaptor( List<CommunityChat> communityChatArrayList,Context context) {
        this.context = context;
        this.communityChatArrayList = communityChatArrayList;
        this.userId = SharedPref.checkDeviceRegisteredAndGetUserId(context);
    }

    public void updateChatList(List<CommunityChat> communityChatArrayList){
        this.communityChatArrayList = communityChatArrayList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View detailItem = inflater.inflate(R.layout.chat_content, parent, false);
        return new ViewHolder(detailItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (communityChatArrayList.size() != 0) {
            if (!Objects.equals(communityChatArrayList.get(position).getUserId(), userId)) {
                holder.tvReceiverMessage.setText(communityChatArrayList.get(position).getMessage());
                holder.tvReceiverMessageTime.setText(communityChatArrayList.get(position).getDateTime());
                holder.llReceiverLayout.setVisibility(View.VISIBLE);
                holder.llSenderLayout.setVisibility(View.GONE);
            } else {
                holder.tvSenderMessage.setText(communityChatArrayList.get(position).getMessage());
                holder.tvSenderMessageTime.setText(communityChatArrayList.get(position).getDateTime());
                holder.llReceiverLayout.setVisibility(View.GONE);
                holder.llSenderLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return communityChatArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llReceiverLayout, llSenderLayout;
        TextView tvSenderMessage, tvSenderMessageTime,
                tvReceiverMessage, tvReceiverMessageTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            llReceiverLayout = itemView.findViewById(R.id.llReceiverLayout);
            llSenderLayout = itemView.findViewById(R.id.llSenderLayout);
            tvSenderMessage = itemView.findViewById(R.id.tvSenderMessage);
            tvSenderMessageTime = itemView.findViewById(R.id.tvSenderMessageTime);
            tvReceiverMessage = itemView.findViewById(R.id.tvReceiverMessage);
            tvReceiverMessageTime = itemView.findViewById(R.id.tvReceiverMessageTime);
        }
    }
}
