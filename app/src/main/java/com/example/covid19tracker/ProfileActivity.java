package com.example.covid19tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.covid19tracker.Adapters.ViewPagerAdapter;
import com.example.covid19tracker.DBData.IllnessData;
import com.example.covid19tracker.DBData.ContactData;
import com.example.covid19tracker.DataModels.User;
import com.example.covid19tracker.DBData.UserData;
import com.example.covid19tracker.Fragments.IllnessDatesFragment;
import com.example.covid19tracker.Fragments.ContactsFragment;
import com.example.covid19tracker.Fragments.ViewPagerContainerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth;
    private FirebaseUser firebaseUser;
    SharedPreferences sharedPref;
    private User loggedUser;
    private SharedPreferences sharedPreferences;

    ImageButton imageButtonEditProfile;
    ImageView imageViewProfilePic;
    ImageView userHealthStatus;
    BottomNavigationView bottom_navigation_menu;
    TextView textViewProfileUsername;
    TextView textViewProfileNameAndSurname;

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
        setContentView(R.layout.activity_profile);

        mFirebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=mFirebaseAuth.getCurrentUser();

        sharedPref = getApplicationContext().getSharedPreferences( "Userdata", Context.MODE_PRIVATE);
        loggedUser= UserData.getInstance().getUser(sharedPref.getString(getString(R.string.loggedUser_email),"email"));

        Toolbar toolbar=findViewById(R.id.profile_toolbar);
        toolbar.inflateMenu(R.menu.toolbar_menu);

        imageButtonEditProfile=toolbar.findViewById(R.id.imageButtonEditProfile);
        imageButtonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent1 = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivityForResult(intent1,567);
            }
        });

        textViewProfileUsername=findViewById(R.id.textViewProfileUsername);
        imageViewProfilePic=findViewById(R.id.imageViewProfilePicture);

        sharedPreferences=getApplicationContext().getSharedPreferences("Userdata",Context.MODE_PRIVATE);

        String username=sharedPreferences.getString(getString(R.string.loggedUser_username),"");
        textViewProfileUsername.setText(username);

        String image=sharedPreferences.getString(getString(R.string.loggedUser_image),"");

        if (image != null && !(image.compareTo("") == 0)) {
            Glide.with(this).load(image).into(imageViewProfilePic);
        }
        else
            imageViewProfilePic.setImageResource(R.drawable.ic_baseline_account_box_24);

        textViewProfileNameAndSurname=findViewById(R.id.textViewProfileNameAndSurname);
        textViewProfileNameAndSurname.setText(loggedUser.name +" "+loggedUser.surname);

        userHealthStatus=findViewById(R.id.ProfileuserHealthStatus);
        if(loggedUser.sick.equals("sick"))
            userHealthStatus.setImageResource(R.drawable.ic_baseline_sick_24);
        else if (loggedUser.sick.equals("healthy"))
            userHealthStatus.setImageResource(R.drawable.ic_baseline_healthy_24);
        else
            userHealthStatus.setImageResource(R.drawable.ic_baseline_face_24);

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
                    {
                        Toast.makeText(getApplicationContext(), "Odjava neuspešna!", Toast.LENGTH_SHORT).show();
                    }

                }
                return false;
            }
        });

        bottom_navigation_menu = findViewById(R.id.bottom_nav_view);
        bottom_navigation_menu.setSelectedItemId(R.id.navigation_profile);

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
                                    return true;

                                case R.id.navigation_notifications:
                                    Intent intentt = new Intent(getApplicationContext(), NotificationsActivity.class);
                                    startActivity(intentt);
                                    finish();
                                    overridePendingTransition(0, 0);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (567):
                {
                if (resultCode == Activity.RESULT_OK)
                {
                    String pic = sharedPref.getString(getString(R.string.loggedUser_image), "");

                    if (pic != null && !(pic.compareTo("") == 0)) {
                        Glide.with(this).load(pic).into(imageViewProfilePic);
                    } else
                        imageViewProfilePic.setImageResource(R.drawable.ic_baseline_account_box_24);

                    loggedUser.sick=sharedPref.getString(getString(R.string.loggedUser_sick),"");
                    if(loggedUser.sick.equals("sick"))
                        userHealthStatus.setImageResource(R.drawable.ic_baseline_sick_24);
                    else if (loggedUser.sick.equals("healthy"))
                        userHealthStatus.setImageResource(R.drawable.ic_baseline_healthy_24);
                    else
                        userHealthStatus.setImageResource(R.drawable.ic_baseline_face_24);
                }

                fillListData();

                break;
            }
        }
    }

    private void fillListData()
    {
        ViewPagerContainerFragment vpcf = (ViewPagerContainerFragment)
                getSupportFragmentManager().findFragmentById(R.id.profileViewPagerContainer);
        ViewPagerAdapter vpa=(ViewPagerAdapter) vpcf.vpc_viewpager.getAdapter();

        IllnessDatesFragment idf=(IllnessDatesFragment) vpa.fragmentList.get(1);
        idf.illnessDatesAdapter.illnessList= IllnessData.getInstance().getIllnessesByUsername(sharedPref.getString(getString(R.string.loggedUser_username), ""));
        idf.illnessDatesAdapter.notifyDataSetChanged();

        ContactsFragment lpf=(ContactsFragment) vpa.fragmentList.get(0);
        lpf.adapter.contactList= ContactData.getInstance().getUserContacts
                (sharedPref.getString(getString(R.string.loggedUser_username), ""));
        lpf.adapter.notifyDataSetChanged();
    }


}


