package com.LearningTracker.LearningTrackerApp.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import com.LearningTracker.LearningTrackerApp.Activities.Tools.CustomListAdapter;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.Test;
import com.LearningTracker.LearningTrackerApp.R;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableTest;

public class TestActivity extends Activity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Test mTest;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_test);
        mRecyclerView = (RecyclerView) findViewById(R.id.test_recycler_view);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //load the test
        Bundle bun = getIntent().getExtras();
        Long testID = bun.getLong("testID");

        mTest = new Test();
        mTest.setIdGlobal(testID);
        mTest.setTestName(DbTableTest.getNameFromTestID(testID));
        mTest.setQuestionsIDs(DbTableTest.getQuestionIDsFromTestName(mTest.getTestName()));
        mTest.loadMap();

        mAdapter = new CustomListAdapter(mTest.arrayOfQuestionIDs(), mTest.getIdMapQmc(), mTest.getIdMapShrtaq());
        mRecyclerView.setAdapter(mAdapter);

    }
}
