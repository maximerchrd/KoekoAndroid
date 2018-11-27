package com.wideworld.koeko.Activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableLearningObjective;
import com.wideworld.koeko.database_management.DbTableRelationTestObjective;
import com.wideworld.koeko.database_management.DbTableSubject;
import com.wideworld.koeko.database_management.DbTableTest;

import java.util.Vector;

public class EvaluationCityRepresentationActivity extends AppCompatActivity {

    private Spinner menuSubjectSpinner;
    private Spinner menuTestSpinner;
    private Integer totalNumberOfObjectives = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluationcityrepresentation);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        totalNumberOfObjectives = DbTableLearningObjective.getResultsPerObjectiveForSubject("All").get(0).size();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_evaluation_results, menu);

        //setup the spinner for subject choice
        MenuItem menuSubject = menu.findItem(R.id.menu_subject);

        menuSubjectSpinner = (Spinner) MenuItemCompat.getActionView(menuSubject);
        Vector<String> subjectsVector = DbTableSubject.getAllSubjects();
        subjectsVector.insertElementAt(getString(R.string.all_subjects), 0);
        String[] arraySpinner = subjectsVector.toArray(new String[subjectsVector.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        menuSubjectSpinner.setAdapter(adapter);

        menuSubjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                drawChart(menuSubjectSpinner.getSelectedItem().toString(), "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        menuSubjectSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                menuTestSpinner.setSelection(0, true);
                return false;
            }
        });

        //setup the menu item for test choice
        MenuItem menuTest = menu.findItem(R.id.menu_test);

        menuTestSpinner = (Spinner) MenuItemCompat.getActionView(menuTest);
        Vector<String[]> testsArrayVector = DbTableTest.getAllTests();
        Vector<String> testsVector = new Vector<>();
        for (String[] test : testsArrayVector) {
            testsVector.add(test[0]);
        }
        testsVector.insertElementAt("All tests", 0);
        String[] arraySpinner2 = testsVector.toArray(new String[testsVector.size()]);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner2);

        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        menuTestSpinner.setAdapter(adapter2);

        menuTestSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                drawChart("", menuTestSpinner.getSelectedItem().toString());
                menuSubjectSpinner.setSelection(0, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        menuTestSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                menuSubjectSpinner.setSelection(0, true);
                return false;
            }
        });

        return true;
    }

    private void drawChart(String subject, String test) {

        if (subject.contentEquals(getString(R.string.all_subjects))) {
            subject = "All";
        }

        if (test.contentEquals("All tests")) {
            test = "All";
        }

        Vector<Vector<String>> evalForObjectives = new Vector<>();
        Vector<Vector<String>> certificativeEvalForObjectives = new Vector<>();

        Vector<String> objectives;
        Vector<String> formativeEvaluations;

        Vector<String> certificativeObjectives = new Vector<>();
        Vector<String> certificativeEvaluations = new Vector<>();
        if (!subject.contentEquals("")) {
            evalForObjectives = DbTableLearningObjective.getResultsPerObjectiveForSubject(subject);

            objectives = evalForObjectives.get(0);
            formativeEvaluations = evalForObjectives.get(1);
        } else if (!test.contentEquals("")) {
            evalForObjectives = DbTableLearningObjective.getResultsPerObjectiveForTest(test);
            if (evalForObjectives.get(0).size() == 0) {
                //certificative test selected
                certificativeEvalForObjectives = DbTableLearningObjective.getResultsPerObjectiveForCertificativeTest(test);

                objectives = certificativeEvalForObjectives.get(0);
                certificativeEvaluations = certificativeEvalForObjectives.get(1);
                formativeEvaluations = certificativeEvalForObjectives.get(2);
            } else {
                //formative test selected
                objectives = evalForObjectives.get(0);
                formativeEvaluations = evalForObjectives.get(1);
            }
        } else {
            objectives = evalForObjectives.get(0);
            formativeEvaluations = evalForObjectives.get(1);
        }


        //LinearLayOut Setup
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearLayoutCityRepres);
        linearLayout.removeAllViews();

        //getting screen width
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int screenWidth = metrics.widthPixels;

        for (int i = 0; i < objectives.size(); i++) {
            // Creating a new RelativeLayout
            RelativeLayout relativeLayout;
            if (certificativeEvaluations.size() > 0) {
                relativeLayout = prepareLayoutForObjectiveEvaluation(objectives.get(i), formativeEvaluations.get(i), certificativeEvaluations.get(i));
            } else {
                relativeLayout = prepareLayoutForObjectiveEvaluation(objectives.get(i), formativeEvaluations.get(i), "-1.0");
            }

            //adding view to layout
            linearLayout.addView(relativeLayout);

        }
    }

    private RelativeLayout prepareLayoutForObjectiveEvaluation(String objective, String formativeResult, String certificativeResult) {
        //check if we have to display only one of the results or both
        int nbResults;
        if (Double.valueOf(formativeResult) >= 0.0 && Double.valueOf(certificativeResult) >= 0.0) {
            nbResults = 2;
        } else {
            nbResults = 1;
        }

        //getting screen width
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // Creating a new RelativeLayout
        RelativeLayout relativeLayout = new RelativeLayout(this);

        // Creating a new TextView
        TextView tv = new TextView(this);
        tv.setText(objective);

        // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams layoutParamsObjText = new RelativeLayout.LayoutParams(
                screenWidth * nbResults / 4,
                screenWidth / 6);
        layoutParamsObjText.addRule(RelativeLayout.ABOVE);
        layoutParamsObjText.addRule(RelativeLayout.CENTER_HORIZONTAL);

        // Setting the parameters on the TextView
        tv.setLayoutParams(layoutParamsObjText);
        tv.setTextColor(Color.BLACK);

        // Adding the TextView to the RelativeLayout as a child
        relativeLayout.addView(tv);


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < 2; i++) {
            if (i == 0 && Double.valueOf(formativeResult) != -1.0 ||
                    i == 1 && Double.valueOf(certificativeResult) != -1.0) {
                // Creating a new RelativeLayout
                RelativeLayout relativeLayoutForIndividualResult = new RelativeLayout(this);

                //ImageView Setup
                ImageView imageView = new ImageView(this);

                //setting image resource
                String result;
                String qualitativeResult;
                if (i == 0) {
                    result = formativeResult;
                } else {
                    result = certificativeResult;
                }
                if (Double.valueOf(result) < 50) {
                    imageView.setImageResource(R.drawable.building_worst);
                    qualitativeResult = getResources().getString(R.string.candobetter);
                } else if (Double.valueOf(result) < 70) {
                    imageView.setImageResource(R.drawable.building_medium);
                    qualitativeResult = getResources().getString(R.string.ok);
                } else if (Double.valueOf(result) < 90) {
                    imageView.setImageResource(R.drawable.building_good);
                    qualitativeResult = getResources().getString(R.string.good) + "!";
                } else {
                    imageView.setImageResource(R.drawable.building_best);
                    qualitativeResult = getResources().getString(R.string.excellent) + "!!!";
                }

                //setting image position
                RelativeLayout.LayoutParams layoutParamsImage = new RelativeLayout.LayoutParams(
                        screenWidth / 4,
                        screenWidth - screenWidth / 5);
                layoutParamsImage.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                imageView.setLayoutParams(layoutParamsImage);

                relativeLayoutForIndividualResult.addView(imageView);

                // Creating a new TextView
                TextView subText = new TextView(this);
                if (i == 0) {
                    subText.setText(getResources().getString(R.string.formative) + ": " + qualitativeResult);
                } else {
                    subText.setText(getResources().getString(R.string.certificative) + ": " + qualitativeResult);
                }

                // Defining the layout parameters of the TextView
                RelativeLayout.LayoutParams layoutParamsSubText = new RelativeLayout.LayoutParams(
                        screenWidth / 4,
                        screenWidth / 6);
                layoutParamsSubText.addRule(RelativeLayout.ALIGN_BOTTOM);
                layoutParamsSubText.addRule(RelativeLayout.CENTER_HORIZONTAL);

                // Setting the parameters on the TextView
                subText.setLayoutParams(layoutParamsSubText);
                subText.setTextColor(Color.BLACK);
                subText.setY(screenHeight - 220);

                relativeLayoutForIndividualResult.addView(subText);

                linearLayout.addView(relativeLayoutForIndividualResult);
            }
        }

        relativeLayout.addView(linearLayout);

        // Defining the RelativeLayout layout parameters.
        // In this case I want to fill its parent
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT);
        rlp.rightMargin = 50;
        relativeLayout.setLayoutParams(rlp);

        return relativeLayout;
    }
}
