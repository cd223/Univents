package uk.co.univents.univents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by RiccardoBroggi on 17/02/2017.
 */

class Event {
    private String activity;
    private String duration;
    private String endTime;
    private String location;
    private String owner;
    private HashMap<String, Object> invitees;
    private String prvate;
    private String startTime;

    public Event(){
    }

    public Event(String activity, String duration, String endTime, String location, String owner, HashMap<String, Object> participants, String prvate, String startTime){
        this.activity = activity;
        this.duration = duration;
        this.endTime = endTime;
        this.location = location;
        this.owner = owner;
        this.invitees = participants;
        this.prvate = prvate;
        this.startTime = startTime;
    }


    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public HashMap<String, Object> getInvitees() {
        return invitees;
    }

    public void setInvitees(HashMap<String, Object> invitees) {
        this.invitees = invitees;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getPrivateOrNot() {
        return prvate;
    }

    public void setPrivateOrNot(String prvate) {
        this.prvate = prvate;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("activity", activity);
        map.put("duration", duration);
        map.put("endTime", endTime);
        map.put("location", location);
        map.put("ownder", owner);
        map.put("invitees", invitees);
        map.put("prvate", prvate);
        map.put("startTime", startTime);
        return map;
    }

    public String toCustomString(){
        String response = "";
        response += ("Activity:   " + activity + "\n");
        response += ("Duration:   " + duration + "\n");
        response += ("Start Time: " + startTime + "\n");
        response += ("End Time:   " + endTime + "\n");
        response += ("Location:   "  +location + "\n");
        //TODO Invited by and Invitees
        return response;
    }
}
