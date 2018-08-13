package com.LearningTracker.LearningTrackerApp.NetworkCommunication;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.LearningTracker.LearningTrackerApp.database_management.DbHelper;
import com.LearningTracker.LearningTrackerApp.LTApplication;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionShortAnswer;

public class NetworkCommunication {
	private Context mContextNetCom;
	private Application mApplication;
	private ArrayList<ArrayList<String>> mNetwork_addresses;
	private WifiCommunication mWifiCom;
	private TextView mTextOut;
	public Boolean connectedThroughBT = false;
	private int network_solution = 0; //0: all devices connected to same wifi router



	public NetworkCommunication(Context arg_context, Application application, TextView textOut, TextView logView) {
		mNetwork_addresses = new ArrayList<ArrayList<String>>();
		mContextNetCom = arg_context;
		mApplication = application;
		mTextOut = textOut;
		mWifiCom = new WifiCommunication(arg_context, application, logView);
		//((LTApplication) mApplication).setAppWifi(mWifiCom);
		((LTApplication) mApplication).setAppNetwork(this);
	}
	/**
	 * method to launch the network of smartphones and 1 laptop communicating using wifi
	 */
	public void ConnectToMaster() {
		if (network_solution == 0) {
			String MacAddress = android.provider.Settings.Secure.getString(mContextNetCom.getContentResolver(), "bluetooth_address");
			DbHelper db_for_name = new DbHelper(mContextNetCom);
			String name = db_for_name.getName();

			final String connection = "CONN" + "///" + MacAddress + "///" + name + "///" +
					DbTableQuestionMultipleChoice.getAllQuestionMultipleChoiceIds() + "|" +
					DbTableQuestionShortAnswer.getAllShortAnswerIds() + "///";
			new Thread(new Runnable() {
				public void run() {
					mWifiCom.connectToServer(connection);
				}
			}).start();
		}
	}

	public void sendAnswerToServer(String answer, String question, String id, String answerType) {
		String MacAddress = android.provider.Settings.Secure.getString(mContextNetCom.getContentResolver(), "bluetooth_address");
		DbHelper db_for_name = new DbHelper(mContextNetCom);
		String name = db_for_name.getName();

		answer = answerType + "///" + MacAddress + "///" + name + "///" + answer + "///" + question + "///" + String.valueOf(id) + "///";
		if (network_solution == 0) {
			mWifiCom.sendAnswerToServer(answer);
		}
	}

	public void sendDisconnectionSignal() {
		PowerManager pm = (PowerManager) mContextNetCom.getSystemService(Context.POWER_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
			if (pm.isInteractive()) {
                String MacAddress = Settings.Secure.getString(mContextNetCom.getContentResolver(), "bluetooth_address");
                DbHelper db_for_name = new DbHelper(mContextNetCom);
                String name = db_for_name.getName();
                String signal = "DISC///" + MacAddress + "///" + name + "///";
                mWifiCom.sendDisconnectionSignal(signal);
            }
		} else {
			String MacAddress = Settings.Secure.getString(mContextNetCom.getContentResolver(), "bluetooth_address");
			DbHelper db_for_name = new DbHelper(mContextNetCom);
			String name = db_for_name.getName();
			String signal = "DISC///" + MacAddress + "///" + name + "///Android///";
			mWifiCom.sendDisconnectionSignal(signal);
			Log.w("sending disc sign:","Too old API doesn't allow to check for disconnection because of screen turned off");
		}
	}

}
