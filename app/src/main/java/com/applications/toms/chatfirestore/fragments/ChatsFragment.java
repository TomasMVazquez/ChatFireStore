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

        userAdapter = new UserAdapter(getContext(),new ArrayList<User>());
        recyclerView.setAdapter(userAdapter);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseFirestore.getInstance();


        reference.collection("Chats").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Chat chat = dc.getDocument().toObject(Chat.class);

                            if (chat.getSender().equals(fuser.getUid())) {
                                if (!userList.contains(chat.getReceiver())) {
                                    userList.add(chat.getReceiver());
                                }
                            }
                            if (chat.getReceiver().equals(fuser.getUid())) {
                                if (!userList.contains(chat.getSender())) {
                                    userList.add(chat.getSender());
                                }
                            }
                            break;
                        case MODIFIED:

                            break;
                        case REMOVED:

                            break;
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

        for (String id:userList) {
            reference.collection("Users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User user = documentSnapshot.toObject(User.class);
                    mUsers.add(user);
                    Log.d(TAG, "onSuccess: mUser:" + mUsers);
                    userAdapter.setmUsers(mUsers);
                }
            });
        }

    }

}
