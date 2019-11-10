package uk.co.univents.univents;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class uploadTimetable extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_timetable);
    }

    public void gotToTimetableUpload(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://api.univents.co/upload"));
        startActivity(browserIntent);
    }

    public void goToHome(View view) {
        Log.d("GOING HOME", "BECAUSE CLICKED");
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }
}
