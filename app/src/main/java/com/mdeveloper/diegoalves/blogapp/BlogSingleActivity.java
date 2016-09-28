package com.mdeveloper.diegoalves.blogapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BlogSingleActivity extends AppCompatActivity {

    private TextView postTitle;
    private TextView postContent;
    private ImageView imagePost;
    private Button removeBtn;
    private String post_key = null;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");

        postTitle = (TextView) findViewById(R.id.post_title_s);
        postContent = (TextView) findViewById(R.id.post_content_s);
        imagePost = (ImageView) findViewById(R.id.image_Post_s);
        removeBtn = (Button) findViewById(R.id.removeBtn_s);

        post_key = getIntent().getExtras().getString("post_id");

        mDatabaseReference.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = (String) dataSnapshot.child("title").getValue();
                String content = (String) dataSnapshot.child("content").getValue();
                String imgUrl = (String) dataSnapshot.child("image").getValue();
                String uidPost = (String) dataSnapshot.child("uid").getValue();

                postTitle.setText(title);
                postContent.setText(content);
                Picasso.with(BlogSingleActivity.this).load(imgUrl).into(imagePost);
                if(mAuth.getCurrentUser().getUid().equals(uidPost)) {
                    removeBtn.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReference.child(post_key).removeValue();
                finish();
            }
        });

    }
}
