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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventInvites extends AppCompatActivity {

    private static final String TAG = Home.class.getName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private FirebaseDatabase mDatabase;
    private HashMap<String, HashMap<String, Event>> listOfInvitesPerDay;
    private HashMap<String, String> eventDataToUniqueIDMap;
    private HashMap<String, String> eventDataToDay;
    private ListView listView ;
    private ArrayAdapter<String> adapter;
    private List<String> listViewItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_invites);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        listOfInvitesPerDay = new HashMap<>();
        eventDataToUniqueIDMap = new HashMap<>();
        eventDataToDay = new HashMap<>();
        listView = (ListView) findViewById(R.id.eventInvitesList);
        listViewItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listViewItems);

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

        DatabaseReference eventReference = mDatabase.getReference("eventInvites/" + user.getUid());
        eventReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) listViewItems.add("You have no Invitations to Events");
                else {
                    listViewItems.clear();
                    for(DataSnapshot day : dataSnapshot.getChildren()){
                        String dayKey = day.getKey();

                        HashMap<String, Event> dayEvents = new HashMap<>();

                        for(DataSnapshot event : day.getChildren()){
                            String eventKey = event.getKey();
                            Event eventData = event.getValue(Event.class);
                            dayEvents.put(eventKey, eventData);
                            String eventDataWithDay = "Day: " + dayKey + "\n" + eventData.toCustomString();
                            eventDataToUniqueIDMap.put(eventDataWithDay, eventKey);
                            eventDataToDay.put(eventDataWithDay, dayKey);
                            listViewItems.add(eventDataWithDay);
                        }
                        listOfInvitesPerDay.put(dayKey, dayEvents);
                    }
                }
                adapter.notifyDataSetChanged();
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
                if(!(eventData.equalsIgnoreCase("You have no Invitations to Events"))){

                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Event Invitation")
                            .setMessage("Do you want to accept or reject??")
                            .setPositiveButton(R.string.eventInviteAccept, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    acceptInvite(position);
                                }})
                            .setNegativeButton(R.string.eventInviteReject, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    rejectInvite(position);
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

    public void rejectInvite(int indexOfView){
        String eventData = (String) listView.getItemAtPosition(indexOfView);
        String eventID = eventDataToUniqueIDMap.get(eventData);
        String day = eventDataToDay.get(eventData);
        String ownerID = listOfInvitesPerDay.get(day).get(eventID).getOwner();

        //Remove event invitation
        String referenceToInvitation = "eventInvites/" + user.getUid() + "/" + day + "/" + eventID;
        mDatabase.getReference(referenceToInvitation).removeValue();

        //Remove participant from owner event
        final DatabaseReference eventReference = mDatabase.getReference("timetable/" + ownerID + "/" + day + "/" + eventID + "/" + "invitees");
        eventReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot inviteePair : dataSnapshot.getChildren()){
                    String inviteeNumber = inviteePair.getKey();
                    String inviteeID = (String) inviteePair.getValue();
                    if(inviteeID.equals(user.getUid())){
                        eventReference.child(inviteeNumber).setValue("empty");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        listViewItems.remove(indexOfView);
        adapter.notifyDataSetChanged();
    }

    public void acceptInvite(int indexOfView){
        String eventData = (String) listView.getItemAtPosition(indexOfView);
        String eventID = eventDataToUniqueIDMap.get(eventData);
        String day = eventDataToDay.get(eventData);

        //Remove event invitation
        String referenceToInvitation = "eventInvites/" + user.getUid() + "/" + day + "/" + eventID;
        mDatabase.getReference(referenceToInvitation).removeValue();

        //Add Event to Timetable
        String referenceToTimetable = "timetable/" + user.getUid() + "/" + day + "/" + eventID;
        mDatabase.getReference(referenceToTimetable).setValue(listOfInvitesPerDay.get(day).get(eventID));

        listViewItems.remove(indexOfView);
        adapter.notifyDataSetChanged();
    }


}
