package com.wideworld.koeko.Activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.wideworld.koeko.Activities.ActivityTools.CustomAlertDialog;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.NetworkCommunication.NetworkCommunication;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.R;

public class MultChoiceQuestionActivity extends Activity {
    private Boolean wasAnswered = false;
    private int number_of_possible_answers = 0;
    private QuestionMultipleChoice currentQ;
    private TextView txtQuestion;
    private Button submitButton;
    private ArrayList<CheckBox> checkBoxesArray;
    private ImageView picture;
    private boolean isImageFitToScreen = true;
    private LinearLayout linearLayout;
    private Context mContext;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_multchoicequestion);
        mContext = this;

        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutMultChoice);
        txtQuestion = (TextView) findViewById(R.id.textViewMultChoiceQuest1);
        picture = new ImageView(getApplicationContext());
        submitButton = new Button(getApplicationContext());
        checkBoxesArray = new ArrayList<>();


        //get bluetooth client object
//		final BluetoothClientActivity bluetooth = (BluetoothClientActivity)getIntent().getParcelableExtra("bluetoothObject");

        //get question from the bundle
        Bundle bun = getIntent().getExtras();
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
        currentQ = new QuestionMultipleChoice("1", question, opt0, opt1, opt2, opt3, opt4, opt5, opt6, opt7, opt8, opt9, image_path);
        currentQ.setID(id);
        currentQ.setNB_CORRECT_ANS(bun.getInt("nbCorrectAnswers"));
        if (currentQ.getIMAGE().length() > 0) {
            picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isImageFitToScreen) {
                        isImageFitToScreen = false;
                        picture.setAdjustViewBounds(true);
                        picture.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        picture.setAdjustViewBounds(true);
                    } else {
                        isImageFitToScreen = true;
                        picture.setAdjustViewBounds(true);
                        picture.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200));
                    }
                }
            });
        }


        setQuestionView();

        //check if no error has occured
        if (question == "the question couldn't be read") {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finish();
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onClick(View v) {
                String answer = "";
                for (int i = 0; i < number_of_possible_answers; i++) {
                    if (checkBoxesArray.get(i).isChecked()) {
                        answer += checkBoxesArray.get(i).getText() + "|||";
                    }
                }

                wasAnswered = true;
                saveActivityState();

                NetworkCommunication networkCommunication = ((Koeko) getApplication()).getAppNetwork();
                networkCommunication.sendAnswerToServer(String.valueOf(answer), question, currentQ.getID(), "ANSW0");

                if (Koeko.wifiCommunicationSingleton.directCorrection.contentEquals("1")) {
                    MltChoiceQuestionButtonClick();
                } else {
                    finish();
                    invalidateOptionsMenu();
                }
            }
        });

        /**
         * START CODE USED FOR TESTING
         */
        if (question.contains("*ç%&")) {
            NetworkCommunication networkCommunication = ((Koeko) getApplication()).getAppNetwork();
            networkCommunication.sendAnswerToServer(String.valueOf(opt0), question, currentQ.getID(), "ANSW0");
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    finish();
                    invalidateOptionsMenu();
                }
            };
            thread.start();

        }
        /**
         * END CODE USED FOR TESTING
         */
    }

    private void setQuestionView() {
        txtQuestion.setText(currentQ.getQUESTION());

        if (currentQ.getIMAGE().contains(":") && currentQ.getIMAGE().length() > currentQ.getIMAGE().indexOf(":") + 1) {
            currentQ.setIMAGE(currentQ.getIMAGE().substring(currentQ.getIMAGE().indexOf(":") + 1));
        }
        File imgFile = new File(getFilesDir() + "/images/" + currentQ.getIMAGE());
        if (imgFile.exists()) {
            String path = imgFile.getAbsolutePath();
            Bitmap myBitmap = BitmapFactory.decodeFile(path);
            picture.setImageBitmap(myBitmap);
        }
        picture.setAdjustViewBounds(true);
        picture.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200));
        linearLayout.addView(picture);

