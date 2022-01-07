package com.example.covid19tracker.DataModels;

import android.icu.text.Transliterator;

import com.google.firebase.database.Exclude;

import java.util.Date;

public class Notification
{
    public String username;
    public float latitude;
    public float longitude;
    public String day;
    public String recipient;
    public boolean read;
    @Exclude
    public String key;

    public Notification(){}

    public Notification(String username, float latitude, float longitude, String day,String recipient, boolean read)
    {
        this.username=username;
        this.latitude=latitude;
        this.longitude=longitude;
        this.day=day;
        this.recipient=recipient;
        this.read=read;
    }

}
