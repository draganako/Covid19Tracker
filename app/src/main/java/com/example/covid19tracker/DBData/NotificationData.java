package com.example.covid19tracker.DBData;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.covid19tracker.DataModels.Notification;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

    public class NotificationData
    {
        private final ArrayList<Notification> notifications;
        private final HashMap<String, Integer> notificationsMapping;
        public DatabaseReference db;
        private static final String FIREBASE_CHILD= "Notifications";

        private NotificationData()
        {
            notifications = new ArrayList<>();
            notificationsMapping  = new HashMap<String, Integer>();

            db = FirebaseDatabase.getInstance().getReference();
            db.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
            db.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);

        }

        private static class SingletonHolder {
            public static final NotificationData instance = new NotificationData();
        }

        public static NotificationData getInstance() {
            return NotificationData.SingletonHolder.instance;
        }

        public ArrayList<Notification> getNotifications() {
            return notifications;
        }

        NotificationData.UpdateEventListener updateListener;
        public void setEventListener(NotificationData.UpdateEventListener listener)
        {
            updateListener = listener;
        }

        public interface UpdateEventListener {
            void onListUpdated();
            void onSingleNotificationUpdated(String username);
            void onNotificationAdded(String username);
        }

        NotificationData.onUpdateNotificationListener updateNotificationListener;
        public void setUserPostListener(NotificationData.onUpdateNotificationListener listener) {
            updateNotificationListener = listener;
        }
        public interface onUpdateNotificationListener {
            void onNotificationUpdated();
        }

        NotificationData.ReadyEventListener probaList;
        public void setReadyList(NotificationData.ReadyEventListener listener) {
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
                    probaList.onReady();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                String notificationKey = dataSnapshot.getKey();

                if (!notificationsMapping.containsKey(notificationKey)) {
                    Notification newNotification = dataSnapshot.getValue(Notification.class);
                    newNotification.key = notificationKey;
                    notifications.add(newNotification);
                    notificationsMapping.put(notificationKey, notifications.size() - 1);
                    }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String notificationKey = dataSnapshot.getKey();
                Notification newNotification = dataSnapshot.getValue(Notification.class);
                newNotification.key = notificationKey;
                if (notificationsMapping.containsKey(notificationKey)) {
                    int index = notificationsMapping.get(notificationKey);
                    notifications.set(index, newNotification);
                } else {
                    notifications.add(newNotification);
                    notificationsMapping.put(notificationKey, notifications.size() - 1);
                }
                if (updateListener != null)
                    updateListener.onSingleNotificationUpdated(newNotification.key);

                if(updateNotificationListener != null)
                    updateNotificationListener.onNotificationUpdated();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String notificationKey = dataSnapshot.getKey();
                if (notificationsMapping.containsKey(notificationKey)) {
                    int index = notificationsMapping.get(notificationKey);
                    notifications.remove(index);
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

        public Notification AddNotification(Notification p)
        {
            String key = db.push().getKey();
            p.key = key;
            notifications.add(p);
            notificationsMapping.put(key, notifications.size() - 1);
            db.child(FIREBASE_CHILD).child(key).setValue(p);

            return p;
        }

        public ArrayList<Notification> getNotificationsByRecipient(String un)
        {
            ArrayList<Notification> userNotifications = new ArrayList<Notification>();

            for (Notification notif : notifications)
                if (notif.recipient.equals(un))
                    userNotifications.add(notif);

            return userNotifications;
        }

        public void deleteNotification(Notification notification)
        {
            int indexx = -1;
            for(int i =0; i < notifications.size(); i++)
            {
                if(notifications.get(i).key.compareTo(notification.key) == 0 )
                {
                    indexx = i;
                    break;
                }
            }
            if(indexx == -1)
                return;

            db.child(FIREBASE_CHILD).child(notifications.get(indexx).key).removeValue();
            notifications.remove(indexx);
            recreateKeyIndexMapping();
        }

        public void updateNotificationRead(String keyy)
        {
            int indexx = -1;
            for(int i =0; i < notifications.size(); i++)
            {
                if(notifications.get(i).key.compareTo(keyy) == 0 )

                {
                    indexx = i;
                    break;
                }
            }
            if(indexx == -1)
                return;

            Notification uu =notifications.get(indexx);
            uu.read=true;

            db.child(FIREBASE_CHILD).child(uu.key).setValue(uu);

        }

        private void recreateKeyIndexMapping()
        {
            notificationsMapping.clear();
            for (int i=0;i<notifications.size();i++)
                notificationsMapping.put(notifications.get(i).key,i);
        }
    }

