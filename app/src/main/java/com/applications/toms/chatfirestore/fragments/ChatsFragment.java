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
import com.applications.toms.chatfirestore.model.Indexs;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "TOM-ChatsFragment";
    public static final String NAME = "fragment_title_chats";

    //Componentes
    private RecyclerView recyclerView;

    //Atributos
    private static UserAdapter userAdapter;
    private List<User> mUsers;

    private FirebaseUser fuser;
    private FirebaseFirestore reference;

    private List<String> userList = new ArrayList<>();

    private List<Indexs> indexsList = new ArrayList<>();
    private List<Indexs> list = new ArrayList<>();

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
                        if (!containsIndexChat(indexsList,dc.getDocument().getId())) {
                            Indexs indexs = new Indexs(dc.getDocument().getId(),chat.getReceiver());
                            indexsList.add(indexs);
                        }
                    }
                    if (chat.getReceiver().equals(fuser.getUid())) {
                        //Guardar con quien se tiene un chat
                        userList.add(chat.getSender());
                        if (!containsIndexChat(indexsList,dc.getDocument().getId())) {
                            Indexs indexs = new Indexs(dc.getDocument().getId(),chat.getSender());
                            indexsList.add(indexs);
                        }
                    }

                }
                
                //Buscar index de los chats
                getIndexChats(indexsList);
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

    private static boolean containsIndexChat(Collection<Indexs> c, String id) {
        for(Indexs o : c) {
            if(o != null && o.getIdChat().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void sortArrayIndex(List<Indexs> chats){
        Collections.sort(chats, new Comparator<Indexs>() {
            @Override
            public int compare(Indexs o1, Indexs o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });
    }

    private void getIndexChats(List<Indexs> indexs){
        list.clear();
        for (Indexs i:indexs) {
            reference.collection(Keys.KEY_CHATS).document(i.getIdChat()).collection(Keys.KEY_INDEX_COLLECTION).document(fuser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Integer index = Integer.valueOf(document.get(Keys.KEY_INDEX).toString());
                            for (Indexs newInd:indexs) {
                                if (newInd.getIdChat().equals(document.getReference().getParent().getParent().getId())){
                                    newInd.setIndex(index);
                                    list.add(newInd);
                                }
                            }
                            if (list.size() == indexs.size()){
                                sortArrayIndex(list);
                                //Llamar al método leer chats
                                // para buscar los usuarios con los que se está hablando
                                // y llenar el recyclerview
                                readChats();
                            }
                        }
                    }
                }
            });
        }
    }

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

                //Arreglamos los usuarios en orden
                List<User> sortedList = new ArrayList<>();
                for (Indexs one:list) {
                    for (User oneUser:mUsers) {
                        if (oneUser.getId().equals(one.getSentedTo())){
                            sortedList.add(oneUser);
                        }
                    }
                }

                userAdapter = new UserAdapter(getContext(),sortedList,true);
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
