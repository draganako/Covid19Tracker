package com.example.covid19tracker.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid19tracker.DataModels.Notification;
import com.example.covid19tracker.DBData.NotificationData;
import com.example.covid19tracker.MainActivity;
import com.example.covid19tracker.R;
import com.example.covid19tracker.UserProfileActivity;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder>
{
    private final List<Notification> notificationList;
    private final Context context;

    public NotificationsAdapter(Context c, List<Notification> list )
    {
        context=c;
        notificationList=list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_view,parent,false);
        return new NotificationsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NotificationsAdapter.ViewHolder holder, int position)
    {
        Notification notif=notificationList.get(position);
        holder.textViewNotifText.setText("Zaražena osoba "+ notif.username +" bila je dana " + notif.day
                +" na lokaciji u Vašoj blizini.\nKoordinate: "+
                notif.latitude+", "+notif.longitude);
        if(!notif.read)
            holder.textViewNotifText.setTypeface(null, Typeface.BOLD);

        holder.buttonUser.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("profile_username", notif.username);
            context.startActivity(intent);

        }
    });

        holder.buttonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, MainActivity.class);
                intent.putExtra("push_message", holder.textViewNotifText.getText());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView textViewNotifText;
        private final Button buttonUser;
        private final Button buttonLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNotifText=itemView.findViewById(R.id.textViewNotificationText);
            buttonUser=itemView.findViewById(R.id.buttonNotificationViewUser);
            buttonLocation=itemView.findViewById(R.id.buttonNotificationViewLocation);
        }
    }

    public void onDestroy()
    {
        for (Notification notif:notificationList)
        {
            notif.read=true;
            NotificationData.getInstance().updateNotificationRead(notif.key);
        }
    }

}
