package edu.augustana.quadsquad.householdmanager;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;
import java.util.Map;

public class NewGroupActivity extends AppCompatActivity {

    private final String TAG = "New Group Activity";
    Firebase mFirebase;
    GoogleApiClient google_api_client;
    GoogleSignInOptions gso;

    Button createGrp;
    EditText groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        groupName = (EditText) findViewById(R.id.editText);
        createGrp = (Button) findViewById(R.id.button);

        Firebase.setAndroidContext(this);

        mFirebase = new Firebase("https://household-manager-136.firebaseio.com");
        mFirebase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                boolean isAuthed = authData != null;
                Log.d("IsAuthed", String.valueOf(isAuthed));
                if (!isAuthed) {
                    authorizeFireBaseUser(SaveSharedPreference.getGoogleOauthToken(getApplicationContext()));
                }
            }
        });

        createGrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName=groupName.getText().toString();
                Context context = getApplicationContext();
                Firebase inviteRef = mFirebase.child("invites");
                Invite invite = new Invite(newName
                        , SaveSharedPreference.getGoogleEmail(context)
                        , SaveSharedPreference.getGooglePictureUrl(context));
                inviteRef.push().setValue(invite);
                finish();
            }
        });




    }

    private void authorizeFireBaseUser(String googleAccessToken) {

        mFirebase.authWithOAuthToken("google", googleAccessToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "OnAuth ran");
                Log.d(TAG, authData.getProvider());
                Log.d(TAG, authData.getUid());
                SaveSharedPreference.setFirebaseUid(getApplicationContext(), authData.getUid());

                Map<String, String> map = new HashMap<>();
                map.put("provider", authData.getProvider());
                if (authData.getProviderData().containsKey("displayName")) {
                    map.put("displayName", authData.getProviderData().get("displayName").toString());
                }
                mFirebase.child("users").child(authData.getUid()).setValue(map);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.e("Auth error", firebaseError.getDetails());
            }
        });
    }

}
