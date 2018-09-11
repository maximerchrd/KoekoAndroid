package com.LearningTracker.LearningTrackerApp.Activities.ActivityTools;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class ExerciseCollectionPagerAdapter extends FragmentStatePagerAdapter {
    public ExerciseCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    private ArrayList<Integer> mQuestionIDs = new ArrayList<>();

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new ExerciseObjectFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(ExerciseObjectFragment.ARG_OBJECT, i);
        args.putIntegerArrayList("IDsArray",mQuestionIDs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return mQuestionIDs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }


    public void setmQuestionIDs(ArrayList<Integer> mQuestionIDs) {
        this.mQuestionIDs = mQuestionIDs;
    }

}