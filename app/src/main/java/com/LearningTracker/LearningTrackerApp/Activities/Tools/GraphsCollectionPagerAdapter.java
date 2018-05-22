package com.LearningTracker.LearningTrackerApp.Activities.Tools;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class GraphsCollectionPagerAdapter extends FragmentPagerAdapter {
    public GraphsCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new GraphsObjectFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(GraphsObjectFragment.ARG_OBJECT, i);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }

}