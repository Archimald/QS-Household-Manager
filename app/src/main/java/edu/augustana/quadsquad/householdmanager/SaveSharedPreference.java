package edu.augustana.quadsquad.householdmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Luke Currie on 4/3/2016.
 */
public class SaveSharedPreference
{
    static final String PREF_GOOGLE_ID_TOKEN = "SignIn Account Id Token";
    static final String PREF_FIREBASE_UID = "Firebase UID";
    static final String PREF_GOOGLE_OAUTH_TOKEN = "Google OAuth Token";
    static final String PREF_IS_LOGGED_IN = "Is Logged In";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setGoogleIdToken(Context ctx, String idToken)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_GOOGLE_ID_TOKEN, idToken);
        editor.apply();
    }

    public static void setFirebaseUid(Context ctx, String uid)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
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

    public static String getFirebaseUid(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_FIREBASE_UID, "");
    }

    public static String getGoogleIdToken(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_GOOGLE_ID_TOKEN, "");
    }

    public static String getGoogleOauthToken(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_GOOGLE_OAUTH_TOKEN, "");
    }

    public static boolean getIsLoggedIn(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_IS_LOGGED_IN, false);
    }

    public static void clearUserName(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear(); // clear all stored data
        editor.apply();
    }

}
