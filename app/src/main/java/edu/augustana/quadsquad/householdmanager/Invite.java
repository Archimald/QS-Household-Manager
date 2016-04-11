package edu.augustana.quadsquad.householdmanager;

import java.net.URI;

/**
 * Created by micha on 4/6/2016.
 */
public class Invite {
    protected String houseName;
    protected String fromText;
    protected String contactPicURI;
    protected String groupReferal;

    public Invite() {

    }


    public Invite(String houseName, String fromText, URI contactPicURI) {
        this.houseName = houseName;
        this.fromText = fromText;
        this.contactPicURI = contactPicURI.toString();
    }

    public Invite(String houseName, String fromText, String contactPicURI, String groupReferal) {
        this.houseName = houseName;
        this.fromText = fromText;
        this.contactPicURI = contactPicURI;
        this.groupReferal = groupReferal;
    }

    public String getHouseName() {
        return houseName;
    }

    public String getFromText() {
        return fromText;
    }

    public String getContactPicURI() {
        return contactPicURI;
    }
}
