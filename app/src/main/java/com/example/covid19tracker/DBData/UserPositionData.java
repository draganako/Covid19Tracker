package com.example.covid19tracker.DBData;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.covid19tracker.DataModels.User;
import com.example.covid19tracker.DataModels.UserPosition;
import com.example.covid19tracker.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class UserPositionData
{
    private final ArrayList<UserPosition> positions;
    private final HashMap<String, Integer> PositionsMapping;
    public DatabaseReference db;
    private static final String FIREBASE_CHILD= "UserPositions";

    private UserPositionData()
    {
        positions = new ArrayList<>();
        PositionsMapping  = new HashMap<String, Integer>();

        db = FirebaseDatabase.getInstance().getReference();
        db.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
        db.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);

    }

    private static class SingletonHolder {
        public static final UserPositionData instance = new UserPositionData();
    }

    public static UserPositionData getInstance() {
        return UserPositionData.SingletonHolder.instance;
    }

    public ArrayList<UserPosition> getPositions() {
        return positions;
    }

    UpdateEventListener updateListener;
    public void setEventListener(UpdateEventListener listener) {
        updateListener = listener;
    }
    public interface UpdateEventListener {
        void onListUpdated();
        void onSingleUserPositionUpdated(String username);
        void onUserPositionAdded(String username);
    }

    onUpdateUserPositionListener updateUserPositionListener;
    public void setUserPosPostListener(onUpdateUserPositionListener listener) {
        updateUserPositionListener = listener;
    }
    public interface onUpdateUserPositionListener {
        void onUserPositionUpdated();
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
            String myPositionKey = dataSnapshot.getKey();

            if (!PositionsMapping.containsKey(myPositionKey)) {
                UserPosition myPosition = dataSnapshot.getValue(UserPosition.class);
                myPosition.key = myPositionKey;
                positions.add(myPosition);
                PositionsMapping.put(myPositionKey, positions.size() - 1);
                if (updateListener != null)
                    updateListener.onUserPositionAdded(myPosition.username);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String myPositionKey = dataSnapshot.getKey();
            UserPosition myPosition = dataSnapshot.getValue(UserPosition.class);
            myPosition.key = myPositionKey;
            if (PositionsMapping.containsKey(myPositionKey)) {
                int index = PositionsMapping.get(myPositionKey);
                positions.set(index, myPosition);
            } else {
                positions.add(myPosition);
                PositionsMapping.put(myPositionKey, positions.size() - 1);
            }
            if (updateListener != null)
                updateListener.onSingleUserPositionUpdated(myPosition.username);
            if (updateListener != null)
                updateUserPositionListener.onUserPositionUpdated();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            String myPositionKey = dataSnapshot.getKey();
            if (PositionsMapping.containsKey(myPositionKey)) {
                int index = PositionsMapping.get(myPositionKey);
                positions.remove(index);
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

    public void AddPosition(UserPosition p)
    {
        String key = db.push().getKey();
        positions.add(p);
        PositionsMapping.put(key, positions.size() - 1);
        db.child(FIREBASE_CHILD).child(key).setValue(p);
        p.key = key;
    }

    public ArrayList<UserPosition> getOthSickPositionsNearby(String username,UserPosition lup, Date date, int radius)
    {
        ArrayList<UserPosition> ret=new ArrayList<UserPosition>();
        if(lup==null)
            return ret;
        for (UserPosition pos : positions)
        {
            if (!(pos.username.equals(username)) && isInRadius(pos,lup,radius)&& inHalfHourInterval(pos.date,date)
                    && !UserData.getInstance().getUserByUsername(pos.username).sick.equals("healthy"))
                ret.add(pos);
        }
        return ret;
    }

    private boolean inHalfHourInterval(Date nearUserDate, Date userDate)
    {
        return Math.abs(nearUserDate.getTime() - userDate.getTime()) <= 1800000;
    }

    boolean isInRadius(UserPosition userpos, UserPosition userpos2, int radius) {
        return Math.abs(userpos.latitude - userpos2.latitude) < radius * 0.015060 //1 km = 0.015060 degrees
                && Math.abs(userpos.longitude - userpos2.longitude)
                < radius * 0.015060;
    }

    public ArrayList<UserPosition> getUserPositions(String username)
    {
        ArrayList<UserPosition> ret=new ArrayList<UserPosition>();
        for (UserPosition pos : positions)
            if (pos.username.equals(username))
                ret.add(pos);
        return ret;
    }

    private void recreateKeyIndexMapping()
    {
        PositionsMapping.clear();
        for (int i=0;i<positions.size();i++)
            PositionsMapping.put(positions.get(i).key,i);
    }
}
