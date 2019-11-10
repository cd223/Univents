package uk.co.univents.univents;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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


public class SignUp extends AppCompatActivity {
    private static final String TAG = Login.class.getName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();

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
        EditText nameField = (EditText) findViewById(R.id.signUpName);

        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String username = nameField.getText().toString();

        if(!(isValidEmailAddress(email) && isValidPassword(password) && isValidUsername(username))){
            if(!isValidEmailAddress(email)){
                Toast.makeText(SignUp.this,"Invalid Email Address",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if(!isValidPassword(password)){
                Toast.makeText(SignUp.this,"Password Not Secure",
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, "PASSWORDINSECURE IS " +  password);
                return;
            }
            if(!isValidUsername(username)){
                Toast.makeText(SignUp.this,"Invalid Username",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUp.this,"Sign Up Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                            Toast.makeText(SignUp.this,"Sign Up Successful",
                                    Toast.LENGTH_SHORT).show();
                            addUsernameToUserInfo();
                            moveOn();

                        }
                    }
                });
    }

    private void addUsernameToUserInfo(){
        EditText nameField = (EditText) findViewById(R.id.signUpName);
        String nameToUpdate = nameField.getText().toString();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nameToUpdate).build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                        else{
                            Log.d(TAG, "Failed, reason is " + task.getException());
                        }
                    }
                });

    }

    private void moveOn() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    private boolean isValidPassword(String password) {
        return (password.length() > 6);
    }

    private boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    private boolean isValidUsername(String username){
        return(username.length() >= 5);
    }

}
