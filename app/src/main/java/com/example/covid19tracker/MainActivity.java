package com.example.covid19tracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.covid19tracker.DBData.IllnessData;
import com.example.covid19tracker.DataModels.Notification;
import com.example.covid19tracker.DBData.NotificationData;
import com.example.covid19tracker.DataModels.User;
import com.example.covid19tracker.DBData.UserData;
import com.example.covid19tracker.DataModels.UserPosition;
import com.example.covid19tracker.DBData.UserPositionData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.MarkerOptions;

import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private boolean locationPermissionsGranted = false;

    private String username;
    private GoogleMap map;
    private UserPosition loggedUserPosition;
    private UserPosition loggedUserPrevPosition;

    String choiceHealthy = "";

    public static UserPosition loggedUserPos;
    private static UserPosition loggedUserPrevPos;

    private FirebaseUser firebaseUser;
    SharedPreferences sharedPref;
    FirebaseAuth mFirebaseAuth;

    private String onesignalNotifUrl = "https://onesignal.com/api/v1/notifications";

    private LocationManager locationManager;
    private int radius;

    BottomNavigationView bottom_navigation_menu;
    private ImageButton imageButtonHealth;

    @RequiresApi(api = Build.VERSION_CODES.O)
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

        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        setContentView(R.layout.activity_main);

        MyTimer timer = new MyTimer(getApplicationContext());
        timer.sendEmptyMessage(MyTimer.TIMER_1);

        sharedPref = getApplicationContext().getSharedPreferences("Userdata", Context.MODE_PRIVATE);
        username = sharedPref.getString(getString(R.string.loggedUser_username), "username");

        radius=sharedPref.getInt(getString(R.string.loggedUser_Radius), 1);

        User loggedUser = UserData.getInstance().getUser(sharedPref.getString(getString(R.string.loggedUser_email), ""));
        setExternalUserId(loggedUser.key);

        getLocationPermission();

        if (getIntent().getBooleanExtra("firstUse", false)) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Obaveštenje");
            alertDialog.setMessage("Ikonica u gornjem desnom uglu označava zdravstveni status korisnika");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.inflateMenu(R.menu.toolbar_menu);

        final String health = sharedPref.getString(getString(R.string.loggedUser_sick), "");
        imageButtonHealth = toolbar.findViewById(R.id.imageButtonMain);
        if (health.equals("healthy"))
            imageButtonHealth.setImageResource(R.drawable.ic_baseline_healthy_24);
        else if (health.equals("sick")) {
            IllnessData.getInstance().updateIllness(username, new Date());
            imageButtonHealth.setImageResource(R.drawable.ic_baseline_sick_24);
        } else
            imageButtonHealth.setImageResource(R.drawable.ic_baseline_face_24);

        imageButtonHealth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Promenite zdravstveno stanje:");

                String[] opts = {"Zdrav", "Bolestan"};

                builder.setItems(opts, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0)
                            choiceHealthy = "healthy";

                        else
                            choiceHealthy = "sick";

                        dialog.dismiss();

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        SharedPreferences.Editor editor = sharedPref.edit();

                        if (choiceHealthy.equals("healthy")) {

                            editor.putString(getString(R.string.loggedUser_sick), "healthy");
                            editor.commit();
                            UserData.getInstance().updateUserHealth(username, "healthy");
                            imageButtonHealth.setImageResource(R.drawable.ic_baseline_healthy_24);
                            Toast.makeText(MainActivity.this, "Zdravstveno stanje promenjeno", Toast.LENGTH_SHORT).show();

                        } else if (choiceHealthy.equals("sick")) {
                            editor.putString(getString(R.string.loggedUser_sick), "sick");
                            editor.commit();

                            UserData.getInstance().updateUserHealth(username, "sick");
                            IllnessData.getInstance().updateIllness(username, new Date());

                            imageButtonHealth.setImageResource(R.drawable.ic_baseline_sick_24);
                            Toast.makeText(MainActivity.this, "Zdravstveno stanje promenjeno", Toast.LENGTH_SHORT).show();

                            CheckPositionsInRadius();

                        }


                    }
                });
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.menu_item_display_mode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                    }
                }
                if (item.getItemId() == R.id.menu_item_logout)
                {
                    SharedPreferences.Editor editor = sharedPref.edit();

                    editor.remove(getString(R.string.loggedUser_username));
                    editor.remove(getString(R.string.loggedUser_email));
                    editor.remove(getString(R.string.loggedUser_image));
                    editor.remove(getString(R.string.loggedUser_sick));
                    editor.commit();

                    if (firebaseUser != null) {

                        clearAllSharedPrefs();
                        mFirebaseAuth.signOut();
                        Toast.makeText(MainActivity.this, "Uspešno ste se odjavili", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                        Toast.makeText(MainActivity.this, "Odjava neuspešna!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        bottom_navigation_menu = findViewById(R.id.bottom_nav_view);
        bottom_navigation_menu.setSelectedItemId(R.id.navigation_home);

        bottom_navigation_menu.setOnNavigationItemSelectedListener
            (
                item -> {
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            return true;

                        case R.id.navigation_symptoms:

                            Intent intent = new Intent(getApplicationContext(), SymptomsActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(0, 0);
                            return true;

                        case R.id.navigation_profile:

                            Intent intent1 = new Intent(getApplicationContext(), ProfileActivity.class);
                            startActivity(intent1);
                            finish();
                            overridePendingTransition(0, 0);
                            return true;

                        case R.id.navigation_notifications:
                            Intent intentt = new Intent(getApplicationContext(), NotificationsActivity.class);
                            startActivity(intentt);
                            finish();
                            overridePendingTransition(0, 0);
                            return true;

                    }
                    return true;
                }
            );
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

    public boolean isServiceVersionRight() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS)
            return true;
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else
            Toast.makeText(MainActivity.this, "Ne može se pristupiti mapi", Toast.LENGTH_SHORT).show();
        return false;

    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
        };
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionsGranted = true;
                if (isServiceVersionRight())
                    initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            locationPermissionsGranted = false;
                            return;
                        }
                    }
                    locationPermissionsGranted = true;
                    if (isServiceVersionRight())
                        initMap();
                }
            }
        }
    }

    public void onMapReady(GoogleMap googleMap)
    {
        loadNearbyUsers(googleMap);

        map = googleMap;
        if (locationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return;

            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);

        }
    }


    private void loadNearbyUsers(GoogleMap googleMap) {
        ArrayList<UserPosition> userPositions = UserPositionData.getInstance()
                .getOthSickPositionsNearby(username, loggedUserPosition,new Date(), radius);

        for (UserPosition up : userPositions) {
            LatLng nposition = new LatLng(up.latitude, up.longitude);
            googleMap.addMarker(new MarkerOptions()
                    .position(nposition)
                    .title(up.username));
        }

    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);
    }

    private void getDeviceLocation() {

        try {
            if (locationPermissionsGranted)
                getLocation();

        } catch (SecurityException e) {
        }
    }

    private void moveCamera(LatLng latLng) {
        if (getIntent().hasExtra("push_message")) {
            String notifMess = getIntent().getStringExtra("push_message");
            String latlngstring = notifMess.substring(notifMess.lastIndexOf("Koordinate:") + 12);
            String[] parts = latlngstring.split(",");

            float lat = Float.parseFloat(parts[0]);
            float lng = Float.parseFloat(parts[1].substring(1));

            LatLng coordinate = new LatLng(lat, lng);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, DEFAULT_ZOOM), 3000, null);
            Toast.makeText(MainActivity.this, "Lokacija zaraženog korisnika", Toast.LENGTH_SHORT).show();
        } else if (getIntent().hasExtra("contact_latitude"))
        {
            float lat = getIntent().getFloatExtra("contact_latitude", 0f);
            float lng = getIntent().getFloatExtra("contact_latitude", 0f);

            LatLng coordinate = new LatLng(lat, lng);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, DEFAULT_ZOOM), 3000, null);
            Toast.makeText(MainActivity.this, "Lokacija zaraženog korisnika", Toast.LENGTH_SHORT).show();
        } else
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    public void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null)
            myLocation = getLastKnownLocation();

        if (myLocation != null) {
            moveCamera(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(getString(R.string.loggedUser_position_lg), (float) myLocation.getLongitude());
            editor.putFloat(getString(R.string.loggedUser_position_lt), (float) myLocation.getLatitude());

            loggedUserPrevPosition = loggedUserPosition;
            loggedUserPrevPos = loggedUserPrevPosition;

            loggedUserPosition = new UserPosition(myLocation.getLatitude(), myLocation.getLongitude(), username, new Date());
            loggedUserPos = loggedUserPosition;
        } else {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(getString(R.string.loggedUser_position_lg), 0.0f);
            editor.putFloat(getString(R.string.loggedUser_position_lt), 0.0f);

            loggedUserPrevPosition = loggedUserPosition;
            loggedUserPrevPos = loggedUserPrevPosition;

            loggedUserPosition = new UserPosition(0.0f, 0.0f, username, new Date());
            loggedUserPos = loggedUserPosition;
        }

        String isUserHealthy = sharedPref.getString(getString(R.string.loggedUser_sick), "");
        if (!isUserHealthy.equals("healthy"))
            CheckPositionsInRadius();

    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission")
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private static void writePositionToDB() {
        UserPosition up = loggedUserPos;
        if (loggedUserPos != null) {
            if (loggedUserPrevPos != null) {
                if (movedMoreThan(0.05f)) //za 1.11 * 5 km pomeren
                    UserPositionData.getInstance().AddPosition(loggedUserPos);
            } else if (loggedUserPos.latitude != 0 && loggedUserPos.longitude != 0) {
                UserPositionData.getInstance().AddPosition(loggedUserPos);
            }
            loggedUserPrevPos = loggedUserPos;

        }
    }

    private static boolean movedMoreThan(float value) {
        return Math.abs(loggedUserPrevPos.latitude - loggedUserPos.latitude) >= value
                || Math.abs(loggedUserPrevPos.longitude - loggedUserPos.longitude) >= value;
    }

    public void CheckPositionsInRadius() {
        ArrayList<UserPosition> ups = UserPositionData.getInstance().getPositions();
        for (UserPosition userpos : ups) {
            if (!userpos.username.equals(username)) {
                if (isInRadius(userpos) && !UserData.getInstance().getUserByUsername(userpos.username).sick.equals("sick")
                        && (isIn1Hour(userpos)))
                    sendNotification(userpos);

            }
        }
    }

    boolean isInRadius(UserPosition userpos) {
        return Math.abs(userpos.latitude - loggedUserPosition.latitude) < sharedPref.getInt(getString(R.string.loggedUser_Radius), 1) * 0.015060
                && Math.abs(userpos.longitude - loggedUserPosition.longitude) < sharedPref.getInt(getString(R.string.loggedUser_Radius), 1) * 0.015060;
    }

    boolean isIn1Hour(UserPosition userpos) {
        return Math.abs(userpos.date.getTime() - loggedUserPosition.date.getTime()) <= TimeUnit.HOURS.toMillis(1);
    }

    void setExternalUserId(String externalUserId) {
        OneSignal.setExternalUserId(externalUserId, new OneSignal.OSExternalUserIdUpdateCompletionHandler() {
            @Override
            public void onSuccess(JSONObject results) {

                OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "Set external user id done with results: " + results.toString());
                try {
                    if (results.has("push") && results.getJSONObject("push").has("success")) {
                        boolean isPushSuccess = results.getJSONObject("push").getBoolean("success");
                        OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "Set external user id for push status: " + isPushSuccess);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (results.has("email") && results.getJSONObject("email").has("success")) {
                        boolean isEmailSuccess = results.getJSONObject("email").getBoolean("success");
                        OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "Sets external user id for email status: "
                                                                                                                    + isEmailSuccess);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(OneSignal.ExternalIdError error) {
                OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "Set external user id done with error: " + error.toString());
            }
        });
    }

    void sendNotification(UserPosition userPos) {

        JSONObject jsonBody;
        User recipient = UserData.getInstance().getUserByUsername(userPos.username);
        String temp = "";

        if (sharedPref.getString(getString(R.string.loggedUser_sick), "").equals("sick"))
            temp = "Zaražena";
        else
            temp = "Potencijalno zaražena";

        String appId="";
        String aKey="";

        try {
            ApplicationInfo ai= getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle=ai.metaData;

            appId=bundle.getString("appKey");
            aKey=bundle.getString("aKey");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        try {
            jsonBody = new JSONObject(
                    "{'app_id':'"+ appId +"'," +
                            "'headings': {'en': 'Obaveštenje'}, " +
                            "'contents': {'en': '" + temp + " osoba " + username + " bila je dana " + DateFormat.format("dd. MM. yyyy.", userPos.date) +
                            " na lokaciji u Vašoj blizini.\nKoordinate: " +
                            loggedUserPosition.latitude + ", " + loggedUserPosition.longitude + "'}, " +
                            "'include_external_user_ids': ['" + recipient.key + "'], " +
                            "'channel_for_external_user_ids': 'push'}");

            String finalAKey = aKey;
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, onesignalNotifUrl, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(MainActivity.this, "Notifikacija uspešno poslata", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Greška pri slanju notifikacije", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Basic "+ finalAKey);
                    params.put("Content-type", "application/json");
                    return params;
                }
            };

            Volley.newRequestQueue(MainActivity.this).add(jsonRequest);

            NotificationData.getInstance().AddNotification(new Notification(username, (float) loggedUserPosition.latitude,
                    (float) loggedUserPosition.longitude,
                    DateFormat.format("dd. MM. yyyy.", userPos.date).toString(), recipient.username, false));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static class MyTimer extends Handler {

        public static final int TIMER_1 = 0;
        Context context;

        public MyTimer(Context conn) {
            context = conn;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMER_1:

                    Log.d("TimerExample", "Timer 1");
                    writePositionToDB();

                    sendEmptyMessageDelayed(TIMER_1, 300000); //5 min
                    break;

                default:
                    removeMessages(TIMER_1);
                    break;
            }
        }
    }
}