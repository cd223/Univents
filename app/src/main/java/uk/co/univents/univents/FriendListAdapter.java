package uk.co.univents.univents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.id;

public class FriendListAdapter extends BaseAdapter {
    private Context con;
    private String[] data;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase database;
    private DatabaseReference frndInvRef;
    private DatabaseReference uidFromNameRef;
    private DatabaseReference friendsRef;
    private DatabaseReference inviteUserFriendsRef;
    private HashMap<String, Object> usernameID;
    private int friendCount;



    public  FriendListAdapter (Context context, String[] data)
    {
        this.con = context;
        this.data = data;

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        frndInvRef = database.getReference("friendInvites/" + mUser.getUid());

        uidFromNameRef = database.getReference("username2uid");

        friendsRef = database.getReference("users/" + mUser.getUid() + "/friends");

    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView= inflater.inflate(R.layout.activity_friendlist_row, parent, false);

        TextView text = (TextView) convertView.findViewById(R.id.listRow);
        text.setText(data[position]);

        Button acceptButton = (Button)  convertView.findViewById(R.id.acceptFriend);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uidFromNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        usernameID = (HashMap<String, Object>) dataSnapshot.getValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, Object> temp = (HashMap<String, Object>) dataSnapshot.getValue();
                        friendCount = temp.size();

                        //Push this user as a new friend
                        String inviteID = usernameID.get(data[position]).toString();
                        temp.put("f"+friendCount, inviteID);
                        friendsRef.updateChildren(temp);

                        inviteUserFriendsRef = database.getReference("users/"+inviteID+"/friends");
                        inviteUserFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                HashMap<String, Object> temp2 = (HashMap<String, Object>) dataSnapshot.getValue();
                                int inviteFriendCount = temp2.size();

                                //Push current user as a new friend also to the the invite's user
                                temp2.put("f"+inviteFriendCount, mUser.getUid());
                                inviteUserFriendsRef.updateChildren(temp2);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                 //Remove invite from list
                frndInvRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot friendInvitation : dataSnapshot.getChildren()) {
                            String friendKey = friendInvitation.getKey();
                            String friendUID = (String) friendInvitation.getValue();

                            uidFromNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    usernameID = (HashMap<String, Object>) dataSnapshot.getValue();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                            if(!friendKey.equals("f0") ) {
                                String inviteID = usernameID.get(data[position]).toString();

                                if (friendUID.equalsIgnoreCase(inviteID)) {
                                    database.getReference("friendInvites/" + mUser.getUid() + "/" + friendKey).removeValue();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        Button rejectButton = (Button)  convertView.findViewById(R.id.rejectFriend);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uidFromNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        usernameID = (HashMap<String, Object>) dataSnapshot.getValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                //Remove invite from list
                frndInvRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot friendInvitation : dataSnapshot.getChildren()) {
                            String friendKey = friendInvitation.getKey();
                            String friendUID = (String) friendInvitation.getValue();

                            if(!friendKey.equals("f0") ) {
                                uidFromNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        usernameID = (HashMap<String, Object>) dataSnapshot.getValue();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                                String inviteID = usernameID.get(data[position]).toString();

                                if (friendUID.equalsIgnoreCase(inviteID)) {
                                    database.getReference("friendInvites/" + mUser.getUid() + "/" + friendKey).removeValue();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
               }
        });

        return convertView;
    }
}
