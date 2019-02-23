package com.wideworld.koeko.Activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.ClientToServerTransferable;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.CtoSPrefix;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.States.QuestionShortAnswerState;
import com.wideworld.koeko.R;
import com.wideworld.koeko.Tools.FileHandler;
import com.wideworld.koeko.database_management.DbTableQuestionShortAnswer;

import java.io.File;
import java.util.ArrayList;

public class ShortAnswerQuestionFragment extends Fragment {
	Boolean wasAnswered = false;
	QuestionShortAnswer currentQ;
	private TextView txtQuestion;
	private EditText textAnswer;
	Button submitButton;
	ImageView picture;
	boolean isImageFitToScreen = true;
	LinearLayout linearLayout;
	private Activity mActivity;
	private Long startingTime = 0L;

	static private String TAG = "ShortAnswerQuestionFragment";

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_shortanswerquestion, container, false);
		mActivity = getActivity();

		Koeko.networkCommunicationSingleton.mInteractiveModeActivity.forwardButton.setTitle("");

		linearLayout = rootView.findViewById(R.id.linearLayoutShortAnswer);
		txtQuestion = rootView.findViewById(R.id.textViewShortAnswerQuest);
		picture = new ImageView(mActivity);
		submitButton = new Button(mActivity);
		textAnswer = new EditText(mActivity);
		TextView timerView = rootView.findViewById(R.id.timerViewShrtaq);


		//get bluetooth client object
//		final BluetoothClientActivity bluetooth = (BluetoothClientActivity)getIntent().getParcelableExtra("bluetoothObject");

		//get question from the bundle
		Bundle bun = getArguments();
		final String question = bun.getString("question");
		String id = bun.getString("id");
		String image_path = bun.getString("image_name");
		Integer timerSeconds = bun.getInt("timerSeconds");
		currentQ = new QuestionShortAnswer("1",question,image_path);
		currentQ.setId(id);
		currentQ.setTimerSeconds(timerSeconds);
		if (currentQ.getImage().length() > 0) {
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
				wasAnswered = true;
				String answer = textAnswer.getText().toString();

				ArrayList<String> answerArray = new ArrayList<>();
				answerArray.add(answer);
				Koeko.networkCommunicationSingleton.sendAnswerToServer(answerArray, answer, question, currentQ.getId(), "ANSW1",
						(SystemClock.elapsedRealtime() - startingTime) / 1000);

				if (Koeko.networkCommunicationSingleton.directCorrection.contentEquals("1")) {
					popupCorrection();
				} else {
					dismiss();
					//invalidateOptionsMenu();
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
				mActivity.runOnUiThread(() -> {
					disactivateQuestion();
				});
			}).start();
		}
		return rootView;
	}
	private void setQuestionView()
	{
		txtQuestion.setText(currentQ.getQuestion());

		if (currentQ.getImage().contains(":") && currentQ.getImage().length() > currentQ.getImage().indexOf(":") + 1) {
			currentQ.setImage(currentQ.getImage().substring(currentQ.getImage().indexOf(":") + 1));
		}
		File imgFile = new  File(mActivity.getFilesDir() + "/"+ FileHandler.mediaDirectory + currentQ.getImage());
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
		int height = mActivity.getResources().getDisplayMetrics().heightPixels;
		int width = mActivity.getResources().getDisplayMetrics().widthPixels;
		params.setMargins(width / 40, height / 200, width / 40, height / 200);  //left, top, right, bottom
		submitButton.setLayoutParams(params);
		submitButton.setTextColor(Color.WHITE);
		linearLayout.addView(submitButton);

		//restore activity state
		QuestionShortAnswerState activityState = null;
		if (Koeko.currentTestFragmentSingleton != null) {
			activityState = Koeko.currentTestFragmentSingleton.shrtaqActivitiesStates.get(String.valueOf(currentQ.getId()));
		} else {
			if (Koeko.shrtaqActivityState != null) {
				activityState = Koeko.shrtaqActivityState;
			}
		}
		if ((activityState != null && Koeko.currentTestFragmentSingleton != null) || (activityState != null &&
				Koeko.currentQuestionShortAnswerSingleton != null &&
				Koeko.currentQuestionShortAnswerSingleton.getId().contentEquals(currentQ.getId()))) {
			if (activityState.getWasAnswered()) {
				disactivateQuestion();
				wasAnswered = true;
			}

			textAnswer.setText(activityState.getQuestionText());

			//restore timer
			try {
				startingTime = activityState.getTimeRemaining();
				Long elapsedTime = SystemClock.elapsedRealtime();
				Long effectiveElapsedTime = elapsedTime - startingTime;
				if (currentQ.getTimerSeconds() != -1 && (currentQ.getTimerSeconds()	- effectiveElapsedTime / 1000) < 0) {
					disactivateQuestion();
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		//finished restoring activity
	}

	protected void saveActivityState() {
		QuestionShortAnswerState activityState = new QuestionShortAnswerState();
		activityState.setQuestionText(textAnswer.getText().toString());
		activityState.setWasAnswered(wasAnswered);
		activityState.setTimeRemaining(startingTime);
		if (Koeko.currentTestFragmentSingleton != null) {
			Koeko.currentTestFragmentSingleton.shrtaqActivitiesStates.put(String.valueOf(currentQ.getId()), activityState);
		} else {
			Koeko.shrtaqActivityState = activityState;
			Koeko.currentQuestionShortAnswerSingleton = currentQ;
		}
	}

	private void popupCorrection() {
		//get the answerof the student
		QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(currentQ.getId());
		String studentAnswer = textAnswer.getText().toString();
		//get the right answers
		ArrayList<String> rightAnswers = questionShortAnswer.getAnswers();

		//compare the student answer with the right answers
		String correction = "";
		if (rightAnswers.contains(studentAnswer)) {
			correction = "<font size='5' color='green'>Right :-)</font> <br/>" + studentAnswer;
		} else {
			correction = studentAnswer + "<br/><font size='5' color='red'>Unfortunately, this wasn't the right answer :-( </font>";
			if (rightAnswers.size() == 0) {
				correction += "The right answer was EMPTY ANSWER";
			} else if (rightAnswers.size() == 1) {
				correction += "The right answer was:<br/>" + rightAnswers.get(0);
			} else {
				correction += "The right answer was for example:<br/>" + rightAnswers.get(0);
			}
		}
		textAnswer.setText(Html.fromHtml(correction));
	}

	private void disactivateQuestion() {
		textAnswer.setEnabled(false);
		submitButton.setText("OK");
		wasAnswered = true;
	}

	private void dismiss() {
		saveActivityState();
		Koeko.networkCommunicationSingleton.mInteractiveModeActivity.getSupportFragmentManager().popBackStack();
		if (Koeko.currentTestFragmentSingleton != null) {
			InteractiveModeActivity.backToTestFromQuestion = true;
		}
		Koeko.networkCommunicationSingleton.mInteractiveModeActivity.setForwardButton(InteractiveModeActivity.forwardQuestionShortAnswer);
	}

}