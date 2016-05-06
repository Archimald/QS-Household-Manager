package edu.augustana.quadsquad.householdmanager.model.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.augustana.quadsquad.householdmanager.R;
import edu.augustana.quadsquad.householdmanager.data.firebaseobjects.ToDoItem;
import edu.augustana.quadsquad.householdmanager.data.preferences.SaveSharedPreference;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ToDoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ToDoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ToDoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    final SimpleDateFormat sdfDate = new SimpleDateFormat("MM'/'dd'/'y");
    final SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a");
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private Firebase ref;
    private ListView lvTodo;
    private FirebaseListAdapter<ToDoItem> todoAdapter;

    public ToDoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ToDoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ToDoFragment newInstance(String param1, String param2) {
        ToDoFragment fragment = new ToDoFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_to_do, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lvTodo = (ListView) getView().findViewById(R.id.todo_listView);
        Firebase.setAndroidContext(getContext());
        ref = new Firebase("https://household-manager-136.firebaseio.com");
        Firebase todoRef = ref.child("todo");
        String groupID = SaveSharedPreference.getPrefGroupId(getContext());
        final Query todoQuery = todoRef.orderByChild("groupTag").startAt(groupID).endAt(groupID);
        final Query todoQuery2 = todoRef.orderByChild("completed");

        todoAdapter = new FirebaseListAdapter<ToDoItem>(getActivity(), ToDoItem.class, R.layout.todo_item, todoQuery) {
            @Override
            protected void populateView(View view, final ToDoItem todoItem, final int position) {

                TextView mainText = (TextView) view.findViewById(R.id.todo_action_item);
                TextView subText = (TextView) view.findViewById(R.id.todo_due_date);
                final AppCompatCheckBox doneCheck = (AppCompatCheckBox) view.findViewById(R.id.todo_check);


                mainText.setText(todoItem.getActionText());
                subText.setText("Due: " + sdfDate.format(todoItem.getDueDate().getTime())
                        + " at " + sdfTime.format(todoItem.getDueDate().getTime()));
                doneCheck.setChecked(todoItem.isCompleted());


                Date dueDate = todoItem.getDueDate().getTime();
                Date today = Calendar.getInstance().getTime();

                Log.d("dueDate", dueDate.toString());
                Log.d("today", today.toString());


                if (dueDate.before(today) && !todoItem.isCompleted()) {
                    view.setBackgroundColor(getResources().getColor(R.color.colorError));

                    mainText.setTypeface(null, Typeface.BOLD);
                    mainText.setTextColor(getResources().getColor(R.color.colorIcons));

                    subText.setTextColor(getResources().getColor(R.color.colorIcons));
                } else {
                    view.setBackgroundColor(getResources().getColor(android.R.color.background_light));

                    mainText.setTypeface(null, Typeface.NORMAL);
                    mainText.setTextColor(getResources().getColor(R.color.colorTextPrimary));

                    subText.setTextColor(getResources().getColor(R.color.colorTextSecondary));
                }


                String photoURL = todoItem.getAvatarUrl();
                CircleImageView avatar = (CircleImageView) view.findViewById(R.id.todo_avatar);

                if (!photoURL.equals("")) {
                    Picasso.with(getContext()).load(photoURL).fit().into(avatar);
                }

                doneCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Firebase checkRef = todoAdapter.getRef(position).child("completed");
                        checkRef.setValue(doneCheck.isChecked());
                    }
                });


            }
        };
        lvTodo.setAdapter(todoAdapter);
        todoAdapter.notifyDataSetChanged();
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
        todoAdapter.cleanup();
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
