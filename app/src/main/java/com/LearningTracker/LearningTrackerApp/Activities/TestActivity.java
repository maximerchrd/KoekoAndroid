package com.LearningTracker.LearningTrackerApp.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;

import com.LearningTracker.LearningTrackerApp.Activities.Tools.TestListAdapter;
import com.LearningTracker.LearningTrackerApp.Activities.Tools.RecyclerTouchListener;
import com.LearningTracker.LearningTrackerApp.LTApplication;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.Test;
import com.LearningTracker.LearningTrackerApp.R;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableTest;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestActivity extends Activity {
    public Map<String, String> mcqActivitiesStates;
    public Map<String, String> shrtaqActivitiesStates;

    private RecyclerView mRecyclerView;
    public RecyclerView.Adapter getmAdapter() {
        return mAdapter;
    }

    private RecyclerView.Adapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;


    private Test mTest;
    public Test getmTest() {
        return mTest;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_test);
        mRecyclerView = (RecyclerView) findViewById(R.id.test_recycler_view);

        //initialize static variables
        mcqActivitiesStates = new LinkedHashMap<>();
        shrtaqActivitiesStates = new LinkedHashMap<>();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //load the test
        Bundle bun = getIntent().getExtras();
        Long testID = bun.getLong("testID");

        if (LTApplication.currentTestActivitySingleton == null) {
            mTest = new Test();
            mTest.setIdGlobal(testID);
            mTest.setTestName(DbTableTest.getNameFromTestID(testID));
            mTest.setQuestionsIDs(DbTableTest.getQuestionIDsFromTestName(mTest.getTestName()));
            mTest.loadMap();
        } else {
            mTest = LTApplication.currentTestActivitySingleton.mTest;
            mcqActivitiesStates = LTApplication.currentTestActivitySingleton.mcqActivitiesStates;
            shrtaqActivitiesStates = LTApplication.currentTestActivitySingleton.shrtaqActivitiesStates;
        }

        mAdapter = new TestListAdapter(mTest);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (mTest.getActiveQuestionIds().contains(mTest.getQuestionsIDs().get(position))) {
                    QuestionMultipleChoice questionMultipleChoice = mTest.getIdMapQmc().get(mTest.getQuestionsIDs().get(position));
                    if (questionMultipleChoice == null) {
                        QuestionShortAnswer questionShortAnswer = mTest.getIdMapShrtaq().get(mTest.getQuestionsIDs().get(position));
                        LTApplication.wifiCommunicationSingleton.launchShortAnswerQuestionActivity(questionShortAnswer,
                                LTApplication.wifiCommunicationSingleton.directCorrection);
                    } else {
                        LTApplication.wifiCommunicationSingleton.launchMultChoiceQuestionActivity(questionMultipleChoice,
                                LTApplication.wifiCommunicationSingleton.directCorrection);
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        LTApplication.currentTestActivitySingleton = this;
    }
}