package com.LearningTracker.LearningTrackerApp.Activities.Tools;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.LearningTracker.LearningTrackerApp.LTApplication;
import com.LearningTracker.LearningTrackerApp.R;

public class CustomAlertDialog extends Dialog {
    private Context context;
    private TextView correctionMessage;
    private Button okButton;
    private Activity activity;
    private Boolean testInstructions;

    public CustomAlertDialog(Context context) {
        super(context);
        this.context = context;
        this.testInstructions = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.correction_alertdialog);
        correctionMessage = findViewById(R.id.correction_text);
        okButton = findViewById(R.id.btn_ok);
        okButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                Log.v("Correction dialog: ", "focus lost");
                ((LTApplication) context.getApplicationContext()).startActivityTransitionTimer();
                dismiss();
                if (!testInstructions) {
                    activity.finish();
                    activity.invalidateOptionsMenu();
                } else {
                    if (LTApplication.currentTestActivitySingleton.testChronometer != null) {
                        LTApplication.currentTestActivitySingleton.testChronometer.reset();
                        LTApplication.currentTestActivitySingleton.testChronometer.run();
                        LTApplication.activeTestStartTime = LTApplication.currentTestActivitySingleton.testChronometer.getStartTime();
                    }
                }
            }
        });
    }

    public void setProperties(String message, Activity activity) {
        correctionMessage.setText(message);
        this.activity = activity;
    }

    public Boolean getTestInstructions() {
        return testInstructions;
    }

    public void setTestInstructions(Boolean testInstructions) {
        this.testInstructions = testInstructions;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.v("Correction dialog: ", "focus lost");
            ((LTApplication) context.getApplicationContext()).startActivityTransitionTimer();
        } else {
            ((LTApplication) context.getApplicationContext()).stopActivityTransitionTimer();
            Log.v("Correction dialog: ", "has focus");
        }
    }
}
