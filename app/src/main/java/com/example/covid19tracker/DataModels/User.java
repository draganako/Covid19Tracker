package com.example.covid19tracker.DataModels;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class User implements Serializable
{
    public String name;
    public String surname;
    public String username;
    public String email;
    public String picture;
    public String sick;

    @Exclude
    public String key;

    public User()
    {}
    public User(String name,String surname,String username,String email,String picture, String sick)
    {
        this.name=name;
        this.surname=surname;
        this.username=username;
        this.email=email;
        this.picture=picture;
        this.sick=sick;
    }
}
