package edu.augustana.quadsquad.householdmanager.model.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Member;
import edu.augustana.quadsquad.householdmanager.R;
import edu.augustana.quadsquad.householdmanager.data.preferences.SaveSharedPreference;
import edu.augustana.quadsquad.householdmanager.model.fragment.FindMyRoommatesFragment;
import edu.augustana.quadsquad.householdmanager.model.fragment.ToDoFragment;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.CorkboardNote;
import edu.augustana.quadsquad.householdmanager.model.fragment.CorkboardFragment;
import edu.augustana.quadsquad.householdmanager.model.fragment.GroupManagementFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener,
        GroupManagementFragment.OnFragmentInteractionListener, CorkboardFragment.OnFragmentInteractionListener, ToDoFragment.OnFragmentInteractionListener, FindMyRoommatesFragment.OnFragmentInteractionListener {


    private static final String TAG = "Main Activity";
    private static final String KEY_STATE_TITLE = "kst";
    private static final String KEY_SELECTED_MENU = "ksm";
    GoogleApiClient google_api_client;
    GoogleSignInOptions gso;

    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;

    Fragment fragment = null;
    Class fragmentClass = null;


    CircleImageView profilePic;
    TextView user_name;
    TextView housename_txt;

    String selectedMenu = "corkboard";

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String NfcTAG = "NfcDemo";

    private NfcAdapter mNfcAdapter;
    public ListView memberList;

    Firebase mFirebase;
    FloatingActionButton fab;
    View.OnClickListener todoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent newTodoIntent = new Intent(MainActivity.this, CreateToDoActivity.class);
            startActivity(newTodoIntent);
        }
    };


    private void postNote(String message) {
        Context ctx = getApplicationContext();
        Firebase mCorkboardNotes = new Firebase("https://household-manager-136.firebaseio.com/corkboardNotes");
        Log.d("Post note: ", message);
        CorkboardNote newNote = new CorkboardNote(message, SaveSharedPreference.getGoogleEmail(ctx),
                SaveSharedPreference.getPrefGroupId(ctx), SaveSharedPreference.getGoogleEmail(ctx));
        mCorkboardNotes.push().setValue(newNote);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        buildNewGoogleApiClient();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }
        fab.hide();


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);


        profilePic = (CircleImageView) headerLayout.findViewById(R.id.profile_pic);
        user_name = (TextView) headerLayout.findViewById(R.id.user_name);
        housename_txt = (TextView) headerLayout.findViewById(R.id.textview_email);

        setPersonalInfo();
        navigationView.getMenu().getItem(0).setChecked(true);

        if (savedInstanceState != null) {

            setTitle(savedInstanceState.getCharSequence(KEY_STATE_TITLE));
            selectedMenu = savedInstanceState.getString(KEY_SELECTED_MENU);

        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter != null) {
            handleIntent(getIntent());
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        CharSequence title = getTitle();
        savedInstanceState.putCharSequence(KEY_STATE_TITLE, title);
        savedInstanceState.putString(KEY_SELECTED_MENU, selectedMenu);
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

            startCorkboard();

        } else if (id == R.id.nav_todo) {
            startTodo();

        } else if (id == R.id.nav_find_my_roommates) {
            startFindMyRoommates();
        } /*else if (id == R.id.nav_calendar) {

        } */ else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_group_management) {
            startGroupManagement();

        } else if (id == R.id.nav_logout) {
            signOut();
        } else if (id == R.id.nav_leavegroup) {
            leaveGroup();
        }

        item.setChecked(true);


        return true;
    }

    private void startFragment() {
        try {
            if (fragmentClass != null) {
                fragment = (Fragment) fragmentClass.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.rlContent, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    private void startGroupManagement() {
        fragmentClass = GroupManagementFragment.class;

        selectedMenu = "groupManagement";

        startFragment();
        setTitle(SaveSharedPreference.getHouseName(getApplicationContext()));
    }

    private void startTodo() {
        fragmentClass = ToDoFragment.class;

        fab.setOnClickListener(todoListener);
        fab.show();

        selectedMenu = "todo";

        startFragment();
        setTitle("To-do List");
    }

    private void startCorkboard() {
        fragmentClass = CorkboardFragment.class;
        fab.hide();
        selectedMenu = "corkboard";

        startFragment();
        setTitle("Corkboard");
    }

    private void startFindMyRoommates() {
        fragmentClass = FindMyRoommatesFragment.class;
        fab.hide();
        selectedMenu = "findMyRoommates";

        startFragment();
        setTitle("Find My Roomates");
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

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(google_api_client).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        mFirebase.unauth();
                    }
                });
    }

    private void leaveGroup() {
        Context context = getApplicationContext();
        Firebase grpRef = mFirebase.child("groups").child(SaveSharedPreference.getPrefGroupId(context));
        Query qryRef = grpRef.child("members").orderByValue().equalTo(SaveSharedPreference.getGoogleEmail(context));
        qryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                    firstChild.getRef().removeValue();
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //Post groupReference to users tree
        Query userQuery = mFirebase.child("users").orderByChild("email").equalTo(SaveSharedPreference.getGoogleEmail(context));
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                    Firebase userRef = firstChild.getRef();
                    userRef.child("groupReferal").setValue("");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        SaveSharedPreference.setHasGroup(context, false);
        SaveSharedPreference.setGroupId(context, "");
        Intent intent = new Intent(this, GroupActivity.class);
        startActivity(intent);
        finish();
    }

    private void setPersonalInfo() {
        Context ctx = getApplicationContext();


        String displayName = SaveSharedPreference.getGoogleDisplayName(ctx);
        String houseName = SaveSharedPreference.getHouseName(ctx);
        String photoURL = SaveSharedPreference.getGooglePictureUrl(ctx);

        if (!photoURL.equals("")) {
            Picasso.with(getApplicationContext()).load(photoURL).fit().into(profilePic);
        }
        user_name.setText(displayName);
        housename_txt.setText(houseName);

    }

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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
        Log.d(TAG, selectedMenu);
        switch (selectedMenu) {
            case "corkboard":
                startCorkboard();
                break;
            case "todo":
                startTodo();
                break;
            case "groupManagement":
                startGroupManagement();
                break;
            case "findMyRoommates":
                startFindMyRoommates();
                break;
            default:
                startCorkboard();
        }
    }

    //START OF NFC READ ACTIVITY
    @TargetApi(21)
    private void handleIntent(Intent intent) {

        String action = intent.getAction();

        //Check for NFC Data Exchange Format
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            //Mime specifies the type of data being transferred over tag
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                //EXTRA TAG creates a tag so that program doesn't need to continually check the physical tag
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                //.execute is inherited
                new NdefReaderTask().execute(tag);
            } else {
                Log.d(TAG, "Wrong mime type: " + type);

            }
            //We shouldn't need this, but just in case
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }


            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        if(mNfcAdapter != null) {
            setupForegroundDispatch(this, mNfcAdapter);
        }
    }


    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        if(mNfcAdapter != null) {
            stopForegroundDispatch(this, mNfcAdapter);
        }
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }
/* This makes it so that once in the app, tags are only scanned when you want them
   Code found at http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278
   Code should be the same for all apps that need this functionality
 */
    @TargetApi(21)
    public static void setupForegroundDispatch(final AppCompatActivity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    @TargetApi(21)
    public static void stopForegroundDispatch(final AppCompatActivity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    //Where the real magic happens, also copied from
//http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278
    @TargetApi(21)
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        @TargetApi(21)
        private String readText(NdefRecord record) throws UnsupportedEncodingException {

            // See NFC forum specification for "Text Record Type Definition" at 3.2.1
            // Actualy definition of payload unclear. developer.android.com has the definition as:
            // Payload: the actual payload

            byte[] payload = record.getPayload();

            String textEncoding;
            if ((payload[0] & 128) == 0) {
                textEncoding = "UTF-8";
            } else {
                textEncoding = "UTF-16";
            }

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        //Tag must be read before something can be done with the information
        @Override
        protected void onPostExecute(String result) {
            //Checks to make sure text is in result
            Context ctx = getApplicationContext();
            if (result != null) {
                displayMessage(result);

                FindMyRoommatesFragment roommatesFragment = new FindMyRoommatesFragment();
                roommatesFragment.toggleLocation(memberList);

                /*TextView tv = (TextView) findViewById(R.id.name_view);

                // code to switch the location in SharedPreferences using NFC tag vb
                //if(result.equals(SaveSharedPreference.getPrefGroupId(ctx))){
                    if (!SaveSharedPreference.getLocation(ctx)) {
                        //the toggle is true
                        SaveSharedPreference.setLocation(ctx, true);
                        *//*CircleImageView avatar = (CircleImageView) findViewById(R.id.avatar);
                        Picasso.with(ctx).load(R.drawable.ic_home_24dp).fit().into(avatar);*//*
                        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_home_24dp, 0);
                    } else {
                        // the toggle is false
                        SaveSharedPreference.setLocation(ctx, false);
                        *//*CircleImageView avatar = (CircleImageView) findViewById(R.id.avatar);
                        Picasso.with(ctx).load(R.drawable.ic_away_24dp).fit().into(avatar);*//*
                        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_away_24dp, 0);
                    }*/
                //}
            } else {
                displayMessage("Tag Empty");
            }
        }
    }

    public void displayMessage(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    public void setMemberList(ListView list){
        memberList = list;
    }

    public ListView getMemberList(){
        return memberList;
    }
}



