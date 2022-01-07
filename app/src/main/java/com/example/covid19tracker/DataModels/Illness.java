package com.example.covid19tracker.DataModels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.Date;

public class Illness implements Parcelable {
    public String username;
    public Date startDate;
    public Date endDate;
    public String key;

    public Illness(){}
    public Illness(String un,Date startDate, Date endDate)
    {
        this.username=un;
        this.startDate=startDate;
        this.endDate=endDate;
        this.key="";
    }

    protected Illness(Parcel in) {
        username = in.readString();
        key = in.readString();
    }

    public static final Creator<Illness> CREATOR = new Creator<Illness>() {
        @Override
        public Illness createFromParcel(Parcel in) {
            return new Illness(in)  ;
        }

        @Override
        public Illness[] newArray(int size) {
            return new Illness[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(key);
    }
}
