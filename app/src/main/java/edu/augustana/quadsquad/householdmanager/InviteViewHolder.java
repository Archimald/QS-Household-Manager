package edu.augustana.quadsquad.householdmanager;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by micha on 4/6/2016.
 */
public class InviteViewHolder extends RecyclerView.ViewHolder {
    protected TextView vHouseName;
    protected TextView vFromText;
    protected ImageView iProfile;
    protected Button bJoin;
    protected Button bDismiss;
    protected String key;

    public InviteViewHolder(View v) {
        super(v);
        vHouseName = (TextView) v.findViewById(R.id.title);
        vFromText = (TextView) v.findViewById(R.id.txtName);
        bJoin = (Button) v.findViewById(R.id.join_button);
        bDismiss = (Button) v.findViewById(R.id.dismiss_button);
        iProfile = (ImageView) v.findViewById(R.id.imageView);
    }
}

