package com.wideworld.koeko.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.NetworkCommunication.NetworkCommunication;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.ClientToServerTransferable;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.CtoSPrefix;
import com.wideworld.koeko.QuestionsManagement.GameState;
import com.wideworld.koeko.QuestionsManagement.GameType;
import com.wideworld.koeko.QuestionsManagement.GameView;
import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableSettings;

import pl.droidsonroids.gif.GifImageView;

public class GameFragment extends Fragment {
    private MenuItem forwardButton;
    private String TAG = "GameFragment";

    private Integer team = 0;
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
        Koeko.gameState.setScoreTeamOne(teamOneScore);
        Koeko.gameState.setScoreTeamTwo(teamTwoScore);

        final float newXTeamone = blueClimberInitx + (float) ((teamOneScore / endScore) * backgroudImage.getWidth() * widthToCoverToTop);
        final float newYTeamOne = blueClimberInity - (float) ((teamOneScore / endScore) * backgroudImage.getWidth() * imageRatio * heightToCoverToTop);
        final float newXTeamTwo = redClimberInitx - (float) ((teamTwoScore / endScore) * backgroudImage.getWidth() * widthToCoverToTop);
        final float newYTeamTwo = redClimberInity - (float) ((teamTwoScore / endScore) * backgroudImage.getWidth() * imageRatio * heightToCoverToTop);
        getActivity().runOnUiThread(() -> {
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
            if (team == 1) {
                String score = String.valueOf(teamOneScore.intValue());
                String prefix = blueScore.getText().toString().split(":")[0];
                blueScore.setText(prefix + ": " + score);
            } else {
                blueScore.setText(String.valueOf(teamOneScore.intValue()));
            }
            if (team == 2) {
                String score = String.valueOf(teamTwoScore.intValue());
                String prefix = redScore.getText().toString().split(":")[0];
                redScore.setText(prefix + ": " + score);
            } else {
                redScore.setText(String.valueOf(teamTwoScore.intValue()));
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_game, container, false);

        Koeko.networkCommunicationSingleton.mInteractiveModeActivity.forwardButton.setTitle("");

        Bundle bun = getArguments();
        endScore = bun.getInt("endScore");
        gameType = bun.getInt("gameType");

        backgroundView = rootView.findViewById(R.id.background_landscape_layout);
        backgroudImage = rootView.findViewById(R.id.background_landscape);

        // do we have a camera?
        if (!getActivity().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(getActivity(), "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(getActivity(), "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
            }
        }

        scanQQButton = rootView.findViewById(R.id.scan_qr_button_game);
        if (gameType == GameType.qrCodeGame) {
            scanQQButton.setOnClickListener(e -> {
                // Check if we have write permission
                int permission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(
                            getActivity(),
                            new String[]{Manifest.permission.CAMERA},
                            1
                    );
                } else {
                    Koeko.networkCommunicationSingleton.mInteractiveModeActivity.launchQRscanning();
                }
            });
        } else {
            scanQQButton.setVisibility(View.GONE);
        }

        readyButton = rootView.findViewById(R.id.ready_button);
        final Activity currentActivity = getActivity();
        if (gameType == GameType.orderedAutomaticSending || gameType == GameType.randomAutomaticSending) {
            readyButton.setOnClickListener(e -> {
                ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.readyPrefix);
                transferable.setOptionalArgument1(DbTableSettings.getUUID());
                Koeko.networkCommunicationSingleton.sendBytesToServer(transferable.getTransferableBytes());
                currentActivity.runOnUiThread(() -> {
                    readyButton.setBackgroundColor(getResources().getColor(R.color.gamegreen));
                });
                Log.d(TAG, "change readybutton UI");
            });
        } else {
            readyButton.setVisibility(View.GONE);
        }

        redCelebration = rootView.findViewById(R.id.red_celebration);
        blueCelebration = rootView.findViewById(R.id.blue_celebration);

