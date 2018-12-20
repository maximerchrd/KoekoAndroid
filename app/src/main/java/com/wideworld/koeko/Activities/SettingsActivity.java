package com.wideworld.koeko.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.view.View;
import android.widget.Switch;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.NetworkCommunication.HotspotServer.HotspotServer;
import com.wideworld.koeko.NetworkCommunication.NearbyCommunication;
import com.wideworld.koeko.NetworkCommunication.NetworkCommunication;
import com.wideworld.koeko.database_management.DbHelper;
import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableSettings;


public class SettingsActivity extends Activity {
	EditText editName;
	EditText editMaster;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		final Button buttonSaveAndBack;
		final Switch automaticConnectionSwitch;
		final Switch hotspotAvailableSwitch;

		editName = (EditText) findViewById(R.id.edittextnom);
		buttonSaveAndBack = (Button) findViewById(R.id.buttonsaveandback);
		editName.setText(DbTableSettings.getName(), null);

		editMaster = (EditText) findViewById(R.id.edittextmaster);
		editMaster.setText(DbTableSettings.getMaster(), null);

		//setup the switch for automatic connection
		automaticConnectionSwitch = findViewById(R.id.automaticConnectionSwitch);
		if (DbTableSettings.getAutomaticConnection() == 1) {
			automaticConnectionSwitch.setChecked(true);
			editMaster.setEnabled(false);
			editMaster.setTextColor(Color.GRAY);
		}

		hotspotAvailableSwitch = findViewById(R.id.hotspotAvailableSwitch);
		if (DbTableSettings.getHotspotAvailable() == 1) {
			hotspotAvailableSwitch.setChecked(true);
		}

		automaticConnectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (automaticConnectionSwitch.isChecked()) {
                DbTableSettings.setAutomaticConnection(1);
                editMaster.setEnabled(false);
                editMaster.setTextColor(Color.GRAY);
            } else {
                DbTableSettings.setAutomaticConnection(0);
                editMaster.setEnabled(true);
                editMaster.setTextColor(Color.BLACK);
            }
        });

		hotspotAvailableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (hotspotAvailableSwitch.isChecked()) {
				DbTableSettings.setHotspotAvailable(1);
			} else {
				DbTableSettings.setHotspotAvailable(0);
			}
		});
	}

	protected void onPause() {
		super.onPause();
		saveSettings();
	}

	private void saveSettings() {
		String name = editName.getText().toString();
		String master = editMaster.getText().toString();
		DbTableSettings.addName(name);
		DbTableSettings.addMaster(master);
		Intent intent = new Intent(SettingsActivity.this, MenuActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("flag", "modify");
		startActivity(intent);
		finish();
	}
}
