package uk.co.univents.univents;

import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class FriendInvites extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase database;
    private DatabaseReference frndInvRef;
    private DatabaseReference nameFromUIDRef;
    private String[] inviteID;
    private String[] friendID;
    private HashMap<String, Object> iDUsername;
    private String[] inviteName;
    private boolean nameMapInit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_invites);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        frndInvRef = database.getReference("friendInvites/" + mUser.getUid());

        nameFromUIDRef = database.getReference("uid2username");

        frndInvRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> temp = (HashMap<String, Object>) dataSnapshot.getValue();

                inviteID = new String[temp.size()];

                for(int i=0; i< temp.size(); i++){
                    String toGet = "f" + String.valueOf(i);
                    inviteID[i] = temp.get(toGet).toString();
                }

                if(nameMapInit){displayInviteList(inviteID);}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        nameFromUIDRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                iDUsername = (HashMap<String, Object>) dataSnapshot.getValue();
                if(!nameMapInit){nameMapInit = true;}
                if(inviteID.length>0){ displayInviteList(inviteID); }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }

    public void displayInviteList(String[] invIDs){
        String[] invNames;

        if(invIDs.length == 1 && invIDs[0].equals("empty")){
            invNames = new String[1];
            invNames[0] = "You currently have no friend invites";
        }else{
            invNames = new String[invIDs.length-1];
            for(int i=1;i<invIDs.length;i++){
                invNames[i-1] = "     " + iDUsername.get(invIDs[i]).toString();
            }
        }

        ListView listView = (ListView) findViewById(R.id.friendInvitesList);

        if(invIDs.length == 1 && invIDs[0].equals("empty")) {
            ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_list_row, invNames);
            listView.setAdapter(adapter);
        }else {

            FriendListAdapter adapter = new FriendListAdapter(this, invNames);
            listView.setAdapter(adapter);
        }



    }
}
