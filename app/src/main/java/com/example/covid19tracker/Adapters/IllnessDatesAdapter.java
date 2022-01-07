package com.example.covid19tracker.Adapters;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid19tracker.DataModels.Illness;

import com.example.covid19tracker.EditProfileActivity;
import com.example.covid19tracker.MyDatePickerDialog;
import com.example.covid19tracker.R;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class IllnessDatesAdapter extends RecyclerView.Adapter<IllnessDatesAdapter.ViewHolder>
{
    public ArrayList<Illness> illnessList;

    public IllnessDatesAdapter(ArrayList<Illness> illnesses,Context c)
    {
        illnessList=illnesses;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.illnessdate_view,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position)
    {
        Illness illness=illnessList.get(position);
        holder.textViewillnessdate.setText("Od "+
                DateFormat.format("dd. MM. yyyy.", illness.startDate)+" do "
                +DateFormat.format("dd. MM. yyyy.", illness.endDate));

    }

    @Override
    public int getItemCount() {
        return illnessList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView textViewillnessdate;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textViewillnessdate= itemView.findViewById(R.id.textViewListMemberIllnessDate);
        }
    }

}


