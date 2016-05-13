package edu.augustana.quadsquad.householdmanager.model.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.augustana.quadsquad.householdmanager.R;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Invite;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Member;
import edu.augustana.quadsquad.householdmanager.data.preferences.SaveSharedPreference;
import edu.augustana.quadsquad.householdmanager.model.activity.MainActivity;

/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FindMyRoommatesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FindMyRoommatesFragment#newInstance} factory method to
 * create an instance of this fragment.*/


public class FindMyRoommatesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final String TAG = "findMyRoommates";
    public ListView membersList;
    public View rootView;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private List<String> nfcMemberList = new ArrayList<>();
    private List<TextView> nfcTextViewList = new ArrayList<>();
    private Firebase ref;
    private FirebaseListAdapter<Member> memberAdapter;
    private FirebaseListAdapter<Invite> inviteAdapter;

    public FindMyRoommatesFragment() {
        // Required empty public constructor
    }


     /* Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FindMyRoommatesFragment.*/


    // TODO: Rename and change types and number of parameters
    public static FindMyRoommatesFragment newInstance(String param1, String param2) {
        FindMyRoommatesFragment fragment = new FindMyRoommatesFragment();
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
        rootView = inflater.inflate(R.layout.fragment_find_my_roommates, container, false);
        // Inflate the layout for this fragment
        membersList = (ListView) rootView.findViewById(R.id.members_list_view);
        /*Firebase.setAndroidContext(getContext());
        ref = new Firebase("https://household-manager-136.firebaseio.com");
        bindListViews();*/
        return rootView;
    }

    @TargetApi(17)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        membersList = (ListView) rootView.findViewById(R.id.members_list_view);

        bindListViews();

        // manual location change
        // code adopted from http://developer.android.com/guide/topics/ui/controls/togglebutton.html
        Switch toggle = (Switch) rootView.findViewById(R.id.toggleSwitch);


        toggle.setChecked(SaveSharedPreference.getLocation(getContext()));
        Log.d("locationStatus", String.format("%b", SaveSharedPreference.getLocation(getContext())));

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                /*CircleImageView avatar = (CircleImageView) getView().findViewById(R.id.avatar);
                TextView tv = (TextView) getView().findViewById(R.id.name_view);
                if (isChecked && avatar != null) {
                    //the toggle is true
                    SaveSharedPreference.setLocation(getContext(), true);
                    Picasso.with(getContext()).load(R.drawable.ic_home_24dp).fit().into(avatar);
                    Toast.makeText(getContext(), "Home", Toast.LENGTH_SHORT).show();
                    tv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_home_24dp, 0);
                } else {
                    // the toggle is false
                    SaveSharedPreference.setLocation(getContext(), false);
                    Picasso.with(getContext()).load(R.drawable.ic_away_24dp).fit().into(avatar);
                    Toast.makeText(getContext(), "Away", Toast.LENGTH_SHORT).show();
                    tv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_away_24dp, 0);

                }*/
                toggleLocation();
            }
        });
    }

    @TargetApi(17)
    private void bindListViews() {
        Context ctx = getContext();
        Firebase memberRef = ref.child("users");
        String groupID = SaveSharedPreference.getPrefGroupId(getContext());
        Query memberQuery = memberRef.orderByChild("groupReferal").startAt(groupID).endAt(groupID);


        memberAdapter = new FirebaseListAdapter<Member>(getActivity(), Member.class, R.layout.avatar_status_list_item, memberQuery) {
            @Override
            protected void populateView(View view, Member member, int position) {
                ((TextView) view.findViewById(R.id.name_view)).setText(member.getDisplayName());


                String photoURL = member.getContactPicURI();
                CircleImageView avatar = (CircleImageView) view.findViewById(R.id.avatar);

                if (!photoURL.equals("")) {
                    Picasso.with(getContext()).load(photoURL).fit().into(avatar);
                }

                ImageView locationStatusImg = (ImageView) view.findViewById(R.id.statusImg);

                if (member.getLocationStatus().equals("Home")) {
                    locationStatusImg.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_home_24dp));
                } else {
                    locationStatusImg.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_away_24dp));
                }
            }
        };
        membersList.setAdapter(memberAdapter);
        MainActivity mainActivity = new MainActivity();
        mainActivity.setMemberList(membersList);
        memberAdapter.notifyDataSetChanged();

        //membersList.getItemAtPosition(0);

        /*TextView tv = (TextView) membersList.getChildAt(0);
        membersList.getChildCount();
        if(tv.getText().equals(SaveSharedPreference.getGoogleDisplayName(getContext()))){

            tv.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_home_24dp,0);
        }*/
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


     /* This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.*/

    @TargetApi(17)
    public void toggleLocation() {
        /*String currentName = SaveSharedPreference.getGoogleDisplayName(ctx);
        int index = nfcMemberList.indexOf(currentName);
        TextView currentTV = nfcTextViewList.get(index);*/
        Context ctx = getContext();
        final boolean newStatus = !SaveSharedPreference.getLocation(ctx);

        Firebase mFirebase = new Firebase("https://household-manager-136.firebaseio.com");
        Query userQuery = mFirebase.child("users").orderByChild("email").equalTo(SaveSharedPreference.getGoogleEmail(ctx));
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                    Firebase userRef = firstChild.getRef();
                    if (newStatus) {
                        userRef.child("locationStatus").setValue("Home");
                    } else {
                        userRef.child("locationStatus").setValue("Away");
                    }

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        SaveSharedPreference.setLocation(ctx, newStatus);
        if (newStatus) {
            Toast.makeText(ctx, "You are now home.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ctx, "You are now away.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}


