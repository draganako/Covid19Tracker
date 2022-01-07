package com.example.covid19tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NotificationsActivity extends AppCompatActivity {

    BottomNavigationView bottom_navigation_menu;

    private FirebaseUser firebaseUser;
    private FirebaseAuth mFirebaseAuth;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
        {
            case Configuration.UI_MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar=findViewById(R.id.notifications_toolbar);
        toolbar.inflateMenu(R.menu.toolbar_menu);

        mFirebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=mFirebaseAuth.getCurrentUser();

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId()==R.id.menu_item_display_mode)
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;}

                }
                if(item.getItemId()==R.id.menu_item_logout)
                {
                    if(firebaseUser != null)
                    {
                        clearAllSharedPrefs();
                        mFirebaseAuth.signOut();
                        Toast.makeText(getApplicationContext(), "Uspešno ste se odjavili", Toast.LENGTH_SHORT).show();

                        Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Odjava neuspešna!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        bottom_navigation_menu = findViewById(R.id.bottom_nav_view);
        bottom_navigation_menu.setSelectedItemId(R.id.navigation_notifications);

        bottom_navigation_menu.setOnNavigationItemSelectedListener
                (
                        item -> {
                            switch (item.getItemId()) {
                                case R.id.navigation_home:
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(0, 0);
                                    return true;

                                case R.id.navigation_symptoms:

                                    Intent intent1 = new Intent(getApplicationContext(), SymptomsActivity.class);
                                    startActivity(intent1);
                                    finish();
                                    overridePendingTransition(0, 0);
                                    return true;

                                case R.id.navigation_profile:

                                    Intent intent2 = new Intent(getApplicationContext(), ProfileActivity.class);
                                    startActivity(intent2);
                                    finish();
                                    overridePendingTransition(0, 0);
                                    return true;

                                case R.id.navigation_notifications:
                                    return true;

                            }
                            return true;
                        });
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
}