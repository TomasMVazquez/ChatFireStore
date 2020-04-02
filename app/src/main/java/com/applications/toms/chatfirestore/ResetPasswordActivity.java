package com.applications.toms.chatfirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText send_email;
    private Button btn_reset;
    private LinearLayout cointainer_reset;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.toolbar_title_reset_pass));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Componentes
        cointainer_reset = findViewById(R.id.cointainer_reset);
        send_email = findViewById(R.id.send_email);
        btn_reset = findViewById(R.id.btn_reset);

        //Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        //Al hacer click en el boton verifica y
        // envía mail para el reseteo de la pass
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = send_email.getText().toString();

                if (email.equals("")){
                    Snackbar.make(cointainer_reset,getString(R.string.error_verification_empty),Snackbar.LENGTH_SHORT).show();
                }else {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Snackbar.make(cointainer_reset, getString(R.string.error_email), Snackbar.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                            }else {
                                String error = task.getException().getMessage();
                                Snackbar.make(cointainer_reset, error, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
}
