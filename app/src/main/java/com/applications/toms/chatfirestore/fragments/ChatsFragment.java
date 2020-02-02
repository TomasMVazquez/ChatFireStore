package com.applications.toms.chatfirestore.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applications.toms.chatfirestore.R;
import com.applications.toms.chatfirestore.adapter.UserAdapter;
import com.applications.toms.chatfirestore.model.Chat;
import com.applications.toms.chatfirestore.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;

    FirebaseUser fuser;
    FirebaseFirestore reference;

    private List<String> userList = new ArrayList<>();


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseFirestore.getInstance();

        reference.collection("Chats").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    Chat chat = dc.getDocument().toObject(Chat.class);

                    if (chat.getSender().equals(fuser.getUid())) {
                        userList.add(chat.getReceiver());
                        Log.d(TAG, "onEvent: Added = " + chat.getId());
                    }
                    if (chat.getReceiver().equals(fuser.getUid())) {
                        userList.add(chat.getSender());
                        Log.d(TAG, "onEvent: Added = " + chat.getId());
                    }

                }

                readChats();
            }
        });


        return view;
    }


    private void readChats(){

        mUsers = new ArrayList<>();
        Log.d(TAG, "readChats: userList: " + userList);



        reference.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @androidx.annotation.Nullable FirebaseFirestoreException e) {

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    User user = dc.getDocument().toObject(User.class);

                    switch (dc.getType()) {
                        case ADDED:
                            if (userList.contains(user.getId())) {
                                if (mUsers.size() != 0) {
                                    for (User u : mUsers) {
                                        if (!user.getId().equals(u.getId())) {
                                            mUsers.add(user);
                                            Log.d(TAG, "onEvent: AGREGADO " + user.getUsername());
                                        }
                                    }
                                } else {
                                    mUsers.add(user);
                                    Log.d(TAG, "onEvent: AGREGADO " + user.getUsername());
                                }
                            }
                            break;
                        case MODIFIED:
                            for (User u : mUsers) {
                                if (user.getId().equals(u.getId())) {
                                    mUsers.add(mUsers.indexOf(u),user);
                                    mUsers.remove(u);
                                    Log.d(TAG, "onEvent: Midificado " + user.getUsername());
                                }
                            }
                            break;
                        case REMOVED:

                            break;
                    }

                }
                userAdapter = new UserAdapter(getContext(),mUsers,true);
                recyclerView.setAdapter(userAdapter);
            }
        });

    }

}
