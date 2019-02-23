package com.wideworld.koeko.Activities;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.wideworld.koeko.Activities.ActivityTools.CustomAlertDialog;
import com.wideworld.koeko.Activities.ActivityTools.TestChronometer;
import com.wideworld.koeko.Activities.ActivityTools.TestListAdapter;
import com.wideworld.koeko.Activities.ActivityTools.RecyclerTouchListener;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.ClientToServerTransferable;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.CtoSPrefix;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.States.QuestionMultipleChoiceState;
import com.wideworld.koeko.QuestionsManagement.States.QuestionShortAnswerState;
import com.wideworld.koeko.QuestionsManagement.Test;
import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableIndividualQuestionForResult;
import com.wideworld.koeko.database_management.DbTableTest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class TestFragment extends Fragment {
    public Map<String, QuestionMultipleChoiceState> mcqActivitiesStates;
    public Map<String, QuestionShortAnswerState> shrtaqActivitiesStates;
    public TestChronometer testChronometer;
    public Boolean testIsFinished = false;
    private Boolean reloadActivity = false;

    private RecyclerView mRecyclerView;

    public RecyclerView.Adapter getmAdapter() {
        return mAdapter;
    }

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private VideoView videoView;
    private ImageButton play_pauseButton;
    private ImageButton stopButton;
    private FrameLayout videoFrame;
    private LinearLayout playerButtons;
    private Uri videoUri;
    private MediaPlayer mediaPlayer;
    private Test mTest;

    private String TAG = "TestFragment";

    public Test getmTest() {
        return mTest;
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_test, container, false);
        mRecyclerView = rootView.findViewById(R.id.test_recycler_view);

        Koeko.networkCommunicationSingleton.mInteractiveModeActivity.forwardButton.setTitle("");

        //initialize static variables
        mcqActivitiesStates = new LinkedHashMap<>();
        shrtaqActivitiesStates = new LinkedHashMap<>();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //load the test
        Bundle bun = getArguments();
        Long testID = bun.getLong("testID");

        if (Koeko.currentTestFragmentSingleton == null) {
            mTest = DbTableTest.getTestFromTestId(String.valueOf(testID));
            if (mTest != null) {
                mTest.setIdGlobal(testID);
                mTest.setTestName(DbTableTest.getNameFromTestID(testID));
                mTest.setQuestionsIDs(DbTableTest.getQuestionIDsFromTestName(mTest.getTestName()));
                mTest.setMediaFileName(DbTableTest.getMediaFileFromTestName(mTest.getTestName()));
                mTest.loadMap();
                reloadActivity = false;
            } else {
                Log.w(TAG, "Received test id but can't find corresponding test in db!");
                dismiss();
                return rootView;
            }
        } else {
            mTest = Koeko.currentTestFragmentSingleton.mTest;
            mcqActivitiesStates = Koeko.currentTestFragmentSingleton.mcqActivitiesStates;
            shrtaqActivitiesStates = Koeko.currentTestFragmentSingleton.shrtaqActivitiesStates;
            reloadActivity = true;
        }

        mAdapter = new TestListAdapter(mTest);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (mTest.getActiveQuestionIds().contains(mTest.getQuestionsIDs().get(position))) {
                    QuestionMultipleChoice questionMultipleChoice = mTest.getIdMapQmc().get(mTest.getQuestionsIDs().get(position));
                    if (questionMultipleChoice == null) {
                        QuestionShortAnswer questionShortAnswer = mTest.getIdMapShrtaq().get(mTest.getQuestionsIDs().get(position));
                        Koeko.networkCommunicationSingleton.launchShortAnswerQuestionActivity(questionShortAnswer,
                                Koeko.networkCommunicationSingleton.directCorrection);
                    } else {
                        Koeko.networkCommunicationSingleton.launchMultChoiceQuestionActivity(questionMultipleChoice,
                                Koeko.networkCommunicationSingleton.directCorrection);
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        if (mTest.getMedalsInstructionsString().length() > 0
                && !mTest.getMedalsInstructionsString().contentEquals("null")
                && Koeko.currentTestFragmentSingleton == null) {
            Vector<Vector<String>> instruc = mTest.getMedalsInstructions();
            if (instruc.size() >=3) {
                String message = "Gold medal\nTime: " + (instruc.get(2).get(0) != "1000000" ? instruc.get(2).get(0) : "no time limit;") + " \nScore: " + instruc.get(2).get(1) + "\n\n";
                message += "Silver medal\nTime: " + (instruc.get(1).get(0) != "1000000" ? instruc.get(1).get(0) : "no time limit;") + " \nScore: " + instruc.get(1).get(1) + "\n\n";
                message += "Bronze medal\nTime: " + (instruc.get(0).get(0) != "1000000" ? instruc.get(0).get(0) : "no time limit;") + " \nScore: " + instruc.get(0).get(1) + "\n\n";
                CustomAlertDialog customAlertDialog = new CustomAlertDialog(getContext());
                customAlertDialog.setTestInstructions(true);
                customAlertDialog.show();
                customAlertDialog.setProperties(message, getActivity());
            }
        }

        //setup media player
        videoView = rootView.findViewById(R.id.videoView);
        videoFrame = rootView.findViewById(R.id.video_frame);
        playerButtons = rootView.findViewById(R.id.buttonsLinearLayout);
        play_pauseButton = rootView.findViewById(R.id.play_pause);
        stopButton = rootView.findViewById(R.id.reset);
        if (mTest.getMediaFileType() == 1) {
            playerButtons.setVisibility(View.VISIBLE);
        } else if (mTest.getMediaFileType() == 2) {
            videoFrame.setVisibility(View.VISIBLE);
            playerButtons.setVisibility(View.VISIBLE);
            String pathtohere = getActivity().getFilesDir().getAbsolutePath().toString();
            videoUri = Uri.parse(pathtohere + "/media/" + mTest.getMediaFileName());
            videoView.setVideoURI(videoUri);
        } else if (mTest.getMediaFileType() == 3) {
            playerButtons.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.GONE);
        }

        //send receipt to server
        ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.activeIdPrefix);
        transferable.setOptionalArgument1(String.valueOf(testID));
        Koeko.networkCommunicationSingleton.sendBytesToServer(transferable.getTransferableBytes());

        if (mTest.getAnsweredQuestionIds().size() == mTest.getActiveQuestionIds().size()) {
            testIsFinished = true;
        }
        Koeko.currentTestFragmentSingleton = this;

        return rootView;
    }
    /*@Override
    public void onResume() {
        Log.d(TAG, "onResume");
        if (mTest.getMediaFileType() == 1) {
            if (mediaPlayer == null) {
                String pathtohere = getActivity().getFilesDir().getAbsolutePath();
                mediaPlayer = MediaPlayer.create(getContext(), Uri.parse(pathtohere + "/media/" + mTest.getMediaFileName()));
            }
        }
        play_pauseButton.setImageResource(R.drawable.play_icon);
        super.onResume();
    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mTest.getMedalsInstructions() != null ) {
            if (mTest.getMedalsInstructionsString().length() > 0) {
                inflater.inflate(R.menu.menu_test, menu);

                testChronometer = (TestChronometer) menu
                        .findItem(R.id.chronometer)
                        .getActionView();

                testChronometer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                testChronometer.setTextColor(Color.WHITE);
                if (reloadActivity) {
                    testChronometer.setStartTime(Koeko.activeTestStartTime);
                    testChronometer.run();
                }
            }
        } else {
            Log.w(TAG, "Medals Instructions are null!!");
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public Boolean checkIfTestFinished() {
        return mTest.getAnsweredQuestionIds().keySet().containsAll(mTest.getActiveQuestionIds()) &&
                mTest.getActiveQuestionIds().containsAll(mTest.getAnsweredQuestionIds().keySet());
    }

    public void finalizeTest() {
        if (testIsFinished) {
            mTest.getQuestionsIDs().add("0");
        }
        if (testIsFinished && testChronometer != null) {
            testChronometer.stop();
            Long testDuration = testChronometer.getOverallDuration();

            //calculate quantitative evaluation
            Double quantEval = 0.0;
            for (Double singleEval : mTest.getAnsweredQuestionIds().values()) {
                quantEval += singleEval;
            }
            quantEval = quantEval / mTest.getAnsweredQuestionIds().size();

            //check if medal won
            String medal = "none";
            String message = "You are a Champ!";
            try {
                if (mTest.getMedalsInstructions().size() >= 3) {
                    if (Long.valueOf(mTest.getMedalsInstructions().get(2).get(0)) >= testDuration &&
                            Double.valueOf(mTest.getMedalsInstructions().get(2).get(1)) <= quantEval) {
                        message += "\nYou won the GOLD MEDAL";
                        medal = "gold-medal";
                    } else if (Long.valueOf(mTest.getMedalsInstructions().get(1).get(0)) >= testDuration &&
                            Double.valueOf(mTest.getMedalsInstructions().get(1).get(1)) <= quantEval) {
                        medal = "silver-medal";
                        message += "\nYou won the SILVER MEDAL";
                    } else if (Long.valueOf(mTest.getMedalsInstructions().get(0).get(0)) >= testDuration &&
                            Double.valueOf(mTest.getMedalsInstructions().get(0).get(1)) <= quantEval) {
                        medal = "bronze-medal";
                        message += "\nYou won the BRONZE MEDAL";
                    }
                } else {
                    Log.v(TAG, "No medal for test");
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "NumberFormatException in medals instructions when checking if medal won.");
            }
            if (!medal.contentEquals("none")) {
                CustomAlertDialog customAlertDialog = new CustomAlertDialog(getActivity(), message, medal);
                customAlertDialog.show();
            }
            DbTableIndividualQuestionForResult.addIndividualTestForStudentResult(String.valueOf(mTest.getIdGlobal()),
                    mTest.getTestName(), String.valueOf(testDuration), "FORMATIVE",
                    quantEval, medal);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void PlayPause(View v) {
        if (mTest.getMediaFileType() == 2) {
            if (videoView.isPlaying()) {
                videoView.pause();
                play_pauseButton.setImageResource(R.drawable.play_icon);
            } else {
                videoView.start();
                play_pauseButton.setImageResource(R.drawable.pause_icon);
            }
        } else if (mTest.getMediaFileType() == 1) {
            if (mediaPlayer == null) {
                String pathtohere = getActivity().getFilesDir().getAbsolutePath() ;
                mediaPlayer = MediaPlayer.create(getContext(), Uri.parse(pathtohere + "/media/" + mTest.getMediaFileName()));
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                play_pauseButton.setImageResource(R.drawable.play_icon);
            } else {
                mediaPlayer.start();
                play_pauseButton.setImageResource(R.drawable.pause_icon);
            }
        } else if (mTest.getMediaFileType() == 3) {
            //Intent intent = new Intent(TestFragment.this, WebViewActivity.class);
            //startActivity(intent);
        }
    }

    public void Stop(View v) {
        if (mTest.getMediaFileType() == 2) {
            videoView.stopPlayback();
            videoView.setVideoURI(videoUri);
        } else if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer = null;
        }
        play_pauseButton.setImageResource(R.drawable.play_icon);
    }

    private void dismiss() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Koeko.networkCommunicationSingleton.mInteractiveModeActivity.getSupportFragmentManager().popBackStack();
    }
}