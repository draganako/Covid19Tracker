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

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    EditText txtName;
    EditText txtSurname;
    EditText txtUsername;
    EditText txtEmail;
    EditText txtPassword;
    TextView txtOrLogin;
    Button signupB;
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
        setContentView(R.layout.activity_signup);

        UserData.getInstance().getUsers();
        firebaseAuth = FirebaseAuth.getInstance();

        progress = new ProgressDialog(this);
        txtName= findViewById(R.id.editSignupTextPersonName);
        txtSurname= findViewById(R.id.editSignupSurname);
        txtUsername= findViewById(R.id.editSignupTextPersonUsername);
        txtEmail = findViewById(R.id.editTextSignupEmailAddress);
        txtPassword = findViewById(R.id.editSignupPassword);
        txtOrLogin = findViewById(R.id.textViewSignupOrLogin);
        txtOrLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activity=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(activity);
                finish();
            }
        });

        signupB = findViewById(R.id.buttonSignup);
        signupB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean proceedWithRegistration=true;

                if(UserData.getInstance().getUserByUsername(txtUsername.getText().toString())!=null)
                {
                    proceedWithRegistration = false;
                    Toast.makeText(getApplicationContext(), "Uneseno ime je zauzeto", Toast.LENGTH_LONG).show();
                }
                if(proceedWithRegistration)
                    signupUser();
            }

        });
    }

    private void signupUser()
    {
        if(TextUtils.isEmpty(txtName.getText().toString())||TextUtils.isEmpty(txtSurname.getText().toString())
                ||TextUtils.isEmpty(txtUsername.getText().toString())||TextUtils.isEmpty(txtEmail.getText().toString())
                ||TextUtils.isEmpty(txtPassword.getText().toString()))
        {
            Toast.makeText(getApplicationContext(), "Sva polja moraju biti popunjena!", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setMessage("Učlanljivanje u toku...");
        progress.show();

        String email=txtEmail.getText().toString();

        firebaseAuth.createUserWithEmailAndPassword(txtEmail.getText().toString(),
                txtPassword.getText().toString()).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {

                    User u = new User();
                    u.name=txtName.getText().toString();
                    u.surname=txtSurname.getText().toString();
                    u.username =txtUsername.getText().toString();
                    u.email = email;
                    u.picture = "";
                    u.sick="healthy";

                    UserData.getInstance().AddUser(u);
                    Toast.makeText(getApplicationContext(), "Učlanjivanje uspešno", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Učlanjivanje neuspešno", Toast.LENGTH_SHORT).show();
                }
            }});

        firebaseAuth.signInWithEmailAndPassword(txtEmail.getText().toString(), txtPassword.getText().toString())
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
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
                            User uu;
                            if (indexx != -1)
                            {
                                uu = probepos.get(indexx);

                                Context context = getApplicationContext();
                                SharedPreferences sharedPref = context.getSharedPreferences(
                                        "Userdata", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.loggedUser_email), uu.email);
                                editor.putString(getString(R.string.loggedUser_username), uu.username);
                                editor.putString(getString(R.string.loggedUser_image), uu.picture);
                                editor.putString(getString(R.string.loggedUser_sick), uu.sick);
                                editor.commit();

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("firstUse",true);
                                startActivity(intent);
                                finish();
                            }
                            Toast.makeText(getApplicationContext(), "Učlanjivanje uspešno", Toast.LENGTH_SHORT).show();

                        }else
                        {
                            Toast.makeText(getApplicationContext(), "Učlanjivanje neuspešno", Toast.LENGTH_SHORT).show();
                        }
                        progress.dismiss();
                    }
                });
    }

}