//		int imageResource = getResources().getIdentifier(currentQ.getIMAGE(), null, getPackageName());
//		picture.setImageResource(imageResource);


        String[] answerOptions;
        answerOptions = new String[10];
        answerOptions[0] = currentQ.getOPT0();
        answerOptions[1] = currentQ.getOPT1();
        answerOptions[2] = currentQ.getOPT2();
        answerOptions[3] = currentQ.getOPT3();
        answerOptions[4] = currentQ.getOPT4();
        answerOptions[5] = currentQ.getOPT5();
        answerOptions[6] = currentQ.getOPT6();
        answerOptions[7] = currentQ.getOPT7();
        answerOptions[8] = currentQ.getOPT8();
        answerOptions[9] = currentQ.getOPT9();

        for (int i = 0; i < 10; i++) {
            if (!answerOptions[i].equals(" ")) {
                number_of_possible_answers++;
            }
        }

        //implementing Fisher-Yates shuffle
        Random rnd = new Random();
        for (int i = number_of_possible_answers - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            String a = answerOptions[index];
            answerOptions[index] = answerOptions[i];
            answerOptions[i] = a;
        }

        CheckBox tempCheckBox = null;

        for (int i = 0; i < number_of_possible_answers; i++) {
            tempCheckBox = new CheckBox(getApplicationContext());
            tempCheckBox.setText(answerOptions[i]);
            tempCheckBox.setTextColor(Color.BLACK);
            checkBoxesArray.add(tempCheckBox);
            if (checkBoxesArray.get(i).getParent() != null)
                ((ViewGroup) checkBoxesArray.get(i).getParent()).removeView(checkBoxesArray.get(i));

            checkBoxesArray.get(i).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 10f));

            linearLayout.addView(checkBoxesArray.get(i));
        }
        submitButton.setText(getString(R.string.answer_button));
        submitButton.setBackgroundColor(Color.parseColor("#00CCCB"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int height = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        params.setMargins(width / 40, height / 200, width / 40, height / 200);  //left, top, right, bottom
        submitButton.setLayoutParams(params);
        submitButton.setTextColor(Color.WHITE);
        linearLayout.addView(submitButton);

        //restore activity state
        String activityState = null;
        if (Koeko.currentTestActivitySingleton != null) {
            activityState = Koeko.currentTestActivitySingleton.mcqActivitiesStates.get(String.valueOf(currentQ.getID()));
        } else {
            if (Koeko.qmcActivityState != null) {
                activityState = Koeko.qmcActivityState;
            }
        }
        if ((activityState != null && Koeko.currentTestActivitySingleton != null) || (activityState != null &&
                Koeko.currentQuestionMultipleChoiceSingleton != null &&
                Koeko.currentQuestionMultipleChoiceSingleton.getID().contentEquals(currentQ.getID()))) {
            String[] parsedState = activityState.split("///");
            if (parsedState[parsedState.length - 1].contentEquals("true")) {
                submitButton.setEnabled(false);
                submitButton.setAlpha(0.3f);
                wasAnswered = true;
            }

            for (CheckBox checkBox : checkBoxesArray) {
                for (int i = 0; i < parsedState.length; i++) {
                    if (parsedState[i].contentEquals(checkBox.getText())) {
                        checkBox.setChecked(true);
                        break;
                    }
                }
            }
        }
        //finished restoring activity
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveActivityState();
    }

    private void saveActivityState() {
        String activityState = "";
        for (CheckBox checkbox : checkBoxesArray) {
            if (checkbox.isChecked()) {
                activityState += checkbox.getText() + "///";
            }
        }
        activityState += wasAnswered;
        if (Koeko.currentTestActivitySingleton != null) {
            Koeko.currentTestActivitySingleton.mcqActivitiesStates.put(String.valueOf(currentQ.getID()), activityState);
        } else {
            Koeko.qmcActivityState = activityState;
            Koeko.currentQuestionMultipleChoiceSingleton = currentQ;
        }
    }

    private void MltChoiceQuestionButtonClick() {
        //get the answers checked by student
        ArrayList<String> studentAnswers = new ArrayList<String>();
        for (int i = 0; i < checkBoxesArray.size(); i++) {
            if (checkBoxesArray.get(i).isChecked()) {
                studentAnswers.add(checkBoxesArray.get(i).getText().toString());
            }
        }
        //get the right answers
        ArrayList<String> rightAnswers = new ArrayList<String>();
        for (int i = 0; i < currentQ.getNB_CORRECT_ANS(); i++) {
            rightAnswers.add(currentQ.getPossibleAnswers().get(i));
        }
        //compare the student answers with the right answers
        String title = "";
        String message = "";
        if (rightAnswers.containsAll(studentAnswers) && studentAnswers.containsAll(rightAnswers)) {
            title = "Good job!";
            message = getString(R.string.correction_correct);

        } else {
            String correct_answers = "";
            for (int i = 0; i < rightAnswers.size(); i++) {
                correct_answers += (rightAnswers.get(i));
                if (!(i == rightAnswers.size() - 1)) correct_answers += " or ";
            }
            title = ":-(";
            message = getString(R.string.correction_incorrect) + correct_answers;
        }

        CustomAlertDialog customAlertDialog = new CustomAlertDialog(this);
        customAlertDialog.show();
        customAlertDialog.setProperties(message, this);
    }

    /**
     * method used to know if we send a disconnection signal to the server
     * @param hasFocus
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.v("Question activity: ", "focus lost");
            ((Koeko) this.getApplication()).startActivityTransitionTimer();
        } else {
            ((Koeko) this.getApplication()).stopActivityTransitionTimer();
            Log.v("Question activity: ", "has focus");
        }
    }

}