package com.LearningTracker.LearningTrackerApp.Activities;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.LearningTracker.LearningTrackerApp.Activities.SwipingTools.ExerciseCollectionPagerAdapter;
import com.LearningTracker.LearningTrackerApp.Activities.SwipingTools.GraphsCollectionPagerAdapter;
import com.LearningTracker.LearningTrackerApp.R;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableSubject;

import java.util.ArrayList;
import java.util.Vector;

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
