package com.wideworld.koeko.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

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
	ToggleButton moreLessSettings;
	View internetServerSeparator;
	EditText internetServerEditText;
	TextView internetServerTextView;
	LinearLayout linearLayoutSettingsInternetServer;
	Spinner rolesSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		final Button buttonSaveAndBack;
		final Switch automaticConnectionSwitch;
		final Switch hotspotAvailableSwitch;

		editName = findViewById(R.id.edittextnom);
		editName.setText(DbTableSettings.getName(), null);

		editMaster = findViewById(R.id.edittextmaster);
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

		rolesSpinner = findViewById(R.id.spinnerHotspotConfiguration);
		String[] arraySpinner;
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			arraySpinner = new String[]{"Standard Role", "Bridge"};
		} else {
			arraySpinner = new String[]{"Standard Role"};
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, arraySpinner);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		rolesSpinner.setAdapter(adapter);
		rolesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				DbTableSettings.updateHotspotConfiguration(String.valueOf(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
			}
		});
		rolesSpinner.setSelection(DbTableSettings.getHotspotConfiguration());

		//setup more/less settings
		linearLayoutSettingsInternetServer = findViewById(R.id.linearLayoutSettingsInternetServer);
		internetServerTextView = findViewById(R.id.textViewInternetServer);
		internetServerEditText = findViewById(R.id.editTextInternetServer);
		internetServerEditText.setText(DbTableSettings.getInternetServer(), null);
		internetServerSeparator = findViewById(R.id.internetAddressSeparator);
		moreLessSettings = findViewById(R.id.moreLessSettingsButton);
		moreLessSettings.setTextOff(getResources().getString(R.string.more_settings));
		moreLessSettings.setTextOn(getResources().getString(R.string.less_settings));
		moreLessSettings.setChecked(false);
	}

	public void toggleSettings(View view) {
		if (moreLessSettings.isChecked()) {
			internetServerSeparator.setVisibility(View.VISIBLE);
			linearLayoutSettingsInternetServer.setVisibility(View.VISIBLE);

		} else {
			internetServerSeparator.setVisibility(View.GONE);
			linearLayoutSettingsInternetServer.setVisibility(View.GONE);
		}
	}

	protected void onPause() {
		super.onPause();
		saveSettings();
	}

	private void saveSettings() {
		DbTableSettings.addName(editName.getText().toString());
		DbTableSettings.addMaster(editMaster.getText().toString());
		DbTableSettings.insertInternetServer(internetServerEditText.getText().toString());
		Intent intent = new Intent(SettingsActivity.this, MenuActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("flag", "modify");
		startActivity(intent);
		finish();
	}
}
