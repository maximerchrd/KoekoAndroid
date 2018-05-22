package com.LearningTracker.LearningTrackerApp.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.LearningTracker.LearningTrackerApp.Activities.Tools.CustomListAdapter;
import com.LearningTracker.LearningTrackerApp.Activities.Tools.RecyclerTouchListener;
import com.LearningTracker.LearningTrackerApp.LTApplication;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.Test;
import com.LearningTracker.LearningTrackerApp.R;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableTest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class TestActivity extends Activity {
    public static Map<String, String> mcqActivitiesStates;
    public static Map<String, String> shrtaqActivitiesStates;

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

        //initialize static variables
        mcqActivitiesStates = new LinkedHashMap<>();
        shrtaqActivitiesStates = new LinkedHashMap<>();
        LTApplication.currentTestActivitySingleton = this;

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
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.v("test", "clicked mother fucker!");
                QuestionMultipleChoice questionMultipleChoice = mTest.getIdMapQmc().get(mTest.getQuestionsIDs().get(position));
                if (questionMultipleChoice == null) {
                    QuestionShortAnswer questionShortAnswer = mTest.getIdMapShrtaq().get(mTest.getQuestionsIDs().get(position));
                    ((LTApplication) getApplication()).getAppWifi().launchShortAnswerQuestionActivity(questionShortAnswer);
                } else {
                    ((LTApplication) getApplication()).getAppWifi().launchMultChoiceQuestionActivity(questionMultipleChoice);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }
}
