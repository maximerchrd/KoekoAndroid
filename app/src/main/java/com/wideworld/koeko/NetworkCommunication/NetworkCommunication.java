package com.wideworld.koeko.NetworkCommunication;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.wideworld.koeko.Activities.InteractiveModeActivity;
import com.wideworld.koeko.Tools.FileHandler;
import com.wideworld.koeko.database_management.DbHelper;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.database_management.DbTableQuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableQuestionShortAnswer;
import com.wideworld.koeko.database_management.DbTableSettings;

public class NetworkCommunication {
	private Context mContextNetCom;
	private Application mApplication;
	private ArrayList<ArrayList<String>> mNetwork_addresses;
	private WifiCommunication mWifiCom;
	private TextView mTextOut;
	static public Boolean connected = false;
	public Boolean connectedThroughBT = false;
	private int network_solution = 0; //0: all devices connected to same wifi router
	private InteractiveModeActivity mInteractiveModeActivity;



	public NetworkCommunication(Context arg_context, Application application, TextView textOut, TextView logView,
								InteractiveModeActivity interactiveModeActivity) {
		mNetwork_addresses = new ArrayList<ArrayList<String>>();
		mContextNetCom = arg_context;
		mApplication = application;
		mTextOut = textOut;
		mWifiCom = new WifiCommunication(arg_context, application, logView, this);
		mInteractiveModeActivity = interactiveModeActivity;
		//((Koeko) mApplication).setAppWifi(mWifiCom);
		((Koeko) mApplication).setAppNetwork(this);
	}
	/**
	 * method to launch the network of smartphones and 1 laptop communicating using wifi
	 */
	public void ConnectToMaster() {
		if (network_solution == 0) {
			String MacAddress = android.provider.Settings.Secure.getString(mContextNetCom.getContentResolver(), "bluetooth_address");
			DbHelper db_for_name = new DbHelper(mContextNetCom);
			String name = DbTableSettings.getName();

			final String connection = "CONN" + "///" + MacAddress + "///" + name + "///";
			new Thread(new Runnable() {
				public void run() {
					mWifiCom.connectToServer(connection, MacAddress);
				}
			}).start();
		}
	}

	public void sendAnswerToServer(String answer, String question, String id, String answerType) {
		String MacAddress = android.provider.Settings.Secure.getString(mContextNetCom.getContentResolver(), "bluetooth_address");
		DbHelper db_for_name = new DbHelper(mContextNetCom);
		String name = DbTableSettings.getName();

		answer = answerType + "///" + MacAddress + "///" + name + "///" + answer + "///" + question + "///" + String.valueOf(id) + "///";
		if (network_solution == 0) {
			mWifiCom.sendAnswerToServer(answer);
		}
	}

	public void sendDisconnectionSignal() {
		PowerManager pm = (PowerManager) mContextNetCom.getSystemService(Context.POWER_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
			//check if device locked
			if (pm.isInteractive()) {
                String MacAddress = Settings.Secure.getString(mContextNetCom.getContentResolver(), "bluetooth_address");
                DbHelper db_for_name = new DbHelper(mContextNetCom);
                String name = DbTableSettings.getName();
                String signal = "DISC///" + MacAddress + "///" + name + "///";
                mWifiCom.sendStringToServer(signal);
                mWifiCom.closeConnection();
                mInteractiveModeActivity.showDisconnected();
                NetworkCommunication.connected = false;
            }
		} else {
			String MacAddress = Settings.Secure.getString(mContextNetCom.getContentResolver(), "bluetooth_address");
			DbHelper db_for_name = new DbHelper(mContextNetCom);
			String name = DbTableSettings.getName();
			String signal = "DISC///" + MacAddress + "///" + name + "///Android///";
			mWifiCom.sendStringToServer(signal);
			mWifiCom.closeConnection();
			mInteractiveModeActivity.showDisconnected();
			NetworkCommunication.connected = false;
			Log.w("sending disc sign:","Too old API doesn't allow to check for disconnection because of screen turned off");
		}
	}

}
