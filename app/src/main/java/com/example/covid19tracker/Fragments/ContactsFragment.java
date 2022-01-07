package com.example.covid19tracker.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.covid19tracker.Adapters.ContactsAdapter;
import com.example.covid19tracker.DataModels.Contact;
import com.example.covid19tracker.R;

import java.util.ArrayList;


public class ContactsFragment extends Fragment {

    ArrayList<Contact> contactList;

    public ContactsAdapter adapter;
    private RecyclerView recyclerView;

    public ContactsFragment() {
    }

    public static ContactsFragment newInstance(ArrayList<Contact> contacts) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("contacts",contacts);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            contactList=getArguments().getParcelableArrayList("contacts");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_contacts, container, false);
        recyclerView= view.findViewById(R.id.rvLocationsProfile);
        recyclerView.setHasFixedSize(true);

        adapter=new ContactsAdapter(this.contactList, recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        return view;

    }
}