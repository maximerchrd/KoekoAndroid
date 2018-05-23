package com.LearningTracker.LearningTrackerApp.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;

import com.LearningTracker.LearningTrackerApp.Activities.Tools.RecyclerTouchListener;
import com.LearningTracker.LearningTrackerApp.Activities.Tools.ResultsListAdapter;
import com.LearningTracker.LearningTrackerApp.Activities.Tools.TestListAdapter;
import com.LearningTracker.LearningTrackerApp.LTApplication;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.Test;
import com.LearningTracker.LearningTrackerApp.R;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableIndividualQuestionForResult;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableTest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class ResultsListActivity extends Activity {
    private RecyclerView mRecyclerView;
    public RecyclerView.Adapter getmAdapter() {
        return mAdapter;
    }

    private RecyclerView.Adapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_resultslist);
        mRecyclerView = (RecyclerView) findViewById(R.id.results_recycler_view);


        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //gather results and pass them to the ListAdapter
        Vector<Vector<String>> results = DbTableIndividualQuestionForResult.getAllResults();
        String[] questions = new String[results.size()];
        String[] evaluations = new String[results.size()];

        for (int i = 0; i < results.size(); i++) {
            QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(Integer.valueOf(results.get(i).get(0)));
            if (questionMultipleChoice == null) {
                QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(Integer.valueOf(results.get(i).get(0)));
                questions[i] = questionShortAnswer.getQUESTION();
                evaluations[i] = results.get(i).get(3);
            } else {
                questions[i] = questionMultipleChoice.getQUESTION();
                evaluations[i] = results.get(i).get(3);
            }
        }

        mAdapter = new ResultsListAdapter(questions, evaluations);
        mRecyclerView.setAdapter(mAdapter);
        /*mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (mTest.getActiveQuestionIds().contains(mTest.getQuestionsIDs().get(position))) {
                    QuestionMultipleChoice questionMultipleChoice = mTest.getIdMapQmc().get(mTest.getQuestionsIDs().get(position));
                    if (questionMultipleChoice == null) {
                        QuestionShortAnswer questionShortAnswer = mTest.getIdMapShrtaq().get(mTest.getQuestionsIDs().get(position));
                        ((LTApplication) getApplication()).getAppWifi().launchShortAnswerQuestionActivity(questionShortAnswer);
                    } else {
                        ((LTApplication) getApplication()).getAppWifi().launchMultChoiceQuestionActivity(questionMultipleChoice);
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));*/
    }
}