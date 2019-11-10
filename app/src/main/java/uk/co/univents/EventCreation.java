package uk.co.univents.univents;

// import libraries
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventCreation extends AppCompatActivity {

    private static final String TAG = Home.class.getName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    int i, numOfDays;

    EditText titleField, locationField, startDateField, startTimeField,
            endDateField, endTimeField, notesField, inviteesField, maxNumParticipantsField;
    CheckBox visibilityField;
    String title, location, startDate, startTime, endDate, endTime, notes, invitees,
            maxNumOfParticipants, isChecked;
    String duration = "01:00";
    Boolean userFound = false;

    Map<String, String> participants;
    String[] participantsArray;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);
        mAuth = FirebaseAuth.getInstance();

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
        startDateField = (EditText) findViewById(R.id.EventStartDate);
        startTimeField = (EditText) findViewById(R.id.EventStartTime);
        endDateField = (EditText) findViewById(R.id.EventEndDate);
        endTimeField = (EditText) findViewById(R.id.EventEndTime);
        notesField = (EditText) findViewById(R.id.EventNotes);
        inviteesField = (EditText) findViewById(R.id.EventInvitees);
        maxNumParticipantsField = (EditText) findViewById(R.id.maxNumParticipantsField);
        visibilityField = (CheckBox) findViewById(R.id.visibility);
        visibilityField.setChecked(true);
    }

    public void createNewEvent(View view) {
        if (isDataValid()) {
            pushToFirebase();
        }
    }

    public void pushToFirebase() {
        title = titleField.getText().toString();
        location = locationField.getText().toString();
        startDate = startDateField.getText().toString();
        startTime = startTimeField.getText().toString();
        endDate = endDateField.getText().toString();
        endTime = endTimeField.getText().toString();
        notes = notesField.getText().toString();
        invitees = inviteesField.getText().toString();
        maxNumOfParticipants= maxNumParticipantsField.getText().toString();
        participantsArray = invitees.split(",");

        if(visibilityField.isChecked()){
            isChecked = "True";
        } else {
            isChecked= "False";
        }

        DatabaseReference user2uidReference = database.getReference("username2uid");
            participants = new HashMap<String, String>();
            participants.put("max", maxNumOfParticipants);

            for(i=1; i<=Integer.parseInt(maxNumOfParticipants); i++){
                if(i<=participantsArray.length){
                    user2uidReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot userStored : dataSnapshot.getChildren()){
                                if(userStored.getKey().equals(participantsArray[i-1]) && !participantsArray[i-1].equals(user.getDisplayName())) {
                                    participants.put("p" + i, userStored.getValue().toString());
                                    userFound = true;
                                }
                            }
                            if(!userFound){
                                Toast.makeText(EventCreation.this, "Username " + participantsArray[i-1] + " can't be found.", Toast.LENGTH_LONG).show();
                                participants.put("p" + i, "empty");
                            }
                            userFound = false;
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "onCancelled", databaseError.toException());
                        }
                    });
                }
                else {
                    participants.put("p" + i, "empty");
                }
            }

            // CHECK IF EVENT DOESN'T ALREADY EXIST
            // CALCULATE DURATION
                // EVENTS OVER MULTIPLE DAYS - SPLIT EVENTS

            myRef = database.getReference("timetable/" + user.getUid() + "/" + startDate);
            Map<String,Object> eventData = new HashMap<String,Object>();
                eventData.put("activity", title);
                eventData.put("duration", duration);
                eventData.put("endTime", endTime);
                eventData.put("location", location);
                eventData.put("notes", notes);
                eventData.put("owner", user.getUid());
                eventData.put("startTime", startTime);
                eventData.put("participants", participants);
                eventData.put("private", isChecked);

            myRef.push().setValue(eventData);

        Toast.makeText(EventCreation.this, "Event created!", Toast.LENGTH_LONG).show();
    }

    // http://stackoverflow.com/a/892204
    String parseDate(String maybeDate, String format, boolean lenient) {
        Date date = null;

        // test date string matches format structure using regex
        // - weed out illegal characters and enforce 4-digit year
        // - create the regex based on the local format string
        String reFormat = Pattern.compile("d+|M+").matcher(Matcher.quoteReplacement(format)).replaceAll("\\\\d{1,2}");
        reFormat = Pattern.compile("y+").matcher(reFormat).replaceAll("\\\\d{4}");

        if ( Pattern.compile(reFormat).matcher(maybeDate).matches() ) {
            // date string matches format structure,
            // - now test it can be converted to a valid date
            SimpleDateFormat sdf = (SimpleDateFormat)DateFormat.getDateInstance();
            sdf.applyPattern(format);
            sdf.setLenient(lenient);
            try { date = sdf.parse(maybeDate); } catch (ParseException e) { }
        }
        return date.toString();
    }

    public static long getDateDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return TimeUnit.MINUTES.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    public boolean isDateValid(String date)
    {
        try {
            dateFormat.setLenient(false);
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public boolean isTimeValid( String timeStr) {
        DateFormat df = new SimpleDateFormat("HH:mm");
        try {
            df.setLenient(false);
            df.parse(timeStr);
            return true;
        } catch ( ParseException exc ) {
        }
        return false;
    }

    public boolean isDateBeforeNow(String Date, String Time){
        return false;
    }

    public boolean isDateDifferenceValid(String Date1, String Time1, String Date2, String Time2){
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
        startDate = parseDate(startDate, "yyyy-MM-dd", false);
        endDate = parseDate(startDate, "yyyy-MM-dd", false);

        if(!isDateValid(startDate)){
            Toast.makeText(EventCreation.this, "Start date isn't valid. Entry needs to be of form yyyy-mm-dd.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!isDateValid(endDate)){
            Toast.makeText(EventCreation.this, "End date isn't valid. Entry needs to be of form yyyy-mm-dd.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!isTimeValid(startTime)){
            Toast.makeText(EventCreation.this, "Start time isn't valid. Entry needs to be of form hh:mm in 24HR format.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!isTimeValid(endTime)){
            Toast.makeText(EventCreation.this, "End time isn't valid. Entry needs to be of form hh:mm in 24HR format.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!isDateBeforeNow(startDate, startTime)){
            Toast.makeText(EventCreation.this, "Can't start event in past.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!isDateBeforeNow(endDate, endTime)){
            Toast.makeText(EventCreation.this, "Can't end event in past.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!isDateDifferenceValid(startDate, startTime, endDate, endTime)){
            Toast.makeText(EventCreation.this, "End date and time is before start date and time", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}