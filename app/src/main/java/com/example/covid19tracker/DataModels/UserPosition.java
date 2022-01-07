package com.example.covid19tracker.DataModels;

import java.util.Date;

public class UserPosition
{
    public double latitude;
    public double longitude;
    public String username;
    public Date date;
    public String key;

    public UserPosition()
    {
        latitude=0;
        longitude=0;
    }

    public UserPosition(double latitude, double longitude, String username, Date date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.username = username;
        this.date = date;
    }


}
