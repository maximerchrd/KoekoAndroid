package com.LearningTracker.LearningTrackerApp.Activities.Tools;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.LearningTracker.LearningTrackerApp.database_management.DbTableIndividualQuestionForResult;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionShortAnswer;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by maximerichard on 21.02.18.
 */
// Instances of this class are fragments representing a single
// object in our collection.
public class ExerciseObjectFragment extends Fragment {
    public static final String ARG_OBJECT = "object";
    private ArrayList<Integer> mQuestionIds = new ArrayList<>();
    private Integer mQuestionPositionInArray = 0;
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
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(R.layout.fragment_collection_object, container, false);
        Bundle args = getArguments();
        mQuestionIds = args.getIntegerArrayList("IDsArray");
        mQuestionPositionInArray = args.getInt(ARG_OBJECT);
        //((TextView) rootView.findViewById(android.R.id.text1)).setText(Integer.toString(args.getIntegerArrayList("IDsArray").get(args.getInt(ARG_OBJECT))));

        mContext = rootView.getContext();
        linearLayout = (LinearLayout) rootView.findViewById(R.id.practice_linearLayout);
        txtQuestion = ((TextView) rootView.findViewById(R.id.questionTextFragmentCollection));
        picture = new ImageView(mContext);
        submitButton = new Button(mContext);
        checkBoxesArray = new ArrayList<>();
        textAnswer = new EditText(mContext);

        mMulChoiceQuestion = DbTableQuestionMultipleChoice.getQuestionWithId(mQuestionIds.get(mQuestionPositionInArray));
        mShortAnsQuestion = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(mQuestionIds.get(mQuestionPositionInArray));
        if (mMulChoiceQuestion.getQUESTION().length() > 0) {
            setMultChoiceQuestionView();
        } else if (mShortAnsQuestion.getQUESTION().length() > 0) {
            setShortAnswerQuestionView();
        } else {
            Log.w("in ExerciseObjFragment", "no question or question type not recognized");
        }
        return rootView;
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

//		int imageResource = getResources().getIdentifier(currentQ.getIMAGE(), null, getPackageName());
//		picture.setImageResource(imageResource);


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

        //implementing Fisher-Yates shuffle
        Random rnd = new Random();
        for (int i = number_of_possible_answers - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            String a = answerOptions[index];
            answerOptions[index] = answerOptions[i];
            answerOptions[i] = a;
        }

        CheckBox tempCheckBox = null;

        for (int i = 0; i < number_of_possible_answers; i++) {
            tempCheckBox = new CheckBox(mContext);
            tempCheckBox.setText(answerOptions[i]);
            tempCheckBox.setTextColor(Color.BLACK);
            checkBoxesArray.add(tempCheckBox);
            if(checkBoxesArray.get(i).getParent()!=null)
                ((ViewGroup)checkBoxesArray.get(i).getParent()).removeView(checkBoxesArray.get(i));

            checkBoxesArray.get(i).setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 10f));

            linearLayout.addView(checkBoxesArray.get(i));
        }
        submitButton.setText(getString(R.string.answer_button));
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
        //get the answers checked by student
        ArrayList<String> studentAnswers = new ArrayList<String>();
        String answerForStoring = "";
        for (int i = 0; i < checkBoxesArray.size(); i++) {
            if (checkBoxesArray.get(i).isChecked()) {
                studentAnswers.add(checkBoxesArray.get(i).getText().toString());
                answerForStoring += checkBoxesArray.get(i).getText().toString() + ";";
            }
        }
        //get the right answers
        ArrayList<String> rightAnswers = new ArrayList<String>();
        for (int i = 0; i < mMulChoiceQuestion.getNB_CORRECT_ANS(); i++) {
            rightAnswers.add(mMulChoiceQuestion.getPossibleAnswers().get(i));
        }
        //compare the student answers with the right answers
        if (rightAnswers.containsAll(studentAnswers) && studentAnswers.containsAll(rightAnswers)) {
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setMessage(getString(R.string.correction_correct));
            alertDialog.show();
            submitButton.setEnabled(false);
            submitButton.setAlpha(0.45f);
            picture.setAlpha(0.45f);
            txtQuestion.setAlpha(0.45f);
            textAnswer.setAlpha(0.45f);
            DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(String.valueOf(mShortAnsQuestion.getID()),"100", answerForStoring);
        } else {
            String correct_answers = "";
            for (int i = 0; i < rightAnswers.size(); i++) {
                correct_answers += (rightAnswers.get(i));
                if (!(i == rightAnswers.size() -1)) correct_answers += " or ";
            }
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setMessage(getString(R.string.correction_incorrect) + correct_answers);
            alertDialog.show();
            submitButton.setEnabled(false);
            submitButton.setAlpha(0.45f);
            picture.setAlpha(0.45f);
            txtQuestion.setAlpha(0.45f);
            textAnswer.setAlpha(0.45f);

            for (int i = 0; i < checkBoxesArray.size(); i++) {
                if (checkBoxesArray.get(i).isChecked()) {
                    if (rightAnswers.contains(checkBoxesArray.get(i).getText())) {
                        checkBoxesArray.get(i).setTextColor(Color.GREEN);
                    } else {
                        checkBoxesArray.get(i).setTextColor(Color.RED);
                    }
                } else {
                    if (rightAnswers.contains(checkBoxesArray.get(i).getText())) {
                        checkBoxesArray.get(i).setTextColor(Color.RED);
                    } else {
                        checkBoxesArray.get(i).setTextColor(Color.GREEN);
                    }
                }
            }
            DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(String.valueOf(mShortAnsQuestion.getID()),"0", answerForStoring);
        }
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

//		int imageResource = getResources().getIdentifier(currentQ.getIMAGE(), null, getPackageName());
//		picture.setImageResource(imageResource);
        textAnswer.setTextColor(Color.BLACK);
        linearLayout.addView(textAnswer);

        submitButton.setText(getString(R.string.answer_button));
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
        //get the answerof the student
        String studentAnswers = textAnswer.getText().toString();
        //get the right answers
        ArrayList<String> rightAnswers = mShortAnsQuestion.getAnswers();

        //compare the student answer with the right answers
        if (rightAnswers.contains(studentAnswers)) {
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setMessage(getString(R.string.correction_correct));
            alertDialog.show();
            submitButton.setEnabled(false);
            submitButton.setAlpha(0.45f);
            picture.setAlpha(0.45f);
            txtQuestion.setAlpha(0.45f);
            textAnswer.setAlpha(0.45f);
            DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(String.valueOf(mShortAnsQuestion.getID()),"100", studentAnswers);
        } else {
            String rightAnswer = "";
            for (int i = 0; i < rightAnswers.size(); i++) {
                rightAnswer += (rightAnswers.get(i));
                if (!(i == rightAnswers.size() -1)) rightAnswer += " or ";
            }
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setMessage(getString(R.string.correction_incorrect) + rightAnswer);
            alertDialog.show();
            submitButton.setEnabled(false);
            submitButton.setAlpha(0.45f);
            picture.setAlpha(0.45f);
            txtQuestion.setAlpha(0.45f);
            textAnswer.setAlpha(0.45f);
            DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(String.valueOf(mShortAnsQuestion.getID()),"0", studentAnswers);
        }
    }
}
