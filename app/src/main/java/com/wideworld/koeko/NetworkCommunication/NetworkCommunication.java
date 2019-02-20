package com.wideworld.koeko.NetworkCommunication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import android.app.Application;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.common.GoogleApiAvailability;
import com.wideworld.koeko.Activities.GameActivity;
import com.wideworld.koeko.Activities.InteractiveModeActivity;
import com.wideworld.koeko.Activities.MultChoiceQuestionFragment;
import com.wideworld.koeko.Activities.ShortAnswerQuestionFragment;
import com.wideworld.koeko.Activities.TestActivity;
import com.wideworld.koeko.NetworkCommunication.HotspotServer.HotspotServer;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.Answer;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.ClientToServerTransferable;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.CtoSPrefix;
import com.wideworld.koeko.QuestionsManagement.GameView;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.R;
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
		new Thread(() -> {
            //TODO: put a WifiLock
            mWifiCom.connectToServer(getConnectionBytes(), uniqueId, reconnection);
        }).start();
	}

	public byte[] getConnectionBytes() {
		String uniqueId = NetworkCommunication.deviceIdentifier;
		String name = DbTableSettings.getName();

		String deviceInfos = "";
		deviceInfos += "A:" + android.os.Build.VERSION.SDK_INT + ":";
		if (mApplication.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			deviceInfos += "BLE:";
		} else {
			deviceInfos += "NOB:";
		}
		try {
			deviceInfos += mApplication.getPackageManager().getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0 ).versionCode;
			deviceInfos += ":";
		} catch (PackageManager.NameNotFoundException e) {
			System.out.println("PackageManager.NameNotFoundException: substituting GOOGLE_PLAY_SERVICES_PACKAGE version number by 0");
			deviceInfos += "0:";
		}
		deviceInfos += DbTableSettings.getHotspotAvailable() + ":";
		deviceInfos += Build.MODEL + ":";

		ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.connectionPrefix);
		LinkedHashMap<String, String> dictionary = new LinkedHashMap<>();
		dictionary.put("uuid", uniqueId);
		dictionary.put("name", name);
		dictionary.put("deviceInfos", deviceInfos);
		String dictionaryJson = "";
		try {
			dictionaryJson = ReceptionProtocol.getObjectMapper().writeValueAsString(dictionary);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		transferable.setFileBytes(dictionaryJson.getBytes());

		return transferable.getTransferableBytes();
	}

	public void sendAnswerToServer(ArrayList<String> answers, String lastAnswerArg, String question, String id, String answerType, Long elapsedTime) {
		String uuid = NetworkCommunication.deviceIdentifier;
		String name = DbTableSettings.getName();

		Answer answer = new Answer();
		answer.setStudentDeviceId(uuid);
		answer.setStudentName(name);
		answer.setQuestionId(id);
		answer.setQuestion(question);
		answer.setQuestionType(answerType);
		answer.setAnswers(answers);
		answer.setTimeSpent(elapsedTime.intValue());
		answer.setQuestionType(answerType);
		String answerTransferable = "";
		try {
			answerTransferable = ReceptionProtocol.getObjectMapper().writeValueAsString(answer);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.answerPrefix);
		transferable.setFileBytes(answerTransferable.getBytes());
		lastAnswer = lastAnswerArg; //save the answer for when we receive the evaluation from the server
		if (network_solution == 0) {
			mWifiCom.sendBytes(transferable.getTransferableBytes());
		} else if (network_solution == 1) {
			if (NearbyCommunication.deviceRole == NearbyCommunication.DISCOVERER_ROLE) {
				mNearbyCom.sendBytes(transferable.getTransferableBytes());
			} else {
				mWifiCom.sendBytes(transferable.getTransferableBytes());
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

	public void sendBytesToServer(byte[] data) {
		if (network_solution == 0) {
			mWifiCom.sendBytes(data);
		} else if (network_solution == 1) {
			if (NearbyCommunication.deviceRole == NearbyCommunication.DISCOVERER_ROLE) {
				mNearbyCom.sendBytes(data);
			} else {
				mWifiCom.sendBytes(data);
			}
		}
	}

	public void sendDisconnectionSignal(String additionalInformation) {
		KeyguardManager myKM = (KeyguardManager) mContextNetCom.getSystemService(Context.KEYGUARD_SERVICE);
		if( myKM.inKeyguardRestrictedInputMode()) {
			System.out.println("Device is locked: sending locked signal");
			ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.disconnectionPrefix);
			transferable.setOptionalArgument1(NetworkCommunication.deviceIdentifier);
			transferable.setOptionalArgument2("locked");
			transferable.setFileBytes(DbTableSettings.getName().getBytes());
			sendBytesToServer(transferable.getTransferableBytes());
		} else {
			System.out.println("Send disconnection signal");
			ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.disconnectionPrefix);
			transferable.setOptionalArgument1(NetworkCommunication.deviceIdentifier);
			transferable.setOptionalArgument2(additionalInformation);
			transferable.setFileBytes(DbTableSettings.getName().getBytes());
			sendBytesToServer(transferable.getTransferableBytes());
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
		Koeko.MAX_ACTIVITY_TRANSITION_TIME_MS = Koeko.MEDIUM_TRANSITION_TIME;
		Intent mIntent = new Intent(mContextNetCom, MultChoiceQuestionFragment.class);
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

		launchFragment(new MultChoiceQuestionFragment(), mIntent);
	}

	public void launchShortAnswerQuestionActivity(QuestionShortAnswer question_to_display, String directCorrection) {
		Koeko.MAX_ACTIVITY_TRANSITION_TIME_MS = Koeko.MEDIUM_TRANSITION_TIME;
		Intent mIntent = new Intent(mContextNetCom, ShortAnswerQuestionFragment.class);
		Bundle bun = new Bundle();
		bun.putString("question", question_to_display.getQuestion());
		bun.putString("id", question_to_display.getId());
		bun.putString("image_name", question_to_display.getImage());
		bun.putString("directCorrection", directCorrection);
		bun.putInt("timerSeconds", question_to_display.getTimerSeconds());
		mIntent.putExtras(bun);
		launchFragment(new ShortAnswerQuestionFragment(), mIntent);
	}

	public void launchTestActivity(Long testID, String directCorrection) {
		Koeko.MAX_ACTIVITY_TRANSITION_TIME_MS = Koeko.MEDIUM_TRANSITION_TIME;
		Intent mIntent = new Intent(mContextNetCom, TestActivity.class);
		Bundle bun = new Bundle();
		bun.putLong("testID", testID);
		bun.putString("directCorrection", directCorrection);
		mIntent.putExtras(bun);
		mContextNetCom.startActivity(mIntent);
	}

	public void launchGameActivity(GameView gameView, int team) {
		Koeko.MAX_ACTIVITY_TRANSITION_TIME_MS = Koeko.MEDIUM_TRANSITION_TIME;
		Intent mIntent = new Intent(mContextNetCom, GameActivity.class);
		Bundle bun = new Bundle();
		bun.putInt("endScore", gameView.getEndScore());
		bun.putInt("gameType", gameView.getGameType());
		bun.putInt("team", team);
		mIntent.putExtras(bun);
		mContextNetCom.startActivity(mIntent);
	}

	private void launchFragment(Fragment fragment, Intent mIntent) {
		FragmentManager fragmentManager = mInteractiveModeActivity.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragment.setArguments(mIntent.getExtras());
		fragmentTransaction.replace(R.id.viewgroup, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
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
