package edu.augustana.quadsquad.householdmanager;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class GroupActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = "Group Activity";
    Firebase mFirebase;
    GoogleApiClient google_api_client;
    GoogleSignInOptions gso;

    RecyclerView invitesRecyclerView;

    FirebaseRecyclerAdapter<Invite, InviteViewHolder> mAdapter;

    Button btnAddGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        buildNewGoogleApiClient();

        Toolbar toolbar = (Toolbar) findViewById(R.id.group_toolbar);
        setSupportActionBar(toolbar);

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

        btnAddGroup = (Button) findViewById(R.id.create_group);

        btnAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newGroupIntent = new Intent(GroupActivity.this, NewGroupActivity.class);
                startActivity(newGroupIntent);
            }
        });

        invitesRecyclerView = (RecyclerView) findViewById(R.id.invites_recycler_view);
        assert invitesRecyclerView != null;
        invitesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        invitesRecyclerView.setLayoutManager(llm);

        final Firebase invitesRef = mFirebase.child("invites");


        mAdapter = new FirebaseRecyclerAdapter<Invite, InviteViewHolder>(Invite.class, R.layout.invite_card, InviteViewHolder.class, invitesRef) {

            @Override
            protected void populateViewHolder(final InviteViewHolder inviteViewHolder, final Invite invite, int i) {
                inviteViewHolder.key = this.getRef(i).getKey();
                inviteViewHolder.vHouseName.setText(invite.houseName);
                inviteViewHolder.vFromText.setText(String.format("From %s", invite.fromText));
                Picasso.with(getApplicationContext())
                        .load(invite.contactPicURI.toString())
                        .placeholder(R.drawable.blank_conact)
                        .fit()
                        .into(inviteViewHolder.iProfile);
                inviteViewHolder.bDismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        invitesRef.child(inviteViewHolder.key).removeValue();
                    }
                });

            }
        };

        invitesRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.groups_menu, menu);
        Drawable logoutIcon = menu.getItem(0).getIcon();
        logoutIcon = DrawableCompat.wrap(logoutIcon);
        DrawableCompat.setTint(logoutIcon, ContextCompat.getColor(getApplicationContext(), R.color.colorIcons));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            signOut();
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    private void buildNewGoogleApiClient() {

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestServerAuthCode(getString(R.string.server_client_id))
                .requestIdToken(getString(R.string.server_client_id))
                .build();

        google_api_client = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(google_api_client).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        mFirebase.unauth();
                        SaveSharedPreference.setIsLoggedIn(getApplicationContext(), false);
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}
