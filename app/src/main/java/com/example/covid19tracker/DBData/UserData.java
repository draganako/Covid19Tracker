package com.example.covid19tracker.DBData;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.covid19tracker.DataModels.Illness;
import com.example.covid19tracker.DataModels.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class UserData
{
    private final ArrayList<User> users;
    private final HashMap<String, Integer> usersMapping;
    public DatabaseReference db;
    private static final String FIREBASE_CHILD= "Users";

    private UserData()
    {
        users = new ArrayList<>();
        usersMapping  = new HashMap<String, Integer>();

        db = FirebaseDatabase.getInstance().getReference();
        db.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
        db.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);

    }

    private static class SingletonHolder {
        public static final UserData instance = new UserData();
    }

    public static UserData getInstance() {
        return SingletonHolder.instance;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    UpdateEventListener updateListener;
    public void setEventListener(UpdateEventListener listener)
    {
        updateListener = listener;
    }
    public interface UpdateEventListener {
        void onListUpdated();
        void onSingleUserUpdated(String username);
        void onUserAdded(String username);
    }

    onUpdateUserListener updateUserListener;
    public void setUserPostListener(onUpdateUserListener listener) {
        updateUserListener = listener;
    }
    public interface onUpdateUserListener {
        void onUserUpdated();
    }

    ReadyEventListener probaList;
    public void setReadyList(ReadyEventListener listener) {
        probaList = listener;
    }
    public interface ReadyEventListener {
        void onReady();
    }

    ValueEventListener parentEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (updateListener != null)
                updateListener.onListUpdated();

            if(probaList != null)
                probaList.onReady();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String myUserKey = dataSnapshot.getKey();

            if (!usersMapping.containsKey(myUserKey)) {
                User myUser = dataSnapshot.getValue(User.class);
                myUser.key = myUserKey;
                users.add(myUser);
                usersMapping.put(myUserKey, users.size() - 1);
                if (updateListener != null)
                    updateListener.onUserAdded(myUser.username);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String myUserKey = dataSnapshot.getKey();
            User myUser = dataSnapshot.getValue(User.class);
            myUser.key = myUserKey;
            if (usersMapping.containsKey(myUserKey)) {
                int index = usersMapping.get(myUserKey);
                users.set(index, myUser);
            } else {
                users.add(myUser);
                usersMapping.put(myUserKey, users.size() - 1);
            }
            if (updateListener != null) {
                updateListener.onSingleUserUpdated(myUser.username);
            }

            if(updateUserListener != null)
            {
                updateUserListener.onUserUpdated();
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            String myUserKey = dataSnapshot.getKey();
            if (usersMapping.containsKey(myUserKey)) {
                int index = usersMapping.get(myUserKey);
                users.remove(index);
                recreateKeyIndexMapping();
            }
            if (updateListener != null)
                updateListener.onListUpdated();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public void AddUser(User p)
    {
        String key = db.push().getKey();
        users.add(p);
        usersMapping.put(key, users.size() - 1);
        db.child(FIREBASE_CHILD).child(key).setValue(p);
        p.key = key;
    }

    public User getUser(String email) {
        User u = null;
        for(int i = 0; i< this.users.size(); i++)
        {
            if(this.users.get(i).email.compareTo(email) == 0)
            {
                u = this.users.get(i);
                break;
            }
        }

        return u;
    }

    public User getUserByUsername(String username) {
        User u = null;
        for(int i = 0; i< this.users.size(); i++)
        {
                if(this.users.get(i).username.compareTo(username) == 0)
            {
                u = this.users.get(i);
                break;
            }
        }

        return u;
    }

    public void updateUserProfileExceptImage(String username, String name, String surname)
    {
        int indexx = -1;
        for(int i =0; i < users.size(); i++)
        {
            if(users.get(i).username.compareTo(username) == 0) {
                indexx = i;
                break;
            }
        }
        if(indexx == -1)
            return;

        User uu =users.get(indexx);
        uu.name = name;
        uu.surname = surname;

        db.child(FIREBASE_CHILD).child(uu.key).setValue(uu);

    }

    public void updateUserImage(String username,String image)
    {
        int indexx = -1;
        for(int i =0; i < users.size(); i++)
        {
            if(users.get(i).username.compareTo(username) == 0) {
                indexx = i;
                break;
            }
        }
        if(indexx == -1)
            return;

        User uu =users.get(indexx);
        uu.picture = image;

        db.child(FIREBASE_CHILD).child(uu.key).setValue(uu);
    }

    public void updateUserHealth(String username,String health)
    {
        int indexx = -1;
        for(int i =0; i < users.size(); i++)
        {
            if(users.get(i).username.compareTo(username) == 0) {
                indexx = i;
                break;
            }
        }
        if(indexx == -1)
            return;

        User uu =users.get(indexx);

        if(health.equals("potentially") && uu.sick.equals("sick"))
            return;

        uu.sick = health;

        db.child(FIREBASE_CHILD).child(uu.key).setValue(uu);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean updateUserHealthByContactNameDate(String username, String contactUsername, Date contactDate)
    {
        LocalDate localContactDate = contactDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        boolean wasSickThen=false;

        ArrayList<Illness> illnesses=IllnessData.getInstance().getIllnessesByUsername(contactUsername);
        for (Illness illn : illnesses)
        {
            LocalDate startDate = illn.startDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            LocalDate endDate = illn.endDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if ((localContactDate.isAfter(startDate) || localContactDate.equals(startDate))
                    && (localContactDate.isBefore(endDate) || localContactDate.equals(endDate)))
                wasSickThen=true;
        }

        if(wasSickThen)
            if(new Date().getTime()-contactDate.getTime()<=1209600000)
                if(UserData.getInstance().getUserByUsername(username).sick.equals("false"))
             {
              updateUserHealth(username,"potentially");
              return true;
             }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateContactHealthByUsernameDate(String username, String contactUsername, Date date)
    {
        LocalDate localContactDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        boolean wasSickThen=false;

        ArrayList<Illness> illnesses=IllnessData.getInstance().getIllnessesByUsername(username);
        for (Illness illn : illnesses)
        {
            LocalDate startDate = illn.startDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            LocalDate endDate = illn.endDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if ((localContactDate.isAfter(startDate) || localContactDate.equals(startDate))
                    && (localContactDate.isBefore(endDate) || localContactDate.equals(endDate)))
                wasSickThen=true;
        }

        if(wasSickThen)
            if(new Date().getTime()-date.getTime()<=1209600000)
                if(UserData.getInstance().getUserByUsername(contactUsername).sick.equals("false"))
                    UserData.getInstance().updateUserHealth(contactUsername,"potentially");

    }

    private void recreateKeyIndexMapping()
    {
        usersMapping.clear();
        for (int i=0; i<users.size(); i++)
            usersMapping.put(users.get(i).key,i);

    }
}
