package edu.augustana.quadsquad.householdmanager.model.activity;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;

import edu.augustana.quadsquad.householdmanager.R;
import edu.augustana.quadsquad.householdmanager.data.preferences.SaveSharedPreference;

//code adapted from:
//http://www.framentos.com/en/android-tutorial/2012/07/31/write-hello-world-into-a-nfc-tag-with-a/
//http://www.creativebloq.com/android/getting-started-nfc-android-5122811


public class NfcWriteActivity extends AppCompatActivity {

    NfcAdapter adapter;
    PendingIntent pendingIntent;
    boolean mInWriteMode;
    NdefMessage ndefMessage;
    Context context;

    @Override
    @TargetApi(21)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_write);
        context = this;

        Button btnWrite = (Button) findViewById(R.id.button);
        /*final EditText message = (EditText)findViewById(R.id.edit_message);*/
        adapter = NfcAdapter.getDefaultAdapter(this);
        final String groupID = SaveSharedPreference.getPrefGroupId(this);

        //Checks for NFC Hardware
        if (adapter == null) {
            // Stop here, we definitely need NFC
            displayMessage("This device doesn't support NFC.");
            finish();
            return;
        }

        //Checks to see if NFC is enabled
        if (!adapter.isEnabled()) {
            displayMessage("NFC is disabled");
        }

        if(btnWrite != null) {
            btnWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle("Scan Tag")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                    String message = groupID;
                    if(message != null){
                        //NFC Tag transfer: String -> NdefRecord -> NdefMessage
                        NdefRecord ndefRecord = NdefRecord.createTextRecord("en", message);
                        ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord, NdefRecord.createApplicationRecord("edu.augustana.quadsquad.householdmanager.model.activity")});
                        //Makes NFC available for scanning
                        enableWriteMode();
                    } else {
                        alertDialog = new AlertDialog.Builder(context)
                                .setTitle("Error with Firebase Connection")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).show();
                    }
                }

            });
        }


    }

    @TargetApi(21)
    private void enableWriteMode(){
        mInWriteMode = true;
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] filters = new IntentFilter[] { tagDetected};

        adapter.enableForegroundDispatch(this, pendingIntent, filters, null);
    }

    public void goHome(){
        Intent homeIntent = new Intent(NfcWriteActivity.this, MainActivity.class);
        startActivity(homeIntent);
    }

    @TargetApi(21)
    public void onNewIntent(Intent intent){
        if(mInWriteMode){
            mInWriteMode = false;

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeTag(tag);
        }
    }

    @TargetApi(21)
    private boolean writeTag(Tag tag){
        try{
            //to see if tag is already NDEF formatted
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                //Checks if tag is write-protected
                if(!ndef.isWritable()){
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setTitle("Read Only Tag")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                    return false;
                }

                //Writes message to tag
                ndef.writeNdefMessage(ndefMessage);
                /*displayMessage("Tag written successfully.");*/
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Tag Written Successfully")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goHome();
                            }
                        }).show();


                return true;
            } else {
                //attempt to format the tag, tags must be formatted before writing
                NdefFormatable format = NdefFormatable.get(tag);
                //if not formatted
                if(format != null){
                    try{
                        format.connect();
                        format.format(ndefMessage);
                        AlertDialog alertDialog = new AlertDialog.Builder(this)
                                .setTitle("Tag Formatted Successfully")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).setIcon(R.drawable.ic_insert_emoticon_24dp).show();
                        return true;
                    } catch (IOException e){
                        AlertDialog alertDialog = new AlertDialog.Builder(this)
                                .setTitle("Unable to Format Tag to NDEF")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).setIcon(R.drawable.ic_insert_emoticon_24dp).show();
                        return false;
                    } catch (android.nfc.FormatException e){
                        AlertDialog alertDialog = new AlertDialog.Builder(this)
                                .setTitle("Unable to Format Tag to NDEF")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).setIcon(R.drawable.ic_insert_emoticon_24dp).show();
                        return false;
                    }
                    //if formated and not ndef then tag is unavailable for writing
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setTitle("Tag doesn't appear to support NDEF")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).setIcon(R.drawable.ic_insert_emoticon_24dp).show();
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

    private void displayMessage(String string){
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }
}
