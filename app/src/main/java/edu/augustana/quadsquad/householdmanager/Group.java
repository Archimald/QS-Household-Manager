package edu.augustana.quadsquad.householdmanager;

/**
 * Created by Luke Currie on 4/11/2016.
 */
public class Group {
    public String housename;
    public String ownerEmail;
    public String ownerToken;

    public Group(String hn, String oe, String ot){
        housename = hn;
        ownerEmail =oe;
        ownerToken = ot;

    }

    public String getHousename(){
        return housename;
    }
    public String getOwnerEmail() {
        return ownerEmail;
    }

    public String getOwnerToken(){
        return ownerToken;
    }

}
