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
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class InteractiveModeActivity extends AppCompatActivity {
    public TextView intmod_out;
    private TextView intmod_wait_for_question;
    private TextView logView = null;
    protected MenuItem forwardButton;
    private InteractiveModeActivity interactiveModeActivity;
    private String TAG = "InteractiveModeActivity";

    //launch scanning QR code
    private Button scanQQButton;
    private Button toggleConnectionButton;
    private Camera camera;
    private int cameraId = 0;
    private int PERMISSION_REQUEST_CODE = 1;

    static protected Boolean backToTestFromQuestion = false;
    static protected int forwardQuestionMultipleChoice = 1;
    static protected int forwardQuestionShortAnswer = 2;
    static protected int forwardTest = 3;
    static protected int forwardGame = 4;

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
                launchQRscanning();
            }
        });

        toggleConnectionButton = findViewById(R.id.toggle_connection);
        toggleConnectionButton.setOnClickListener(l -> toggleConnection());

        connectToTeacher();
    }

    protected void launchQRscanning() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ContinuousQrScanning continuousQrScanning = new ContinuousQrScanning();
        fragmentTransaction.replace(R.id.viewgroup, continuousQrScanning);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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

    @Override
    protected void onPause() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        super.onPause();
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
    }

    protected void processQRCode() {
        if (!Koeko.qrCode.contentEquals("")) {
            launchResourceFromCode();
            Koeko.qrCode = "";
        }
    }

    protected void setForwardButton(int forwardType) {
        if (forwardButton != null && backToTestFromQuestion == false) {
            if (forwardType == forwardGame) {
                forwardButton.setTitle(getString(R.string.back_to_game) + " >");
            } else if (Koeko.qmcActivityState != null || Koeko.shrtaqActivityState != null) {
                forwardButton.setTitle(getString(R.string.back_to_question) + " >");
            } else if (Koeko.currentTestFragmentSingleton != null) {
                forwardButton.setTitle(getString(R.string.back_to_test) + " >");
            }
        } else {
            backToTestFromQuestion = false;
        }
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

    @Override
    public void onBackPressed() {
        Fragment fragment = getCurrentTopFragment(getSupportFragmentManager());
        if (fragment != null) {
            String classname = fragment.getClass().getName();
            classname = classname.split("\\.")[classname.split("\\.").length - 1];
            switch (classname) {
                case "ShortAnswerQuestionFragment":
                    ((ShortAnswerQuestionFragment) fragment).saveActivityState();
                    setForwardButton(forwardQuestionMultipleChoice);
                    break;
                case "MultChoiceQuestionFragment":
                    ((MultChoiceQuestionFragment) fragment).saveActivityState();
                    setForwardButton(forwardQuestionMultipleChoice);
                    break;
                case "TestFragment":
                    setForwardButton(forwardTest);
                    break;
                case "GameFragment":
                    setForwardButton(forwardGame);
                    break;
                default:
                    System.out.println("back from other fragment");
            }
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    public static Fragment getCurrentTopFragment(FragmentManager fm) {
        int stackCount = fm.getBackStackEntryCount();
        if (stackCount > 0) {
            if (stackCount == 2 && fm.getFragments().get(0).getClass().getName().contains("TestFragment")) {
                backToTestFromQuestion = true;
            }
            return  fm.getFragments().get(fm.getFragments().size() - 1);
        } else {
            List<Fragment> fragments = fm.getFragments();
            if (fragments != null && fragments.size()>0) {
                for (Fragment f: fragments) {
                    if (f != null && !f.isHidden()) {
                        return f;
                    }
                }
            }
        }
        return null;
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
            if (forwardButton.getTitle().toString().contentEquals("")) {
                System.out.println("forwardButton pressed but it shouldn't make any action");
            } else if (Koeko.gameState != null && forwardButton.getTitle().toString().contains(getString(R.string.back_to_game))) {
                Koeko.networkCommunicationSingleton.launchGameActivity(Koeko.gameState.getGameView(), Koeko.gameState.getGameView().getTeam());
            } else if (Koeko.qmcActivityState != null && Koeko.currentQuestionMultipleChoiceSingleton != null) {
                Koeko.networkCommunicationSingleton.launchMultChoiceQuestionActivity(Koeko.currentQuestionMultipleChoiceSingleton,
                        Koeko.networkCommunicationSingleton.directCorrection);
            } else if (Koeko.shrtaqActivityState != null && Koeko.currentQuestionShortAnswerSingleton != null) {
                Koeko.networkCommunicationSingleton.launchShortAnswerQuestionActivity(Koeko.currentQuestionShortAnswerSingleton,
                        Koeko.networkCommunicationSingleton.directCorrection);
            } else if (Koeko.currentTestFragmentSingleton != null) {
                Koeko.networkCommunicationSingleton.launchTestActivity(Koeko.currentTestFragmentSingleton.getmTest().getIdGlobal(),
                        Koeko.networkCommunicationSingleton.directCorrection);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.v("WARNING: ", "lost focus: user left application");
            Koeko.networkCommunicationSingleton.sendDisconnectionSignal("");
        } else {
            Log.v(TAG, "has focus");
        }
    }
}
