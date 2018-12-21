package com.wideworld.koeko.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.NetworkCommunication.NetworkCommunication;
import com.wideworld.koeko.QuestionsManagement.GameType;
import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableSettings;

import pl.droidsonroids.gif.GifImageView;

public class GameActivity extends AppCompatActivity {
    private MenuItem forwardButton;
    private String TAG = "GameActivity";

    private TextView redScore;
    private TextView blueScore;

    private float widthToCoverToTop = 0.23f;
    private float heightToCoverToTop = 0.37f;
    private float imageRatio = 0.9257f;
    private ImageView blueClimber;
    private float blueClimberInitx = 0;
    private float blueClimberInity = 0;
    private float redClimberInitx = 0;
    private float redClimberInity = 0;
    private ImageView redClimber;
    private GifImageView redCelebration;
    private GifImageView blueCelebration;
    private RelativeLayout backgroundView;
    private ImageView backgroudImage;
    private Integer gameType = -1;
    private Integer endScore = 0;

    //launch scanning QR code
    private Button scanQQButton;
    private Button readyButton;
    private Camera camera;
    private int cameraId = 0;
    private int PERMISSION_REQUEST_CODE = 1;


    public void changeScore (Double teamOneScore, Double teamTwoScore) {
        Log.d(TAG, "changeScore: " + teamOneScore + "; " + teamTwoScore);

        final float newXTeamone = blueClimberInitx + (float) ((teamOneScore / endScore) * backgroudImage.getWidth() * widthToCoverToTop);
        final float newYTeamOne = blueClimberInity - (float) ((teamOneScore / endScore) * backgroudImage.getWidth() * imageRatio * heightToCoverToTop);
        final float newXTeamTwo = redClimberInitx - (float) ((teamTwoScore / endScore) * backgroudImage.getWidth() * widthToCoverToTop);
        final float newYTeamTwo = redClimberInity - (float) ((teamTwoScore / endScore) * backgroudImage.getWidth() * imageRatio * heightToCoverToTop);
        this.runOnUiThread(() -> {
            blueClimber.setX(newXTeamone);
            blueClimber.setY(newYTeamOne);
            redClimber.setX(newXTeamTwo);
            redClimber.setY(newYTeamTwo);
            if (teamOneScore / endScore == 1) {
                if (blueClimber != null) {
                    blueClimber.setVisibility(View.GONE);
                    blueCelebration.setX(blueClimber.getX());
                    blueCelebration.setY(blueClimber.getY());
                    blueCelebration.getLayoutParams().width = blueClimber.getLayoutParams().width;
                    blueCelebration.getLayoutParams().height = blueClimber.getLayoutParams().height;
                    blueCelebration.setVisibility(View.VISIBLE);
                }
            }
            if (teamTwoScore / endScore == 1) {
                if (redClimber != null) {
                    redClimber.setVisibility(View.GONE);
                    redCelebration.setVisibility(View.VISIBLE);
                    redCelebration.setX(redClimber.getX());
                    redCelebration.setY(redClimber.getY());
                    redCelebration.getLayoutParams().width = redClimber.getLayoutParams().width;
                    redCelebration.getLayoutParams().height = redClimber.getLayoutParams().height;
                }
            }

            //change score text
            blueScore.setText(String.valueOf(teamOneScore.intValue()));
            redScore.setText(String.valueOf(teamTwoScore.intValue()));
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize view
        setContentView(R.layout.activity_game);

        Bundle bun = getIntent().getExtras();
        endScore = bun.getInt("endScore");
        gameType = bun.getInt("gameType");

        backgroundView = findViewById(R.id.background_landscape_layout);
        backgroudImage = findViewById(R.id.background_landscape);

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

        scanQQButton = findViewById(R.id.scan_qr_button_game);
        if (gameType == GameType.qrCodeGame) {
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
                    Intent capturecodeIntent = new Intent(GameActivity.this, QRCodeReaderActivity.class);
                    startActivity(capturecodeIntent);
                }
            });
        } else {
            scanQQButton.setVisibility(View.GONE);
        }

