package com.mdeveloper.diegoalves.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText mNameField;
    private EditText mEmail;
    private EditText mPassword;
    private Button mButton;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDBReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDBReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mNameField = (EditText) findViewById(R.id.fieldNameReg);
        mEmail = (EditText) findViewById(R.id.fieldEmailReg);
        mPassword = (EditText) findViewById(R.id.fieldPasswordReg);
        mButton = (Button) findViewById(R.id.btnRegister);
        mProgressDialog = new ProgressDialog(this);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegister();
            }
        });

    }

    private void startRegister() {
        final String name = mNameField.getText().toString().trim();
        final String email = mEmail.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mProgressDialog.setMessage("Cadastrando...");
            mProgressDialog.show();
            Log.e("USER", email + " " + password);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        String userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference dbRef = mDBReference.child(userId);
                        dbRef.child("name").setValue(name);
                        dbRef.child("image").setValue("default");

                        mProgressDialog.dismiss();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }

            });

        } else {
            mProgressDialog.dismiss();
        }

    }

}
