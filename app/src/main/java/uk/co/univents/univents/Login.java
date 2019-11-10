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

public class Login extends AppCompatActivity {
    private static final String TAG = Login.class.getName();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                    moveOn();
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };


        EditText emailField = (EditText) findViewById(R.id.loginEmail);
        Editable forCursor = emailField.getText();
        Selection.setSelection(forCursor, 0);
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

    private void moveOn(){
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    public void signIn(View view){
        EditText emailField = (EditText) findViewById(R.id.loginEmail);
        EditText passwordField = (EditText) findViewById(R.id.loginPassword);

        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(task.getException() instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException){
                                Log.d("LoginAttemptFailed", "Invalid Credentials");
                                Toast.makeText(Login.this, "Invalid Credentials",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else if(task.getException() instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException){
                                Log.d("LoginAttemptFailed", "User Doesn't Exist");
                                Toast.makeText(Login.this, "No User Exists",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else{

                                Log.w(TAG, "signInWithEmailFailed", task.getException());
                            }
                        }
                        else {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                            Toast.makeText(Login.this, "Authentication Successful.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
