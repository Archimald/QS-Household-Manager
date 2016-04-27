package edu.augustana.quadsquad.householdmanager.data.firebaseobjects;

/**
 * Created by Luke Currie on 4/26/2016.
 */
public class CorkboardNote {

    String message;
    String fromText;
    String groupId;
    String ownerEmail;

    public CorkboardNote() {

    }

    public CorkboardNote(String mes, String ft, String gi, String oe){
        message=mes;
        fromText=ft;
        groupId=gi;
        ownerEmail=oe;
    }

    public String getMessage(){return message;}

    public String getFromText() {return fromText;}

    public String getGroupId() {return groupId;}

    public String getOwnerEmail() {return ownerEmail;}
}
