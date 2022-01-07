package com.example.covid19tracker.Fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.covid19tracker.Adapters.ContactsEditAdapter;
import com.example.covid19tracker.DataModels.Contact;
import com.example.covid19tracker.DBData.ContactData;
import com.example.covid19tracker.DBData.UserData;
import com.example.covid19tracker.MyDatePickerDialog;
import com.example.covid19tracker.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

public class ContactsEditFragment extends Fragment implements View.OnClickListener
{
    SharedPreferences sharedPrefs;
    RecyclerView recyclerView;
    ArrayList<Contact> contacts;

    private String tmpAdd;
    private String newContactName;
    private Date newContactDate;
    private String username;
    private boolean isNameAdded;

    ContactsEditAdapter contactsEditAdapter;

    public ContactsEditFragment() {}

    public static ContactsEditFragment newInstance(String param1, String param2) {
        ContactsEditFragment fragment = new ContactsEditFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isNameAdded=false;

        sharedPrefs= getContext().getSharedPreferences("Userdata", Context.MODE_PRIVATE);
        username=sharedPrefs.getString(getString(R.string.loggedUser_username), "");

        View view= inflater.inflate(R.layout.fragment_contacts_edit, container, false);
        recyclerView= view.findViewById(R.id.rvContactsEdit);
        recyclerView.setHasFixedSize(true);

        contacts= new ArrayList<>();
        contactsEditAdapter=new ContactsEditAdapter(contacts, recyclerView.getContext());
        recyclerView.setAdapter(contactsEditAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        Button buttonAddContact = view.findViewById(R.id.buttonContacts);
        buttonAddContact.setOnClickListener(this);

        return view;

    }

    @Override
    public void onClick(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle("Dodavanje kontakta:");

        String[] opts = {"Ime korisnika", "Dan kontakta"};
        builder.setItems(opts, new DialogInterface.OnClickListener()
        {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               if (which == 0) {
                   tmpAdd = "name";
               } else if (which == 1) {
                   tmpAdd = "day";
               }
           }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                if (tmpAdd != null)
                {
                    if (tmpAdd == "day")
                    {
                        MyDatePickerDialog dialogg = new MyDatePickerDialog(Objects.requireNonNull(getContext()));
                        dialogg.setTitle("Unesite datum");
                        dialogg.showDatePicker(new DatePickerDialog.OnDateSetListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                Date date = new GregorianCalendar(year, month, dayOfMonth).getTime();

                                newContactDate = date;
                                Toast.makeText(Objects.requireNonNull(getContext()), "Datum kontakta unet", Toast.LENGTH_SHORT).show();
                                addContactToDB();

                            }
                        }, Calendar.getInstance());

                    }
                    else if(tmpAdd=="name")
                    {
                        AlertDialog.Builder builderr = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                        builderr.setTitle("Unesite ime korisnika");

                        final EditText input = new EditText(Objects.requireNonNull(getContext()));
                        input.setPadding(10,10,10,0);

                        builderr.setView(input);

                        builderr.setPositiveButton("Potvrdi", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                newContactName = input.getText().toString();
                                isNameAdded=true;

                            }
                        });
                        builderr.setNegativeButton("Otkaži", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builderr.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if(isNameAdded)
                                    Toast.makeText(getContext(),"Ime korisnika uneto", Toast.LENGTH_SHORT);

                                if (UserData.getInstance().getUserByUsername(newContactName) == null)
                                {
                                    isNameAdded=false;

                                    Toast.makeText(getContext(),
                                            "Korisnik sa unetim korisničkim imenom ne postoji", Toast.LENGTH_SHORT);
                                    addContactToDB();

                                }
                            }
                        });

                        builderr.show();

                    }

                }

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void addContactToDB()
    {
        if (newContactDate != null && newContactName!=null) {


            if (UserData.getInstance().getUserByUsername(newContactName) != null)
            {
                if (UserData.getInstance().updateUserHealthByContactNameDate(username, newContactName, newContactDate))
                {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(getString(R.string.loggedUser_sick), "potentially");
                    editor.commit();
                }
            }
            else {
                Toast.makeText(getContext(), "Korisnik sa unetim korisničkim imenom ne postoji", Toast.LENGTH_SHORT);
                return;
            }

            Contact addedContact = ContactData.getInstance()
                    .AddContact(new Contact(
                            newContactDate,
                            username,
                            newContactName));

            if(UserData.getInstance().updateUserHealthByContactNameDate(username, newContactName,newContactDate))
            {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(getContext().getString(R.string.loggedUser_sick), "potentially");
                editor.commit();

            }

            UserData.getInstance().updateContactHealthByUsernameDate(username, newContactName,newContactDate);

            contactsEditAdapter.contactList.add(addedContact);

            newContactDate=null;
            newContactName=null;
            isNameAdded=false;

            contactsEditAdapter.notifyDataSetChanged();

        }
    }
}
