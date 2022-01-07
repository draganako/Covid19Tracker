package com.example.covid19tracker.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class ViewPagerAdapter extends FragmentPagerAdapter {

    public final List<Fragment> fragmentList = new ArrayList<>();
    public final List<String> fragmentTitles = new ArrayList<>();

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitles.get(position);
    }

    public void AddFragment(@NonNull Fragment fragment, @NonNull String title)
    {
        fragmentList.add(fragment);
        fragmentTitles.add(title);
    }

}
