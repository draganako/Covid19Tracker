package com.example.covid19tracker.DBData;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.covid19tracker.DataModels.Illness;
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

public class IllnessData
{
    private final ArrayList<Illness> illnesses;
    private final HashMap<String, Integer> illnessesMapping;
    public DatabaseReference db;
    private static final String FIREBASE_CHILD= "Illnesses";

    private IllnessData()
    {
        illnesses = new ArrayList<>();
        illnessesMapping  = new HashMap<String, Integer>();

        db = FirebaseDatabase.getInstance().getReference();
        db.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
        db.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);

    }

    private static class SingletonHolder {
        public static final IllnessData instance = new IllnessData();
    }

    public static IllnessData getInstance() {
        return IllnessData.SingletonHolder.instance;
    }

    public ArrayList<Illness> getIllnesses() {
        return illnesses;
    }

    IllnessData.UpdateEventListener updateListener;
    public void setEventListener(IllnessData.UpdateEventListener listener)
    {
        updateListener = listener;
    }

    public interface UpdateEventListener {
        void onListUpdated();
        void onSingleIllnessUpdated(String username);
        void onIllnessAdded(String username);
    }

    IllnessData.onUpdateIllnessListener updateIllnessListener;
    public void setUserPostListener(IllnessData.onUpdateIllnessListener listener) {
        updateIllnessListener = listener;
    }
    public interface onUpdateIllnessListener {
        void onIllnessUpdated();
    }

    IllnessData.ReadyEventListener probaList;
    public void setReadyList(IllnessData.ReadyEventListener listener) {
        probaList = listener;
    }
    public interface ReadyEventListener {
        void onReady();
    }

    ValueEventListener parentEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
        {

            if(probaList != null)
            {
                probaList.onReady();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
        {
            String illnessKey = dataSnapshot.getKey();

            if (!illnessesMapping.containsKey(illnessKey)) {
                Illness newIllness = dataSnapshot.getValue(Illness.class);
                newIllness.key = illnessKey;
                illnesses.add(newIllness);
                illnessesMapping.put(illnessKey, illnesses.size() - 1);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String illnessKey = dataSnapshot.getKey();
            Illness newIllness = dataSnapshot.getValue(Illness.class);
            newIllness.key = illnessKey;
            if (illnessesMapping.containsKey(illnessKey)) {
                int index = illnessesMapping.get(illnessKey);
                illnesses.set(index, newIllness);
            } else {
                illnesses.add(newIllness);
                illnessesMapping.put(illnessKey, illnesses.size() - 1);
            }
            if (updateListener != null)
                updateListener.onSingleIllnessUpdated(newIllness.key);

            if(updateIllnessListener != null)
                updateIllnessListener.onIllnessUpdated();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            String illnessKey = dataSnapshot.getKey();
            if (illnessesMapping.containsKey(illnessKey)) {
                int index = illnessesMapping.get(illnessKey);
                illnesses.remove(index);
                recreateKeyIndexMapping();
            }

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    public Illness AddIllness(Illness p)
    {
        String key = db.push().getKey();
        p.key = key;
        illnesses.add(p);
        illnessesMapping.put(key, illnesses.size() - 1);
        db.child(FIREBASE_CHILD).child(key).setValue(p);

        return p;
    }

    public ArrayList<Illness> getIllnessesByUsername(String un)
    {
        ArrayList<Illness> userIllnesses = new ArrayList<Illness>();

        for (Illness illn : illnesses)
        {
            if (illn.username.equals(un))
                userIllnesses.add(illn);
        }
        return userIllnesses;

    }

    public void deleteIllness(Illness illness)
    {
        int indexx = -1;
        for(int i =0; i < illnesses.size(); i++)
        {
            if(illnesses.get(i).key.compareTo(illness.key) == 0 )

            {
                indexx = i;
                break;
            }
        }
        if(indexx == -1)
            return;

        db.child(FIREBASE_CHILD).child(illnesses.get(indexx).key).removeValue();
        illnesses.remove(indexx);
        recreateKeyIndexMapping();

    }


    public void updateIllness(Illness illness)
    {
        int indexx = -1;
        for(int i =0; i < illnesses.size(); i++)
        {
            if(illnesses.get(i).key.compareTo(illness.key) == 0 )

            {
                indexx = i;
                break;
            }
        }
        if(indexx == -1)
            return;

        Illness uu =illnesses.get(indexx);
        uu.startDate = illness.startDate;
        uu.endDate = illness.endDate;

        db.child(FIREBASE_CHILD).child(uu.key).setValue(uu);

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateIllness(String username, Date date)
    {
        LocalDate localdate= date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

            int indexx=-1;

            for (int i = 0; i < illnesses.size(); i++)
            {
                LocalDate localIllnessEndDate = illnesses.get(i).endDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                if (localIllnessEndDate.plusDays(1).equals(localdate))
                {
                    Illness uu = illnesses.get(i);
                    uu.endDate = date;

                    db.child(FIREBASE_CHILD).child(uu.key).setValue(uu);
                    indexx=i;
                }
                if(localIllnessEndDate.equals(localdate))
                    indexx=i;
            }

            if(indexx==-1)
            {
                Illness newIllness = new Illness(username, date, date);
                AddIllness(newIllness);
            }
    }

    private void recreateKeyIndexMapping()
    {
        illnessesMapping.clear();
        for (int i=0;i<illnesses.size();i++)
            illnessesMapping.put(illnesses.get(i).key,i);

    }
}
