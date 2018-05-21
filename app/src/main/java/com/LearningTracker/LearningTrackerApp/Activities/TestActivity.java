package com.LearningTracker.LearningTrackerApp.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import com.LearningTracker.LearningTrackerApp.R;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_shortanswerquestion);

        Bundle bun = getIntent().getExtras();
        Long testID = bun.getLong("testID");

    }
}
