package uk.co.univents.univents;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;



public class FriendInvite extends AppCompatActivity {
    private String targetUsername;
    private String targetID = "";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    int temp2 = 0;
    private EditText txtTarget;
    private DatabaseReference inviteLoc;

    private Map<String, Object> usernameID;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference usertoIDRef;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_invite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarFInvite);
        toolbar.setTitle("Invite Friends");
        setSupportActionBar(toolbar);



        txtTarget = (EditText)findViewById(R.id.txt_targetUsername);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                targetUsername = txtTarget.getText().toString();
                sendFriendRequest();
            }
        });
    }

    private void sendFriendRequest(){

        usertoIDRef = database.getReference("username2uid");
        usertoIDRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usernameID = (HashMap<String, Object>)dataSnapshot.getValue();
                if(usernameID.get(targetUsername) == null){
                    Snackbar.make(findViewById(R.id.activity_friend_invite), "Couldn't find user ::" + targetUsername, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else{

                    //Cechk uid is not user.uid

                    targetID = usernameID.get(targetUsername).toString();
                    Map<String, String> temp = new HashMap<>();
                    temp.put("f" + getInviteNumber(targetID), user.getUid());
                    database.getReference().child("friendInvites/" + targetID).setValue(temp);
                    Snackbar.make(findViewById(R.id.activity_friend_invite), "Sent friend invite ::" + targetUsername, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                usertoIDRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





    }
    /*
        Gets current number of invites for a given user
     */
    private int getInviteNumber(String targetID){
        inviteLoc = database.getReference("friendInvites/" + targetID);
        temp2 = 0;
        inviteLoc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> temp = (HashMap<String, Object>)dataSnapshot.getValue();
                temp2 = ((HashMap<String, Object>) dataSnapshot.getValue()).size();
                inviteLoc.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return temp2;
    }

}
