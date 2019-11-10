package uk.co.univents.univents;

// import libraries
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

public class EventCreation extends AppCompatActivity {

    private static final String TAG = Home.class.getName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    EditText titleField, locationField, startDateField, startTimeField,
            endDateField, endTimeField, notesField, inviteesField, maxNumParticipantsField;
    CheckBox visibilityField;
    String title, location, startDate, startTime, endDate, endTime, notes, invitees,
            maxNumOfParticipants, isChecked;
    String duration;
    Map<String, String> participants;
    HashMap<String, String> username2uidMap;
    ArrayList<String> participantsArray = new ArrayList<String>();
    String[] participantsUIDArray;
    String[] friendID;
    Map<String, Object> usernameID;
    String[] list;
    ArrayAdapter<String> listAdapter;

    Button btnStartDatePicker, btnStartTimePicker, btnEndDatePicker, btnEndTimePicker;
    public int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        titleField = (EditText) findViewById(R.id.eventCreationTitle);
        locationField = (EditText) findViewById(R.id.EventLocation);
        notesField = (EditText) findViewById(R.id.EventNotes);
        maxNumParticipantsField = (EditText) findViewById(R.id.maxNumParticipantsField);
        visibilityField = (CheckBox) findViewById(R.id.visibility);
        visibilityField.setChecked(true);

        btnStartDatePicker=(Button)findViewById(R.id.btn_start_date);
        btnStartTimePicker = (Button) findViewById(R.id.btn_start_time);
        btnEndDatePicker =(Button)findViewById(R.id.btn_end_date);
        btnEndTimePicker = (Button) findViewById(R.id.btn_end_time);

        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
        SimpleDateFormat sdf2 = new SimpleDateFormat( "HH:mm" );

        endDateField = (EditText) findViewById(R.id.EventEndDate);
        endDateField.setFocusable(false);
        endDateField.setText( sdf.format( new Date() ));

