package com.applications.toms.chatfirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applications.toms.chatfirestore.util.Keys;
import com.applications.toms.chatfirestore.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private RelativeLayout registerContainer;
    private MaterialEditText username, email, password;

    private FirebaseAuth auth;
    private FirebaseFirestore reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.toolbar_title_register));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Componentes
        registerContainer = findViewById(R.id.registerContainer);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        Button btn_register = findViewById(R.id.btn_register);

        //Firebase Auth
        auth = FirebaseAuth.getInstance();

        //Al hacer click en el boton registrar verifica y llama al método para registrar
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                    Snackbar.make(registerContainer,getString(R.string.error_verification_empty),Snackbar.LENGTH_SHORT).show();
                }else if (txt_password.length() < 6){
                    Snackbar.make(registerContainer,getString(R.string.error_short_password),Snackbar.LENGTH_SHORT).show();
                }else {
                    register(txt_username,txt_email,txt_password);
                }

            }
        });

        //Esconde teclado una vez clickeado enter en la pass
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Util.hideKeyboard(RegisterActivity.this);
                    handled = true;
                }
                return handled;
            }
        });

    }

    //Método para realizar la registraciñon a través de firebase
    private void register (final String username, String email, String password){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String userId = firebaseUser.getUid();
                    reference = FirebaseFirestore.getInstance();

                    final Map<String, String> user = new HashMap<>();
                    user.put(Keys.KEY_USERS_ID, userId);
                    user.put(Keys.KEY_USERS_USERNAME, username);
                    user.put(Keys.KEY_USERS_IMAGEURL, getString(R.string.image_default));
                    user.put(Keys.KEY_USERS_STATUS, getString(R.string.status_off));
                    user.put(Keys.KEY_USERS_SEARCH, username.toLowerCase());

                    final DocumentReference userRef = reference
                            .collection(Keys.KEY_USERS)
                            .document(userId);

                    userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (!documentSnapshot.exists()) {
                               //Usuario No Existe Creando...
                                userRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }else {
                                Snackbar.make(registerContainer,getString(R.string.error_email_already_registered),Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    Snackbar.make(registerContainer,getString(R.string.error_register),Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

}
