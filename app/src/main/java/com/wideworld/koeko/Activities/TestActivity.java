package com.wideworld.koeko.Activities;

import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import com.wideworld.koeko.Activities.ActivityTools.CustomAlertDialog;
import com.wideworld.koeko.Activities.ActivityTools.TestChronometer;
import com.wideworld.koeko.Activities.ActivityTools.TestListAdapter;
import com.wideworld.koeko.Activities.ActivityTools.RecyclerTouchListener;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.Test;
import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableIndividualQuestionForResult;
import com.wideworld.koeko.database_management.DbTableTest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class TestActivity extends AppCompatActivity {
    public Map<String, String> mcqActivitiesStates;
    public Map<String, String> shrtaqActivitiesStates;
    public TestChronometer testChronometer;
    private Boolean reloadActivity = false;

    private RecyclerView mRecyclerView;

    public RecyclerView.Adapter getmAdapter() {
        return mAdapter;
    }

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    private Test mTest;

    private String TAG = "TestActivity";

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

        if (Koeko.currentTestActivitySingleton == null) {
            mTest = DbTableTest.getTestFromTestId(String.valueOf(testID));
            mTest.setIdGlobal(testID);
            mTest.setTestName(DbTableTest.getNameFromTestID(testID));
            mTest.setQuestionsIDs(DbTableTest.getQuestionIDsFromTestName(mTest.getTestName()));
            mTest.loadMap();
            reloadActivity = false;
        } else {
            mTest = Koeko.currentTestActivitySingleton.mTest;
            mcqActivitiesStates = Koeko.currentTestActivitySingleton.mcqActivitiesStates;
            shrtaqActivitiesStates = Koeko.currentTestActivitySingleton.shrtaqActivitiesStates;
            reloadActivity = true;
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
                        Koeko.wifiCommunicationSingleton.launchShortAnswerQuestionActivity(questionShortAnswer,
                                Koeko.wifiCommunicationSingleton.directCorrection);
                    } else {
                        Koeko.wifiCommunicationSingleton.launchMultChoiceQuestionActivity(questionMultipleChoice,
                                Koeko.wifiCommunicationSingleton.directCorrection);
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        if (mTest.getMedalsInstructionsString().length() > 0
                && !mTest.getMedalsInstructionsString().contentEquals("null")
                && Koeko.currentTestActivitySingleton == null) {
            Vector<Vector<String>> instruc = mTest.getMedalsInstructions();
            if (instruc.size() >=3) {
                String message = "Gold medal\nTime: " + (instruc.get(2).get(0) != "1000000" ? instruc.get(2).get(0) : "no time limit;") + " \nScore: " + instruc.get(2).get(1) + "\n\n";
                message += "Silver medal\nTime: " + (instruc.get(1).get(0) != "1000000" ? instruc.get(1).get(0) : "no time limit;") + " \nScore: " + instruc.get(1).get(1) + "\n\n";
                message += "Bronze medal\nTime: " + (instruc.get(0).get(0) != "1000000" ? instruc.get(0).get(0) : "no time limit;") + " \nScore: " + instruc.get(0).get(1) + "\n\n";
                CustomAlertDialog customAlertDialog = new CustomAlertDialog(this);
                customAlertDialog.setTestInstructions(true);
                customAlertDialog.show();
                customAlertDialog.setProperties(message, this);
            }
        }

        Koeko.currentTestActivitySingleton = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTest.getMedalsInstructionsString().length() > 0) {
            getMenuInflater().inflate(R.menu.menu_test, menu);

            testChronometer = (TestChronometer) menu
                    .findItem(R.id.chronometer)
                    .getActionView();

            testChronometer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            testChronometer.setTextColor(Color.WHITE);
            if (reloadActivity) {
                testChronometer.setStartTime(Koeko.activeTestStartTime);
                testChronometer.run();
            }
        }

        return (super.onCreateOptionsMenu(menu));
    }

    public void checkIfTestFinished() {
        if (mTest.getAnsweredQuestionIds().containsAll(mTest.getActiveQuestionIds()) &&
                mTest.getActiveQuestionIds().containsAll(mTest.getAnsweredQuestionIds())) {
            testChronometer.stop();
            Long testDuration = testChronometer.getOverallDuration();

            //calculate quantitative evaluation
            Double quantEval = 0.0;
            for (Double singleEval : mTest.getQuestionsScores()) {
                quantEval += singleEval;
            }
            quantEval = quantEval / mTest.getQuestionsScores().size();

            //check if medal won
            String medal = "none";
            String message = "You are a Champ!";
            try {
                if (Long.valueOf(mTest.getMedalsInstructions().get(2).get(0)) >= testDuration &&
                        Double.valueOf(mTest.getMedalsInstructions().get(2).get(1)) <= quantEval) {
                    message += "\nYou won the GOLD MEDAL";
                    medal = "gold-medal";
                } else if (Long.valueOf(mTest.getMedalsInstructions().get(1).get(0)) >= testDuration &&
                        Double.valueOf(mTest.getMedalsInstructions().get(1).get(1)) <= quantEval) {
                    medal = "silver-medal";
                    message += "\nYou won the SILVER MEDAL";
                } else if (Long.valueOf(mTest.getMedalsInstructions().get(0).get(0)) >= testDuration &&
                        Double.valueOf(mTest.getMedalsInstructions().get(0).get(1)) <= quantEval) {
                    medal = "bronze-medal";
                    message += "\nYou won the BRONZE MEDAL";
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "NumberFormatException in medals instructions when checking if medal won.");
            }
            if (!medal.contentEquals("none")) {
                CustomAlertDialog customAlertDialog = new CustomAlertDialog(this, message, medal);
                customAlertDialog.show();
            }
            DbTableIndividualQuestionForResult.addIndividualTestForStudentResult(String.valueOf(mTest.getIdGlobal()),
                    mTest.getTestName(), String.valueOf(testDuration), "FORMATIVE",
                    quantEval, medal);
        }
    }

    /**
     * method used to know if we send a disconnection signal to the server
     *
     * @param hasFocus
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.v("test activity: ", "focus lost");
            ((Koeko) this.getApplication()).startActivityTransitionTimer();
        } else {
            ((Koeko) this.getApplication()).stopActivityTransitionTimer();
            Log.v("test activity: ", "has focus");
        }
    }
}