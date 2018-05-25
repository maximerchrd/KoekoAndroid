package com.LearningTracker.LearningTrackerApp.Activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.LearningTracker.LearningTrackerApp.database_management.DbTableSubject;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableTest;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.LearningTracker.LearningTrackerApp.R;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableLearningObjective;

import java.util.ArrayList;
import java.util.Vector;

import static android.content.ContentValues.TAG;

public class EvaluationResultsActivity extends Activity {

    private Spinner menuSubjectSpinner;
    private Spinner menuTestSpinner;
    private Integer totalNumberOfObjectives = 1000;
    private Boolean automaticItemSelection = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation_results);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        totalNumberOfObjectives = DbTableLearningObjective.getResultsPerObjectiveForSubject("All").get(0).size();

        drawChart(getString(R.string.all_subjects), "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_evaluation_results, menu);

        //setup the spinner for subject choice
        MenuItem menuSubject = menu.findItem(R.id.menu_subject);

        menuSubjectSpinner = (Spinner) MenuItemCompat.getActionView(menuSubject);
        Vector <String> subjectsVector = DbTableSubject.getAllSubjects();
        subjectsVector.insertElementAt(getString(R.string.all_subjects),0);
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
        Vector <String[]> testsArrayVector = DbTableTest.getAllTests();
        Vector <String> testsVector = new Vector<>();
        for (String[] test : testsArrayVector) {
            testsVector.add(test[0]);
        }
        testsVector.insertElementAt("All tests",0);
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

    private void drawChart (String subject, String test) {
        if (subject.contentEquals(getString(R.string.all_subjects))) {
            subject = "All";
        }

        if (test.contentEquals("All")) {
            test = "All";
        }

        Vector<Vector<String>> evalForObjectives = new Vector<>();
        if (!subject.contentEquals("")) {
            evalForObjectives = DbTableLearningObjective.getResultsPerObjectiveForSubject(subject);
        } else if (!test.contentEquals("")) {
            evalForObjectives = DbTableLearningObjective.getResultsPerObjectiveForTest(test);
        }
        Vector<String> objectives = evalForObjectives.get(0);
        Vector<String> evaluations = evalForObjectives.get(1);
        HorizontalBarChart chart = (HorizontalBarChart) findViewById(R.id.chart);

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < evaluations.size(); i++) {
            entries.add(new BarEntry(i, Float.valueOf(evaluations.get(i))));
        }
        BarDataSet dataset = new BarDataSet(entries, "Evaluation for each learning objective");

        //Defining the X-Axis Labels
        final ArrayList<String> labels = new ArrayList<String>();

        for (int i = 0; i < objectives.size(); i++) {
            labels.add(objectives.get(i));
        }
        for (int i = objectives.size(); i < totalNumberOfObjectives; i ++) {
            labels.add("if you see this, there was a problem!");
        }


        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1.0f);
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return labels.get((int) value);
            }
        });

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMaximum(100);
        yAxis.setAxisMinimum(0);
        YAxis yAxis2 = chart.getAxisRight();
        yAxis2.setAxisMaximum(100);
        yAxis2.setAxisMinimum(0);

        chart.getDescription().setEnabled(false);
        dataset.setDrawValues(false);
        BarData data = new BarData(dataset);
        chart.setData(data);
        chart.invalidate();
    }

}
