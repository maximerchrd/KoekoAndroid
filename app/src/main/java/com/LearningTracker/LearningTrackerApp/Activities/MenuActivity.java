package com.LearningTracker.LearningTrackerApp.Activities;


import com.LearningTracker.LearningTrackerApp.AndroidDatabaseManager;
import com.LearningTracker.LearningTrackerApp.database_management.DbHelper;
import com.LearningTracker.LearningTrackerApp.R;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuActivity extends Activity {
	Button  scoresButton, exerciceButton, buttonChangeSettings, interactiveModeButton;
	TextView consignes;
	private Boolean firstTime = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		scoresButton = (Button)findViewById(R.id.scoresbutton);
		exerciceButton = (Button)findViewById(R.id.exercicebutton);
		interactiveModeButton = (Button)findViewById(R.id.interactivemodebutton);
		buttonChangeSettings = (Button)findViewById(R.id.buttonchangesettings);
		consignes = (TextView) findViewById(R.id.textViewmenu);
		final DbHelper db = new DbHelper(this);


		//set  text for consignes
		consignes.setText(getString(R.string.hello) + " " + db.getName().split(" ")[0] + getString(R.string.instructions_menu));

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,      
				LinearLayout.LayoutParams.WRAP_CONTENT
				);
		int height = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
		int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
		params.setMargins(width / 40, height / 200, width / 40, height / 200);  //left, top, right, bottom
		//startButton.setLayoutParams(params);
		scoresButton.setLayoutParams(params);
		exerciceButton.setLayoutParams(params);
		interactiveModeButton.setLayoutParams(params);
		buttonChangeSettings.setLayoutParams(params);

		//start interactive questions session
		interactiveModeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, InteractiveModeActivity.class);
				startActivity(intent);
			}
		});

		//start interactive questions session
		exerciceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, ExerciseActivity.class);
				startActivity(intent);
			}
		});

		//go to scores button
		scoresButton.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, ResultsActivtity.class);
				startActivity(intent);
			}
		});

		//open change settings activity
		buttonChangeSettings.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});

		Button button = (Button)findViewById(R.id.dbBrowsingButton);
		//button.setVisibility(View.GONE);

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				Intent dbmanager = new Intent(MenuActivity.this,AndroidDatabaseManager.class);
				startActivity(dbmanager);
			}
		});

	}
}
