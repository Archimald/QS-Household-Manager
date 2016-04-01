package edu.augustana.quadsquad.householdmanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int SIGN_IN_CODE = 0;
    private static final int PROFILE_PIC_SIZE = 120;
    private static final String TAG = "RetrieveAccessToken";
    private static final int REQ_SIGN_IN_REQUIRED = 136;
    GoogleApiClient google_api_client;
    GoogleApiAvailability google_api_availability;
    SignInButton signIn_btn;
    ProgressDialog progress_dialog;
    CircleImageView profilePic;
    TextView user_name;
    TextView gemail_id;
    Person currentPerson;
    Firebase mFirebase;
    String googleAccessToken = "";
    AuthData mAuthData;
    boolean isAuthed = false;
    String userId = "";
    private ConnectionResult connection_result;
    private boolean is_intent_inprogress;
    private boolean is_signInBtn_clicked;
    private int request_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);

        mFirebase = new Firebase("https://household-manager-136.firebaseio.com");

        mFirebase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    isAuthed = true;
                } else {
                    isAuthed = false;
                }
            }
        });

        mAuthData = mFirebase.getAuth();
        if (mAuthData != null) {
            isAuthed = true;
        } else {
            isAuthed = false;
        }

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
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);


        profilePic = (CircleImageView) headerLayout.findViewById(R.id.profile_pic);
        user_name = (TextView) headerLayout.findViewById(R.id.user_name);
        gemail_id = (TextView) headerLayout.findViewById(R.id.textview_email);

        progress_dialog = new ProgressDialog(this);
        progress_dialog.setMessage("Signing in....");


        buildNewGoogleApiClient();
        customizeSignBtn();
        setBtnClickListeners();

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        DrawableCompat.setTint(garbageIcon, getResources().getColor(R.color.colorIcons));
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
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
   create and  initialize GoogleApiClient object to use Google Plus Api.
   While initializing the GoogleApiClient object, request the Plus.SCOPE_PLUS_LOGIN scope.
   */

    private void buildNewGoogleApiClient() {

        google_api_client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        is_signInBtn_clicked = false;
        // Get user's information and set it into the layout
        getProfileInfo();
        // Update the UI after signin
        changeUI(true);
        new RetrieveTokenTask().execute(Plus.AccountApi.getAccountName(google_api_client));


    }

    @Override
    public void onConnectionSuspended(int i) {
        google_api_client.connect();
        changeUI(false);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (!result.hasResolution()) {
            google_api_availability.getErrorDialog(this, result.getErrorCode(), request_code).show();
            return;
        }

        if (!is_intent_inprogress) {

            connection_result = result;

            if (is_signInBtn_clicked) {

                resolveSignInError();
            }
        }

    }

    /*Customize sign-in button. The sign-in button can be displayed in
  multiple sizes and color schemes. It can also be contextually
  rendered based on the requested scopes. For example. a red button may
  be displayed when Google+ scopes are requested, but a white button
  may be displayed when only basic profile is requested. Try adding the
  Plus.SCOPE_PLUS_LOGIN scope to see the  difference.
*/

    private void customizeSignBtn() {

        signIn_btn = (SignInButton) findViewById(R.id.sign_in_button);
        signIn_btn.setSize(SignInButton.SIZE_STANDARD);
        signIn_btn.setScopes(new Scope[]{Plus.SCOPE_PLUS_LOGIN});

    }

    /*
    Set on click Listeners on the sign-in sign-out and disconnect buttons
     */
    private void setBtnClickListeners() {
        // Button listeners
        signIn_btn.setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Toast.makeText(this, "start sign process", Toast.LENGTH_SHORT).show();
                gPlusSignIn();
                break;
            case R.id.sign_out_button:
                Toast.makeText(this, "Sign Out from G+", Toast.LENGTH_LONG).show();
                gPlusSignOut();

                break;
            case R.id.disconnect_button:
                Toast.makeText(this, "Revoke Access from G+", Toast.LENGTH_LONG).show();
                gPlusRevokeAccess();

                break;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Add this line to initiate connection
        google_api_client.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (google_api_client.isConnected()) {
            google_api_client.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (google_api_client.isConnected()) {
            google_api_client.connect();
        }
    }

    /**
     * Sign-in into the Google + account
     */
    private void gPlusSignIn() {
        if (!google_api_client.isConnecting()) {
            Log.d("user connected", "connected");
            is_signInBtn_clicked = true;
            progress_dialog.show();
            resolveSignInError();

        }
    }

    /**
     * Method to resolve any signin errors
     */
    private void resolveSignInError() {
        if (connection_result.hasResolution()) {
            try {
                is_intent_inprogress = true;
                connection_result.startResolutionForResult(this, SIGN_IN_CODE);
                Log.d("resolve error", "sign in error resolved");
            } catch (IntentSender.SendIntentException e) {
                is_intent_inprogress = false;
                google_api_client.connect();
            }
        }
    }

    /**
     * Sign-out from Google+ account
     */
    private void gPlusSignOut() {
        if (google_api_client.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(google_api_client);
            google_api_client.disconnect();
            google_api_client.connect();
            changeUI(false);
            mFirebase.unauth();
        }
    }

    /**
     * Revoking access from Google+ account
     */
    private void gPlusRevokeAccess() {
        if (google_api_client.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(google_api_client);
            Plus.AccountApi.revokeAccessAndDisconnect(google_api_client)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.d("MainActivity", "User access revoked!");
                            buildNewGoogleApiClient();
                            google_api_client.connect();
                            mFirebase.unauth();
                            changeUI(false);
                        }

                    });
        }
    }

    /*
     Show and hide of the Views according to the user login status
     */
    private void changeUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);

            setPersonalInfo(currentPerson);

        } else {

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);

            user_name.setText("NULL");
            gemail_id.setText("quad@squad.com");
            profilePic.setImageDrawable(getResources().getDrawable(android.R.drawable.sym_def_app_icon));

        }
    }


    /**
     * get user's information name, email, profile pic,Date of birth,tag line and about me
     */
    private void getProfileInfo() {

        try {

            if (Plus.PeopleApi.getCurrentPerson(google_api_client) != null) {
                currentPerson = Plus.PeopleApi.getCurrentPerson(google_api_client);
                setPersonalInfo(currentPerson);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
    set the User information into the views defined in the layout
    */

    private void setPersonalInfo(Person currentPerson) {


        String personName = currentPerson.getDisplayName();
        String personPhotoUrl = currentPerson.getImage().getUrl();
        String email = Plus.AccountApi.getAccountName(google_api_client);

        user_name.setText(personName);

        gemail_id.setText(email);
        Picasso.with(getApplicationContext()).load(personPhotoUrl).resize(200, 200).into(profilePic);

        progress_dialog.dismiss();
    }

    /*
      Will receive the activity result and check which request we are responding to

     */
    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        // Check which request we're responding to
        if (requestCode == SIGN_IN_CODE) {
            request_code = requestCode;
            if (responseCode != RESULT_OK) {
                is_signInBtn_clicked = false;
                progress_dialog.dismiss();

            }

            is_intent_inprogress = false;

            if (!google_api_client.isConnecting()) {
                google_api_client.connect();
            }
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
                Log.e(TAG, firebaseError.getDetails());
            }
        });
    }

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

        }


    }
}