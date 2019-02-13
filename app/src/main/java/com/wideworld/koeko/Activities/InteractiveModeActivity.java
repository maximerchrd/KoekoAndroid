package com.wideworld.koeko.Activities;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.NetworkCommunication.NetworkCommunication;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.ClientToServerTransferable;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.CtoSPrefix;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableQuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableQuestionShortAnswer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class InteractiveModeActivity extends AppCompatActivity {
    public TextView intmod_out;
    private TextView intmod_wait_for_question;
    private TextView logView = null;
    private MenuItem forwardButton;
    private InteractiveModeActivity interactiveModeActivity;
    private String TAG = "InteractiveModeActivity";

    //launch scanning QR code
    private Button scanQQButton;
    private Button toggleConnectionButton;
    private Camera camera;
    private int cameraId = 0;
    private int PERMISSION_REQUEST_CODE = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        interactiveModeActivity = this;
        if (Koeko.networkCommunicationSingleton != null) {
            Koeko.networkCommunicationSingleton.mInteractiveModeActivity = this;
        }

        //initialize view
        setContentView(R.layout.activity_interactivemode);
        intmod_wait_for_question = (TextView) findViewById(R.id.textView2);

        //START code for functional testing
        if (Koeko.testConnectivity > 0) {
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (Exception e) {
                }
                Handler mainHandler = new Handler(getApplicationContext().getMainLooper());

                // This is your code
                Runnable myRunnable = () -> onBackPressed();
                mainHandler.post(myRunnable);
            }).start();
        }
        //END code for functional testing

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

        scanQQButton = findViewById(R.id.scan_qr_button);
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
                Koeko.MAX_ACTIVITY_TRANSITION_TIME_MS = Koeko.LONG_TRANSITION_TIME;
                Intent capturecodeIntent = new Intent(InteractiveModeActivity.this, ContinuousQrScanning.class);
                startActivity(capturecodeIntent);
            }
        });

        toggleConnectionButton = findViewById(R.id.toggle_connection);
        toggleConnectionButton.setOnClickListener(l -> toggleConnection());

        connectToTeacher();
    }

    private void toggleConnection() {
        Log.d(TAG, "toggleConnection: NetworkCommunication.connected=" + NetworkCommunication.connected);
        //TODO: fix problem when stopping connection when wifi was lost (keeps displaying "stop connection" when we are disconnected)
        if (NetworkCommunication.connected == 1) {
            Koeko.networkCommunicationSingleton.sendDisconnectionSignal("close-connection");
            Koeko.networkCommunicationSingleton.closeConnection();
            showDisconnected();
        } else if (NetworkCommunication.connected == 0) {
            this.runOnUiThread(() -> toggleConnectionButton.setText(R.string.stop_connection));
            connectToTeacher();
        } else {
            Koeko.networkCommunicationSingleton.closeConnection();
            showDisconnected();
        }
    }

    private void connectToTeacher() {
        Log.d(TAG, "connectToTeacher, NetworkCommunication.connected: " + NetworkCommunication.connected);
        if (NetworkCommunication.connected == 0) {
            if (((Koeko) getApplication()).getAppNetwork() == null) {
                ((Koeko) getApplication()).setAppNetwork(new NetworkCommunication(this, getApplication(), intmod_out, logView, interactiveModeActivity));
            }
            ((Koeko) getApplication()).getAppWifi().connectionSuccess = 0;
            ((Koeko) getApplication()).getAppNetwork().connectToMaster(0);

            intmod_wait_for_question.setText(getString(R.string.connecting));

            new Thread(() -> {
                Boolean connectionInfo = false;
                for (int i = 0; !connectionInfo && i < 24; i++) {
                    if (((Koeko) getApplication()).getAppWifi().connectionSuccess == 1) {
                        showConnected();
                        connectionInfo = true;
                    } else if (((Koeko) getApplication()).getAppWifi().connectionSuccess == -1) {
                        this.runOnUiThread(() -> intmod_wait_for_question.setText(getString(R.string.keep_calm_and_restart)));
                        connectionInfo = true;
                        NetworkCommunication.connected = 0;
                        this.runOnUiThread(() -> toggleConnectionButton.setText(R.string.start_connection));
                        Log.d(TAG, "connectToTeacher: connectionSuccess = -1");
                    } else if (((Koeko) getApplication()).getAppWifi().connectionSuccess == -2) {
                        this.runOnUiThread(() -> intmod_wait_for_question.setText(getString(R.string.automatic_connection_failed)));
                        connectionInfo = true;
                        NetworkCommunication.connected = 0;
                        this.runOnUiThread(() -> toggleConnectionButton.setText(R.string.start_connection));
                        Log.d(TAG, "connectToTeacher: connectionSuccess = -2");
                    }
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (i >= 23) {
                        this.runOnUiThread(() -> intmod_wait_for_question.setText(getString(R.string.keep_calm_problem)));
                        NetworkCommunication.connected = 0;
                        this.runOnUiThread(() -> toggleConnectionButton.setText(R.string.start_connection));
                    }
                }
            }).start();

            ((Koeko) this.getApplication()).resetQuitApp();
        } else if (NetworkCommunication.connected == 2) {
            Log.d(TAG, "connectToTeacher: trying to connect but already connecting");
        } else {
            Log.d(TAG, "connectToTeacher: Sending Connection String from OnCreate");
            Koeko.networkCommunicationSingleton.sendBytesToServer(Koeko.networkCommunicationSingleton.getConnectionBytes());
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d(TAG, "Camera found");
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

        if (NetworkCommunication.connected == 1) {
            showConnected();
        } else if (NetworkCommunication.connected == 0){
            showDisconnected();
        } else {
            this.runOnUiThread(() -> {
                Log.d(TAG, "showConnecting");
                intmod_wait_for_question.setText(getString(R.string.connecting));
                toggleConnectionButton.setText(R.string.stop_connection);
            });
        }

        if (forwardButton != null) {
            if (Koeko.qmcActivityState != null || Koeko.shrtaqActivityState != null) {
                forwardButton.setTitle(getString(R.string.back_to_question) + " >");
            } else if (Koeko.currentTestActivitySingleton != null) {
                forwardButton.setTitle(getString(R.string.back_to_test) + " >");
            }
        }

        if (!Koeko.qrCode.contentEquals("")) {
            launchResourceFromCode();
            Koeko.qrCode = "";
        }

        if (NetworkCommunication.network_solution == 1) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            12345);
                    return;
                }
            }
        }

        Koeko.MAX_ACTIVITY_TRANSITION_TIME_MS = Koeko.SHORT_TRANSITION_TIME;
    }

    public void showDisconnected() {
        this.runOnUiThread(() -> {
            Log.d(TAG, "showDisconnected");
            intmod_wait_for_question.setText(getString(R.string.disconnected));
            toggleConnectionButton.setText(R.string.start_connection);
        });
    }

    public void showConnected() {
        this.runOnUiThread(() -> {
            Log.d(TAG, "showConnected");
            intmod_wait_for_question.setText(getString(R.string.keep_calm_and_wait));
            toggleConnectionButton.setText(R.string.stop_connection);
        });
    }

    public void showMessage(String message) {
        this.runOnUiThread(() -> intmod_wait_for_question.setText(message));
    }

    public void showLongToast(String message) {
        this.runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    public void showShortToast(String message) {
        this.runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void launchResourceFromCode() {
        String[] codeArray = Koeko.qrCode.split(":");
        if (codeArray.length >= 3 && codeArray[0].contentEquals("GAME")) {
            ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.gameTeamPrefix);
            transferable.setOptionalArgument1(codeArray[1]);
            transferable.setOptionalArgument2(codeArray[2]);
            Koeko.networkCommunicationSingleton.sendDataToClient(transferable.getTransferableBytes());
        } else if (codeArray.length >= 3) {
            String directCorrection = codeArray[2];
            String resCodeString = codeArray[0];
            if (DbTableQuestionMultipleChoice.checkIfIdMatchResource(resCodeString)) {
                Long resCode = Long.valueOf(resCodeString);
                if (resCode < 0) {
                    resCode = -resCode;
                    Koeko.networkCommunicationSingleton.launchTestActivity(resCode, directCorrection);
                } else {
                    QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(resCodeString);
                    if (questionShortAnswer.getQuestion().length() == 0 || questionShortAnswer.getQuestion().contentEquals("none")) {
                        QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(resCodeString);
                        Koeko.networkCommunicationSingleton.launchMultChoiceQuestionActivity(questionMultipleChoice, directCorrection);
                    } else {
                        Koeko.networkCommunicationSingleton.launchShortAnswerQuestionActivity(questionShortAnswer, directCorrection);
                    }
                }
            } else {
                ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.requestPrefix);
                transferable.setOptionalArgument1(NetworkCommunication.deviceIdentifier);
                transferable.setOptionalArgument2(resCodeString);
                Koeko.networkCommunicationSingleton.sendBytesToServer(transferable.getTransferableBytes());
            }
        } else {
            Log.w(TAG, "Array from QR code string is too short");
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
            if (Koeko.qmcActivityState != null && Koeko.currentQuestionMultipleChoiceSingleton != null) {
                Koeko.networkCommunicationSingleton.launchMultChoiceQuestionActivity(Koeko.currentQuestionMultipleChoiceSingleton,
                        Koeko.networkCommunicationSingleton.directCorrection);
            } else if (Koeko.shrtaqActivityState != null && Koeko.currentQuestionShortAnswerSingleton != null) {
                Koeko.networkCommunicationSingleton.launchShortAnswerQuestionActivity(Koeko.currentQuestionShortAnswerSingleton,
                        Koeko.networkCommunicationSingleton.directCorrection);
            } else if (Koeko.currentTestActivitySingleton != null) {
                Koeko.networkCommunicationSingleton.launchTestActivity(Koeko.currentTestActivitySingleton.getmTest().getIdGlobal(),
                        Koeko.networkCommunicationSingleton.directCorrection);
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
     *
     * @param hasFocus
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.v("interactive mode: ", "focus lost");
            ((Koeko) this.getApplication()).startActivityTransitionTimer();
        } else {
            ((Koeko) this.getApplication()).stopActivityTransitionTimer();
            Log.v("interactive mode: ", "has focus");
        }
    }
}
