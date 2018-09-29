package com.LearningTracker.LearningTrackerApp.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.LearningTracker.LearningTrackerApp.R;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableSubject;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by maximerichard on 20.02.18.
 */
public class ExerciseActivity extends Activity {
    private Button homeWorkButton, freePracticeButton;
    private Spinner teachersSpinner, subjectsSpinner;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercice);

        mContext = getApplicationContext();

        //couple code with UI
        homeWorkButton = (Button) findViewById(R.id.homework_button);
        freePracticeButton = (Button) findViewById(R.id.freepractice_button);
        teachersSpinner = (Spinner) findViewById(R.id.teachers_spinner);
        subjectsSpinner = (Spinner) findViewById(R.id.subjects_spinner);

        //puts the subjects for which there are poorly evaluated questions into the spinner
        Vector<Vector<String>> questionIdAndSubjectsVector = DbTableSubject.getSubjectsAndQuestionsNeedingPractice();
        final Vector<String> subjectsVector = questionIdAndSubjectsVector.get(1);
        subjectsVector.insertElementAt(getString(R.string.all_subjects),0);
        String[] arraySpinner = subjectsVector.toArray(new String[subjectsVector.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, arraySpinner);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        subjectsSpinner.setAdapter(adapter);

        freePracticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implements the practice button
                Vector<String> questionIDsVector =  DbTableSubject.getSubjectsAndQuestionsNeedingPractice().get(0);
                final ArrayList<String> questionIDsArray = new ArrayList<>();
                for (int i = 0; i < questionIDsVector.size(); i++) {
                    questionIDsArray.add(questionIDsVector.get(i));
                }
                //ArrayList<Integer> questionIDsArrayCopy = (ArrayList<Integer>) questionIDsArray.clone();
                String selectedSubject = subjectsSpinner.getSelectedItem().toString();
                if (!selectedSubject.contentEquals(getString(R.string.all_subjects))) {
                    int arraySize = questionIDsArray.size();
                    for (int i = 0; i < arraySize; i++) {
                        Vector<String> subjectForQuestion = DbTableSubject.getSubjectsForQuestionID(Long.valueOf(questionIDsArray.get(i)));
                        if (!subjectForQuestion.contains(selectedSubject)) {
                            questionIDsArray.remove(i);
                            i--;
                            arraySize--;
                        }
                    }
                }
                if (questionIDsArray.size() == 0) {
                    AlertDialog alertDialog = new AlertDialog.Builder(ExerciseActivity.this).create();
                    alertDialog.setMessage(getString(R.string.noQuestionToPractice));
                    alertDialog.show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("IDsArray", questionIDsArray);
                    Intent myIntent = new Intent(mContext, QuestionSetActivity.class);
                    myIntent.putExtras(bundle);
                    mContext.startActivity(myIntent);
                }
            }
        });
    }
}
