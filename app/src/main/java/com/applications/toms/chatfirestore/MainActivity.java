package com.applications.toms.chatfirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.applications.toms.chatfirestore.adapter.MyViewPagerAdapter;
import com.applications.toms.chatfirestore.fragments.ChatsFragment;
import com.applications.toms.chatfirestore.fragments.ProfileFragment;
import com.applications.toms.chatfirestore.fragments.UsersFragment;
import com.applications.toms.chatfirestore.model.Chat;
import com.applications.toms.chatfirestore.model.Message;
import com.applications.toms.chatfirestore.model.User;
import com.applications.toms.chatfirestore.util.Keys;
import com.applications.toms.chatfirestore.util.ResultListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TOM-MainActivity";

    //Componentes
    private CircleImageView profile_image;
    private TextView username;

    List<String> chatsList = new ArrayList<>();

    //Firebase Componentes
    private FirebaseUser firebaseUser;
    private FirebaseFirestore reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        //Componentes
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        //Firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseFirestore.getInstance();

        //Buscar nombre y avatar del usuario para pegar en el Toolbar
        DocumentReference userRef = reference.collection(Keys.KEY_USERS).document(firebaseUser.getUid());
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    User user = documentSnapshot.toObject(User.class);
                    username.setText(user.getUsername());
                    if (user.getImageURL().equals(getString(R.string.image_default))){
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    }else {
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                    }
                }
            }
        });


        //TabLayout y ViewPager
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);
        //Asociar al view pager
        tabLayout.setupWithViewPager(viewPager);
        /*
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(new ChatsFragment(),getString(R.string.fragment_title_chats));
        viewPagerAdapter.addFragment(new UsersFragment(),getString(R.string.fragment_title_users));
        viewPagerAdapter.addFragment(new ProfileFragment(),getString(R.string.fragment_title_profile));

        viewPager.setAdapter(viewPagerAdapter);
        */

        //Lista de Fragments
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new ChatsFragment());
        fragmentList.add(new UsersFragment());
        fragmentList.add(new ProfileFragment());

        //Titulos del tab
        List<String> titulos = new ArrayList<>();
        titulos.add(getString(R.string.fragment_title_chats));
        titulos.add(getString(R.string.fragment_title_users));
        titulos.add(getString(R.string.fragment_title_profile));


        //Adapter
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(getSupportFragmentManager(),fragmentList,titulos);
        viewPager.setAdapter(adapter);

        //Inicializado
        viewPager.setCurrentItem(0);

        chatList(new ResultListener<List<String>>() {
            @Override
            public void finish(List<String> result) {
                final int[] unreadMsgs = {0};
                for (String id:result) {
                    reference.collection(Keys.KEY_CHATS).document(id).collection(Keys.KEY_MESSAGES).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots.getDocuments().size() > 0) {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                                    Message msg = doc.getDocument().toObject(Message.class);
                                    if (!msg.isIsseen() && msg.getReceiver().equals(firebaseUser.getUid())){
                                        unreadMsgs[0]++;
                                        String chatTitle = titulos.get(0).split(" ")[0] + " (" + unreadMsgs[0] + ")";
                                        titulos.remove(0);
                                        titulos.add(0,chatTitle);
                                        adapter.setTitulos(titulos);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });

    }


    //Menú del Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //Boton del toolbar para desloguearse
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                finish();
                return true;
        }

        return false;
    }

    //Método para contar cantidad de mensajes sin leer y pasarlo al titulo del fragment
    private void chatList(ResultListener<List<String>> resultListener){
        reference.collection(Keys.KEY_CHATS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                for (QueryDocumentSnapshot snapshots: queryDocumentSnapshots) {
                    Chat chat = snapshots.toObject(Chat.class);
                    if (chat.getSender().equals(firebaseUser.getUid()) || chat.getReceiver().equals(firebaseUser.getUid())) {
                        if (!chatsList.contains(snapshots.getId())) {
                            chatsList.add(snapshots.getId());
                        }
                    }
                }

                resultListener.finish(chatsList);

            }
        });
    }

    //Método de cambio de estado en la base de datos
    private void status(String status){
        DocumentReference userRef = reference.collection(Keys.KEY_USERS).document(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Keys.KEY_USERS_STATUS,status);

        userRef.update(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Cambio del estado del usuario a online estando la app en primer plano
        status(getString(R.string.status_on));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Cambio del estado del usuario a offline estando la app en segundo plano
        status(getString(R.string.status_off));
    }

}
