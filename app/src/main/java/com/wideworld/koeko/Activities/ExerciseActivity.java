package com.wideworld.koeko.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.wideworld.koeko.Activities.ActivityTools.ExerciseObjectFragment;
import com.wideworld.koeko.Activities.ActivityTools.HomeworkCodeAlertDialog;
import com.wideworld.koeko.NetworkCommunication.RemoteServerCommunication;
import com.wideworld.koeko.QuestionsManagement.Homework;
import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableHomework;
import com.wideworld.koeko.database_management.DbTableIndividualQuestionForResult;
import com.wideworld.koeko.database_management.DbTableQuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableQuestionShortAnswer;
import com.wideworld.koeko.database_management.DbTableSettings;
import com.wideworld.koeko.database_management.DbTableSubject;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by maximerichard on 20.02.18.
 */
public class ExerciseActivity extends Activity {
    private Button homeWorkButton, freePracticeButton, downloadHomeworkButton;
    private Spinner subjectsSpinner;

    private Spinner codeSpinner;
    private ArrayList<String> codeSpinnerOriginalList;
    private ArrayList<String> codeSpinnerList;
    private ArrayAdapter<String> codeSpinnerAdapter;

    private Spinner homeworksSpinner;
    private ArrayList<String> homeworkSpinnerList;
    private ArrayList<Homework> homeworkSpinnerObjectList;
    private ArrayAdapter<String> homeworkSpinnerAdapter;

    private Context mContext;
    private Activity currentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercice);

        mContext = getApplicationContext();
        currentActivity = this;

        //couple code with UI
        homeWorkButton = findViewById(R.id.homework_button);
        freePracticeButton = findViewById(R.id.freepractice_button);
        subjectsSpinner = findViewById(R.id.subjects_spinner);

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

        downloadHomeworkButton = findViewById(R.id.download_homework_button);
        downloadHomeworkButton.setOnClickListener(v -> {
            downloadHomeworkQuestions(homeworkSpinnerObjectList.get(homeworksSpinner.getSelectedItemPosition()).getQuestions());
        });

        homeworksSpinner = findViewById(R.id.homeworks_spinner);
        if (codeSpinner.getSelectedItemPosition() >= 0) {
            homeworkSpinnerObjectList = DbTableHomework.getHomeworksWithCode(codeSpinnerOriginalList.get(codeSpinner.getSelectedItemPosition()).split("/")[0]);
        } else {
            homeworkSpinnerObjectList = new ArrayList<>();
        }
        homeworkSpinnerList = new ArrayList<>();
        for (Homework homework : homeworkSpinnerObjectList) {
            homeworkSpinnerList.add(homework.getName());
        }

        homeworkSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, homeworkSpinnerList);
        homeworkSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        homeworksSpinner.setAdapter(homeworkSpinnerAdapter);
        homeworksSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                checkIfHomeworkComplete(position);
                downloadHomeworkButton.setEnabled(true);
                downloadHomeworkButton.setBackgroundColor(getResources().getColor(R.color.blue));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                downloadHomeworkButton.setEnabled(false);
                downloadHomeworkButton.setBackgroundColor(getResources().getColor(R.color.blue_faded));
            }
        });

        homeWorkButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("IDsArray", homeworkSpinnerObjectList.
                    get(homeworksSpinner.getSelectedItemPosition()).getQuestions());
            bundle.putInt("Type", DbTableIndividualQuestionForResult.type2HomeworkNotSynced);
            bundle.putString("HomeworkName", homeworkSpinnerList.get(homeworksSpinner.getSelectedItemPosition()));
            Intent myIntent = new Intent(mContext, QuestionSetActivity.class);
            myIntent.putExtras(bundle);
            currentActivity.startActivity(myIntent);
        });

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
                bundle.putInt("Type", DbTableIndividualQuestionForResult.type2FreePractice);
                bundle.putString("HomeworkName", "freePractice");
                Intent myIntent = new Intent(mContext, QuestionSetActivity.class);
                myIntent.putExtras(bundle);
                currentActivity.startActivity(myIntent);
            }
        });
    }

    private void downloadHomeworkQuestions(ArrayList<String> questions) {
        new Thread(() -> {
            try {
                if (questions.size() > 0) {
                    RemoteServerCommunication.singleton().getQuestionsFromServer(questions);
                    currentActivity.runOnUiThread(() -> {
                        checkIfHomeworkComplete(homeworksSpinner.getSelectedItemPosition());
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    private void checkIfHomeworkComplete(int position) {
        Homework homework = homeworkSpinnerObjectList.get(position);
        int counter = 0;
        for (String questionid : homework.getQuestions()) {
            if (DbTableQuestionMultipleChoice.getQuestionWithId(questionid).getQuestion().length()> 0) {
                counter++;
            } else if (DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(questionid).getQuestion().length() > 0) {
                counter++;
            } else {
                break;
            }
        }

        if (counter != 0 && counter == homework.getQuestions().size()) {
            homeWorkButton.setEnabled(true);
            homeWorkButton.setBackgroundColor(getResources().getColor(R.color.blue));

        } else {
            homeWorkButton.setEnabled(false);
            homeWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_faded));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(() -> {
            for (String code : codeSpinnerOriginalList) {
                try {
                    ArrayList<Homework> homeworks = RemoteServerCommunication.singleton().
                            getUpdatedHomeworksForCode(code.split("/")[0]);
                    for (Homework homework : homeworks) {
                        DbTableHomework.insertHomework(homework);
                    }
                    currentActivity.runOnUiThread(() -> {
                        if (code.contentEquals(codeSpinnerOriginalList.get(codeSpinner.
                                getSelectedItemPosition()))) {
                            homeworkSpinnerObjectList = homeworks;
                            homeworkSpinnerList.clear();
                            for (Homework homework : homeworkSpinnerObjectList) {
                                homeworkSpinnerList.add(homework.getName());
                            }
                            homeworkSpinnerAdapter.notifyDataSetChanged();
                        }
                    });
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
