package com.wideworld.koeko.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wideworld.koeko.Activities.ActivityTools.CustomAlertDialog;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.NetworkCommunication.NetworkCommunication;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableQuestionShortAnswer;

import java.io.File;
import java.util.ArrayList;

public class ShortAnswerQuestionActivity extends Activity {
	Boolean wasAnswered = false;
	QuestionShortAnswer currentQ;
	private TextView txtQuestion;
	private EditText textAnswer;
	Button submitButton;
	ImageView picture;
	boolean isImageFitToScreen = true;
	LinearLayout linearLayout;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_shortanswerquestion);
		mContext = this;

		linearLayout = (LinearLayout) findViewById(R.id.linearLayoutShortAnswer);
		txtQuestion = (TextView)findViewById(R.id.textViewShortAnswerQuest);
		picture = new ImageView(getApplicationContext());
		submitButton = new Button(getApplicationContext());
		textAnswer = new EditText(getApplicationContext());


		//get bluetooth client object
//		final BluetoothClientActivity bluetooth = (BluetoothClientActivity)getIntent().getParcelableExtra("bluetoothObject");

		//get question from the bundle
		Bundle bun = getIntent().getExtras();
		final String question = bun.getString("question");
		String id = bun.getString("id");
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

		//send receipt to server
		String receipt = "ACTID///" + currentQ.getID() + "///";
		Koeko.wifiCommunicationSingleton.sendStringToServer(receipt);

		submitButton.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("SimpleDateFormat") @Override
			public void onClick(View v) {
				wasAnswered = true;
				String answer = textAnswer.getText().toString();

				NetworkCommunication networkCommunication = ((Koeko) getApplication()).getAppNetwork();
				networkCommunication.sendAnswerToServer(String.valueOf(answer), question, currentQ.getID(), "ANSW1");

				if (Koeko.wifiCommunicationSingleton.directCorrection.contentEquals("1")) {
					popupCorrection();
				} else {
					finish();
					invalidateOptionsMenu();
				}
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

		//restore activity state
		String activityState = null;
		if (Koeko.currentTestActivitySingleton != null) {
			activityState = Koeko.currentTestActivitySingleton.shrtaqActivitiesStates.get(String.valueOf(currentQ.getID()));
		} else {
			if (Koeko.shrtaqActivityState != null) {
				activityState = Koeko.shrtaqActivityState;
			}
		}
		if ((activityState != null && Koeko.currentTestActivitySingleton != null) || (activityState != null &&
				Koeko.currentQuestionShortAnswerSingleton != null &&
				Koeko.currentQuestionShortAnswerSingleton.getID().contentEquals(currentQ.getID()))) {
			String[] parsedState = activityState.split("///");
			if (parsedState[parsedState.length - 1].contentEquals("true")) {
				submitButton.setEnabled(false);
				submitButton.setAlpha(0.3f);
				wasAnswered = true;
			}

			textAnswer.setText(parsedState[0]);
		}
		//finished restoring activity
	}

	@Override
	protected void onPause() {
		super.onPause();

		saveActivityState();
	}

	private void saveActivityState() {
		String activityState = textAnswer.getText().toString() + "///";
		activityState += wasAnswered;
		if (Koeko.currentTestActivitySingleton != null) {
			Koeko.currentTestActivitySingleton.shrtaqActivitiesStates.put(String.valueOf(currentQ.getID()), activityState);
		} else {
			Koeko.shrtaqActivityState = activityState;
			Koeko.currentQuestionShortAnswerSingleton = currentQ;
		}
	}

	private void popupCorrection() {
		//get the answerof the student
		QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(currentQ.getID());
		String studentAnswers = textAnswer.getText().toString();
		//get the right answers
		ArrayList<String> rightAnswers = questionShortAnswer.getAnswers();

		//compare the student answer with the right answers
		String title = "";
		String message = "";
		if (rightAnswers.contains(studentAnswers)) {
			title = ":-)";
			message = getString(R.string.correction_correct);
		} else {
			String rightAnswer = "";
			for (int i = 0; i < rightAnswers.size(); i++) {
				rightAnswer += (rightAnswers.get(i));
				if (!(i == rightAnswers.size() -1)) rightAnswer += " or ";
			}
			title = ":-(";
			message = getString(R.string.correction_incorrect) + rightAnswer;
		}

		CustomAlertDialog customAlertDialog = new CustomAlertDialog(this);
		customAlertDialog.show();
		customAlertDialog.setProperties(message, this);
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) {
			Log.v("Question activity: ", "focus lost");
			((Koeko)this.getApplication()).startActivityTransitionTimer();
		} else {
			((Koeko)this.getApplication()).stopActivityTransitionTimer();
			Log.v("Question activity: ", "has focus");
		}
	}

}