package edu.augustana.quadsquad.householdmanager.data.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.augustana.quadsquad.householdmanager.R;

/**
 * Created by Luke Currie on 4/26/2016.
 */
public class CorkboardViewHolder  extends RecyclerView.ViewHolder {
    TextView vMessage;
    TextView vFromName;
    Button bDelete;

    public CorkboardViewHolder(View v) {
        super(v);
        vMessage = (TextView) v.findViewById(R.id.corkboard_message);
        vFromName = (TextView) v.findViewById(R.id.txtCorkboardFromName);
        bDelete = (Button) v.findViewById(R.id.corkboard_delete_button);


    }

    public TextView getvMessage(){return vMessage;}
    public TextView getvFromName(){return vFromName;}
    public Button getbDelete() {return bDelete; }
}
