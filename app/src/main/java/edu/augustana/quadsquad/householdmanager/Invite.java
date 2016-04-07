package edu.augustana.quadsquad.householdmanager;

import java.net.URI;

/**
 * Created by micha on 4/6/2016.
 */
public class Invite {
    protected String houseName;
    protected String fromText;
    protected URI contactPicURI;

    public Invite() {

    }


    public Invite(String houseName, String fromText, URI contactPicURI) {
        this.houseName = houseName;
        this.fromText = fromText;
        this.contactPicURI = contactPicURI;
    }

    public Invite(String houseName, String fromText, String contactPicURI) {
        this.houseName = houseName;
        this.fromText = fromText;
        this.contactPicURI = URI.create(contactPicURI);
    }

    public String getHouseName() {
        return houseName;
    }

    public String getFromText() {
        return fromText;
    }

    public URI getContactPicURI() {
        return contactPicURI;
    }
}