        endTimeField = (EditText) findViewById(R.id.EventEndTime);
        endTimeField.setFocusable(false);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 1);
        endTimeField.setText(sdf2.format( cal.getTime() ));

        startDateField = (EditText) findViewById(R.id.EventStartDate);
        startDateField.setFocusable(false);
        startDateField.setText( sdf.format( new Date() ));

        startTimeField = (EditText) findViewById(R.id.EventStartTime);
        startTimeField.setFocusable(false);
        startTimeField.setText(sdf2.format( Calendar.getInstance().getTime() ));

        getFriendsList();
    }

    public void startDatePicker(View view){
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        startDateField.setText(year + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth));
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    public void startTimePicker(View view){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTimeField.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    public void endDatePicker(View view){
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        endDateField.setText(year + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth));
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    public void endTimePicker(View view){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTimeField.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    public void createNewEvent(View view) {
        if (isDataValid()) {
            pushToFirebase();
        }
    }

    public void selectFriend(View v){
        final TextView textSource = (TextView) v;

        if(!textSource.getText().toString().equals("No friends to display.")){
            if(textSource.getCurrentTextColor()==Color.GRAY) {
                textSource.setTextColor(Color.BLUE);
                addUserToList(textSource.getText().toString());
            }
            else{
                textSource.setTextColor(Color.GRAY);
                removeUserFromList(textSource.getText().toString());
            }
        }
    }

    private void removeUserFromList(String user) {
        participantsArray.remove(user);
    }

    private void addUserToList(String user) {
        participantsArray.add(user);
    }

    public void pushToFirebase() {
        title = titleField.getText().toString();
        location = locationField.getText().toString();
        startDate = startDateField.getText().toString();
        startTime = startTimeField.getText().toString();
        endDate = endDateField.getText().toString();
        endTime = endTimeField.getText().toString();
        notes = notesField.getText().toString();
        maxNumOfParticipants= maxNumParticipantsField.getText().toString();
        participantsUIDArray = new String[participantsArray.size()];

        if(visibilityField.isChecked()){
            isChecked = "True";
        } else {
            isChecked= "False";
        }

        participants = new HashMap<String, String>();

        if(maxNumParticipantsField.equals("")){
            participants.put("max", "-1");
            pullUsernames();
        }
        else {
            try{
                int max = Integer.parseInt(maxNumOfParticipants);
                if(max<1){
                    Toast.makeText(EventCreation.this, "Max number of invitees needs to be 1 or more.", Toast.LENGTH_LONG);
                }
                else{
                    participants.put("max", maxNumOfParticipants);
                    pullUsernames();
                }
            }
            catch(NumberFormatException nfe) {
                Toast.makeText(EventCreation.this, "Max number of invitees needs to be an integer.", Toast.LENGTH_LONG);
            }
        }
    }

    public long getDateDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return TimeUnit.HOURS.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    public void pullUsernames() {
        DatabaseReference user2uidReference = database.getReference("username2uid");
        user2uidReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username2uidMap = (HashMap<String, String>) dataSnapshot.getValue();

                for (int i = 1; i <= Integer.parseInt(maxNumOfParticipants); i++) {
                    if (i <= participantsArray.size()) {
                        if (username2uidMap.isEmpty()) {
                            Toast.makeText(EventCreation.this, "No current users are stored.", Toast.LENGTH_LONG);
                        }
                        else {
                            participantsUIDArray[i - 1] = username2uidMap.get(participantsArray.get(i-1));
                            participants.put("p" + i, participantsUIDArray[i - 1]);
                        }
                    } else {
                        participants.put("p" + i, "empty");
                    }
                }

                DatabaseReference myRef = database.getReference("timetable/" + user.getUid() + "/" + startDate);
                Map<String, Object> eventData = new HashMap<String, Object>();
                eventData.put("activity", title);
                eventData.put("duration", duration);
                eventData.put("endTime", endTime);
                eventData.put("location", location);
                eventData.put("notes", notes);
                eventData.put("owner", user.getUid());
                eventData.put("startTime", startTime);
                eventData.put("invitees", participants);
                eventData.put("prvate", isChecked);
                DatabaseReference uniqueRef = myRef.push();
                uniqueRef.setValue(eventData);
                String newEventID = uniqueRef.getKey().toString();

                if (isChecked.equals("False")) {
                    DatabaseReference publicEventRef = database.getReference("publicEvents/" + startDate + "/" + newEventID);
                    publicEventRef.setValue(eventData);
                }

                for (int j = 0; j < participantsUIDArray.length; j++) {
                    if (participantsUIDArray[j] == null) continue;
                    DatabaseReference userInviteRef = database.getReference("eventInvites/" + participantsUIDArray[j] + "/" + startDate + "/" + newEventID);
                    userInviteRef.setValue(eventData);
                }

                Toast.makeText(EventCreation.this, "Event created!", Toast.LENGTH_LONG).show();
                EventCreation.super.onBackPressed();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public void getFriendsList(){
            DatabaseReference userRef = database.getReference("users/" + user.getUid() + "/friends");
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Object> temp = (HashMap<String, Object>)dataSnapshot.getValue();
                    if(temp != null) {
                        friendID = temp.values().toArray(new String[0]);
                        getArrayOfUsernames();
                    }
                    else{
                        getArrayOfUsernames();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
    }

    private void getArrayOfUsernames() {
        DatabaseReference usersRef = database.getReference("uid2username");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usernameID = (HashMap<String, Object>)dataSnapshot.getValue();

                if (friendID != null) {
                    list = new String[friendID.length];
                    for (int count = 0; count < friendID.length; count++) {
                        list[count] = usernameID.get(friendID[count]).toString();
                    }
                    populateList();
                }
                else{
                    list = new String[1];
                    list[0] = "No friends to display.";
                    populateList();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void populateList() {
        listAdapter = new ArrayAdapter<>(this, R.layout.simple_row_eventcreation, list);
        ListView mainListView = (ListView) findViewById(R.id.friend_select);
        mainListView.setAdapter(listAdapter);
    }

    public boolean isDateBeforeNow(String DateAndTime){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.setLenient(false);
            Date today = new Date();
            sdf.format(today);
            Date givenDate = sdf.parse(DateAndTime);

            if(givenDate.compareTo(today)<=0){ return true;}
            else{ return false; }
        }
        catch(ParseException ex){
            ex.printStackTrace();
        }
        return false;
    }

    public boolean isDateDifferenceValid(String DateAndTime1, String DateAndTime2){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.setLenient(false);
            Date startDate = sdf.parse(DateAndTime1);
            Date endDate = sdf.parse(DateAndTime2);

            if(startDate.compareTo(endDate)<0){ return false;}
            else{
                long hoursDiff = getDateDiff(startDate, endDate);
                duration = String.format("%02d", (hoursDiff)) + ":00";
                return true;
            }
        }
        catch(ParseException ex){
            ex.printStackTrace();
        }
        return false;
    }

    public void goToHome(View view) {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
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

    public boolean isDataValid(){
        /*if(!isDateBeforeNow(startDate + " " + startTime)){
            Toast.makeText(EventCreation.this, "Can't start event in past.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!isDateBeforeNow(endDate + " " + endTime)){
            Toast.makeText(EventCreation.this, "Can't end event in past.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!isDateDifferenceValid(startDate + " " + startTime, endDate + " " + endTime)) {
            Toast.makeText(EventCreation.this, "End date and time is before start date and time", Toast.LENGTH_LONG).show();
            return false;
        }*/
        if (titleField.getText().toString() == null || titleField.getText().toString().trim().equals("")) {
            Toast.makeText(EventCreation.this, "Title cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(locationField.getText().toString() == null || locationField.getText().toString().trim().equals("")) {
            Toast.makeText(EventCreation.this, "Location cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}