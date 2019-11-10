package uk.co.univents.univents;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Invites extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites);
    }

    public void viewFriendInvites(View view){
        Intent intent = new Intent(this, FriendInvites.class);
        startActivity(intent);
    }

    public void viewGroupInvites(View view){
        Intent intent = new Intent(this, GroupInvites.class);
        startActivity(intent);
    }

    public void viewEventInvites(View view){
        Intent intent = new Intent(this, EventInvites.class);
        startActivity(intent);
    }
}
