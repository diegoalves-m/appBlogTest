package com.mdeveloper.diegoalves.blogapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mdeveloper.diegoalves.blogapp.model.Blog;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;
    private DatabaseReference mCurrentUser;

    private Query mQueryCurrentUser;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private boolean likePost = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");

        String currentUserId = mAuth.getCurrentUser().getUid();
        mCurrentUser = FirebaseDatabase.getInstance().getReference().child("Blog");

        // trocar no recycler view para vir apenas post do usuario
        mQueryCurrentUser = mCurrentUser.orderByChild("uid").equalTo(currentUserId);

        mDatabaseReference.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        mRecyclerView.setItemAnimator(itemAnimator);

        checkUserExist();

    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerAdapter<Blog, ViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Blog, ViewHolder>(
                        Blog.class,
                        R.layout.post_item,
                        ViewHolder.class,
                        mDatabaseReference
                ) {
                    @Override
                    protected void populateViewHolder(ViewHolder viewHolder, Blog model, int position) {

                        final String post_key = getRef(position).getKey();

                        viewHolder.tvTitle.setText(model.getTitle());
                        viewHolder.tvContent.setText(model.getContent());
                        Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imagePost);
                        viewHolder.tvUsername.setText(model.getUsername());
                        viewHolder.setLikeButton(post_key);

                        viewHolder.holderView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i("Post_key", post_key);
                                Intent intent = new Intent(MainActivity.this, BlogSingleActivity.class);
                                intent.putExtra("post_id", post_key);
                                startActivity(intent);
                            }
                        });

                        viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                likePost = true;

                                mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (likePost) {
                                            if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                                mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                likePost = false;
                                            } else {
                                                mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("randon");
                                                likePost = false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        });

                    }
                };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addPost) {
            startActivity(new Intent(this, PostActivity.class));
        } else if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }

    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null) {

            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imagePost;
        ImageButton likeButton;
        TextView tvTitle, tvContent, tvUsername;
        View holderView;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public ViewHolder(View itemView) {
            super(itemView);
            holderView = itemView;
            tvTitle = (TextView) itemView.findViewById(R.id.post_title);
            tvContent = (TextView) itemView.findViewById(R.id.post_content);
            imagePost = (ImageView) itemView.findViewById(R.id.post_image);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
            likeButton = (ImageButton) itemView.findViewById(R.id.likeBtn);
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);
        }

        public void setLikeButton(final String post_key) {

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                        likeButton.setImageResource(R.drawable.thumb_up_2);
                    } else {
                        likeButton.setImageResource(R.drawable.thumb_up_outline);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

}
