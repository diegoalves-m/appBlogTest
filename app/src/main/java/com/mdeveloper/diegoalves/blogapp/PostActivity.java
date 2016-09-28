package com.mdeveloper.diegoalves.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private ImageButton imageButton;
    private EditText etTitle, etContent;
    private Button buttonSubmit;
    private ProgressDialog progressDialog;
    private Uri imageUri = null;

    private static int GALLERY_REQUEST = 1;

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseUser;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        etTitle = (EditText) findViewById(R.id.editTextTitle);
        etContent = (EditText) findViewById(R.id.editTextContent);
        buttonSubmit = (Button) findViewById(R.id.buttonPost);
        progressDialog = new ProgressDialog(this);

        imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galley = new Intent(Intent.ACTION_GET_CONTENT);
                galley.setType("image/*");
                startActivityForResult(galley, GALLERY_REQUEST);
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPost();
            }
        });
    }

    private void startPost() {

        progressDialog.setMessage("Posting in server");
        progressDialog.show();

        final String titleValue = etTitle.getText().toString().trim();
        final String contentPost = etContent.getText().toString();

        if(!TextUtils.isEmpty(titleValue) && !TextUtils.isEmpty(contentPost) && imageUri != null) {

            StorageReference ref = mStorageReference.child("Blog_images").child(imageUri.getLastPathSegment());

            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUri = taskSnapshot.getDownloadUrl();

                    final DatabaseReference newPost = mDatabaseReference.push();

                    mDatabaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPost.child("title").setValue(titleValue);
                            newPost.child("content").setValue(contentPost);
                            newPost.child("image").setValue(downloadUri.toString());
                            newPost.child("uid").setValue(mCurrentUser.getUid());
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        finish();
                                    } else {
                                        Toast.makeText(PostActivity.this, "Erro ao postar", Toast.LENGTH_SHORT);
                                    }
                                }

                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    progressDialog.dismiss();
                    //startActivity(new Intent(PostActivity.this, MainActivity.class));

                }
            });

        } else {
            Toast.makeText(this, "Erro ao postar", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            imageUri = data.getData();
            imageButton.setImageURI(imageUri);

        }

    }
}
