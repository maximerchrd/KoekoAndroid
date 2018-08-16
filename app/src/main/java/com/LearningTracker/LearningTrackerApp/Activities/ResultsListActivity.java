package com.LearningTracker.LearningTrackerApp.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.LearningTracker.LearningTrackerApp.database_management.DbTableLearningObjective;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class ResultsListActivity extends Activity {
    private Context mContext;
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
        mContext = this;


        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //gather results and pass them to the ListAdapter
        final Vector<Vector<String>> results = DbTableIndividualQuestionForResult.getAllResults();
        Collections.reverse(results);
        String[] questions = new String[results.size()];
        String[] evaluations = new String[results.size()];
        final String[] types = new String[results.size()];

        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).get(4) == null || !results.get(i).get(4).contentEquals("2")) {
                QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(results.get(i).get(0));
                if (questionMultipleChoice.getQUESTION().length() == 0) {
                    QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(results.get(i).get(0));
                    questions[i] = questionShortAnswer.getQUESTION();
                    evaluations[i] = results.get(i).get(3);
                    types[i] = "1";
                } else {
                    questions[i] = questionMultipleChoice.getQUESTION();
                    evaluations[i] = results.get(i).get(3);
                    types[i] = "0";
                }
            } else {
                questions[i] = DbTableLearningObjective.getObjectiveWithID(results.get(i).get(0));
                evaluations[i] = results.get(i).get(3);
                types[i] = "2";
            }
        }

        mAdapter = new ResultsListAdapter(questions, evaluations);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (!types[position].contentEquals("2")) {
                    Bundle bun = new Bundle();

                    QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(results.get(position).get(0));
                    QuestionShortAnswer questionShortAnswer = null;
                    if (questionMultipleChoice.getQUESTION().length() == 0) {
                        questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(results.get(position).get(0));

                        String allAnswers = "";
                        for (String answer : questionShortAnswer.getAnswers()) {
                            allAnswers += answer + "; ";
                        }

                        bun.putString("questionText", questionShortAnswer.getQUESTION());
                        bun.putString("questionImage", questionShortAnswer.getIMAGE());
                        bun.putString("studentAnswer", results.get(position).get(1));
                        bun.putString("allAnswers", allAnswers);
                        bun.putString("date", results.get(position).get(2));
                        bun.putString("evaluation", results.get(position).get(3));
                    } else {
                        ArrayList<String> allAnswers = questionMultipleChoice.getPossibleAnswers();
                        String rightAnswers = "";
                        String wrongAnswers = "";
                        for (int i = 0; i < allAnswers.size(); i++) {
                            if (i < questionMultipleChoice.getNB_CORRECT_ANS()) {
                                rightAnswers += allAnswers.get(i) + "; ";
                            } else if (!allAnswers.get(i).contentEquals(" ")) {
                                wrongAnswers += allAnswers.get(i) + "; ";
                            }
                        }

                        String allAnswersString = "Right Answers: \n" + rightAnswers +
                                "\nWrong Answers: \n" + wrongAnswers;

                        bun.putString("questionText", questionMultipleChoice.getQUESTION());
                        bun.putString("questionImage", questionMultipleChoice.getIMAGE());
                        bun.putString("studentAnswer", results.get(position).get(1));
                        bun.putString("allAnswers", allAnswersString);
                        bun.putString("date", results.get(position).get(2));
                        bun.putString("evaluation", results.get(position).get(3));
                    }

                    Intent mIntent = new Intent(mContext, ResultsFullViewActivity.class);
                    mIntent.putExtras(bun);
                    startActivity(mIntent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }
}