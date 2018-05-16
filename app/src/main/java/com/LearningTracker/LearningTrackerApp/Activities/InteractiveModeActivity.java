package com.LearningTracker.LearningTrackerApp.Activities;

import com.LearningTracker.LearningTrackerApp.LTApplication;
import com.LearningTracker.LearningTrackerApp.NetworkCommunication.NetworkCommunication;
import com.LearningTracker.LearningTrackerApp.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class InteractiveModeActivity extends Activity {
    NetworkCommunication mNetCom;
    public TextView intmod_out;
    private TextView intmod_wait_for_question;
    private TextView logView = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize view
        setContentView(R.layout.activity_interactivemode);
        intmod_wait_for_question = (TextView) findViewById(R.id.textView2);

        //mNetCom = new NetworkCommunication(this, getApplication());
        mNetCom = new NetworkCommunication(this, getApplication(), intmod_out, logView);
        mNetCom.ConnectToMaster();
        Boolean connectionInfo = false;
        for (int i = 0;!connectionInfo && i < 16; i++) {
            if (((LTApplication) getApplication()).getAppWifi().connectionSuccess == 1) {
                intmod_wait_for_question.setText(getString(R.string.keep_calm_and_wait));
                connectionInfo = true;
            } else if (((LTApplication) getApplication()).getAppWifi().connectionSuccess == -1) {
                intmod_wait_for_question.setText(getString(R.string.keep_calm_and_restart));
                connectionInfo = true;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (i >= 15) {
                intmod_wait_for_question.setText(getString(R.string.keep_calm_problem));
            }
        }


        ((LTApplication) this.getApplication()).resetQuitApp();
    }

    public void onStart() {
        super.onStart();
    }

    public void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
    }


    /**
     * Called when system is low on resources or finish() called on activity
     */
    public void onDestroy() {
        super.onDestroy();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.v("interactive mode: ", "focus lost");
            ((LTApplication) this.getApplication()).startActivityTransitionTimer();
        } else {
            ((LTApplication) this.getApplication()).stopActivityTransitionTimer();
            Log.v("interactive mode: ", "has focus");
        }
    }
}
