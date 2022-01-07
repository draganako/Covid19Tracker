package com.example.covid19tracker.Adapters;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import androidx.recyclerview.widget.RecyclerView;

import com.example.covid19tracker.DataModels.Illness;

import com.example.covid19tracker.DBData.IllnessData;
import com.example.covid19tracker.MyDatePickerDialog;
import com.example.covid19tracker.R;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class IllnessDatesEditAdapter extends RecyclerView.Adapter<IllnessDatesEditAdapter.ViewHolder>
{
    private final SharedPreferences sharedPreferences;
    public List<Illness> illnessList;
    private static Context context;
    private String tmp;

    public IllnessDatesEditAdapter(ArrayList<Illness> il, Context c)
    {
        sharedPreferences= c.getSharedPreferences("Userdata",Context.MODE_PRIVATE);
        tmp="";
        String username=sharedPreferences.getString("loggedUserUsername","");
        illnessList=IllnessData.getInstance().getIllnessesByUsername(username);
        context=c;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.illnessdate_edit_view,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position)
    {
        Illness illness=illnessList.get(position);
        holder.textViewillnessdate.setText("Od "+
                DateFormat.format("dd. MM. yyyy.", illness.startDate)+" do "
                +DateFormat.format("dd. MM. yyyy.", illness.endDate));
        holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IllnessData.getInstance().deleteIllness(illness);
                Toast.makeText(context,"Termin obrisan",Toast.LENGTH_SHORT).show();
                illnessList.remove(illness);
                notifyDataSetChanged();

            }
        });
        holder.imageViewEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Promenite datum:");

                String[] opts = {"Početka bolesti", "Kraja bolesti"};
                builder.setItems(opts, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(which==0)
                            tmp="start";
                        else
                            tmp="end";

                        dialog.dismiss();

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        if(!tmp.equals(""))
                        {
                            MyDatePickerDialog dialogg = new MyDatePickerDialog(context);
                            dialogg.setTitle("Unesite datum");
                            dialogg.showDatePicker(new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                                {

                                    Date date=new GregorianCalendar(year,month,dayOfMonth).getTime();
                                    if (date.after(new Date()))
                                    {
                                        Toast.makeText(context,"Uneseni datum ne sme premašivati današnji",Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if(tmp=="start")
                                    {
                                        illness.startDate = date;
                                        Toast.makeText(context,"Datum početka promenjen",Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                        {
                                        illness.endDate = date;
                                        Toast.makeText(context, "Datum kraja promenjen", Toast.LENGTH_SHORT).show();
                                    }

                                        holder.textViewillnessdate.setText("Od " +
                                            DateFormat.format("dd. MM. yyyy.", illness.startDate) + " do "
                                            + DateFormat.format("dd. MM. yyyy.", illness.endDate));

                                    IllnessData.getInstance().updateIllness(illness);

                                }
                            }, Calendar.getInstance());

                        }
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return illnessList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView textViewillnessdate;
        private final ImageView imageViewEdit;
        private final ImageView imageViewDelete;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textViewillnessdate= itemView.findViewById(R.id.textViewProfileIllnessDate);
            imageViewEdit=itemView.findViewById(R.id.imageViewEditIllness);
            imageViewDelete=itemView.findViewById(R.id.imageViewDeleteIllness);

        }
    }


}

