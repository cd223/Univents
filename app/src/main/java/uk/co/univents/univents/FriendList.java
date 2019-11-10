package uk.co.univents.univents;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class FriendList extends AppCompatActivity {

    private String friendID[];
    private Map<String, Object> usernameID;
    private ArrayAdapter<String> listAdapter;
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userRef = database.getReference("users/" + user.getUid() + "/friends");
        usersRef = database.getReference("uid2username");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usernameID = (HashMap<String, Object>)dataSnapshot.getValue();
                Log.e("FriendList", usernameID.toString());
                displayFriends();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> temp = (HashMap<String, Object>)dataSnapshot.getValue();
                if(temp != null) {
                    friendID = temp.values().toArray(new String[0]);
                    displayFriends();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });



    }
    private void displayFriends(){
        if(usernameID != null && friendID != null) {
            String[] list = new String[friendID.length];
            for (int count = 0; count < friendID.length; count++) {

                Log.e("FriendList", "" + friendID.length);
                list[count] = usernameID.get(friendID[count]).toString();

            }
            listAdapter = new ArrayAdapter<>(this, R.layout.simple_row, list);
            ListView mainListView = (ListView) findViewById(R.id.friend_list);
            mainListView.setAdapter(listAdapter);


        }


    }


    /* @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.menu_main, menu);
         return true;
     }
 */
    public void goToAddFriend(View viewIn){
        Intent intent = new Intent(this, FriendInvite.class);
        startActivity(intent);
    }

}
