package com.wideworld.koeko.NetworkCommunication;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.wideworld.koeko.Activities.InteractiveModeActivity;
import com.wideworld.koeko.Activities.MultChoiceQuestionActivity;
import com.wideworld.koeko.Activities.ShortAnswerQuestionActivity;
import com.wideworld.koeko.Activities.TestActivity;
import com.wideworld.koeko.NetworkCommunication.HotspotServer.HotspotServer;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.database_management.DbTableSettings;

public class NetworkCommunication {
	final static public String nearbyServiceID = "org.wideoworld.koeko";
	static public String deviceIdentifier = "";
	private Context mContextNetCom;
	private Application mApplication;
	private ArrayList<ArrayList<String>> mNetwork_addresses;
	private WifiCommunication mWifiCom;
	private NearbyCommunication mNearbyCom;
	private TextView mTextOut;
	private String lastAnswer = "";
	static public int connected = 0;
	static public int network_solution = 0; //0: all devices connected to a WAN; 1: 3 layers, 1->WAN, 2->Wan to Nearby, 3-> Nearby to hotspot
	static public String directCorrection = "0";
	public HashSet<String> idsToSync;
	public InteractiveModeActivity mInteractiveModeActivity;
	private HotspotServer hotspotServerHotspot;



	public NetworkCommunication(Context arg_context, Application application, TextView textOut, TextView logView,
								InteractiveModeActivity interactiveModeActivity) {
		mNetwork_addresses = new ArrayList<ArrayList<String>>();
		mContextNetCom = arg_context;
		mApplication = application;
		mTextOut = textOut;
		mWifiCom = new WifiCommunication(arg_context, application, logView);
		mInteractiveModeActivity = interactiveModeActivity;
		mNearbyCom = new NearbyCommunication(mContextNetCom);
		NetworkCommunication.deviceIdentifier = DbTableSettings.getUUID();
		idsToSync = new HashSet<>();
		Koeko.networkCommunicationSingleton = this;
	}
	/**
	 * method to launch the network of smartphones and 1 laptop communicating using wifi
	 */
	public void connectToMaster(int reconnection) {
		String uniqueId = NetworkCommunication.deviceIdentifier;
		final String connection = getConnectionString();
		new Thread(() -> {
            //TODO: put a WifiLock
            mWifiCom.connectToServer(connection, uniqueId, reconnection);
        }).start();
	}

	public String getConnectionString() {
		String uniqueId = NetworkCommunication.deviceIdentifier;
		String name = DbTableSettings.getName();

		String deviceInfos = "";
		deviceInfos += "android:" + android.os.Build.VERSION.SDK_INT + ":";
		if (mApplication.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			deviceInfos += "BLE:";
		} else {
			deviceInfos += "NO-BLE:";
		}
		try {
			deviceInfos += mApplication.getPackageManager().getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0 ).versionCode;
			deviceInfos += ":";
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		deviceInfos += DbTableSettings.getHotspotAvailable() + ":";
		deviceInfos += Build.MODEL + ":";
		deviceInfos += "///";

		return "CONN" + "///" + uniqueId + "///" + name + "///" + deviceInfos;
	}

	public void sendAnswerToServer(String answer, String question, String id, String answerType) {
		String uuid = NetworkCommunication.deviceIdentifier;
		String name = DbTableSettings.getName();

		lastAnswer = answer; //save the answer for when we receive the evaluation from the server
		answer = answerType + "///" + uuid + "///" + name + "///" + answer + "///" + question + "///" + String.valueOf(id) + "///";
		if (network_solution == 0) {
			mWifiCom.sendAnswerToServer(answer);
		} else if (network_solution == 1) {
			if (NearbyCommunication.deviceRole == NearbyCommunication.DISCOVERER_ROLE) {
				mNearbyCom.sendBytes(answer.getBytes());
			} else {
				mWifiCom.sendAnswerToServer(answer);
			}
		}
	}

	public void sendStringToServer(String message) {
        if (network_solution == 0) {
            mWifiCom.sendAnswerToServer(message);
        } else if (network_solution == 1) {
            if (NearbyCommunication.deviceRole == NearbyCommunication.DISCOVERER_ROLE) {
                mNearbyCom.sendBytes(message.getBytes());
            } else {
                mWifiCom.sendAnswerToServer(message);
            }
        }
    }

