package uk.co.univents.univents;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupInvites extends AppCompatActivity {



    private static final String TAG = GroupInvites.class.getName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private FirebaseDatabase mDatabase;
    private ListView listView ;
    private ArrayAdapter<String> adapter;
    private List<String> listViewItems;
    private ArrayList<String> groupIDperIndex;
    private ArrayList<String> groupKeyperIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_invites);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();

        listView = (ListView) findViewById(R.id.groupInvitesList);
        listViewItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listViewItems);

        groupIDperIndex = new ArrayList<>();
        groupKeyperIndex = new ArrayList<>();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        DatabaseReference groupReference = mDatabase.getReference("groupInvites/" + user.getUid());
        groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listViewItems.clear();
                ArrayList<String> arrayOfGroupIDs = new ArrayList<String>();

                if(dataSnapshot.getValue() == null){
                    listViewItems.add("You have no Invitations to Groups");
                    adapter.notifyDataSetChanged();
                }
                else{
                    for(DataSnapshot groupInvite : dataSnapshot.getChildren()) {
                        final String groupID = (String) groupInvite.getValue();
                        final String groupKey = (String) groupInvite.getKey();

                        DatabaseReference groupReference = mDatabase.getReference("groups/" + groupID);
                        groupReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Group thisGroup = dataSnapshot.getValue(Group.class);
                                listViewItems.add(thisGroup.toString());
                                groupIDperIndex.add(groupID);
                                groupKeyperIndex.add(groupKey);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                String eventData = (String) listView.getItemAtPosition(position);
                if(!(eventData.equalsIgnoreCase("You have no Invitations to Groups"))){

                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Group Invitation")
                            .setMessage("Do you want to accept or reject??")
                            .setPositiveButton(R.string.eventInviteAccept, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    acceptInvitation(position);
                                }})
                            .setNegativeButton(R.string.eventInviteReject, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    rejectInvitation(position);
                                }})
                            .show();
                }
            }
        });
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


    private void rejectInvitation(int indexOfView){
        listViewItems.remove(indexOfView);
        removeInvitation(indexOfView);
        adapter.notifyDataSetChanged();
    }

    private void removeInvitation(int indexOfEvent){
        final String groupKey = groupKeyperIndex.get(indexOfEvent);

        final DatabaseReference groupInvitationReference = mDatabase.getReference("groupInvites/" + user.getUid());
        groupInvitationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupInvitationReference.child(groupKey).removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void acceptInvitation(int indexOfView){
        listViewItems.remove(indexOfView);
        final String newGroupID = groupIDperIndex.get(indexOfView);

        final DatabaseReference newGroupForUser = mDatabase.getReference("users/" + user.getUid() + "/groups");
        newGroupForUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int newGroupNumber;
                if(dataSnapshot.getValue() == null){
                    newGroupNumber = 1;
                }
                else{
                    newGroupNumber = ((int) dataSnapshot.getChildrenCount()) + 1;
                }
                String newGroupKey = "g" + newGroupNumber;
                newGroupForUser.child(newGroupKey).setValue(newGroupID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final DatabaseReference refForNewGroupMember = mDatabase.getReference("groups/" + newGroupID + "/members");
        refForNewGroupMember.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int newGroupMemberNumber;
                if(dataSnapshot.getValue() == null){
                    newGroupMemberNumber = 1;
                }
                else{
                    newGroupMemberNumber = ((int) dataSnapshot.getChildrenCount()) + 1;
                }
                String newGroupKey = "m" + newGroupMemberNumber;
                refForNewGroupMember.child(newGroupKey).setValue(user.getUid());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        removeInvitation(indexOfView);
        adapter.notifyDataSetChanged();
    }

}
