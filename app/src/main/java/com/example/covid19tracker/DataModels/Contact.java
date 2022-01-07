package com.example.covid19tracker.DataModels;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Date;

public class Contact implements Parcelable
{
    public Date date;
    public String username;
    public String nameOfContact;
    public String key;

    public Contact()
    {}

    public Contact(Date date, String username, String nameOfContact)
    {
        this.date=date;
        this.username=username;
        this.nameOfContact=nameOfContact;
    }

    protected Contact(Parcel in) {
        username = in.readString();
        key = in.readString();
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
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
