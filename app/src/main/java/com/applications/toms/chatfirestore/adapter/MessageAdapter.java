package com.applications.toms.chatfirestore.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applications.toms.chatfirestore.MessageActivity;
import com.applications.toms.chatfirestore.R;
import com.applications.toms.chatfirestore.model.Chat;
import com.applications.toms.chatfirestore.model.Message;
import com.applications.toms.chatfirestore.model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Message> mMsg;
    private String imageurl;

    public MessageAdapter(Context mContext, List<Message> mMsg, String imageurl) {
            this.mContext = mContext;
            this.mMsg= mMsg;
            this.imageurl = imageurl;
    }

    public void setmChat(List<Message> mMsg) {
            this.mMsg= mMsg;
            notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Message msg = mMsg.get(position);

        holder.show_message.setText(msg.getMessage());

        if (imageurl.equals(mContext.getString(R.string.image_default))){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(mContext.getApplicationContext()).load(imageurl).into(holder.profile_image);
        }

        if (position == mMsg.size()-1){
            if (msg.isIsseen()){
                holder.txt_seen.setText(mContext.getString(R.string.seen));
            }else {
                holder.txt_seen.setText(mContext.getString(R.string.delivered));
            }
        }else {
            holder.txt_seen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
            return mMsg.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message;
        private ImageView profile_image;
        public TextView txt_seen;

        public ViewHolder (View itemview){
            super(itemview);

            show_message = itemview.findViewById(R.id.show_message);
            profile_image = itemview.findViewById(R.id.profile_image);
            txt_seen = itemview.findViewById(R.id.txt_seen);

        }

    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mMsg.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }
}