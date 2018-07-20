package com.LearningTracker.LearningTrackerApp.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.R;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionShortAnswer;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by maximerichard on 21.02.18.
 */
// Instances of this class are fragments representing a single
// object in our collection.
public class CorrectedQuestionActivity extends Activity {
    private String mQuestionId = "-1";
    private QuestionMultipleChoice mMulChoiceQuestion = null;
    private QuestionShortAnswer mShortAnsQuestion = null;
    private TextView txtQuestion;
    private ImageView picture;
    private EditText textAnswer;
    private int number_of_possible_answers = 0;
    private ArrayList<CheckBox> checkBoxesArray;
    private Button submitButton;
    private LinearLayout linearLayout;
    private Context mContext;
    boolean isImageFitToScreen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrected_question);

        Bundle bun = getIntent().getExtras();
        mQuestionId = bun.getString("questionID");

        mContext = this.getApplicationContext();
        linearLayout = (LinearLayout) findViewById(R.id.correction_linearLayout);
        txtQuestion = ((TextView) findViewById(R.id.questionCorrection));
        picture = new ImageView(mContext);
        submitButton = new Button(mContext);
        checkBoxesArray = new ArrayList<>();
        textAnswer = new EditText(mContext);

        mMulChoiceQuestion = DbTableQuestionMultipleChoice.getQuestionWithId(mQuestionId);
        mShortAnsQuestion = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(mQuestionId);
        if (mMulChoiceQuestion.getQUESTION().length() > 0) {
            setMultChoiceQuestionView();
        } else if (mShortAnsQuestion.getQUESTION().length() > 0) {
            setShortAnswerQuestionView();
        } else {
            Log.w("in ExerciseObjFragment", "no question or question type not recognized");
        }
    }

    private void setMultChoiceQuestionView()
    {
        if (mMulChoiceQuestion.getIMAGE().length() > 0) {
            picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isImageFitToScreen) {
                        isImageFitToScreen=false;
                        picture.setAdjustViewBounds(true);
                        picture.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        picture.setAdjustViewBounds(true);
                    }else{
                        isImageFitToScreen=true;
                        picture.setAdjustViewBounds(true);
                        picture.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200));
                    }
                }
            });
        }

        txtQuestion.setText(mMulChoiceQuestion.getQUESTION());

        File imgFile = new  File(mContext.getFilesDir()+"/images/" + mMulChoiceQuestion.getIMAGE());
        if(imgFile.exists()){
            String path = imgFile.getAbsolutePath();
            Bitmap myBitmap = BitmapFactory.decodeFile(path);
            picture.setImageBitmap(myBitmap);
        }
        picture.setAdjustViewBounds(true);
        picture.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200));
        linearLayout.addView(picture);



        String[] answerOptions;
        answerOptions = new String[10];
        answerOptions[0] = mMulChoiceQuestion.getOPT0();
        answerOptions[1] = mMulChoiceQuestion.getOPT1();
        answerOptions[2] = mMulChoiceQuestion.getOPT2();
        answerOptions[3] = mMulChoiceQuestion.getOPT3();
        answerOptions[4] = mMulChoiceQuestion.getOPT4();
        answerOptions[5] = mMulChoiceQuestion.getOPT5();
        answerOptions[6] = mMulChoiceQuestion.getOPT6();
        answerOptions[7] = mMulChoiceQuestion.getOPT7();
        answerOptions[8] = mMulChoiceQuestion.getOPT8();
        answerOptions[9] = mMulChoiceQuestion.getOPT9();

        for (int i = 0; i < 10; i++) {
            if (!answerOptions[i].equals(" ")) {
                number_of_possible_answers++;
            }
        }

        CheckBox tempCheckBox = null;

        for (int i = 0; i < number_of_possible_answers; i++) {
            tempCheckBox = new CheckBox(mContext);
            tempCheckBox.setText(answerOptions[i]);
            tempCheckBox.setTextColor(Color.BLACK);
            tempCheckBox.setEnabled(false);
            if (i < mMulChoiceQuestion.getNB_CORRECT_ANS()) {
                tempCheckBox.setChecked(true);
            }
            checkBoxesArray.add(tempCheckBox);
            if(checkBoxesArray.get(i).getParent()!=null)
                ((ViewGroup)checkBoxesArray.get(i).getParent()).removeView(checkBoxesArray.get(i));

            checkBoxesArray.get(i).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 10f));

            linearLayout.addView(checkBoxesArray.get(i));
        }
        submitButton.setText(getString(R.string.ok_button));
        submitButton.setBackgroundColor(Color.parseColor("#00CCCB"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int height = mContext.getResources().getDisplayMetrics().heightPixels;
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        params.setMargins(width / 40, height / 200, width / 40, height / 200);  //left, top, right, bottom
        submitButton.setLayoutParams(params);
        submitButton.setTextColor(Color.WHITE);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SimpleDateFormat") @Override
            public void onClick(View v) {
                MltChoiceQuestionButtonClick();

            }
        });

        linearLayout.addView(submitButton);
    }

    private void MltChoiceQuestionButtonClick() {
        finish();
    }

    private void setShortAnswerQuestionView()
    {
        if (mShortAnsQuestion.getIMAGE().length() > 0) {
            picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isImageFitToScreen) {
                        isImageFitToScreen=false;
                        picture.setAdjustViewBounds(true);
                        picture.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        picture.setAdjustViewBounds(true);
                    }else{
                        isImageFitToScreen=true;
                        picture.setAdjustViewBounds(true);
                        picture.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200));
                    }
                }
            });
        }

        txtQuestion.setText(mShortAnsQuestion.getQUESTION());

        File imgFile = new  File(mContext.getFilesDir()+"/images/" + mShortAnsQuestion.getIMAGE());
        if(imgFile.exists()){
            String path = imgFile.getAbsolutePath();
            Bitmap myBitmap = BitmapFactory.decodeFile(path);
            picture.setImageBitmap(myBitmap);
        }
        picture.setAdjustViewBounds(true);
        picture.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200));
        linearLayout.addView(picture);


        textAnswer.setTextColor(Color.BLACK);
        String answer = getString(R.string.answer_example);
        if (mShortAnsQuestion.getAnswers().size() > 0) {
            answer += mShortAnsQuestion.getAnswers().get(0);
        }
        textAnswer.setText(answer);
        linearLayout.addView(textAnswer);

        submitButton.setText(getString(R.string.ok_button));
        submitButton.setBackgroundColor(Color.parseColor("#00CCCB"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int height = mContext.getResources().getDisplayMetrics().heightPixels;
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        params.setMargins(width / 40, height / 200, width / 40, height / 200);  //left, top, right, bottom
        submitButton.setLayoutParams(params);
        submitButton.setTextColor(Color.WHITE);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SimpleDateFormat") @Override
            public void onClick(View v) {
                shrtAnswerQuestionButtonClick();

            }
        });
        linearLayout.addView(submitButton);
    }

    private void shrtAnswerQuestionButtonClick() {
        finish();
    }
}
