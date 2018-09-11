package com.LearningTracker.LearningTrackerApp.Activities;

import com.LearningTracker.LearningTrackerApp.Activities.CameraHandling.CameraActivity;
import com.LearningTracker.LearningTrackerApp.LTApplication;
import com.LearningTracker.LearningTrackerApp.NetworkCommunication.NetworkCommunication;
import com.LearningTracker.LearningTrackerApp.R;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class InteractiveModeActivity extends Activity {
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

        intmod_wait_for_question.setText("Connecting...");

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
                                intmod_wait_for_question.setText("Automatic connection failed. If it keeps failing, " +
                                        "try to set the teachers'IP address manually in \"Settings\"");
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
                camera = Camera.open(cameraId);
                Intent capturecodeIntent = new Intent(InteractiveModeActivity.this, CameraActivity.class);
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
                forwardButton.setTitle("Back To Question >");
            } else if (LTApplication.currentTestActivitySingleton != null) {
                forwardButton.setTitle("Back To Test >");
            }
        }

        if (!LTApplication.qrCode.contentEquals("")) {
            Log.d(TAG, "onResume: found valid qr code");
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
