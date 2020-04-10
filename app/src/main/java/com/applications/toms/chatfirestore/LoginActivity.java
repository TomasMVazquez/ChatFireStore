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

import com.applications.toms.chatfirestore.util.LoadingDialog;
import com.applications.toms.chatfirestore.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {

    private RelativeLayout loginContainer;
    private MaterialEditText  email, password;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.toolbar_title_login));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Componentes
        loginContainer = findViewById(R.id.loginContainer);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        Button btn_login = findViewById(R.id.btn_login);
        TextView forgot_password = findViewById(R.id.forgot_password);

        //Firebase Auth
        auth = FirebaseAuth.getInstance();

        //Loading custom view para que sepa que esta cargando el login
        LoadingDialog loadingDialog = new LoadingDialog(LoginActivity.this);

        //Al hacer click en el boton Login realiza la verificación
        // y Genera la autenticaciñon con firebase email y pass
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Escondemos el teclado
                Util.hideKeyboard(LoginActivity.this);
                //Empezamos el loading
                loadingDialog.startLoadingDialog();

                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                    loadingDialog.endLoadingDialog();
                    Snackbar.make(loginContainer,getString(R.string.error_verification_empty),Snackbar.LENGTH_SHORT).show();
                }else {
                    auth.signInWithEmailAndPassword(txt_email,txt_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            loadingDialog.endLoadingDialog();

                            if (task.isSuccessful()){
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }else {
                                Snackbar.make(loginContainer,getString(R.string.error_authentication),Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        //Esconde el teclado cuando se presiona enter
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Util.hideKeyboard(LoginActivity.this);
                    handled = true;
                }
                return handled;
            }
        });

        //Botón para ir a la activity de restear pass
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });

    }
}
