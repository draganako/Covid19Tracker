package com.example.covid19tracker.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid19tracker.DataModels.Contact;
import com.example.covid19tracker.R;
import com.example.covid19tracker.UserProfileActivity;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder>
{
    public List<Contact> contactList;
    private final Context context;

    public ContactsAdapter(List<Contact> ll, Context c)
    {
        contactList=ll;
        context=c;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_view,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Contact contact=contactList.get(position);
        holder.contactText.setText("Sa korisnikom "+contact.nameOfContact+" dana "+
                DateFormat.format("dd. MM. yyyy.", contact.date));

        holder.buttonViewProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("profile_username", contact.nameOfContact);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public Button buttonViewProfile;
        public TextView contactText;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contactText= itemView.findViewById(R.id.textViewProfileContactName);
            buttonViewProfile= itemView.findViewById(R.id.buttonProfileViewContactProfile);

        }
    }

}
