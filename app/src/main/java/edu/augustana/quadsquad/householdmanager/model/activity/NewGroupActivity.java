package edu.augustana.quadsquad.householdmanager.model.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

import edu.augustana.quadsquad.householdmanager.R;
import edu.augustana.quadsquad.householdmanager.data.preferences.SaveSharedPreference;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Group;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Invite;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Member;

//import com.google.android.gms.appinvite.AppInviteInvitation;

public class NewGroupActivity extends AppCompatActivity {

    final int REQUEST_INVITE = 13;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.new_group_toolbar);
        setSupportActionBar(toolbar);

        assert toolbar != null;
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setTitle("New Group");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        groupName = (EditText) findViewById(R.id.editText);
        createGrp = (Button) toolbar.findViewById(R.id.toolbar_save);

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
                String newName = groupName.getText().toString();
                Context context = getApplicationContext();

                Firebase groupRef = mFirebase.child("groups");
                Group group = new Group(newName, SaveSharedPreference.getGoogleEmail(context), SaveSharedPreference.getGoogleIdToken(context));
                Firebase newPostRef = groupRef.push();
                newPostRef.setValue(group);
                Firebase memberRef = newPostRef.child("members");
                memberRef.push().setValue(SaveSharedPreference.getGoogleEmail(context));
                final String newGroupReferralKey = newPostRef.getKey();

                Invite invite = new Invite(newName
                        , SaveSharedPreference.getGoogleEmail(context)
                        , SaveSharedPreference.getGoogleEmail(context)
                        , SaveSharedPreference.getGooglePictureUrl(context)
                        , newGroupReferralKey);

                SaveSharedPreference.setGroupId(context, newGroupReferralKey);
                SaveSharedPreference.setHasGroup(context, true);

                Firebase inviteRef = mFirebase.child("invites");
                inviteRef.push().setValue(invite);

                //Post groupReference to users tree
                Query userQuery = mFirebase.child("users").orderByChild("email").equalTo(SaveSharedPreference.getGoogleEmail(context));
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                            Firebase userRef = firstChild.getRef();
                            userRef.child("groupReferal").setValue(newGroupReferralKey);

                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                SaveSharedPreference.setHouseName(context, newName);
                Intent intent = new Intent(NewGroupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();


            }
        });


    }

    /*private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder("Join My Household!")
                .setMessage("Hey! \n" +
                        "I'm using Household Manager to keep track of chores and todo-lists and communication.\n" +
                        "Since we're roommates, you should use it too. Accept my invite.")
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }*/

    private void authorizeFireBaseUser(String googleAccessToken) {

        mFirebase.authWithOAuthToken("google", googleAccessToken, new Firebase.AuthResultHandler() {
            Context ctx = getApplicationContext();
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "OnAuth ran");
                Log.d(TAG, authData.getProvider());
                Log.d(TAG, authData.getUid());
                SaveSharedPreference.setFirebaseUid(getApplicationContext(), authData.getUid());
                Member newMember = new Member(authData.getProviderData().get("displayName").toString(),
                        authData.getProvider(), SaveSharedPreference.getGooglePictureUrl(ctx), SaveSharedPreference.getGoogleEmail(ctx));

                mFirebase.child("users").child(authData.getUid()).setValue(newMember);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.e("Auth error", firebaseError.getDetails());
            }
        });
    }

}
