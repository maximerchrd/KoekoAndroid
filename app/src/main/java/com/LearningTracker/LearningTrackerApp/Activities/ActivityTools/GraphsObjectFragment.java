package com.LearningTracker.LearningTrackerApp.Activities.ActivityTools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.R;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableLearningObjective;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableSubject;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by maximerichard on 21.02.18.
 */
// Instances of this class are fragments representing a single
// object in our collection.
public class GraphsObjectFragment extends Fragment {
    public static final String ARG_OBJECT = "object";
    private RelativeLayout relativeLayout;
    private ArrayList<Integer> mQuestionIds = new ArrayList<>();
    private Integer mTabPosition = 0;
    private QuestionMultipleChoice mMulChoiceQuestion = null;
    private QuestionShortAnswer mShortAnsQuestion = null;
    private ImageView picture;
    private int number_of_possible_answers = 0;
    private ArrayList<CheckBox> checkBoxesArray;
    private Context mContext;
    boolean isImageFitToScreen = true;
    private Spinner menuSubjectSpinner;
    private View rootView;
    private Integer totalNumberOfObjectives = 1000;
    private HorizontalBarChart chart;
    private Integer width;
    private Integer height;
    private Vector<TextView> textViewVector;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativeLayoutFragment);
        Bundle args = getArguments();
        mTabPosition = args.getInt(ARG_OBJECT);

        mContext = rootView.getContext();
        picture = new ImageView(mContext);
        checkBoxesArray = new ArrayList<>();
        chart = (HorizontalBarChart) rootView.findViewById(R.id.horizontal_chart);
        picture = (ImageView) rootView.findViewById(R.id.target_image);
        picture.setAdjustViewBounds(false);
        textViewVector = new Vector<>();


        if (mTabPosition == 0) {
            totalNumberOfObjectives = DbTableLearningObjective.getResultsPerObjectiveForSubject("All").get(0).size();
            drawChart(getString(R.string.all_subjects));
            chart.setVisibility(View.VISIBLE);
            picture.setVisibility(View.GONE);
            Log.v("tab:", "0");
        } else {
            chart.setVisibility(View.GONE);
            picture.setVisibility(View.VISIBLE);
            drawTargetRepresentation(getString(R.string.all_subjects));
            Log.v("tab:", "1");
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.activity_evaluation_results, menu);
        MenuItem menuSubject = menu.findItem(R.id.menu_subject);

        menuSubjectSpinner = (Spinner) MenuItemCompat.getActionView(menuSubject);
        Vector<String> subjectsVector = DbTableSubject.getAllSubjects();
        subjectsVector.insertElementAt(getString(R.string.all_subjects),0);
        String[] arraySpinner = subjectsVector.toArray(new String[subjectsVector.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_item, arraySpinner);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        menuSubjectSpinner.setAdapter(adapter);

        menuSubjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.v("menuSpinner: ", "onItemSelected");
                drawChart(menuSubjectSpinner.getSelectedItem().toString());
                drawTargetRepresentation(menuSubjectSpinner.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }

    private void drawChart (String subject) {
        if (subject.contentEquals(getString(R.string.all_subjects))) {
            subject = "All";
        }
        Vector<Vector<String>> evalForObjectives = DbTableLearningObjective.getResultsPerObjectiveForSubject(subject);
        Vector<String> objectives = evalForObjectives.get(0);
        Vector<String> evaluations = evalForObjectives.get(1);


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

    private void drawTargetRepresentation(String subject) {
        for (int i = textViewVector.size() - 1; i > 0; i--) {
            relativeLayout.removeView(textViewVector.get(i));
            textViewVector.remove(i);
        }
        if (subject.contentEquals(getString(R.string.all_subjects))) {
            subject = "All";
        }

        Vector<Vector<String>> evalForObjectives = DbTableLearningObjective.getResultsPerObjectiveForSubject(subject);
        Vector<String> objectives = evalForObjectives.get(0);
        Vector<String> evaluations = evalForObjectives.get(1);
        Vector<String> evaluations_low = new Vector<>();
        Vector<String> evaluations_middle = new Vector<>();
        Vector<String> evaluations_high = new Vector<>();
        Vector<String> evaluations_top = new Vector<>();
        for (int i = 0; i < evaluations.size(); i++) {
            Integer eval = Double.valueOf(evaluations.get(i)).intValue();
            if (eval < 50) {
                evaluations_low.add(objectives.get(i));
            } else if (eval < 70) {
                evaluations_middle.add(objectives.get(i));
            } else if (eval < 90) {
                evaluations_high.add(objectives.get(i));
            } else {
                evaluations_top.add(objectives.get(i));
            }
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;;

        displayObjectives(evaluations_low, Color.RED, 0.5);
        displayObjectives(evaluations_middle, Color.GREEN, 0.3);
        displayObjectives(evaluations_high, Color.BLUE, 0.1);
        displayObjectives(evaluations_top, Color.MAGENTA, 0.0);
    }
    private void displayObjectives (final Vector<String> objectives, int color, Double performanceFactor) {
        for (int i = 0; i < objectives.size(); i++) {
            //objectives.set(i,objectives.get(i) + " but this is a very important learning objective that you should understand in its full extent. Yes, I promise!");
            final String fullText = objectives.get(i);
            final String shortText;
            if (fullText.length() > 25) {
                shortText = objectives.get(i).substring(0,25) + "...";
            } else {
                shortText = fullText;
            }
            final TextView objectiveText = new TextView(mContext);
            objectiveText.setText(shortText);
            objectiveText.setTextColor(color);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            int stepper = 1 - (i % 3);
            int horizontalStep = width/3 * (stepper);
            int leftMargin = width / 3 + horizontalStep;
            int verticalStep = (i / 3) * height/20;
            int topMargin = (int)(height * performanceFactor) + verticalStep;    //should increase one step every 3 textViews
            layoutParams.setMargins(leftMargin, topMargin, 0, 0);
            objectiveText.setLayoutParams(layoutParams);
            if (mTabPosition == 0) {
                objectiveText.setVisibility(View.GONE);
            } else {
                objectiveText.setVisibility(View.VISIBLE);
            }
            objectiveText.setOnClickListener(new View.OnClickListener() {
                Boolean expanded = false;
                @Override
                public void onClick(View v) {
                    if (expanded) {
                        objectiveText.setText(shortText);
                        objectiveText.setBackgroundColor(Color.TRANSPARENT);
                        expanded = false;
                    } else {
                        objectiveText.setText(fullText);
                        objectiveText.setBackgroundColor(Color.WHITE);
                        objectiveText.bringToFront();
                        expanded = true;
                    }
                }
            });
            textViewVector.add(objectiveText);
            relativeLayout.addView(objectiveText);
        }
    }
}
