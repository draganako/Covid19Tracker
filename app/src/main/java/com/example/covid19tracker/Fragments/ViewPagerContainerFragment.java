package com.example.covid19tracker.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.covid19tracker.Adapters.ViewPagerAdapter;
import com.example.covid19tracker.DBData.ContactData;
import com.example.covid19tracker.DBData.IllnessData;
import com.example.covid19tracker.R;
import com.example.covid19tracker.UserProfileActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;


public class ViewPagerContainerFragment extends Fragment {

    public TabLayout vpc_tablayout;
    public ViewPager vpc_viewpager;
    View view;

    String username;
    String usernameOther;

    SharedPreferences sharedPreferences;

    public ViewPagerContainerFragment() {
    }

    public static ViewPagerContainerFragment newInstance(boolean param1) {
        ViewPagerContainerFragment fragment = new ViewPagerContainerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_view_pager_container, container, false);
        vpc_tablayout = view.findViewById(R.id.tablayoutViewPagerContainer);
        vpc_viewpager = view.findViewById(R.id.viewpagerInViewPagerContainer);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        ArrayList illnessData=new ArrayList();
        ArrayList contactData=new ArrayList();

        if(getTag().equals("userProfile"))
        {
            sharedPreferences = getContext().getSharedPreferences("Userdata", Context.MODE_PRIVATE);

            username=sharedPreferences.getString(getString(R.string.loggedUser_username), "");
            illnessData = IllnessData.getInstance().getIllnessesByUsername(username);
            contactData=ContactData.getInstance().getUserContacts(username);
        }

        if(getTag().equals("otherUserProfile"))
        {
            usernameOther= ((UserProfileActivity)getActivity()).user.username;
            illnessData = IllnessData.getInstance().getIllnessesByUsername(usernameOther);
            contactData = ContactData.getInstance().getUserContacts(usernameOther);

        }

        adapter.AddFragment(ContactsFragment.newInstance(contactData), "Kontakti");
        adapter.AddFragment(IllnessDatesFragment.newInstance(illnessData), "Istorija bolesti");

        vpc_viewpager.setAdapter(adapter);
        vpc_tablayout.setupWithViewPager(vpc_viewpager);

        return view;
    }
}