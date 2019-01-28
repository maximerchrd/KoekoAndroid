package com.wideworld.koeko.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.wideworld.koeko.Activities.ActivityTools.HomeworkCodeAlertDialog;
import com.wideworld.koeko.NetworkCommunication.RemoteServerCommunication;
import com.wideworld.koeko.QuestionsManagement.Homework;
import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableHomework;
import com.wideworld.koeko.database_management.DbTableSettings;
import com.wideworld.koeko.database_management.DbTableSubject;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by maximerichard on 20.02.18.
 */
public class ExerciseActivity extends Activity {
    private Button homeWorkButton, freePracticeButton;
    private Spinner subjectsSpinner;

    private Spinner codeSpinner;
    private ArrayList<String> codeSpinnerOriginalList;
    private ArrayList<String> codeSpinnerList;
    private ArrayAdapter<String> codeSpinnerAdapter;

    private Spinner homeworksSpinner;
    private ArrayList<String> homeworkSpinnerList;
    private ArrayAdapter<String> homeworkSpinnerAdapter;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercice);

        mContext = getApplicationContext();

        //couple code with UI
        homeWorkButton = (Button) findViewById(R.id.homework_button);
        freePracticeButton = (Button) findViewById(R.id.freepractice_button);
        subjectsSpinner = (Spinner) findViewById(R.id.subjects_spinner);

        codeSpinner = findViewById(R.id.homework_keys);
        codeSpinnerOriginalList = DbTableSettings.getHomeworkKeys();
        codeSpinnerList = new ArrayList<>();
        for (String pair : codeSpinnerOriginalList) {
            if (pair.split("/").length > 1 && pair.split("/")[1].length() > 0) {
                codeSpinnerList.add(pair.split("/")[1]);
            } else {
                codeSpinnerList.add(pair.split("/")[0]);
            }
        }
        codeSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, codeSpinnerList);
        codeSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        codeSpinner.setAdapter(codeSpinnerAdapter);
        codeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        homeworksSpinner = findViewById(R.id.homeworks_spinner);

        //puts the subjects for which there are poorly evaluated questions into the spinner
        Vector<Vector<String>> questionIdAndSubjectsVector = DbTableSubject.getSubjectsAndQuestionsNeedingPractice();
        final Vector<String> subjectsVector = questionIdAndSubjectsVector.get(1);
        subjectsVector.insertElementAt(getString(R.string.all_subjects),0);
        String[] arraySpinner = subjectsVector.toArray(new String[subjectsVector.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, arraySpinner);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        subjectsSpinner.setAdapter(adapter);

        freePracticeButton.setOnClickListener(v -> {
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
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(() -> {
            for (String code : codeSpinnerOriginalList) {
                try {
                    ArrayList<Homework> homeworks = RemoteServerCommunication.singleton().getUpdatedHomeworksForCode(code.split("/")[0]);
                    for (Homework homework : homeworks) {
                        DbTableHomework.insertHomework(homework);
                    }
                    System.out.println(homeworks.get(0).getDueDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void addCode(View view) {
        HomeworkCodeAlertDialog homeworkCodeAlertDialog = new HomeworkCodeAlertDialog(this,
                "", "", codeSpinnerAdapter, codeSpinnerList, codeSpinnerOriginalList,
                codeSpinner);
        homeworkCodeAlertDialog.show();
    }

    public void editCode(View view) {
        if (codeSpinner.getSelectedItemPosition() >= 0) {
            String pair = codeSpinnerOriginalList.get(codeSpinner.getSelectedItemPosition());
            String key = pair.split("/")[0];
            String name = "";
            if (pair.split("/").length > 1 && pair.split("/")[1].length() > 0) {
                name = pair.split("/")[1];
            }
            HomeworkCodeAlertDialog homeworkCodeAlertDialog = new HomeworkCodeAlertDialog(this,
                    key, name, codeSpinnerAdapter, codeSpinnerList, codeSpinnerOriginalList,
                    codeSpinner);
            homeworkCodeAlertDialog.show();
        }
    }

    public void deleteCode(View view) {
        if (codeSpinner.getSelectedItemPosition() >= 0) {
            String pair = codeSpinnerOriginalList.get(codeSpinner.getSelectedItemPosition());
            String key = pair.split("/")[0];
            String name = "";
            if (pair.split("/").length > 1 && pair.split("/")[1].length() > 0) {
                name = pair.split("/")[1];
            }
            DbTableSettings.deleteHomeworkKey(key, name);
            codeSpinnerList.remove(codeSpinner.getSelectedItemPosition());
            codeSpinnerOriginalList.remove(codeSpinner.getSelectedItemPosition());
            codeSpinnerAdapter.notifyDataSetChanged();
        }
    }
}
