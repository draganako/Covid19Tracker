package com.example.covid19tracker.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.covid19tracker.Adapters.NotificationsAdapter;
import com.example.covid19tracker.DataModels.Notification;
import com.example.covid19tracker.DBData.NotificationData;
import com.example.covid19tracker.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private List<Notification> notificationList;
    NotificationsAdapter notificationsAdapter;
    SharedPreferences sharedPreferences;

    public NotificationsFragment() {
    }

    public static NotificationsFragment newInstance(String param1, String param2) {
        NotificationsFragment fragment = new NotificationsFragment();
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
        sharedPreferences=getContext().getSharedPreferences("Userdata", Context.MODE_PRIVATE);

        View view= inflater.inflate(R.layout.fragment_notifications, container, false);
        RecyclerView recyclerView= view.findViewById(R.id.rvNotifications);
        recyclerView.setHasFixedSize(true);

        notificationList= new ArrayList<Notification>();
        notificationList= NotificationData.getInstance().getNotificationsByRecipient
                (sharedPreferences.getString(getString(R.string.loggedUser_username),""));
        notificationsAdapter=new NotificationsAdapter(recyclerView.getContext(),notificationList );
        recyclerView.setAdapter(notificationsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationsAdapter.onDestroy();
    }
}