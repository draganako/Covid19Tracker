package com.example.covid19tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;

import com.example.covid19tracker.DBData.ContactData;
import com.example.covid19tracker.DataModels.Illness;
import com.example.covid19tracker.DBData.IllnessData;
import com.example.covid19tracker.DBData.NotificationData;
import com.example.covid19tracker.DataModels.User;
import com.example.covid19tracker.DBData.UserData;
import com.example.covid19tracker.DBData.UserPositionData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class LauncherActivity extends AppCompatActivity implements  UserData.ReadyEventListener,
        IllnessData.ReadyEventListener, UserPositionData.ReadyEventListener,
        NotificationData.ReadyEventListener,ContactData.ReadyEventListener
        {

    SharedPreferences sharedPref;
    FirebaseAuth firebaseAuth;
    int allDataOk = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
        setContentView(R.layout.activity_launcher);

        firebaseAuth = FirebaseAuth.getInstance();

        IllnessData.getInstance().setReadyList(this);
        UserData.getInstance().setReadyList(this);
        UserPositionData.getInstance().setReadyList(this);
        NotificationData.getInstance().setReadyList(this);
        ContactData.getInstance().setReadyList(this);

        if (UserData.getInstance().getUsers().size() > 0 &&
                    UserPositionData.getInstance().getPositions().size()>0)
        {
            go();
        }

    }

    @Override
    protected void onDestroy() {
        IllnessData.getInstance().setReadyList(null);
        UserData.getInstance().setReadyList(null);
        UserPositionData.getInstance().setReadyList(null);
        NotificationData.getInstance().setReadyList(null);
        ContactData.getInstance().setReadyList(null);

        super.onDestroy();
    }

    void go()
    {
        sharedPref = getApplicationContext().getSharedPreferences("Userdata", Context.MODE_PRIVATE);
        String emaill = sharedPref.getString(getString(R.string.loggedUser_email), "EMPTY");

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && emaill.compareTo("EMPTY") != 0) {

            User u = UserData.getInstance().getUser(emaill);

            Intent loginIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(loginIntent);
            finish();
        } else
            {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {

                    clearAllSharedPrefs();
                    Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
                    startActivity(intent);

                    finish();
                }
            }, 500);
        }


    }

    private void clearAllSharedPrefs()
    {
        sharedPref = getApplicationContext().getSharedPreferences("Userdata", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.remove(getString(R.string.loggedUser_username));
        editor.remove(getString(R.string.loggedUser_email));
        editor.remove(getString(R.string.loggedUser_image));
        editor.remove(getString(R.string.loggedUser_sick));

        editor.remove(getString(R.string.loggedUser_position_lt));
        editor.remove(getString(R.string.loggedUser_position_lg));

        editor.commit();
    }

    @Override
    public void onReady() {
        allDataOk++;

        if (allDataOk == 5) {
            go();
            allDataOk = 0;
        }
    }

}


