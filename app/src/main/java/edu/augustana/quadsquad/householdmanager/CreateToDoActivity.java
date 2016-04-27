package edu.augustana.quadsquad.householdmanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateToDoActivity extends AppCompatActivity {

    EditText etTask;
    EditText etDate;
    EditText etTime;
    ListView lvAssign;
    Button btnAssign;

    ArrayList<Member> memberArrayList;

    Firebase ref;
    Context ctx;

    FirebaseListAdapter<Member> memberAdapter;
    Member selectedMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_to_do);

        Toolbar toolbar = (Toolbar) findViewById(R.id.new_group_toolbar);
        setSupportActionBar(toolbar);

        assert toolbar != null;
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setTitle("New Todo Item");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final Calendar mCalendar = Calendar.getInstance();

        etTask = (EditText) findViewById(R.id.todo_task);
        lvAssign = (ListView) findViewById(R.id.todo_assign_list);
        etDate = (EditText) findViewById(R.id.todo_due_date_edit_text);
        etTime = (EditText) findViewById(R.id.todo_due_time_edit_text);
        btnAssign = (Button) findViewById(R.id.todo_toolbar_assign);
        ctx = getApplicationContext();

        memberArrayList = new ArrayList<>();

        final SimpleDateFormat sdfDate = new SimpleDateFormat("MM'/'dd'/'y");
        final SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a");
        String currentDate = sdfDate.format(mCalendar.getTime());
        String currentTime = sdfTime.format(mCalendar.getTime());
        etDate.setHint(currentDate);
        etTime.setHint(currentTime);

        Firebase.setAndroidContext(getApplicationContext());
        ref = new Firebase("https://household-manager-136.firebaseio.com");

        Firebase memberRef = ref.child("users");
        String groupID = SaveSharedPreference.getPrefGroupId(ctx);
        Query memberQuery = memberRef.orderByChild("groupReferal").startAt(groupID).endAt(groupID);

        memberAdapter = new FirebaseListAdapter<Member>(this, Member.class, R.layout.avatar_list_item, memberQuery) {
            @Override
            protected void populateView(View view, Member member, int position) {

                ((TextView) view.findViewById(R.id.name_view)).setText(member.getDisplayName());


                String photoURL = member.getContactPicURI();
                CircleImageView avatar = (CircleImageView) view.findViewById(R.id.avatar);

                if (!photoURL.equals("")) {
                    Picasso.with(ctx).load(photoURL).fit().into(avatar);
                }

                memberArrayList.add(position, member);


            }
        };
        lvAssign.setAdapter(memberAdapter);
        memberAdapter.notifyDataSetChanged();
        lvAssign.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        lvAssign.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //view.setSelected(true);
                selectedMember = memberArrayList.get(position);
                parent.setSelection(position);
            }
        });


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                etDate.setText(sdfDate.format(mCalendar.getTime()));
            }
        };

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(CreateToDoActivity.this, R.style.DialogTheme, date, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                datePicker.setTitle("Pick Due Date");
                datePicker.show();

            }
        });

        final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);

                etTime.setText(sdfTime.format(mCalendar.getTime()));
            }
        };

        etTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TimePickerDialog timePicker = new TimePickerDialog(CreateToDoActivity.this, R.style.DialogTheme, time, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), false);
                timePicker.setTitle("Pick Due Time");
                timePicker.show();
            }
        });

        btnAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMember != null) {
                    String task = etTask.getText().toString();
                    ToDoItem newTodoItem = new ToDoItem(task, SaveSharedPreference.getGoogleDisplayName(ctx),
                            selectedMember.getEmail(), selectedMember.getContactPicURI(),
                            false, mCalendar, SaveSharedPreference.getPrefGroupId(ctx));

                    Firebase todoRef = ref.child("todo");
                    todoRef.push().setValue(newTodoItem);
                    finish();
                }
            }
        });
    }
}