	public void sendDisconnectionSignal() {
		PowerManager pm = (PowerManager) mContextNetCom.getSystemService(Context.POWER_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
			//check if device locked
			if (pm.isInteractive()) {
                String uuid = NetworkCommunication.deviceIdentifier;
                String name = DbTableSettings.getName();
                String signal = "DISC///" + uuid + "///" + name + "///";
                sendStringToServer(signal);
            }
		} else {
			String uuid = NetworkCommunication.deviceIdentifier;
			String name = DbTableSettings.getName();
			String signal = "DISC///" + uuid + "///" + name + "///Android///";
			sendStringToServer(signal);
			Log.w("sending disc sign:","Too old API doesn't allow to check for disconnection because of screen turned off");
		}
	}

	public void closeConnection() {
		NetworkCommunication.connected = 0;
		if (network_solution == 0) {
			mWifiCom.closeConnection();
		} else if (network_solution == 1) {
			if (NearbyCommunication.deviceRole == NearbyCommunication.DISCOVERER_ROLE) {
				mNearbyCom.closeNearbyConnection();
			} else {
				mWifiCom.closeConnection();
			}
		}
	}

	public void closeOnlyWifiConnection() {
		mWifiCom.closeConnection();
	}

	public void sendDataToClient(byte[] data) {
		if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
			mNearbyCom.sendBytes(data);
		}
	}

	public void launchMultChoiceQuestionActivity(QuestionMultipleChoice question_to_display, String directCorrection) {
		Intent mIntent = new Intent(mContextNetCom, MultChoiceQuestionActivity.class);
		Bundle bun = new Bundle();
		bun.putString("question", question_to_display.getQuestion());
		bun.putString("opt0", question_to_display.getOpt0());
		bun.putString("opt1", question_to_display.getOpt1());
		bun.putString("opt2", question_to_display.getOpt2());
		bun.putString("opt3", question_to_display.getOpt3());
		bun.putString("opt4", question_to_display.getOpt4());
		bun.putString("opt5", question_to_display.getOpt5());
		bun.putString("opt6", question_to_display.getOpt6());
		bun.putString("opt7", question_to_display.getOpt7());
		bun.putString("opt8", question_to_display.getOpt8());
		bun.putString("opt9", question_to_display.getOpt9());
		bun.putString("id", question_to_display.getId());
		bun.putString("image_name", question_to_display.getImage());
		bun.putString("directCorrection", directCorrection);
		bun.putInt("nbCorrectAnswers", question_to_display.getNB_CORRECT_ANS());
		bun.putInt("timerSeconds", question_to_display.getTimerSeconds());
		mIntent.putExtras(bun);
		mContextNetCom.startActivity(mIntent);
	}

	public void launchShortAnswerQuestionActivity(QuestionShortAnswer question_to_display, String directCorrection) {
		Intent mIntent = new Intent(mContextNetCom, ShortAnswerQuestionActivity.class);
		Bundle bun = new Bundle();
		bun.putString("question", question_to_display.getQuestion());
		bun.putString("id", question_to_display.getId());
		bun.putString("image_name", question_to_display.getImage());
		bun.putString("directCorrection", directCorrection);
		bun.putInt("timerSeconds", question_to_display.getTimerSeconds());
		mIntent.putExtras(bun);
		mContextNetCom.startActivity(mIntent);
	}

	public void launchTestActivity(Long testID, String directCorrection) {
		Intent mIntent = new Intent(mContextNetCom, TestActivity.class);
		Bundle bun = new Bundle();
		bun.putLong("testID", testID);
		bun.putString("directCorrection", directCorrection);
		mIntent.putExtras(bun);
		mContextNetCom.startActivity(mIntent);
	}

	public String getLastAnswer() {
		return lastAnswer;
	}

	public NearbyCommunication getmNearbyCom() {
		return mNearbyCom;
	}

	public HotspotServer getHotspotServerHotspot() {
		return hotspotServerHotspot;
	}

	public void setHotspotServerHotspot(HotspotServer hotspotServerHotspot) {
		this.hotspotServerHotspot = hotspotServerHotspot;
	}
}
