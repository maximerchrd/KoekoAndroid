package com.LearningTracker.LearningTrackerApp.Activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.LearningTracker.LearningTrackerApp.Activities.ActivityTools.GraphsCollectionPagerAdapter;
import com.LearningTracker.LearningTrackerApp.R;

/**
 * Created by maximerichard on 21.02.18.
 */
public class EvaluationsGraphsActivity extends FragmentActivity {
    // When requested, this adapter returns a ExerciseObjectFragment,
    // representing an object in the collection.
    GraphsCollectionPagerAdapter mCollectionPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluationgraphs);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mCollectionPagerAdapter =
                new GraphsCollectionPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.evaluationgraphs_pager);
        mViewPager.setAdapter(mCollectionPagerAdapter);
    }
}
