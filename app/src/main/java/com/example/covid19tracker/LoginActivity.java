package com.example.covid19tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.covid19tracker.DataModels.User;
import com.example.covid19tracker.DBData.UserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPref;

    EditText txtEmail;
    EditText txtPassword;
    TextView txtOrSignup;
    Button loginB;
    ProgressDialog progress;

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
        setContentView(R.layout.activity_login);

        UserData.getInstance().getUsers();
        firebaseAuth = FirebaseAuth.getInstance();

        progress = new ProgressDialog(this);
        txtEmail = findViewById(R.id.editTextLoginEmailAddress);
        txtPassword = findViewById(R.id.editLoginTextPersonName);
        txtOrSignup = findViewById(R.id.textViewLoginOrSignup);
        txtOrSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activity=new Intent(getApplicationContext(),SignupActivity.class);
                startActivity(activity);
                finish();
            }
        });

        loginB = findViewById(R.id.buttonLoginLog);
        loginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        UserData.getInstance().getUsers();
    }

    private void loginUser()
    {
        String email = txtEmail.getText().toString();
        String pass = txtPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(getApplicationContext(), "Email neispravan", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(pass))
        {
            Toast.makeText(getApplicationContext(), "Lozinka neispravna", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setMessage("Prijavljivanje u toku...");
        progress.show();

        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful()) {
                    ArrayList<User> probepos = new ArrayList<>();
                    int indexx = -1;

                    probepos = UserData.getInstance().getUsers();
                    for (int i = 0; i < probepos.size(); i++) {
                        String a = probepos.get(i).email;
                        if (a.compareTo(email) == 0) {
                            indexx = i;
                        }
                    }
                    User uu = new User();
                    if (indexx != -1) {
                        uu = probepos.get(indexx);

                        Context context = getApplicationContext();
                        sharedPref = context.getSharedPreferences(
                                "Userdata", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.loggedUser_email), uu.email);
                        editor.putString(getString(R.string.loggedUser_username), uu.username);
                        editor.putString(getString(R.string.loggedUser_image), uu.picture);
                        editor.putString(getString(R.string.loggedUser_sick),uu.sick);
                        editor.commit();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Prijavljivanje neuspešno", Toast.LENGTH_SHORT).show();
                }
                progress.dismiss();
            }
        });
    }
}