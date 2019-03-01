package com.wideworld.koeko.Activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TextView;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.ClientToServerTransferable;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.CtoSPrefix;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.States.QuestionMultipleChoiceState;
import com.wideworld.koeko.R;
import com.wideworld.koeko.Tools.FileHandler;

public class MultChoiceQuestionFragment extends Fragment {
    private Boolean wasAnswered = false;
    private int number_of_possible_answers = 0;
    private QuestionMultipleChoice currentQ;
    private TextView txtQuestion;
    private Button submitButton;
    private ArrayList<CheckBox> checkBoxesArray;
    private ImageView picture;
    private LinearLayout linearLayout;
    private Activity mActivity;
    private Long startingTime = 0L;

    private String TAG = "MultChoiceQuestionFragment";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_multchoicequestion, container, false);
        mActivity = getActivity();

        Koeko.networkCommunicationSingleton.mInteractiveModeActivity.forwardButton.setTitle("");


        linearLayout = rootView.findViewById(R.id.linearLayoutMultChoice);
        txtQuestion = rootView.findViewById(R.id.textViewMultChoiceQuest1);
        picture = new ImageView(mActivity);
        submitButton = rootView.findViewById(R.id.submit_button_mcq);
        checkBoxesArray = new ArrayList<>();
        TextView timerView = rootView.findViewById(R.id.timerViewMcq);


        //get bluetooth client object
