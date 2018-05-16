package com.LearningTracker.LearningTrackerApp.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.LearningTracker.LearningTrackerApp.LTApplication;
import com.LearningTracker.LearningTrackerApp.NetworkCommunication.NetworkCommunication;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShortAnswerQuestionActivity extends Activity {
	int score=0;
	int qid=0;
	int level=1;
	int number_of_possible_answers = 0;
	int nbQuestionsLevel1 = 8;
	int nbQuestionsLevel2 = 6;
	int nbQuestionsLevel3 = 5;
	int trialCounter = 1;
	int questionId = 1;
	QuestionShortAnswer currentQ;
	private TextView txtQuestion;
	private EditText textAnswer;
	Button answerButton1, answerButton2, answerButton3, answerButton4, submitButton;
	CheckBox checkbox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6, checkBox7;
	ArrayList<CheckBox> checkBoxesArray;
	ArrayList<String> arrayOfOptions;
	ImageView picture;
	boolean isImageFitToScreen = true;
	LinearLayout linearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_shortanswerquestion);


		linearLayout = (LinearLayout) findViewById(R.id.linearLayoutShortAnswer);
		txtQuestion = (TextView)findViewById(R.id.textViewShortAnswerQuest);
		picture = new ImageView(getApplicationContext());
		submitButton = new Button(getApplicationContext());
		checkBoxesArray = new ArrayList<>();
		textAnswer = new EditText(getApplicationContext());


		//get bluetooth client object
//		final BluetoothClientActivity bluetooth = (BluetoothClientActivity)getIntent().getParcelableExtra("bluetoothObject");

		//get question from the bundle
		Bundle bun = getIntent().getExtras();
		final String question = bun.getString("question");
		int id = bun.getInt("id");
		String image_path = bun.getString("image_name");
		currentQ = new QuestionShortAnswer("1",question,image_path);
		currentQ.setID(id);
		if (currentQ.getIMAGE().length() > 0) {
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
			@SuppressLint("SimpleDateFormat") @Override
			public void onClick(View v) {
				String answer = textAnswer.getText().toString();

				NetworkCommunication networkCommunication = ((LTApplication) getApplication()).getAppNetwork();
				networkCommunication.sendAnswerToServer(String.valueOf(answer), question, currentQ.getID(), "ANSW1");
				finish();
				invalidateOptionsMenu();
			}
		});
	}
	private void setQuestionView()
	{
		txtQuestion.setText(currentQ.getQUESTION());

		if (currentQ.getIMAGE().contains(":") && currentQ.getIMAGE().length() > currentQ.getIMAGE().indexOf(":") + 1) {
			currentQ.setIMAGE(currentQ.getIMAGE().substring(currentQ.getIMAGE().indexOf(":") + 1));
		}
		File imgFile = new  File(getFilesDir()+"/images/" + currentQ.getIMAGE());
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
		int height = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
		int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
		params.setMargins(width / 40, height / 200, width / 40, height / 200);  //left, top, right, bottom
		submitButton.setLayoutParams(params);
		submitButton.setTextColor(Color.WHITE);
		linearLayout.addView(submitButton);
		qid++;
	}


	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) {
			Log.v("Question activity: ", "focus lost");
			((LTApplication)this.getApplication()).startActivityTransitionTimer();
		} else {
			((LTApplication)this.getApplication()).stopActivityTransitionTimer();
			Log.v("Question activity: ", "has focus");
		}
	}

}