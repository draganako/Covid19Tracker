package com.example.covid19tracker.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.covid19tracker.Adapters.IllnessDatesAdapter;
import com.example.covid19tracker.DataModels.Illness;
import com.example.covid19tracker.R;

import java.util.ArrayList;

public class IllnessDatesFragment extends Fragment {

    ArrayList<Illness> illnessArrayList;
    private RecyclerView recyclerView;
    public IllnessDatesAdapter illnessDatesAdapter;
    private SharedPreferences sharedPreferences;

    public IllnessDatesFragment() {
    }

    public static IllnessDatesFragment newInstance(ArrayList<Illness> illnesses) {
        IllnessDatesFragment fragment = new IllnessDatesFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("illnesses",illnesses);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            illnessArrayList=getArguments().getParcelableArrayList("illnesses");
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        sharedPreferences= getContext().getSharedPreferences("Userdata", Context.MODE_PRIVATE);
        String username=sharedPreferences.getString(getString(R.string.loggedUser_username),"");

        View view= inflater.inflate(R.layout.fragment_illness_dates, container, false);
        recyclerView= view.findViewById(R.id.rvIllnessDates);
        recyclerView.setHasFixedSize(true);

        illnessDatesAdapter=new IllnessDatesAdapter(this.illnessArrayList,recyclerView.getContext());
        recyclerView.setAdapter(illnessDatesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        return view;
    }
}