package edu.augustana.quadsquad.householdmanager.model.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.augustana.quadsquad.householdmanager.R;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Invite;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.Member;
import edu.augustana.quadsquad.householdmanager.data.preferences.SaveSharedPreference;

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


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ListView membersList;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_my_roommates, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        membersList = (ListView) getView().findViewById(R.id.members_list_view);

        bindListViews();

        // manual location change
        // code adopted from http://developer.android.com/guide/topics/ui/controls/togglebutton.html
        Switch toggle = (Switch) getView().findViewById(R.id.toggleSwitch);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    //the toggle is true
                    SaveSharedPreference.setLocation(getContext(), true);
                } else {
                    // the toggle is false
                    SaveSharedPreference.setLocation(getContext(), false);
                }
            }
        });

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


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}


