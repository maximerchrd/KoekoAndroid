package com.wideworld.koeko.Activities;


import com.google.android.gms.common.GoogleApiAvailability;
import com.wideworld.koeko.AndroidDatabaseManager;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.database_management.DbHelper;
import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableSettings;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MenuActivity extends Activity {
	Button  scoresButton, exerciceButton, buttonChangeSettings, interactiveModeButton;
	TextView consignes;
	private Boolean firstTime = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new DbHelper(this);
		setContentView(R.layout.activity_menu);
		scoresButton = (Button)findViewById(R.id.scoresbutton);
		exerciceButton = (Button)findViewById(R.id.exercicebutton);
		interactiveModeButton = (Button)findViewById(R.id.interactivemodebutton);
		buttonChangeSettings = (Button)findViewById(R.id.buttonchangesettings);

		//start interactive questions session
		interactiveModeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//check if the user changed the default name
				if (DbTableSettings.getName().contentEquals(getString(R.string.no_name))) {
					Toast toast = Toast.makeText(getApplicationContext(), R.string.please_change_name, Toast.LENGTH_LONG);
					toast.setGravity(Gravity.TOP,0,100);
					toast.show();
				} else {
					Intent intent = new Intent(MenuActivity.this, InteractiveModeActivity.class);
					startActivity(intent);
				}
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
		if (!DbTableSettings.getName().contentEquals("secretcode")) {
			button.setVisibility(View.GONE);
		}

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				Intent dbmanager = new Intent(MenuActivity.this,AndroidDatabaseManager.class);
				startActivity(dbmanager);
			}
		});

		try {
			if (getApplication().getPackageManager().getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0 ).versionCode < 12451000) {
				String message = "If it's possible, please install a newer version of Google Play Services from the Play Store";
				for (int i = 0; i < 2; i++) {
					Toast.makeText(this, message, Toast.LENGTH_LONG).show();
				}
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void onStart() {
		super.onStart();
		if (Koeko.testConnectivity > 0) {
			DbTableSettings.addName(String.valueOf(Koeko.testConnectivity));
			DbTableSettings.addMaster("kill the masters");
			Koeko.testConnectivity++;
			Intent intent = new Intent(MenuActivity.this, InteractiveModeActivity.class);
			startActivity(intent);
		}
	}
}
