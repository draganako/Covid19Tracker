package com.example.covid19tracker.Adapters;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid19tracker.DBData.UserData;
import com.example.covid19tracker.DataModels.Contact;
import com.example.covid19tracker.DBData.ContactData;
import com.example.covid19tracker.MyDatePickerDialog;
import com.example.covid19tracker.R;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

public class ContactsEditAdapter extends RecyclerView.Adapter<ContactsEditAdapter.ViewHolder>
{
    private final SharedPreferences sharedPreferences;
    public List<Contact> contactList;
    private static Context context;
    private String tmp;
    String username;

    public ContactsEditAdapter(ArrayList<Contact> il, Context c)
    {
        sharedPreferences= c.getSharedPreferences("Userdata",Context.MODE_PRIVATE);
        tmp="";
        username=sharedPreferences.getString("loggedUserUsername","");
        contactList= ContactData.getInstance().getUserContacts(username);
        context=c;
    }

    @NonNull
    @NotNull
    @Override
    public ContactsEditAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.illnessdate_edit_view,parent,false);
        return new ContactsEditAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ContactsEditAdapter.ViewHolder holder, int position)
    {
        Contact contact=contactList.get(position);
        holder.textViewContactText.setText("Sa korisnikom "+contact.nameOfContact+" dana "+
                DateFormat.format("dd. MM. yyyy.", contact.date));
        holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactData.getInstance().deleteContact(contact);
                Toast.makeText(context,"Kontakt obrisan",Toast.LENGTH_SHORT).show();
                contactList.remove(contact);
                notifyDataSetChanged();

            }
        });
        holder.imageViewEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Promenite kontakt:");

                String[] opts = {"Ime korisnika", "Dan kontakta"};
                builder.setItems(opts, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 1)
                            tmp = "dan";
                        dialog.dismiss();

                    }
                });

                AlertDialog dialog1 = builder.create();

                 dialog1.setOnDismissListener(new DialogInterface.OnDismissListener() {
                     @Override
                     public void onDismiss(DialogInterface dialog1)
                     {
                         if(tmp.equals("dan"))
                         {
                             MyDatePickerDialog dialogg = new MyDatePickerDialog(context);
                             dialogg.setTitle("Unesite datum");
                             dialogg.showDatePicker(new DatePickerDialog.OnDateSetListener() {
                                 @RequiresApi(api = Build.VERSION_CODES.O)
                                 @Override
                                 public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                                 {

                                     Date date=new GregorianCalendar(year,month,dayOfMonth).getTime();
                                     contact.date = date;
                                     Toast.makeText(context,"Datum kontakta promenjen",Toast.LENGTH_SHORT).show();

                                     holder.textViewContactText.setText("Sa korisnikom "+contact.nameOfContact+" dana "+
                                             DateFormat.format("dd. MM. yyyy.", contact.date));

                                     ContactData.getInstance().updateContact(contact);
                                     if(UserData.getInstance().updateUserHealthByContactNameDate(username, contact.username,contact.date))
                                     {
                                         SharedPreferences.Editor editor = sharedPreferences.edit();
                                         editor.putString(context.getString(R.string.loggedUser_sick), "potentially");
                                         editor.commit();

                                     }
                                     UserData.getInstance().updateContactHealthByUsernameDate(username, contact.username,contact.date);
                                     tmp="";

                                 }
                             }, Calendar.getInstance());

                         }
                         else
                         {
                             AlertDialog.Builder builderr = new AlertDialog.Builder(Objects.requireNonNull(context));
                             builderr.setTitle("Unesite ime korisnika");

                             final EditText input = new EditText(Objects.requireNonNull(context));
                             input.setPadding(10,10,10,0);

                             builderr.setView(input);

                             builderr.setPositiveButton("Potvrdi", new DialogInterface.OnClickListener() {
                                 @RequiresApi(api = Build.VERSION_CODES.O)
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {

                                     contact.nameOfContact=input.getText().toString();
                                     Toast.makeText(context,"Ime kontakta promenjeno",Toast.LENGTH_SHORT).show();

                                     holder.textViewContactText.setText("Sa korisnikom "+contact.nameOfContact+" dana "+
                                             DateFormat.format("dd. MM. yyyy.", contact.date));

                                     ContactData.getInstance().updateContact(contact);
                                     if(UserData.getInstance().updateUserHealthByContactNameDate(username, contact.username,contact.date))
                                     {
                                         SharedPreferences.Editor editor = sharedPreferences.edit();
                                         editor.putString(context.getString(R.string.loggedUser_sick), "potentially");
                                         editor.commit();

                                     }
                                     UserData.getInstance().updateContactHealthByUsernameDate(username, contact.username,contact.date);

                                 }
                             });
                             builderr.setNegativeButton("Otkaži", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     dialog.cancel();
                                 }
                             });

                             builderr.show();
                         }

                     }
                 });

                 dialog1.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView textViewContactText;
        private final ImageView imageViewEdit;
        private final ImageView imageViewDelete;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textViewContactText= itemView.findViewById(R.id.textViewProfileIllnessDate);
            imageViewEdit=itemView.findViewById(R.id.imageViewEditIllness);
            imageViewDelete=itemView.findViewById(R.id.imageViewDeleteIllness);

        }
    }
}
