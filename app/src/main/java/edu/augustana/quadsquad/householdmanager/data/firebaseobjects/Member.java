package edu.augustana.quadsquad.householdmanager.data.firebaseobjects;

/**
 * Created by micha on 4/18/2016.
 */
public class Member {
    protected String displayName;
    protected String provider;
    protected String contactPicURI;
    protected String email;
    protected String groupReferal;
    protected String locationStatus;

    public Member() {

    }


    public Member(String displayName, String provider, String contactPicURI, String email, String groupReferal) {
        this.displayName = displayName;
        this.provider = provider;
        this.email = email;
        this.groupReferal = groupReferal;
        this.contactPicURI = contactPicURI;
        this.locationStatus = "Away";
    }

    public Member(String displayName, String provider, String contactPicURI, String email) {
        this.displayName = displayName;
        this.provider = provider;
        this.email = email;
        this.groupReferal = "";
        this.contactPicURI = contactPicURI;
        this.locationStatus = "Away";
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProvider() {
        return provider;
    }

    public String getContactPicURI() {
        return contactPicURI;
    }

    public String getEmail() {
        return email;
    }

    public String getGroupReferal() {
        return groupReferal;
    }

    public String getLocationStatus() {
        return locationStatus;
    }
}
