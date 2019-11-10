package uk.co.univents.univents;

import java.util.HashMap;

/**
 * Created by RiccardoBroggi on 20/02/2017.
 */

public class Group {
    private String name;
    private HashMap<String, Object> members;
    private String prvate;
    private String admin;

    public Group() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Object> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, Object> members) {
        this.members = members;
    }

    public String getPrvate() {
        return prvate;
    }

    public void setPrvate(String prvate) {
        this.prvate = prvate;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String toString(){
        String toReturn = "";
        toReturn += "Group Name:" + name + "\n";
        toReturn += "Group Admin:" + admin;
        return toReturn;
    }
}