        final Context context = getActivity();
        backgroudImage.post(() -> {
            //position climbers
            blueClimber = new ImageView(context);
            blueClimber.setImageResource(R.drawable.blueclimber);
            backgroundView.addView(blueClimber);
            blueClimberInitx = backgroudImage.getWidth() * 0.22f;
            blueClimberInity = backgroudImage.getHeight() * 0.5f;
            if (Koeko.gameState == null) {
                blueClimber.setX(blueClimberInitx);
                blueClimber.setY(blueClimberInity);
            } else {
                final float newXTeamone = blueClimberInitx + (float) ((Koeko.gameState.getScoreTeamOne() / endScore) * backgroudImage.getWidth() * widthToCoverToTop);
                final float newYTeamOne = blueClimberInity - (float) ((Koeko.gameState.getScoreTeamOne() / endScore) * backgroudImage.getWidth() * imageRatio * heightToCoverToTop);
                blueClimber.setX(newXTeamone);
                blueClimber.setY(newYTeamOne);
            }
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
            if (Koeko.gameState == null) {
                redClimber.setX(redClimberInitx);
                redClimber.setY(redClimberInity);
            } else {
                final float newXTeamTwo = redClimberInitx - (float) ((Koeko.gameState.getScoreTeamTwo() / endScore) * backgroudImage.getWidth() * widthToCoverToTop);
                final float newYTeamTwo = redClimberInity - (float) ((Koeko.gameState.getScoreTeamTwo() / endScore) * backgroudImage.getWidth() * imageRatio * heightToCoverToTop);
                redClimber.setX(newXTeamTwo);
                redClimber.setY(newYTeamTwo);
            }
            redClimber.getLayoutParams().width = imageWidth;
            redClimber.getLayoutParams().height = imageHeight;
            redClimber.requestLayout();

            //initialize scores
            team = bun.getInt("team");
            Log.d(TAG, "onCreate: team=" + team);
            blueScore = rootView.findViewById(R.id.blue_score);
            String initialScoreOne = "0";
            String initialScoreTwo = "0";
            if (Koeko.gameState != null) {
                team = Koeko.gameState.getGameView().getTeam();
                initialScoreOne = String.valueOf(Koeko.gameState.getScoreTeamOne());
                initialScoreTwo = String.valueOf(Koeko.gameState.getScoreTeamTwo());
            }
            if (team == 1) {
                blueScore.setText(getString(R.string.me) + ": " + initialScoreOne);
            } else {
                blueScore.setText(initialScoreOne);
            }
            redScore = rootView.findViewById(R.id.red_score);
            if (team == 2) {
                redScore.setText(getString(R.string.me) + "Me: " + initialScoreTwo);
            } else {
                redScore.setText(initialScoreTwo);
            }

            if (Koeko.gameState == null) {
                GameView gameView = new GameView();
                gameView.setEndScore(endScore);
                gameView.setGameType(gameType);
                gameView.setTeam(team);
                Koeko.gameState = new GameState();
                Koeko.gameState.setGameView(gameView);
            }
        });
        Koeko.currentGameFragment = this;
        return rootView;
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

    /*@Override
    protected void onResume() {
        super.onResume();

        if (forwardButton != null) {
            if (Koeko.qmcActivityState != null || Koeko.shrtaqActivityState != null) {
                forwardButton.setTitle(getString(R.string.back_to_question) + " >");
            } else if (Koeko.currentTestFragmentSingleton != null) {
                forwardButton.setTitle(getString(R.string.back_to_test) + " >");
            }
        }

        if (!Koeko.qrCode.contentEquals("")) {
            sendQuestionRequest();
            Koeko.qrCode = "";
        }

        Koeko.MAX_ACTIVITY_TRANSITION_TIME_MS = Koeko.SHORT_TRANSITION_TIME;

        readyButton.setBackgroundColor(getResources().getColor(R.color.koekored));
    }*/

    private void sendQuestionRequest() {
        String[] codeArray = Koeko.qrCode.split(":");
        if (codeArray.length >= 3) {
            String resCodeString = codeArray[0];
            ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.gamesetPrefix);
            transferable.setOptionalArgument1(NetworkCommunication.deviceIdentifier);
            transferable.setOptionalArgument2(resCodeString);
            Koeko.networkCommunicationSingleton.sendDataToClient(transferable.getTransferableBytes());
        } else {
            Log.w(TAG, "Array from QR code string is too short");
        }
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
            } else if (Koeko.currentTestFragmentSingleton != null) {
                Koeko.networkCommunicationSingleton.launchTestActivity(Koeko.currentTestFragmentSingleton.getmTest().getIdGlobal(),
                        Koeko.networkCommunicationSingleton.directCorrection);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}