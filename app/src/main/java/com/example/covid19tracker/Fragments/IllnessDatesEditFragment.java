package com.example.covid19tracker.Fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.covid19tracker.Adapters.IllnessDatesEditAdapter;
import com.example.covid19tracker.DataModels.Illness;
import com.example.covid19tracker.DBData.IllnessData;
import com.example.covid19tracker.MyDatePickerDialog;
import com.example.covid19tracker.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;


public class IllnessDatesEditFragment extends Fragment implements View.OnClickListener
{
    SharedPreferences sharedPrefs;
    RecyclerView recyclerView;
    ArrayList<Illness> illnesses;

    private String tmpAdd;
    private Date newIllnessStartDate;
    private Date newIllnessEndDate;

    IllnessDatesEditAdapter illnessDatesEditAdapter;

    public IllnessDatesEditFragment() {

    }

    public static IllnessDatesEditFragment newInstance(String param1, String param2) {
        IllnessDatesEditFragment fragment = new IllnessDatesEditFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        sharedPrefs= getContext().getSharedPreferences("Userdata", Context.MODE_PRIVATE);

        View view= inflater.inflate(R.layout.fragment_illness_dates_edit, container, false);
        recyclerView= view.findViewById(R.id.rvIllnessDatesEdit);
        recyclerView.setHasFixedSize(true);

        illnesses= new ArrayList<>();
        illnessDatesEditAdapter=new IllnessDatesEditAdapter(illnesses, recyclerView.getContext());
        recyclerView.setAdapter(illnessDatesEditAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        Button buttonAddPeriod= view.findViewById(R.id.buttonIllnessDates);
        buttonAddPeriod.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle("Unesite datum:");

        String[] opts = {"Početka bolesti", "Kraja bolesti"};
        builder.setItems(opts, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    tmpAdd = "start";
                } else if(which==1){
                    tmpAdd = "end";
                }

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                if (tmpAdd!=null) {
                    MyDatePickerDialog dialogg = new MyDatePickerDialog(Objects.requireNonNull(getContext()));
                    dialogg.setTitle("Unesite datum");
                    dialogg.showDatePicker(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                        {

                            Date date = new GregorianCalendar(year, month, dayOfMonth).getTime();
                            if (tmpAdd == "start")
                            {
                                Toast.makeText(getContext(),"Ubačen početni datum", Toast.LENGTH_SHORT).show();
                                newIllnessStartDate = date;
                            }
                            else
                            {
                                newIllnessEndDate = date;
                                Toast.makeText(getContext(),"Ubačen krajnji datum", Toast.LENGTH_SHORT).show();

                            }

                            if(newIllnessStartDate!=null && newIllnessEndDate!=null)
                            {
                                Illness addedIllness=IllnessData.getInstance()
                                        .AddIllness(new Illness(sharedPrefs.getString(getString(R.string.loggedUser_username),"")
                                        ,newIllnessStartDate,newIllnessEndDate));
                                illnessDatesEditAdapter.illnessList.add(addedIllness);
                                illnessDatesEditAdapter.notifyDataSetChanged();

                            }

                        }
                    }, Calendar.getInstance());

                }
            }

        });
    }

}