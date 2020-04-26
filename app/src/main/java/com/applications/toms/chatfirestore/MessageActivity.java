package com.applications.toms.chatfirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applications.toms.chatfirestore.adapter.MessageAdapter;
import com.applications.toms.chatfirestore.model.Message;
import com.applications.toms.chatfirestore.notifications.APIService;
import com.applications.toms.chatfirestore.fragments.ChatsFragment;
import com.applications.toms.chatfirestore.model.Chat;
import com.applications.toms.chatfirestore.model.User;
import com.applications.toms.chatfirestore.notifications.Client;
import com.applications.toms.chatfirestore.notifications.Data;
import com.applications.toms.chatfirestore.notifications.MyResponse;
import com.applications.toms.chatfirestore.notifications.Sender;
import com.applications.toms.chatfirestore.util.Keys;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "TOM-MessageActivity";

    private static final String DATE_FORMAT_1 = "yy-MM-dd HH:mm";

    //Componentes
    private CircleImageView profile_image;
    private TextView username;
    private EditText text_send;
    private RelativeLayout bottom;
    private RecyclerView recyclerView;

    //Firebase
    private FirebaseUser fuser;
    private FirebaseFirestore reference;

    private MessageAdapter messageAdapter;
    private List<Message> mMsg;
    private String userid;
    private ListenerRegistration seenListener;
    private APIService apiService;
    private Boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        //Servicio para las notificaciones
        apiService = Client.getClient(getString(R.string.fcm_url)).create(APIService.class);

        //Componentes
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        text_send = findViewById(R.id.text_send);
        ImageButton btn_send = findViewById(R.id.btn_send);
        bottom = findViewById(R.id.bottom);
        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        //Intent para abrir el chat
        Intent intent = getIntent();
        userid = intent.getStringExtra(Keys.KEY_MSG_USERID);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseFirestore.getInstance();

        //Click en el btn para enviar el mensaje
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = text_send.getText().toString().trim();
                if (!msg.equals("")){
                    sendMessage(fuser.getUid(),userid,msg);
                }else {
                    Snackbar.make(bottom,getString(R.string.error_no_msg),Snackbar.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        //Llamar a los métodos para llenar el chat
        getUserMessage();

        seenMessage(userid);

    }

    //Métodos

    //Obtener al usuario para luego obtener los mensajes
    private void getUserMessage(){
        if (userid != null) {
            DocumentReference userRef = reference.collection(Keys.KEY_USERS).document(userid);

            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User user = documentSnapshot.toObject(User.class);
                    username.setText(user.getUsername());
                    if (user.getImageURL().equals(getString(R.string.image_default))) {
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(MessageActivity.this).load(user.getImageURL()).into(profile_image);
                    }

                    readMessage(fuser.getUid(), userid, user.getImageURL());
                }
            });
        }
    }

    //Actualizar si el mensaje enviado fue visto o no
    private void seenMessage(final String userId){

        getChatDB(fuser.getUid(), userId, new ResultListener<String>() {
            @Override
            public void finish(String result) {
                if (result!=null) {
                    seenListener = reference.collection(Keys.KEY_CHATS)
                            .document(result)
                            .collection(Keys.KEY_MESSAGES)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@androidx.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                                    for (QueryDocumentSnapshot dc : queryDocumentSnapshots) {
                                        Message msg = dc.toObject(Message.class);
                                        if (msg.getReceiver().equals(fuser.getUid()) && msg.getSender().equals(userId)) {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put(Keys.KEY_CHATS_ISSEEN, true);
                                            dc.getReference().update(hashMap);
                                        }
                                    }
                                }
                            });
                }
            }
        });

    }

    //Enviar mensaje
    private void sendMessage(String sender, final String receiver, String message){

        final HashMap<String,Object> hashMapUsers = new HashMap<>();
        hashMapUsers.put(Keys.KEY_MESSAGES_SEN,sender);
        hashMapUsers.put(Keys.KEY_MESSAGES_REC,receiver);

        final HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put(Keys.KEY_MESSAGES_SEN,sender);
        hashMap.put(Keys.KEY_MESSAGES_REC,receiver);
        hashMap.put(Keys.KEY_MESSAGES_MSG,message);
        hashMap.put(Keys.KEY_CHATS_ISSEEN,false);
        hashMap.put(Keys.KEY_MESSAGES_TIME,getCurrentDate());

        //Mandar mensaje a la base de datos
        getChatDB(sender, receiver, new ResultListener<String>() {
            @Override
            public void finish(String result) {
                DocumentReference chatRef;

                //Tiene ya una base creada:
                if (result != null){
                    chatRef = reference.collection(Keys.KEY_CHATS).document(result);
                }else {
                    //Si no la tiene la creamos la base de chat
                    chatRef = reference.collection(Keys.KEY_CHATS).document();
                    chatRef.set(hashMapUsers);
                    getUserMessage();
                }

                //Creamos la base del mensaje
                final CollectionReference msgRef = chatRef.collection(Keys.KEY_MESSAGES);
                getMessageList(msgRef, new ResultListener<List<Message>>() {
                    @Override
                    public void finish(List<Message> result) {
                        hashMap.put(Keys.KEY_MESSAGES_ID,result.size());
                        msgRef.document(String.valueOf(result.size())).set(hashMap);
                    }
                });


                //Cambiamos los index
                String sendedTo = fuser.getUid().equals(sender) ? receiver : sender;
                addIndexes(chatRef.getId(),sendedTo);

            }
        });

        reference.collection(Keys.KEY_USERS).document(fuser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User userSendingMsg = document.toObject(User.class);
                        if (notify) {
                            sendNotification(receiver, userSendingMsg.getUsername(), message);
                        }
                        notify = false;
                    }
                }

            }
        });

    }

    //Obtener index
    public void addIndexes(String chatRefId, String sendedTo){

        final HashMap<String,Object> hashMapIndex = new HashMap<>();
        hashMapIndex.put(Keys.KEY_INDEX,"0");

        //Creamos la base del INDEX
        DocumentReference chatRef = reference.collection(Keys.KEY_CHATS).document(chatRefId);

        final CollectionReference indexRef = chatRef.collection(Keys.KEY_INDEX_COLLECTION);
        indexRef.document(fuser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        getIndexDB(document.getReference(), new ResultListener<String>() {
                            @Override
                            public void finish(String result) {
                                changeIndexes(chatRefId,result,fuser.getUid());
                                document.getReference().set(hashMapIndex);
                            }
                        });
                    } else {
                        changeIndexes(chatRefId,"0",fuser.getUid());
                        document.getReference().set(hashMapIndex);
                    }
                }
            }
        });

        indexRef.document(sendedTo).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        getIndexDB(document.getReference(), new ResultListener<String>() {
                            @Override
                            public void finish(String result) {
                                changeIndexes(chatRefId,result,sendedTo);
                                document.getReference().set(hashMapIndex);
                            }
                        });
                    } else {
                        changeIndexes(chatRefId,"0",sendedTo);
                        document.getReference().set(hashMapIndex);
                    }
                }
            }
        });

    }

    public void changeIndexes(String chatRefId,String oldIndex,String user){

        Integer untilIndex = Integer.valueOf(oldIndex);

        CollectionReference chatsRef = reference.collection(Keys.KEY_CHATS);

        chatsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    String rdo = null;
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        if (!queryDocumentSnapshot.getId().equals(chatRefId)) {
                            if (queryDocumentSnapshot.get(Keys.KEY_CHATS_SENDER).equals(user) || queryDocumentSnapshot.get(Keys.KEY_CHATS_RECEIVER).equals(user)) {
                                getIndexDB(chatsRef.document(queryDocumentSnapshot.getId()).collection(Keys.KEY_INDEX_COLLECTION).document(user), new ResultListener<String>() {
                                    @Override
                                    public void finish(String result) {
                                        Integer index = Integer.valueOf(result);
                                        if (untilIndex != 0) {
                                            if (index < untilIndex) {
                                                String newIndex = String.valueOf((index + 1));
                                                queryDocumentSnapshot.getReference().collection(Keys.KEY_INDEX_COLLECTION).document(user).update(Keys.KEY_INDEX, newIndex);
                                            }
                                        }else {
                                            String newIndex = String.valueOf((index + 1));
                                            queryDocumentSnapshot.getReference().collection(Keys.KEY_INDEX_COLLECTION).document(user).update(Keys.KEY_INDEX, newIndex);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        });
    }

    public void getIndexDB(DocumentReference docRef,final ResultListener<String> resultListener){
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        resultListener.finish(document.get(Keys.KEY_INDEX).toString());
                    } else {
                        resultListener.finish(null);
                    }
                }else {
                    resultListener.finish(null);
                }
            }
        });
    }

    //Enviar notificacion
    private void sendNotification(String receiver, String username, String message) {

        reference.collection(Keys.KEY_USERS).document(receiver).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User userReceivingMsg = document.toObject(User.class);
                        String token = userReceivingMsg.getToken();
                        Data data = new Data(fuser.getUid(),R.drawable.ic_stat_name,getString(R.string.new_msg),username+": "+message,userid);
                        Sender sender = new Sender(data,token);
                        apiService.sendNotification(sender)
                                .enqueue(new Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                        if (response.code() == 200){
                                            if (response.body().success != 1){
                                                Toast.makeText(getApplicationContext(), getString(R.string.error_msg_push), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<MyResponse> call, Throwable t) {

                                    }
                                });
                    }
                }
            }
        });

    }

    //Obtener id del chat
    public void getChatDB(final String sender, final String receiver, final ResultListener<String> resultListener){

        CollectionReference chatsRef = reference.collection(Keys.KEY_CHATS);

        chatsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().size()>0) {
                        String rdo = null;
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.get(Keys.KEY_CHATS_SENDER).equals(sender) && queryDocumentSnapshot.get(Keys.KEY_CHATS_RECEIVER).equals(receiver) ||
                                    queryDocumentSnapshot.get(Keys.KEY_CHATS_SENDER).equals(receiver) && queryDocumentSnapshot.get(Keys.KEY_CHATS_RECEIVER).equals(sender)) {
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

    //Obtener la lista de mensajes
    public void getMessageList(CollectionReference msgRef, final ResultListener<List<Message>> resultListener){
        final List<Message> msgList = new ArrayList<>();

        msgRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                msgList.clear();
                for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()) {
                    Message msg = queryDocumentSnapshot.toObject(Message.class);
                    msgList.add(msg);
                }
                resultListener.finish(msgList);
            }
        });
    }

    //Leer mensajes
    private void readMessage(final String myid, final String userid, final String imageurl){
        mMsg = new ArrayList<>();

        final CollectionReference chatRef = reference.collection(Keys.KEY_CHATS);

        messageAdapter = new MessageAdapter(MessageActivity.this,mMsg,imageurl);


        getChatDB(myid, userid, new ResultListener<String>() {
            @Override
            public void finish(String result) {
                mMsg.clear();
                if (result != null) {
                    CollectionReference msgRef = chatRef.document(result).collection(Keys.KEY_MESSAGES);
                    msgRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                Message msg = dc.getDocument().toObject(Message.class);
                                switch (dc.getType()) {
                                    case ADDED:
                                        if (mMsg.size() > 0) {
                                            if (!mMsg.get(mMsg.size() - 1).getId().equals(msg.getId())) {
                                                mMsg.add(msg);
                                            }
                                        } else {
                                            mMsg.add(msg);
                                        }
                                        break;
                                    case MODIFIED:
                                        //Change database so the message in the chat appears as seen instead of delivered
                                        for (Message modifMsg : mMsg) {
                                            if (modifMsg.getId().equals(msg.getId())){
                                                mMsg.set(mMsg.indexOf(modifMsg),msg);
                                            }
                                        }
                                        break;
                                    case REMOVED:
                                        break;
                                }
                            }

                            sortArray(mMsg);
                        }
                    });
                }
            }
        });

    }

    private void sortArray(List<Message> msgs){
        Collections.sort(msgs, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o1.getId() - o2.getId();
            }
        });

        messageAdapter.setmChat(msgs);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.smoothScrollToPosition(msgs.size());
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentuser",userid);
        editor.apply();
    }

    private void status(String status){
        DocumentReference userRef = reference.collection(Keys.KEY_USERS).document(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Keys.KEY_USERS_STATUS,status);

        userRef.update(hashMap);

    }

    private static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_1);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        seenMessage(userid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status(getString(R.string.status_on));
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (seenListener!=null) {
            seenListener.remove();
        }
        status(getString(R.string.status_off));
        currentUser("none");
    }
}
