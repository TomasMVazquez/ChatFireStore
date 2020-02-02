package com.applications.toms.chatfirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applications.toms.chatfirestore.adapter.MessageAdapter;
import com.applications.toms.chatfirestore.model.Chat;
import com.applications.toms.chatfirestore.model.User;
import com.applications.toms.chatfirestore.util.ResultListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";

    CircleImageView profile_image;
    TextView username;

    FirebaseUser fuser;
    FirebaseFirestore reference;

    ImageButton btn_send;
    EditText text_send;
    RelativeLayout bottom;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView recyclerView;

    String imageurl;
    Intent intent;
    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                //finish();
            }
        });

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        text_send = findViewById(R.id.text_send);
        btn_send = findViewById(R.id.btn_send);
        bottom = findViewById(R.id.bottom);
        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseFirestore.getInstance();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")){
                    sendMessage(fuser.getUid(),userid,msg);
                }else {
                    Snackbar.make(bottom,"You cannot send empty msg",Snackbar.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });


        getUserMessage();

    }

    private void getUserMessage(){
        DocumentReference userRef = reference.collection("Users").document(userid);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }else {
                    Glide.with(MessageActivity.this).load(user.getImageURL()).into(profile_image);
                }

                readMessage(fuser.getUid(),userid,user.getImageURL());
            }
        });

    }


    private void sendMessage(String sender, final String receiver, String message){

        final HashMap<String,Object> hashMapUsers = new HashMap<>();
        hashMapUsers.put("sender",sender);
        hashMapUsers.put("receiver",receiver);

        final HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);

        getChatDB(sender, receiver, new ResultListener<String>() {
            @Override
            public void finish(String result) {
                DocumentReference chatRef;

                if (result != null){
                    chatRef = reference.collection("Chats").document(result);
                }else {
                    chatRef = reference.collection("Chats").document();
                    chatRef.set(hashMapUsers);
                    getUserMessage();
                }

                final CollectionReference msgRef = chatRef.collection("Messages");
                getMessageList(msgRef, new ResultListener<List<Chat>>() {
                    @Override
                    public void finish(List<Chat> result) {
                        hashMap.put("id",result.size());
                        msgRef.document(String.valueOf(result.size())).set(hashMap);
                    }
                });


            }
        });

    }

    public void getChatDB(final String sender, final String receiver, final ResultListener<String> resultListener){

        CollectionReference chatsRef = reference.collection("Chats");

        chatsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().size()>0) {
                        String rdo = null;
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.get("sender").equals(sender) && queryDocumentSnapshot.get("receiver").equals(receiver) ||
                                    queryDocumentSnapshot.get("sender").equals(receiver) && queryDocumentSnapshot.get("receiver").equals(sender)) {
                                rdo = queryDocumentSnapshot.getId();
                            }
                        }
                        resultListener.finish(rdo);
                    }else {
                        resultListener.finish(null);
                    }
                }
            }
        });

    }

    public void getMessageList(CollectionReference msgRef, final ResultListener<List<Chat>> resultListener){
        final List<Chat> chatList = new ArrayList<>();

        msgRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                chatList.clear();
                for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()) {
                    Chat chat = queryDocumentSnapshot.toObject(Chat.class);
                    chatList.add(chat);
                }
                resultListener.finish(chatList);
            }
        });
    }

    private void readMessage(final String myid, final String userid, final String imageurl){
        mChat = new ArrayList<>();

        final CollectionReference chatRef = reference.collection("Chats");

        messageAdapter = new MessageAdapter(MessageActivity.this,mChat,imageurl);


        getChatDB(myid, userid, new ResultListener<String>() {
            @Override
            public void finish(String result) {
                mChat.clear();
                if (result!=null) {
                    CollectionReference msgRef = chatRef.document(result).collection("Messages");
                    msgRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                Chat chat = dc.getDocument().toObject(Chat.class);
                                switch (dc.getType()) {
                                    case ADDED:
                                        if (mChat.size() > 0) {
                                            if (!mChat.get(mChat.size() - 1).getId().equals(chat.getId())) {
                                                mChat.add(chat);
                                                Log.d(TAG, "New: " + dc.getDocument().getData());
                                            }
                                        } else {
                                            mChat.add(chat);
                                        }
                                        break;
                                    case MODIFIED:
                                        Log.d(TAG, "Modified: " + dc.getDocument().getData());
                                        break;
                                    case REMOVED:
                                        Log.d(TAG, "Removed: " + dc.getDocument().getData());
                                        break;
                                }
                            }

                            sortArray(mChat);
                        }
                    });
                }
            }
        });

    }

    private void sortArray(List<Chat> chats){
        Collections.sort(chats, new Comparator<Chat>() {
            @Override
            public int compare(Chat o1, Chat o2) {
                return o1.getId() - o2.getId();
            }
        });

        messageAdapter.setmChat(chats);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.smoothScrollToPosition(chats.size());
    }

    private void status(String status){
        DocumentReference userRef = reference.collection("Users").document(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);

        userRef.update(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
