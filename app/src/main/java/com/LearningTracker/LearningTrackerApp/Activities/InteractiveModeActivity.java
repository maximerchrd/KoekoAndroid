package com.LearningTracker.LearningTrackerApp.Activities;

import com.LearningTracker.LearningTrackerApp.LTApplication;
import com.LearningTracker.LearningTrackerApp.NetworkCommunication.NetworkCommunication;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.Test;
import com.LearningTracker.LearningTrackerApp.R;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableTest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class InteractiveModeActivity extends AppCompatActivity {
    NetworkCommunication mNetCom;
    public TextView intmod_out;
    private TextView intmod_wait_for_question;
    private TextView logView = null;
    private MenuItem forwardButton;
    private InteractiveModeActivity interactiveModeActivity;

    //launch scanning QR code
    private Button scanQQButton;
    private final static String DEBUG_TAG = "Interactive Mode";
    private Camera camera;
    private int cameraId = 0;
    private Camera camer;
    private int PERMISSION_REQUEST_CODE = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        interactiveModeActivity = this;

        //initialize view
        setContentView(R.layout.activity_interactivemode);
        intmod_wait_for_question = (TextView) findViewById(R.id.textView2);

        //mNetCom = new NetworkCommunication(this, getApplication());
        mNetCom = new NetworkCommunication(this, getApplication(), intmod_out, logView);
        mNetCom.ConnectToMaster();

        intmod_wait_for_question.setText(getString(R.string.connecting));

        new Thread(new Runnable() {
            public void run() {
                Boolean connectionInfo = false;
                for (int i = 0;!connectionInfo && i < 24; i++) {
                    if (((LTApplication) getApplication()).getAppWifi().connectionSuccess == 1) {
                        interactiveModeActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                intmod_wait_for_question.setText(getString(R.string.keep_calm_and_wait));
                            }
                        });

                        connectionInfo = true;
                    } else if (((LTApplication) getApplication()).getAppWifi().connectionSuccess == -1) {
                        interactiveModeActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                intmod_wait_for_question.setText(getString(R.string.keep_calm_and_restart));
                            }
                        });
                        connectionInfo = true;
                    } else if (((LTApplication) getApplication()).getAppWifi().connectionSuccess == -2) {
                        interactiveModeActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                intmod_wait_for_question.setText(getString(R.string.automatic_connection_failed));
                            }
                        });
                        connectionInfo = true;
                    }
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (i >= 23) {
                        interactiveModeActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                intmod_wait_for_question.setText(getString(R.string.keep_calm_problem));
                            }
                        });
                    }
                }
            }
        }).start();


        ((LTApplication) this.getApplication()).resetQuitApp();

        if (LTApplication.testConnectivity > 0) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(1500);
                    } catch (Exception e) {
                    }
                    Handler mainHandler = new Handler(getApplicationContext().getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {onBackPressed();} // This is your code
                    };
                    mainHandler.post(myRunnable);
                }
            }).start();
        }

        // do we have a camera?
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
            }
        }

        scanQQButton = findViewById(R.id.scanQRButton);
        scanQQButton.setOnClickListener(e -> {
            // Check if we have write permission
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.CAMERA},
                        1
                );
            } else {
                ((LTApplication) this.getApplication()).MAX_ACTIVITY_TRANSITION_TIME_MS = 1200;
                Intent capturecodeIntent = new Intent(InteractiveModeActivity.this, QRCodeReaderActivity.class);
                startActivity(capturecodeIntent);
            }
        });
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d(DEBUG_TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (forwardButton != null) {
            if (LTApplication.qmcActivityState != null || LTApplication.shrtaqActivityState != null) {
                forwardButton.setTitle(getString(R.string.back_to_question) + " >");
            } else if (LTApplication.currentTestActivitySingleton != null) {
                forwardButton.setTitle(getString(R.string.back_to_test) + " >");
            }
        }

        if (!LTApplication.qrCode.contentEquals("")) {
            launchResourceFromCode();
            LTApplication.qrCode = "";
        }
    }

    private void launchResourceFromCode() {
        String[] codeArray = LTApplication.qrCode.split(":");
        if (codeArray.length >= 3) {
            String directCorrection = codeArray[2];
            String resCodeString = codeArray[0];
            Long resCode = Long.valueOf(resCodeString);
            if (resCode < 0) {
                resCode = -resCode;
                LTApplication.wifiCommunicationSingleton.launchTestActivity(resCode, directCorrection);
            } else {
                QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(resCodeString);
                if (questionShortAnswer.getQUESTION().length() == 0 || questionShortAnswer.getQUESTION().contentEquals("none")) {
                    QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(resCodeString);
                    LTApplication.wifiCommunicationSingleton.launchMultChoiceQuestionActivity(questionMultipleChoice, directCorrection);
                } else {
                    LTApplication.wifiCommunicationSingleton.launchShortAnswerQuestionActivity(questionShortAnswer, directCorrection);
                }
            }
        } else {
            Log.w(TAG,"Array from QR code string is too short");
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_interactivemode, menu);
        forwardButton = menu.findItem(R.id.forwardbutton);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.forwardbutton) {
            if (LTApplication.qmcActivityState != null && LTApplication.currentQuestionMultipleChoiceSingleton != null) {
                LTApplication.wifiCommunicationSingleton.launchMultChoiceQuestionActivity(LTApplication.currentQuestionMultipleChoiceSingleton,
                        LTApplication.wifiCommunicationSingleton.directCorrection);
            } else if (LTApplication.shrtaqActivityState != null && LTApplication.currentQuestionShortAnswerSingleton != null) {
                LTApplication.wifiCommunicationSingleton.launchShortAnswerQuestionActivity(LTApplication.currentQuestionShortAnswerSingleton,
                        LTApplication.wifiCommunicationSingleton.directCorrection);
            } else if (LTApplication.currentTestActivitySingleton != null) {
                LTApplication.wifiCommunicationSingleton.launchTestActivity(LTApplication.currentTestActivitySingleton.getmTest().getIdGlobal(),
                        LTApplication.wifiCommunicationSingleton.directCorrection);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when system is low on resources or finish() called on activity
     */
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * method used to know if we send a disconnection signal to the server
     * @param hasFocus
     */
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
