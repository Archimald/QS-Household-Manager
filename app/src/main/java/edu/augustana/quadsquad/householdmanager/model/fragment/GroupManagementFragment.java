package edu.augustana.quadsquad.householdmanager.model.fragment;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Invite;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Member;
import edu.augustana.quadsquad.householdmanager.R;
import edu.augustana.quadsquad.householdmanager.data.preferences.SaveSharedPreference;
import edu.augustana.quadsquad.householdmanager.model.activity.MainActivity;
import edu.augustana.quadsquad.householdmanager.model.activity.NfcWriteActivity;
//import com.google.android.gms.appinvite.AppInviteInvitation;
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
//import com.google.api.services.people.v1.People;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupManagementFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupManagementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupManagementFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    final int REQUEST_INVITE = 13;
    final int PICK_CONTACT = 14;
    final int GET_EMAIL = 15;
    private final String TAG = "GroupManagement";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button btnInvites;
    private Button btnWrite;
    private OnFragmentInteractionListener mListener;

    private ListView membersList;
    private ListView invitesList;

    private Firebase ref;
    private FirebaseListAdapter<Member> memberAdapter;
    private FirebaseListAdapter<Invite> inviteAdapter;

    NfcAdapter adapter;
    PendingIntent pendingIntent;
    boolean mInWriteMode;
    NdefMessage ndefMessage;
    MainActivity mainActivity;

    public GroupManagementFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupManagementFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupManagementFragment newInstance(String param1, String param2) {
        GroupManagementFragment fragment = new GroupManagementFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Firebase.setAndroidContext(getContext());
        ref = new Firebase("https://household-manager-136.firebaseio.com");
        /*ref.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                boolean isAuthed = authData != null;
                Log.d("IsAuthed", String.valueOf(isAuthed));
                if (!isAuthed && SaveSharedPreference.getIsLoggedIn(getContext())) {
                    authorizeFireBaseUser(SaveSharedPreference.getGoogleOauthToken(getContext()));
                }
            }
        });*/
    }

    private void onInviteClicked() {
        new MaterialDialog.Builder(getContext())
                .title("New Invite")
                .content("Please enter the email address of the housemate you wish to invite.")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Email Address", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        postInvite(input.toString());
                    }
                }).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_management, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = new MainActivity();
        btnInvites = (Button) getView().findViewById(R.id.invite_button);
        btnInvites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInviteClicked();
            }
        });
        btnWrite = (Button) getView().findViewById(R.id.writeNFC);
        if(btnWrite != null) {
            btnWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*mainActivity.startWriteNfc();*/
                    Intent writeIntent = new Intent(getActivity(), NfcWriteActivity.class);
                    getActivity().startActivity(writeIntent);
                }

            });
        }



        invitesList = (ListView) getView().findViewById(R.id.invites_list_view);

        membersList = (ListView) getView().findViewById(R.id.members_list_view);

        bindListViews();

    }

    private void bindListViews() {
        Firebase memberRef = ref.child("users");
        String groupID = SaveSharedPreference.getPrefGroupId(getContext());
        Query memberQuery = memberRef.orderByChild("groupReferal").startAt(groupID).endAt(groupID);

        memberAdapter = new FirebaseListAdapter<Member>(getActivity(), Member.class, R.layout.avatar_list_item, memberQuery) {
            @Override
            protected void populateView(View view, Member member, int position) {

                ((TextView) view.findViewById(R.id.name_view)).setText(member.getDisplayName());


                String photoURL = member.getContactPicURI();
                CircleImageView avatar = (CircleImageView) view.findViewById(R.id.avatar);

                if (!photoURL.equals("")) {
                    Picasso.with(getContext()).load(photoURL).fit().into(avatar);
                }


            }
        };
        membersList.setAdapter(memberAdapter);
        memberAdapter.notifyDataSetChanged();

        Firebase inviteRef = ref.child("invites");
        Query inviteQuery = inviteRef.orderByChild("groupReferal").startAt(groupID).endAt(groupID);

        inviteAdapter = new FirebaseListAdapter<Invite>(getActivity(), Invite.class, R.layout.avatar_list_item, inviteQuery) {
            @Override
            protected void populateView(View view, Invite invite, int position) {

                ((TextView) view.findViewById(R.id.name_view)).setText(invite.getToText());


            }
        };
        invitesList.setAdapter(inviteAdapter);
        inviteAdapter.notifyDataSetChanged();

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void postInvite(String email) {
        Context ctx = getContext();
        Firebase mFirebaseInvites = new Firebase("https://household-manager-136.firebaseio.com/invites");
        Log.d("Post invite: ", email);
        Invite newInvite = new Invite(SaveSharedPreference.getHouseName(ctx), SaveSharedPreference.getGoogleEmail(ctx),
                email, SaveSharedPreference.getGooglePictureUrl(ctx), SaveSharedPreference.getPrefGroupId(ctx));
        mFirebaseInvites.push().setValue(newInvite);
    }

    private void authorizeFireBaseUser(String googleAccessToken) {

        ref.authWithOAuthToken("google", googleAccessToken, new Firebase.AuthResultHandler() {
            Context ctx = getContext();

            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "OnAuth ran");
                Log.d(TAG, authData.getProvider());
                Log.d(TAG, authData.getUid());
                SaveSharedPreference.setFirebaseUid(getContext(), authData.getUid());
                Member newMember = new Member(authData.getProviderData().get("displayName").toString(),
                        authData.getProvider(), SaveSharedPreference.getGooglePictureUrl(ctx), SaveSharedPreference.getGoogleEmail(ctx));

                ref.child("users").child(authData.getUid()).setValue(newMember);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.e("Auth error", firebaseError.getDetails());
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /*@TargetApi(21)
    public void writeNFC(){
        Context context = getContext();
        adapter = NfcAdapter.getDefaultAdapter(context);
        //Checks for NFC Hardware
        if (adapter == null) {
            // Stop here, we definitely need NFC
            displayMessage("This device doesn't support NFC.");
            getActivity().finish();
            return;
        }

        //Checks to see if NFC is enabled
        if (!adapter.isEnabled()) {
            displayMessage("NFC is disabled");
        }


        displayMessage("Please scan tag");
        //NFC Tag transfer: String -> NdefRecord -> NdefMessage
        NdefRecord ndefRecord = NdefRecord.createTextRecord("en", "Test");
        ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
        //Makes NFC available for scanning
        mainActivity.enableWriteMode();
    }

    *//*private void enableWriteMode(){
        mInWriteMode = true;
        pendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] filters = new IntentFilter[] { tagDetected};

        adapter.enableForegroundDispatch(getActivity(), pendingIntent, filters, null);
    }*//*

    public void onNewIntent(Intent intent){
        displayMessage("test");
        if(mInWriteMode){
            mInWriteMode = false;

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeTag(tag);
        }
    }

    public boolean writeTag(Tag tag){
        try{
            //to see if tag is already NDEF formatted
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                //Checks if tag is write-protected
                if(!ndef.isWritable()){
                    displayMessage("Read-Only tag.");
                    return false;
                }

                //Writes message to tag
                ndef.writeNdefMessage(ndefMessage);
                displayMessage("Tag written successfully.");
                return true;
            } else {
                //attempt to format the tag, tags must be formatted before writing
                NdefFormatable format = NdefFormatable.get(tag);
                //if not formatted
                if(format != null){
                    try{
                        format.connect();
                        format.format(ndefMessage);
                        displayMessage("Tag formatted successfully!");
                        return true;
                    } catch (IOException e){
                        displayMessage("Unable to format tag to NDEF.");
                        return false;
                    } catch (android.nfc.FormatException e){
                        displayMessage("Unable to format tag to NDEF.");
                        return false;
                    }
                    //if formated and not ndef then tag is unavailable for writing
                } else {
                    displayMessage("Tag doesn't appear to support NDEF format.");
                    return false;
                }
            }
        } catch (IOException e){
            displayMessage("Input Output Exception");
            return false;
        } catch (android.nfc.FormatException e){
            displayMessage("Formatting Exception");
            return false;
        }
    }

    public void displayMessage(String string){
        Toast.makeText(getContext(), string, Toast.LENGTH_LONG).show();
    }*/
}