        readyButton = findViewById(R.id.ready_button);
        final Activity currentActivity = this;
        if (gameType == GameType.orderedAutomaticSending || gameType == GameType.randomAutomaticSending) {
            readyButton.setOnClickListener(e -> {
                Koeko.networkCommunicationSingleton.sendStringToServer("READY///" + DbTableSettings.getUUID() + "///");
                currentActivity.runOnUiThread(() -> {
                    readyButton.setBackgroundColor(getResources().getColor(R.color.gamegreen));
                });
                Log.d(TAG, "change readybutton UI");
            });
        } else {
            readyButton.setVisibility(View.GONE);
        }

        redCelebration = findViewById(R.id.red_celebration);
        blueCelebration = findViewById(R.id.blue_celebration);

        final Context context = this;
        backgroudImage.post(() -> {
            //position climbers
            blueClimber = new ImageView(context);
            blueClimber.setImageResource(R.drawable.blueclimber);
            backgroundView.addView(blueClimber);
            blueClimberInitx = backgroudImage.getWidth() * 0.22f;
            blueClimberInity = backgroudImage.getHeight() * 0.5f;
            blueClimber.setX(blueClimberInitx);
            blueClimber.setY(blueClimberInity);
            Double imageRatio = (double)backgroudImage.getHeight() / (double)backgroudImage.getWidth();
            int imageWidth = (int)(backgroudImage.getWidth() * 0.1f);
            int imageHeight = (int)(backgroudImage.getWidth() * 0.1f * imageRatio);
            blueClimber.getLayoutParams().width = imageWidth;
            blueClimber.getLayoutParams().height = imageHeight;
            blueClimber.requestLayout();

            redClimber = new ImageView(context);
            redClimber.setImageResource(R.drawable.redclimber);
            backgroundView.addView(redClimber);
            redClimberInitx = backgroudImage.getWidth() * 0.67f;
            redClimberInity = backgroudImage.getHeight() * 0.5f;
            redClimber.setX(redClimberInitx);
            redClimber.setY(redClimberInity);
            redClimber.getLayoutParams().width = imageWidth;
            redClimber.getLayoutParams().height = imageHeight;
            redClimber.requestLayout();

            //initialize scores
            blueScore = findViewById(R.id.blue_score);
            blueScore.setText("0");
            redScore = findViewById(R.id.red_score);
            redScore.setText("0");
        });

        Koeko.currentGameActivity = this;
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

        if (forwardButton != null) {
            if (Koeko.qmcActivityState != null || Koeko.shrtaqActivityState != null) {
                forwardButton.setTitle(getString(R.string.back_to_question) + " >");
            } else if (Koeko.currentTestActivitySingleton != null) {
                forwardButton.setTitle(getString(R.string.back_to_test) + " >");
            }
        }

        if (!Koeko.qrCode.contentEquals("")) {
            sendQuestionRequest();
            Koeko.qrCode = "";
        }

        Koeko.MAX_ACTIVITY_TRANSITION_TIME_MS = Koeko.SHORT_TRANSITION_TIME;

        readyButton.setBackgroundColor(getResources().getColor(R.color.koekored));
    }

    private void sendQuestionRequest() {
        String[] codeArray = Koeko.qrCode.split(":");
        if (codeArray.length >= 3) {
            String resCodeString = codeArray[0];
            Koeko.networkCommunicationSingleton.sendStringToServer("GAMESET///"
                    + NetworkCommunication.deviceIdentifier + "///" + resCodeString + "///");
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
            Log.v(TAG, "focus lost");
            ((Koeko) this.getApplication()).startActivityTransitionTimer();
        } else {
            ((Koeko) this.getApplication()).stopActivityTransitionTimer();
            Log.v(TAG, "has focus");
        }
    }
}
