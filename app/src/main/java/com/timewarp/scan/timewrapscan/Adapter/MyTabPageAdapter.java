package com.timewarp.scan.timewrapscan.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class MyTabPageAdapter extends FragmentPagerAdapter {

    ArrayList<Fragment> fragmentsList = new ArrayList<>();
    ArrayList<String> fragmentTitleList = new ArrayList<>();

    @Override
    public int getItemPosition(Object obj) {
        return -2;
    }

    public MyTabPageAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int i) {
        return this.fragmentsList.get(i);
    }

    @Override
    public int getCount() {
        return this.fragmentTitleList.size();
    }

    public void addFragments(Fragment fragment, String str) {
        this.fragmentsList.add(fragment);
        this.fragmentTitleList.add(str);
    }

    @Override
    public CharSequence getPageTitle(int i) {
        return this.fragmentTitleList.get(i);
    }
}
