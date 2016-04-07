package edu.augustana.quadsquad.householdmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by micha on 4/4/2016.
 */
public class SplashActivity extends AppCompatActivity {

    boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isLoggedIn = SaveSharedPreference.getIsLoggedIn(getApplicationContext());

        if (isLoggedIn) {
            Intent intent = new Intent(this, GroupActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


    }
}