//		final BluetoothClientActivity bluetooth = (BluetoothClientActivity)getIntent().getParcelableExtra("bluetoothObject");

        //get question from the bundle
        Bundle bun = getArguments();
        final String question = bun.getString("question");
        String opt0 = bun.getString("opt0");        //should also be the answer
        String opt1 = bun.getString("opt1");
        String opt2 = bun.getString("opt2");
        String opt3 = bun.getString("opt3");
        String opt4 = bun.getString("opt4");
        String opt5 = bun.getString("opt5");
        String opt6 = bun.getString("opt6");
        String opt7 = bun.getString("opt7");
        String opt8 = bun.getString("opt8");
        String opt9 = bun.getString("opt9");
        String id = bun.getString("id");
        String image_path = bun.getString("image_name");
        Integer timerSeconds = bun.getInt("timerSeconds");
        currentQ = new QuestionMultipleChoice("1", question, opt0, opt1, opt2, opt3, opt4, opt5, opt6, opt7, opt8, opt9, image_path);
        currentQ.setId(id);
        currentQ.setNB_CORRECT_ANS(bun.getInt("nbCorrectAnswers"));
        currentQ.setTimerSeconds(timerSeconds);

        setQuestionView();

        //check if no error has occured
        if (question == "the question couldn't be read") {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            dismiss();
        }

        //send receipt to server
        ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.activeIdPrefix);
        transferable.setOptionalArgument1(currentQ.getId());
        if (Koeko.networkCommunicationSingleton != null) {
            Koeko.networkCommunicationSingleton.sendBytesToServer(transferable.getTransferableBytes());
        } else {
            Log.d(TAG, "onCreate: sending receipt to null networkCommunicationSingleton");
        }

        submitButton.setOnClickListener(v -> {
            if (!wasAnswered) {
                String answer = "";
                ArrayList<String> answers = new ArrayList<>();
                for (int i = 0; i < number_of_possible_answers; i++) {
                    if (checkBoxesArray.get(i).isChecked()) {
                        answer += checkBoxesArray.get(i).getText() + "|||";
                        answers.add(checkBoxesArray.get(i).getText().toString());
                    }
                }

                wasAnswered = true;

                Koeko.networkCommunicationSingleton.sendAnswerToServer(answers, answer, question, currentQ.getId(), "ANSW0",
                        (SystemClock.elapsedRealtime() - startingTime) / 1000);

                if (Koeko.networkCommunicationSingleton.directCorrection.contentEquals("1")) {
                    MltChoiceQuestionButtonClick();
                } else {
                    dismiss();
                }
            } else {
                dismiss();
            }
        });

        if (timerSeconds > 0 && submitButton.isEnabled()) {
            timerView.setVisibility(View.VISIBLE);
            if (startingTime == 0L) {
                startingTime = SystemClock.elapsedRealtime();
            }
            String remainingTime = String.valueOf(timerSeconds - (SystemClock.elapsedRealtime() - startingTime) / 1000);
            timerView.setText(remainingTime);
            new Thread(() -> {
                while ((timerSeconds - (SystemClock.elapsedRealtime() - startingTime) / 1000) > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mActivity.runOnUiThread(() -> timerView.setText(String.valueOf((timerSeconds -
                            (SystemClock.elapsedRealtime() - startingTime) / 1000))));
                }
                mActivity.runOnUiThread(() -> disactivateQuestion());
            }).start();
        }


        /**
         * START CODE USED FOR TESTING
         */
        if (question.contains("*ç%&")) {
            ArrayList<String> answer = new ArrayList<>();
            answer.add(String.valueOf(opt0));
            Koeko.networkCommunicationSingleton.sendAnswerToServer(answer, String.valueOf(opt0), question, currentQ.getId(), "ANSW0", 4239L);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mActivity.runOnUiThread(() -> dismiss());
                    //invalidateOptionsMenu();
                }
            };
            thread.start();

        }
        /**
         * END CODE USED FOR TESTING
         */

        return rootView;
    }

    private void setQuestionView() {
        txtQuestion.setText(currentQ.getQuestion());

        if (currentQ.getImage().contains(":") && currentQ.getImage().length() > currentQ.getImage().indexOf(":") + 1) {
            currentQ.setImage(currentQ.getImage().substring(currentQ.getImage().indexOf(":") + 1));
        }
        File imgFile = new File(mActivity.getFilesDir() + "/"+ FileHandler.mediaDirectory + currentQ.getImage());
        if (imgFile.exists()) {
            String path = imgFile.getAbsolutePath();
            Bitmap myBitmap = BitmapFactory.decodeFile(path);
            picture.setImageBitmap(myBitmap);
            picture.setAdjustViewBounds(true);
            picture.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(picture);
        }

        String[] answerOptions;
        answerOptions = new String[10];
        answerOptions[0] = currentQ.getOpt0();
        answerOptions[1] = currentQ.getOpt1();
        answerOptions[2] = currentQ.getOpt2();
        answerOptions[3] = currentQ.getOpt3();
        answerOptions[4] = currentQ.getOpt4();
        answerOptions[5] = currentQ.getOpt5();
        answerOptions[6] = currentQ.getOpt6();
        answerOptions[7] = currentQ.getOpt7();
        answerOptions[8] = currentQ.getOpt8();
        answerOptions[9] = currentQ.getOpt9();

        for (int i = 0; i < 10; i++) {
            if (!answerOptions[i].equals(" ")) {
                number_of_possible_answers++;
            }
        }

        //implementing Fisher-Yates shuffle
        Random rnd = new Random();
        for (int i = number_of_possible_answers - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            String a = answerOptions[index];
            answerOptions[index] = answerOptions[i];
            answerOptions[i] = a;
        }

        CheckBox tempCheckBox = null;

        for (int i = 0; i < number_of_possible_answers; i++) {
            tempCheckBox = new CheckBox(mActivity);
            tempCheckBox.setText(answerOptions[i]);
            tempCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            tempCheckBox.setTextColor(Color.BLACK);
            checkBoxesArray.add(tempCheckBox);
            if (checkBoxesArray.get(i).getParent() != null)
                ((ViewGroup) checkBoxesArray.get(i).getParent()).removeView(checkBoxesArray.get(i));

            TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT, 10f);
            params.setMargins(0,10,0,0);
            checkBoxesArray.get(i).setLayoutParams(params);

            linearLayout.addView(checkBoxesArray.get(i));
        }
        submitButton.setText(getString(R.string.answer_button));
        submitButton.setBackgroundColor(Color.parseColor("#00CCCB"));
        submitButton.post(() -> {
            //add empty view to fix the button hiding part of the scrollview
            TextView emptyView = new TextView(mActivity);
            submitButton.refreshDrawableState();
            emptyView.setHeight(submitButton.getHeight() * 4 / 3);
            linearLayout.addView(emptyView);
        });

        //restore activity state
        QuestionMultipleChoiceState activityState = null;
        if (Koeko.currentTestFragmentSingleton != null) {
            activityState = Koeko.currentTestFragmentSingleton.mcqActivitiesStates.get(String.valueOf(currentQ.getId()));
        } else {
            if (Koeko.qmcActivityState != null) {
                activityState = Koeko.qmcActivityState;
            }
        }
        if ((activityState != null && Koeko.currentTestFragmentSingleton != null) || (activityState != null &&
                Koeko.currentQuestionMultipleChoiceSingleton != null &&
                Koeko.currentQuestionMultipleChoiceSingleton.getId().contentEquals(currentQ.getId()))) {

            //restore checkboxes
            for (int i = 0; i < checkBoxesArray.size() && i < activityState.getCheckboxes().size(); i++) {
                checkBoxesArray.get(i).setChecked(activityState.getCheckboxes().get(i).isChecked());
                checkBoxesArray.get(i).setText(activityState.getCheckboxes().get(i).getText());
            }

            if (activityState.getWasAnswered()) {
                disactivateQuestion();
                wasAnswered = activityState.getWasAnswered();
            }

            //restore timer
            try {
                startingTime = activityState.getTimeRemaining();
                Long elapsedTime = SystemClock.elapsedRealtime();
                Long effectiveElapsedTime = elapsedTime - startingTime;
                if (currentQ.getTimerSeconds() != -1 && (currentQ.getTimerSeconds() - effectiveElapsedTime / 1000) < 0) {
                    disactivateQuestion();
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        //finished restoring activity
    }

    protected void saveActivityState() {
        QuestionMultipleChoiceState activityState = new QuestionMultipleChoiceState();
        activityState.setCheckboxes(checkBoxesArray);
        activityState.setWasAnswered(wasAnswered);
        activityState.setTimeRemaining(startingTime);
        if (Koeko.currentTestFragmentSingleton != null) {
            Koeko.currentTestFragmentSingleton.mcqActivitiesStates.put(String.valueOf(currentQ.getId()), activityState);
        } else {
            Koeko.qmcActivityState = activityState;
            Koeko.currentQuestionMultipleChoiceSingleton = currentQ;
        }
    }

    private void MltChoiceQuestionButtonClick() {
        //get the answers checked by student
        //get the right answers
        ArrayList<String> rightAnswers = new ArrayList<>();
        for (int i = 0; i < currentQ.getNB_CORRECT_ANS(); i++) {
            rightAnswers.add(currentQ.getPossibleAnswers().get(i));
        }
        for (int i = 0; i < checkBoxesArray.size(); i++) {
            if (checkBoxesArray.get(i).isChecked()) {
                if (rightAnswers.contains(checkBoxesArray.get(i).getText().toString())) {
                    String correction = " <font size=\"5\" color=\"green\">Right :-)</font> <br/>" +
                            checkBoxesArray.get(i).getText().toString();
                    checkBoxesArray.get(i).setText(Html.fromHtml(correction));
                } else {
                    String correction = " <font size=\"5\" color=\"red\">This shouldn't be selected :-(</font> <br/>" +
                            checkBoxesArray.get(i).getText().toString();
                    checkBoxesArray.get(i).setText(Html.fromHtml(correction));
                }
            } else {
                if (rightAnswers.contains(checkBoxesArray.get(i).getText().toString())) {
                    String correction = " <font size=\"5\" color=\"red\">This should be selected :-(</font> <br/>" +
                            checkBoxesArray.get(i).getText().toString();
                    checkBoxesArray.get(i).setText(Html.fromHtml(correction));
                } else {
                    String correction = " <font size=\"5\" color=\"green\">Right :-)</font> <br/>" +
                            checkBoxesArray.get(i).getText().toString();
                    checkBoxesArray.get(i).setText(Html.fromHtml(correction));
                }
            }
        }
        disactivateQuestion();
    }

    private void disactivateQuestion() {
        //submitButton.setEnabled(false);
        //submitButton.setAlpha(0.3f);
        for (CheckBox checkBox : checkBoxesArray) {
            checkBox.setEnabled(false);
            //checkBox.setAlpha(0.3f);
        }
        submitButton.setText("OK");
        wasAnswered = true;
    }

    private void dismiss() {
        saveActivityState();
        Koeko.networkCommunicationSingleton.mInteractiveModeActivity.getSupportFragmentManager().popBackStack();
        if (Koeko.currentTestFragmentSingleton != null) {
            InteractiveModeActivity.backToTestFromQuestion = true;
        }
        Koeko.networkCommunicationSingleton.mInteractiveModeActivity.setForwardButton(InteractiveModeActivity.forwardQuestionMultipleChoice);
    }
}