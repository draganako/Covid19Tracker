package com.example.covid19tracker.DBData;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.covid19tracker.DataModels.Contact;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactData {

    private final ArrayList<Contact> contacts;
    private final HashMap<String, Integer> ContactsMapping;
    public DatabaseReference db;
    private static final String FIREBASE_CHILD= "Contacts";

    private ContactData()
    {
        contacts = new ArrayList<>();
        ContactsMapping  = new HashMap<String, Integer>();

        db = FirebaseDatabase.getInstance().getReference();
        db.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
        db.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);

    }

    private static class SingletonHolder {
        public static final ContactData instance = new ContactData();
    }

    public static ContactData getInstance() {
        return SingletonHolder.instance;
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    ListUpdatedEventListener updateListener;
    public void setEventListener(ListUpdatedEventListener listener) {
        updateListener = listener;
    }
    public interface ListUpdatedEventListener {
        void onListUpdated();
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
            String myContactKey = dataSnapshot.getKey();

            if (!ContactsMapping.containsKey(myContactKey)) {
                Contact myContact = dataSnapshot.getValue(Contact.class);
                myContact.key = myContactKey;
                contacts.add(myContact);
                ContactsMapping.put(myContactKey, contacts.size() - 1);
                if (updateListener != null)
                    updateListener.onListUpdated();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String myContactKey = dataSnapshot.getKey();
            Contact myContact = dataSnapshot.getValue(Contact.class);
            myContact.key = myContactKey;
            if (ContactsMapping.containsKey(myContactKey)) {
                int index = ContactsMapping.get(myContactKey);
                contacts.set(index, myContact);
            } else {
                contacts.add(myContact);
                ContactsMapping.put(myContactKey, contacts.size() - 1);
            }
            if (updateListener != null)
                updateListener.onListUpdated();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            String myContactKey = dataSnapshot.getKey();
            if (ContactsMapping.containsKey(myContactKey)) {
                int index = ContactsMapping.get(myContactKey);
                contacts.remove(index);
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

    public Contact AddContact(Contact p)
    {
        String key = db.push().getKey();
        contacts.add(p);
        ContactsMapping.put(key, contacts.size() - 1);
        db.child(FIREBASE_CHILD).child(key).setValue(p);
        p.key = key;
        return p;
    }

    public ArrayList<Contact> getUserContacts(String username)
    {
        ArrayList<Contact> ret=new ArrayList<Contact>();
        for (Contact con : contacts)
        {
            if (con.username.equals(username))
                ret.add(con);
        }
        return ret;
    }

    public void deleteContact(Contact contact)
    {
        int indexx = -1;
        for(int i =0; i < contacts.size(); i++)
        {
            if(contacts.get(i).key.compareTo(contact.key) == 0 )

            {
                indexx = i;
                break;
            }
        }
        if(indexx == -1)
            return;

        db.child(FIREBASE_CHILD).child(contacts.get(indexx).key).removeValue();
        contacts.remove(indexx);
        recreateKeyIndexMapping();

    }

    public void updateContact(Contact contact)
    {
        int indexx = -1;
        for(int i =0; i < contacts.size(); i++)
        {
            if(contacts.get(i).key.compareTo(contact.key) == 0 )
            {
                indexx = i;
                break;
            }
        }
        if(indexx == -1)
            return;

            Contact contactt = contacts.get(indexx);
            contactt.username = contact.username;
            contactt.date = contact.date;
            contactt.nameOfContact = contact.nameOfContact;

            db.child(FIREBASE_CHILD).child(contact.key).setValue(contact);

    }

    private void recreateKeyIndexMapping()
    {
        ContactsMapping.clear();
        for (int i=0;i<contacts.size();i++)
            ContactsMapping.put(contacts.get(i).key,i);
    }
}
