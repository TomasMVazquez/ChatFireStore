package com.applications.toms.chatfirestore.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.applications.toms.chatfirestore.MessageActivity;
import com.applications.toms.chatfirestore.R;
import com.applications.toms.chatfirestore.model.Chat;
import com.applications.toms.chatfirestore.model.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private static final String TAG = "TOM-UserAdapter";

    private Context mContext;
    private static List<User> mUsers;
    private boolean ischat;

    String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers,boolean ischat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.ischat = ischat;
    }

    public void setmUsers(List<User> mUsers) {
        this.mUsers = mUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(mContext.getApplicationContext()).load(user.getImageURL()).into(holder.profile_image);
        }

        if (ischat){
            lastMessage(user.getId(), holder.last_msg);
        }else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (ischat){
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            }else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid",user.getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        private ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;

        public ViewHolder (View itemview){
            super(itemview);

            username = itemview.findViewById(R.id.username);
            profile_image = itemview.findViewById(R.id.profile_image);
            img_on = itemview.findViewById(R.id.img_on);
            img_off = itemview.findViewById(R.id.img_off);
            last_msg = itemview.findViewById(R.id.last_msg);

        }

    }

    private void lastMessage(final String userid, final TextView last_msg){
        theLastMessage = "";

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final FirebaseFirestore reference = FirebaseFirestore.getInstance();

        reference.collection("Chats").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "onEvent: lastmessage " + queryDocumentSnapshots.getMetadata());
                String idChat = null;
                for (QueryDocumentSnapshot snapshots: queryDocumentSnapshots) {
                    Chat chat = snapshots.toObject(Chat.class);
                    if (chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(userid) ||
                            chat.getSender().equals(userid) && chat.getReceiver().equals(firebaseUser.getUid())) {
                        idChat = snapshots.getId();
                        break;
                    }
                }
                if (idChat!=null) {
                    reference.collection("Chats").document(idChat).collection("Messages")
                            .orderBy("id", Query.Direction.DESCENDING)
                            .limit(1)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            theLastMessage = queryDocumentSnapshots.getDocuments().get(0).toObject(Chat.class).getMessage();
                            last_msg.setText(theLastMessage);
                        }
                    });
                }else {
                    theLastMessage = "No Message";
                    last_msg.setText(theLastMessage);
                }

                last_msg.setText(theLastMessage);

            }
        });
    }


    public void moveChat(String userId){
        for (User u : mUsers) {
            if (u.getId().equals(userId)) {
                Log.d(TAG, "moving");
                moveItems(mUsers.indexOf(u));
                break;
            }
        }
    }

    private void moveItems(int position){
        User targetUser = mUsers.get(position);
        mUsers.remove(position);
        mUsers.add(0,targetUser);
        this.notifyItemMoved(position,0);
    }

}
