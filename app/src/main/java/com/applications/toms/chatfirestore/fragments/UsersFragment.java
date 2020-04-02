package com.applications.toms.chatfirestore.fragments;


import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.applications.toms.chatfirestore.R;
import com.applications.toms.chatfirestore.adapter.UserAdapter;
import com.applications.toms.chatfirestore.model.User;
import com.applications.toms.chatfirestore.util.Keys;
import com.applications.toms.chatfirestore.util.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    private UserAdapter userAdapter;
    private List<User> mUser;

    //Componente
    private EditText search_users;

    //Constructor
    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        mUser = new ArrayList<>();

        //Componentes
        search_users = view.findViewById(R.id.search_users);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(getContext(),mUser, false);
        recyclerView.setAdapter(userAdapter);

        //Método para buscar todos los usuarios de la app
        readUsers();

        //Búsqueda de usuarios por nombre
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Esconder el teclado al enter
        search_users.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Util.hideKeyboard(getActivity());
                    handled = true;
                }
                return handled;

            }
        });

        return view;
    }

    //Métodos
    private void searchUsers(String s) {
        //Buscar usuario en la base de datos
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseFirestore.getInstance().collection(Keys.KEY_USERS)
                .orderBy(Keys.KEY_USERS_SEARCH)
                .startAt(s)
                .endAt(s+"\uf0ff");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                mUser.clear();
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    User user = snapshot.toObject(User.class);

                    assert fuser != null;
                    if (!user.getId().equals(fuser.getUid())) {
                        mUser.add(user);
                    }
                }
                userAdapter.setmUsers(mUser);

            }
        });

    }

    private void readUsers(){

        //Buscar todos los usuarios de la app en la DB
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore reference = FirebaseFirestore.getInstance();

        CollectionReference listUserRef = reference.collection(Keys.KEY_USERS);
        listUserRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (search_users.getText().toString().equals("")) {
                    mUser.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                        User user = queryDocumentSnapshot.toObject(User.class);
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            mUser.add(user);
                        }
                    }

                    userAdapter.setmUsers(mUser);
                }
            }
        });

    }

}
