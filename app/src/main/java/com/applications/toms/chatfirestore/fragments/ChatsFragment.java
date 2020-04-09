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
import android.widget.Toast;

import com.applications.toms.chatfirestore.R;
import com.applications.toms.chatfirestore.adapter.UserAdapter;
import com.applications.toms.chatfirestore.model.Chat;
import com.applications.toms.chatfirestore.model.User;
import com.applications.toms.chatfirestore.util.Keys;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "TOM-ChatsFragment";

    //Componentes
    private RecyclerView recyclerView;

    //Atributos
    private static UserAdapter userAdapter;
    private List<User> mUsers;

    private FirebaseUser fuser;
    private FirebaseFirestore reference;

    private List<String> userList = new ArrayList<>();

    //Constructor
    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        //Componentes
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //Firebase
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseFirestore.getInstance();

        //Buscar chats del usuario en la base de datos
        reference.collection(Keys.KEY_CHATS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    Chat chat = dc.getDocument().toObject(Chat.class);

                    if (chat.getSender().equals(fuser.getUid())) {
                        //Guardar con quien se tiene un chat
                        userList.add(chat.getReceiver());
                    }
                    if (chat.getReceiver().equals(fuser.getUid())) {
                        //Guardar con quien se tiene un chat
                        userList.add(chat.getSender());
                    }

                }

                //Llamar al método leer chats
                // para buscar los usuarios con los que se está hablando
                // y llenar el recyclerview
                readChats();
            }
        });

        //Buscar Token y guardarlo para las notificaciones
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        Log.d(TAG, "onComplete: token is " + token);
                        //LLamar al método para actualizar el token en la base de datos
                        updateTokenDB(token);
                    }
                });

        return view;
    }

    //Métodos

    private void updateTokenDB(String token){
        if (fuser != null) {
            DocumentReference userRef = reference.collection(Keys.KEY_USERS).document(fuser.getUid());
            userRef.update(Keys.KEY_USERS_TOKEN, token);
        }
    }

    private void readChats(){
        mUsers = new ArrayList<>();
        //Buscar a los usuarios de los chats en la base de datos
        reference.collection(Keys.KEY_USERS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @androidx.annotation.Nullable FirebaseFirestoreException e) {

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    User user = dc.getDocument().toObject(User.class);

                    switch (dc.getType()) {
                        case ADDED:
                            if (userList.contains(user.getId())) {
                                if (mUsers.size() != 0) {
                                    if (!containsUser(mUsers,user.getId())){
                                        mUsers.add(user);
                                    }
                                } else {
                                    mUsers.add(user);
                                }
                            }
                            break;
                        case MODIFIED:
                            //Para los cambios de estado online/offline de los usuarios
                            if (!user.getId().equals(fuser.getUid())){
                                for (User u : mUsers) {
                                    if (user.getId().equals(u.getId())) {
                                        mUsers.add(mUsers.indexOf(u),user);
                                        mUsers.remove(u);
                                        break;
                                    }
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

    public static boolean containsUser(Collection<User> c, String id) {
        for(User o : c) {
            if(o != null && o.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    //Método para mover el item del recycler según con quien hablé último
    public static void refresh(String userId){
        Log.d(TAG, "refresh: " + userId);
        userAdapter.moveChat(userId);
    }

}
