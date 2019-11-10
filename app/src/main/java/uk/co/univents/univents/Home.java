package uk.co.univents.univents;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {

    private static final String TAG = Home.class.getName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                    updateSignedInAs();
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void updateSignedInAs() {
        TextView updateSignedinAs = (TextView) findViewById (R.id.homeYouSignedInAs);
        updateSignedinAs.setText("You are signed in as " + user.getDisplayName());
    }

    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, Welcome.class);
        startActivity(intent);
    }

    public void goToInvites(View view) {
        Intent intent = new Intent(this, Invites.class);
        startActivity(intent);
    }

    public void goToAddFriend(View view) {
        Intent intent = new Intent(this, FriendList.class);
        startActivity(intent);
    }

    public void goToCreateEvent(View view) {
        Intent intent = new Intent(this, EventCreation.class);
        startActivity(intent);
    }
}
