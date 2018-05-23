package com.LearningTracker.LearningTrackerApp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.LearningTracker.LearningTrackerApp.R;

public class ResultsActivtity extends Activity {

    private RecyclerView mRecyclerView;
    public RecyclerView.Adapter getmAdapter() {
        return mAdapter;
    }
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
    }

    public void showResultsList(View view) {
        Intent mIntent = new Intent(this, ResultsListActivity.class);
        startActivity(mIntent);
    }

    public void showResultsGraph(View view) {
        Intent mIntent = new Intent(this, EvaluationResultsActivity.class);
        startActivity(mIntent);
    }

    public void showMountainRepresentation(View view) {
        Intent mIntent = new Intent(this, EvaluationResultsActivity.class);
        startActivity(mIntent);
    }
}
