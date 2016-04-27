package edu.augustana.quadsquad.householdmanager.model.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;
import com.firebase.ui.FirebaseRecyclerAdapter;

import java.util.List;

import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.CorkboardNote;
import edu.augustana.quadsquad.householdmanager.data.viewholder.CorkboardViewHolder;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Invite;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Member;
import edu.augustana.quadsquad.householdmanager.R;
import edu.augustana.quadsquad.householdmanager.data.preferences.SaveSharedPreference;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CorkboardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CorkboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CorkboardFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private final String TAG = "Corkboard";

    private FloatingActionButton btnAddNote;

    private OnFragmentInteractionListener mListener;

    private Firebase ref;
    private FirebaseListAdapter<Member> memberAdapter;
    private FirebaseListAdapter<Invite> inviteAdapter;

    private StaggeredGridLayoutManager sglm;
    private FirebaseRecyclerAdapter<CorkboardNote, CorkboardViewHolder> noteAdapter;
    private RecyclerView rv;
    private List<CorkboardNote> notesList;

    public CorkboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CorkboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CorkboardFragment newInstance(String param1, String param2) {
        CorkboardFragment fragment = new CorkboardFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_corkboard, container, false);


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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnAddNote = (FloatingActionButton) getView().findViewById(R.id.create_card);
        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onNewNoteClicked();
            }
        });

        rv = (RecyclerView) view.findViewById(R.id.corkboard_recycler_view);
        rv.setHasFixedSize(true);

        sglm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rv.setLayoutManager(sglm);

        bindRecyclerView();

    }

    private void  bindRecyclerView() {
        Firebase notesRef = ref.child("corkboardnotes");
        String groupID = SaveSharedPreference.getPrefGroupId(getContext());
        Query noteQuery = notesRef.orderByChild("groupId").startAt(groupID).endAt(groupID);

        noteAdapter = new FirebaseRecyclerAdapter<CorkboardNote, CorkboardViewHolder>(CorkboardNote.class, R.layout.corkboard_card, CorkboardViewHolder.class, noteQuery) {

            @Override
            protected void populateViewHolder(final CorkboardViewHolder view, final CorkboardNote note, int position) {

                final Firebase cardRef=this.getRef(position);

                view.getvMessage().setText(note.getMessage());
                view.getvFromName().setText(note.getFromText());

                view.getbDelete().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cardRef.removeValue();
                    }
                });


//                String photoURL = member.getContactPicURI();
//                CircleImageView avatar = (CircleImageView) view.findViewById(R.id.avatar);
//
//                if (!photoURL.equals("")) {
//                    Picasso.with(getContext()).load(photoURL).fit().into(avatar);
//                }


            }
        };
        rv.setAdapter(noteAdapter);
        noteAdapter.notifyDataSetChanged();

    }
    protected void onNewNoteClicked() {
        new MaterialDialog.Builder(getContext())
                .title("New Note")
                .content("Enter Message")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Message", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        postNote(input.toString());
                    }
                }).show();
    }

    private void postNote(String message) {
        Context ctx = getContext();
        Firebase mCorkboardNotes = new Firebase("https://household-manager-136.firebaseio.com/corkboardnotes");
        Log.d("Post note: ", message);
        CorkboardNote newNote = new CorkboardNote(message, SaveSharedPreference.getGoogleDisplayName(ctx),
                SaveSharedPreference.getPrefGroupId(ctx), SaveSharedPreference.getGoogleEmail(ctx));
        mCorkboardNotes.push().setValue(newNote);
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
}
