package uk.co.univents.univents;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class SignUp extends AppCompatActivity {
    private static final String TAG = SignUp.class.getName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        EditText emailField = (EditText) findViewById(R.id.signUpEmail);
        Editable forCursor = emailField.getText();
        Selection.setSelection(forCursor, 0);
    }

    private void addUsername2UIDMapping(String username){

        String uid = user.getUid();

        DatabaseReference username2UIDkey = mDatabase.getReference("username2uid");
        username2UIDkey.child(username).setValue(uid);

        DatabaseReference uID2UsernameMapping = mDatabase.getReference("uid2username");
        uID2UsernameMapping.child(uid).setValue(username);
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

    public void signUpUser(View view){
        EditText passwordField = (EditText) findViewById(R.id.signUpPassword);
        EditText emailField = (EditText) findViewById(R.id.signUpEmail);

        final String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        final String username;
        int indexOfAt = email.indexOf('@');
        if(indexOfAt == -1 ) username = "";
        else username = email.substring(0, indexOfAt);

        if(!(isValidEmailAddress(email) && isValidPassword(password) && isValidUsername(username))){
            if(!isValidEmailAddress(email)){
                Toast.makeText(SignUp.this,"Need to be Bath Email",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if(!isValidPassword(password)){
                Toast.makeText(SignUp.this,"Password Length must be >6",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if(!isValidUsername(username)){
                Toast.makeText(SignUp.this,"Invalid Bath Username",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            if(task.getException() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException){
                                    Toast.makeText(SignUp.this,"User Exists Already",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(SignUp.this,"Sign Up Failed",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                        else {
                            Toast.makeText(SignUp.this,"Sign Up Successful",
                                    Toast.LENGTH_SHORT).show();
                            addUsernameToUserInfo(username);

                        }
                    }
                });
    }

    private void addUsernameToUserInfo(final String username){

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username).build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            addUsername2UIDMapping(username);
                            moveOn();
                        }
                        else{
                            Log.d(TAG, "Failed, reason is " + task.getException());
                        }
                    }
                });

    }

    private void moveOn() {
        Intent intent = new Intent(this, uploadTimetable.class);
        startActivity(intent);
    }

    private boolean isValidPassword(String password) {
        return (password.length() > 6);
    }

    private boolean isValidEmailAddress(String email) {
        int indexOfAt = email.indexOf('@');
        String bathDomain = email.substring(indexOfAt+1, email.length());
        Log.d("domainExtractedId ", bathDomain);
        return bathDomain.equalsIgnoreCase("bath.ac.uk");
    }

    private boolean isValidUsername(String username){
        return(username.length() >= 5);
    }

}
