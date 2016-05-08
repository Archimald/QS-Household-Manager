package edu.augustana.quadsquad.householdmanager.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by Luke Currie on 4/3/2016.
 */
public class SaveSharedPreference {
    static final String PREF_GOOGLE_ID_TOKEN = "SignIn Account Id Token";
    static final String PREF_GOOGLE_DISPLAY_NAME = "SignIn Account Display Name";
    static final String PREF_GOOGLE_EMAIL = "SignIn Account Email";
    static final String PREF_GOOGLE_PICTURE_URL = "SignIn Account Picture";

    static final String PREF_FIREBASE_UID = "Firebase UID";
    static final String PREF_GOOGLE_OAUTH_TOKEN = "Google OAuth Token";
    static final String PREF_IS_LOGGED_IN = "Is Logged In";
    static final String PREF_HAS_GROUP = "Has Group";
    static final String PREF_GROUP_ID ="Group ID";
    static final String PREF_HOUSE_NAME = "Housename";

    static final String PREF_LOCATION_STATUS = "Location";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setGoogleAccount(Context ctx, GoogleSignInAccount acct) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_GOOGLE_ID_TOKEN, acct.getIdToken());
        editor.putString(PREF_GOOGLE_DISPLAY_NAME, acct.getDisplayName());
        editor.putString(PREF_GOOGLE_EMAIL, acct.getEmail());
        if (acct.getPhotoUrl() != null) {
            editor.putString(PREF_GOOGLE_PICTURE_URL, acct.getPhotoUrl().toString());
        }
        editor.apply();
    }

    public static void setFirebaseUid(Context ctx, String uid) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        Log.d(PREF_FIREBASE_UID, uid);
        editor.putString(PREF_FIREBASE_UID, uid);
        editor.apply();
    }

    public static void setGoogleOAuthToken(Context ctx, String token) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_GOOGLE_OAUTH_TOKEN, token);
        editor.apply();
    }

    public static void setIsLoggedIn(Context ctx, boolean isLoggedIn) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public static void setHasGroup(Context ctx, boolean hasGroup) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_HAS_GROUP, hasGroup);
        editor.apply();
    }

    public static void setGroupId(Context ctx, String newId) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_GROUP_ID, newId);
        editor.apply();
    }

    public static void setHouseName(Context ctx, String newHouseName) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_HOUSE_NAME, newHouseName);
        editor.apply();
    }

    // added for NFC
    public static void setLocation(Context ctx, Boolean locationStatus) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_LOCATION_STATUS, locationStatus);
        editor.apply();
    }

    public static String getFirebaseUid(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_FIREBASE_UID, "");
    }

    public static String getGoogleIdToken(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_GOOGLE_ID_TOKEN, "");
    }

    public static String getGoogleEmail(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_GOOGLE_EMAIL, "");
    }

    public static String getGooglePictureUrl(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_GOOGLE_PICTURE_URL, "");
    }

    public static String getGoogleDisplayName(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_GOOGLE_DISPLAY_NAME, "");
    }

    public static String getGoogleOauthToken(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_GOOGLE_OAUTH_TOKEN, "");
    }

    public static boolean getIsLoggedIn(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_IS_LOGGED_IN, false);
    }

    public static boolean getHasGroup(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_HAS_GROUP, false);
    }

    public static String getPrefGroupId(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_GROUP_ID, "");
    }

    public static String getHouseName(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_HOUSE_NAME, "");
    }
    /*public static void clearUserName(Context ctx) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear(); // clear all stored data
        editor.apply();
    }*/


    // added for NFC
    public static Boolean getLocation(Context ctx){
        return getSharedPreferences(ctx).getBoolean(PREF_LOCATION_STATUS, false);
    }

}
