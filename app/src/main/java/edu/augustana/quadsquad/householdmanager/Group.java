package edu.augustana.quadsquad.householdmanager;

/**
 * Created by Luke Currie on 4/11/2016.
 */
public class Group {
    public String houseName;
    public String ownerEmail;
    public String ownerToken;

    public Group(String hn, String oe, String ot){
        houseName = hn;
        ownerEmail =oe;
        ownerToken = ot;

    }

    public String getHousename(){
        return houseName;
    }
    public String getOwnerEmail() {
        return ownerEmail;
    }

    public String getOwnerToken(){
        return ownerToken;
    }

}
