package edu.augustana.quadsquad.householdmanager;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "RetrieveAccessToken";
    private static final int REQ_SIGN_IN_REQUIRED = 136;
    final int version = Build.VERSION.SDK_INT;
    final int RC_SIGN_IN = 1;
    GoogleApiClient google_api_client;
    GoogleApiAvailability google_api_availability;
    SignInButton signInButton;
    CircleImageView profilePic;
    TextView user_name;
    TextView gemail_id;
    Firebase mFirebase;
    String userIdToken = "";
    AuthData mAuthData;
    boolean isAuthed = false;
    String userId = "";
    GoogleSignInOptions gso;
    GoogleSignInAccount acct;
    String email_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);

        mFirebase = new Firebase("https://household-manager-136.firebaseio.com");

        mFirebase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                isAuthed = authData != null;
                Log.d("IsAuthed", String.valueOf(isAuthed));
            }
        });

        mAuthData = mFirebase.getAuth();
        isAuthed = mAuthData != null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);


        profilePic = (CircleImageView) headerLayout.findViewById(R.id.profile_pic);
        user_name = (TextView) headerLayout.findViewById(R.id.user_name);
        gemail_id = (TextView) headerLayout.findViewById(R.id.textview_email);
        updateUI(acct != null);


        buildNewGoogleApiClient();
        customizeSignInBtn();
        setBtnClickListeners();

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        Drawable garbageIcon = menu.getItem(0).getIcon();
        garbageIcon = DrawableCompat.wrap(garbageIcon);
        DrawableCompat.setTint(garbageIcon, ContextCompat.getColor(getApplicationContext(), R.color.colorIcons));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_corkboard) {
            // Handle the camera action
        } else if (id == R.id.nav_todo) {

        } else if (id == R.id.nav_bills) {

        } else if (id == R.id.nav_calendar) {

        } else if (id == R.id.nav_settings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    /*
   create and  initialize GoogleApiClient object to use Google Plus Api.
   While initializing the GoogleApiClient object, request the Plus.SCOPE_PLUS_LOGIN scope.
   */

    private void buildNewGoogleApiClient() {

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestServerAuthCode(getString(R.string.server_client_id))
                .build();

        google_api_client = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    private void customizeSignInBtn() {
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        if (signInButton != null) {
            signInButton.setSize(SignInButton.SIZE_STANDARD);
        }
        signInButton.setScopes(gso.getScopeArray());
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(google_api_client);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(google_api_client).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        acct = null;
                        updateUI(!status.isSuccess());
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(google_api_client).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        acct = null;
                        updateUI(!status.isSuccess());
                    }
                });
    }

    /*
    Set on click Listeners on the sign-in sign-out and disconnect buttons
     */
    private void setBtnClickListeners() {
        // Button listeners
        signInButton.setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();

                break;
            case R.id.disconnect_button:
                revokeAccess();

                break;
        }

    }


    /*
     Show and hide of the Views according to the user login status
     */
    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);

            setPersonalInfo(acct);

        } else {

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);

            user_name.setText("NULL");
            gemail_id.setText("quad@squad.com");
            profilePic.setImageDrawable(getResources().getDrawable(android.R.drawable.sym_def_app_icon));

        }
    }





    /*
    set the User information into the views defined in the layout
    */

    private void setPersonalInfo(GoogleSignInAccount account) {


        String personName = acct.getDisplayName();

        String personPhotoUrl = acct.getPhotoUrl().toString();
        String email = acct.getEmail();

        user_name.setText(personName);

        gemail_id.setText(email);
        Picasso.with(getApplicationContext()).load(personPhotoUrl).resize(200, 200).into(profilePic);
    }

    /*
      Will receive the activity result and check which request we are responding to

     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result, Intent data) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();

            email_address = acct.getEmail();
            updateUI(true);

            new GetAuthToken().execute(userId);


        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }


    private void authorizeFireBaseUser(String googleAccessToken) {

        mFirebase.authWithOAuthToken("google", googleAccessToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "OnAuth ran");
                mAuthData = authData;
                Log.d(TAG, mAuthData.getProvider());
                Log.d(TAG, mAuthData.getUid());
                userId = mAuthData.getUid();

                Map<String, String> map = new HashMap<String, String>();
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        public String doInBackground(String... params) {
            String accountName = params[0];
            String scopes = "oauth2:"
                    + Scopes.PLUS_LOGIN + " "
                    + Scopes.PROFILE;

            try {
                if (!isAuthed) {
                    googleAccessToken = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);
                    authorizeFireBaseUser(googleAccessToken);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage());
            }


            return googleAccessToken;

        }*/

    public class GetAuthToken extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String scopes = "oauth2:profile email";
            String token = null;

            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), email_address, scopes);
            } catch (IOException | GoogleAuthException e) {
                e.printStackTrace();
            }
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            userIdToken = s;
            Log.d(TAG, userIdToken);

            authorizeFireBaseUser(userIdToken);
        }
    }


}

