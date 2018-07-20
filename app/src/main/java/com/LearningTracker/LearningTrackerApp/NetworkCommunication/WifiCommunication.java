package com.LearningTracker.LearningTrackerApp.NetworkCommunication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import com.LearningTracker.LearningTrackerApp.Activities.CorrectedQuestionActivity;
import com.LearningTracker.LearningTrackerApp.Activities.MultChoiceQuestionActivity;
import com.LearningTracker.LearningTrackerApp.Activities.ShortAnswerQuestionActivity;
import com.LearningTracker.LearningTrackerApp.Activities.TestActivity;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.Test;
import com.LearningTracker.LearningTrackerApp.Tools.StringTools;
import com.LearningTracker.LearningTrackerApp.database_management.DbHelper;
import com.LearningTracker.LearningTrackerApp.LTApplication;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableIndividualQuestionForResult;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableLearningObjective;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableRelationQuestionQuestion;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableRelationTestObjective;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableTest;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

public class WifiCommunication {
	public Integer connectionSuccess = 0;
	private WifiManager mWifi;
	private Context mContextWifCom;
	private Application mApplication;
	private OutputStream mOutputStream = null;
	private InputStream mInputStream = null;
	private Vector<OutputStream> outputStreamVector = null;
	private Vector<InputStream> inputStreamVector = null;
	private int current = 0;
	private int bytes_read = 0;
	private String ip_address = "192.168.1.100";
	private String lastAnswer = "";
	final private int PORTNUMBER = 9090;
	public String directCorrection = "0";
	List<android.net.wifi.ScanResult> mScanResults = new ArrayList<android.net.wifi.ScanResult>();
	BroadcastReceiver scanningreceiver;
	private TextView logView = null;

	public WifiCommunication(Context arg_context, Application arg_application, TextView logView) {
		if (android.os.Build.VERSION.SDK_INT > 9)
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		mApplication = arg_application;
		((LTApplication) mApplication).setAppWifi(this);
		mContextWifCom = arg_context;
		mWifi = (WifiManager) mContextWifCom.getSystemService(Context.WIFI_SERVICE);
		final DbHelper db = new DbHelper(arg_context);
		ip_address = db.getMaster();
		this.logView = logView;
	}


	public void connectToServer(String connectionString) {
		try {
			Log.v("connectToServer", "beginning");
			Socket s = new Socket(ip_address,PORTNUMBER);
			connectionSuccess = 1;
			//outgoing stream redirect to socket
			mOutputStream = s.getOutputStream();
			mInputStream = s.getInputStream();

			byte[] conBuffer = connectionString.getBytes();
			try {
				mOutputStream.write(conBuffer, 0, conBuffer.length);
				mOutputStream.flush();
			} catch (IOException e) {
				String msg = "In connectToServer() and an exception occurred during write: " + e.getMessage();
				Log.e("Fatal Error", msg);
			}

			listenForQuestions();



		} catch (UnknownHostException e) {
			Log.v("connection to server", ": failure, unknown host");

			// TODO Auto-generated catch block
			connectionSuccess = -1;
			e.printStackTrace();
		} catch (IOException e) {
			Log.v("connection to server", ": failure, i/o exception");

			// TODO Auto-generated catch block
			connectionSuccess = -1;
			e.printStackTrace();
		}
	}

	public void sendAnswerToServer(String answer) {
		if (answer.split("///").length > 3) {
			lastAnswer = answer.split("///")[3]; //save the answer for when we receive the evaluation from the server
		}
		byte[] ansBuffer = answer.getBytes();
		try {
			mOutputStream.write(ansBuffer, 0, ansBuffer.length);
			Log.d("answer buffer length: ", String.valueOf(ansBuffer.length));
			mOutputStream.flush();
		} catch (IOException e) {
			String msg = "In sendAnswerToServer() and an exception occurred during write: " + e.getMessage();
			Log.e("Fatal Error", msg);
		}
		answer = "";
	}

	public void sendDisconnectionSignal (String signal) {
		byte[] sigBuffer = signal.getBytes();
		try {
			if (mOutputStream != null) {
				mOutputStream.write(sigBuffer, 0, sigBuffer.length);
				mOutputStream.flush();
			} else {
				Log.v("disconnection: ", "tries to send signal to null output stream");
			}
		} catch (IOException e) {
			String msg = "In sendDisconnectionSignal() and an exception occurred during write: " + e.getMessage();
			Log.e("Fatal Error", msg);
		}
	}

	public void listenForQuestions() {
		final WifiCommunication selfWifiCommunication = this;
		new Thread(new Runnable() {
			public void run() {
				Boolean able_to_read = true;
				while (able_to_read && mInputStream != null) {
					current = 0;
					byte[] prefix_buffer = new byte[80];
					String sizes = "";
					String byteread = "";
					try {
						bytes_read = mInputStream.read(prefix_buffer, 0, 80);
						if (bytes_read < 0) {
							able_to_read = false;
						}
						sizes = new String(prefix_buffer, "UTF-8");
					} catch (IOException e) {
						e.printStackTrace();
						able_to_read = false;
					}
					Log.v("WifiCommunication", "received string: " + sizes);
					if (sizes.split("///")[0].split(":")[0].contentEquals("MULTQ")) {
						int size_of_image = Integer.parseInt(sizes.split(":")[1]);
						int size_of_text = Integer.parseInt(sizes.split(":")[2].replaceAll("\\D+", ""));
						byte[] whole_question_buffer = new byte[80 + size_of_image + size_of_text];
						for (int i = 0; i < 80; i++) {
							whole_question_buffer[i] = prefix_buffer[i];
						}
						current = 80;
						do {
							try {
								//Log.v("read input stream", "second");

								bytes_read = mInputStream.read(whole_question_buffer, current, (80 + size_of_image + size_of_text - current));
								Log.v("number of bytes read:", Integer.toString(bytes_read));
							} catch (IOException e) {
								e.printStackTrace();
								able_to_read = false;
							}
							if (bytes_read >= 0) {
								current += bytes_read;
								if (able_to_read == false) {
									bytes_read = -1;
									able_to_read = true;
								}
							}
						}
						while (bytes_read > 0);    //shall be sizeRead > -1, because .read returns -1 when finished reading, but outstream not closed on server side
						bytes_read = 1;
						DataConversion convert_question = new DataConversion(mContextWifCom);
						QuestionMultipleChoice multquestion_to_save = convert_question.bytearrayvectorToMultChoiceQuestion(whole_question_buffer);
						try {
							DbTableQuestionMultipleChoice.addMultipleChoiceQuestion(multquestion_to_save);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (sizes.split("///")[0].split(":")[0].contentEquals("SHRTA")) {
						int size_of_image = Integer.parseInt(sizes.split(":")[1]);
						int size_of_text = Integer.parseInt(sizes.split(":")[2].replaceAll("\\D+", ""));
						byte[] whole_question_buffer = new byte[80 + size_of_image + size_of_text];
						for (int i = 0; i < 80; i++) {
							whole_question_buffer[i] = prefix_buffer[i];
						}
						current = 80;
						do {
							try {
								//Log.v("read input stream", "second");

								bytes_read = mInputStream.read(whole_question_buffer, current, (80 + size_of_image + size_of_text - current));
								Log.v("WifiCommunication", "number of bytes read:" + Integer.toString(bytes_read));
//                                    for (int k = 0; k < 80 && current > 80; k++) {
//                                        byteread += whole_question_buffer[current -21 + k];
//                                    }
//                                    Log.v("last bytes read: ", byteread);
//                                    byteread = "";
							} catch (IOException e) {
								e.printStackTrace();
								able_to_read = false;
							}
							if (bytes_read >= 0) {
								current += bytes_read;
								if (able_to_read == false) {
									bytes_read = -1;
									able_to_read = true;
								}
							}
						} while (bytes_read > 0);    //shall be sizeRead > -1, because .read returns -1 when finished reading, but outstream not closed on server side
						bytes_read = 1;
						DataConversion convert_question = new DataConversion(mContextWifCom);
							QuestionShortAnswer shrtquestion_to_save = convert_question.bytearrayvectorToShortAnswerQuestion(whole_question_buffer);
						try {
							DbTableQuestionShortAnswer.addShortAnswerQuestion(shrtquestion_to_save);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (sizes.split("///")[0].split(":")[0].contentEquals("QID")) {
						if (sizes.split(":")[1].contains("MLT")) {
							String id_global = sizes.split("///")[1];
							if (Long.valueOf(id_global) < 0 ) {
								//setup test and show it
								Long testId = -(Long.valueOf(sizes.split("///")[1]));
								directCorrection = sizes.split("///")[2];
								launchTestActivity(testId, directCorrection);
								LTApplication.shrtaqActivityState = null;
								LTApplication.qmcActivityState = null;
							} else {
								QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(id_global);
								if (questionMultipleChoice.getQUESTION().length() > 0) {
									questionMultipleChoice.setID(id_global);
									directCorrection = sizes.split("///")[2];
									launchMultChoiceQuestionActivity(questionMultipleChoice, directCorrection);
									LTApplication.shrtaqActivityState = null;
									LTApplication.currentTestActivitySingleton = null;
								} else {
									QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(id_global);
									questionShortAnswer.setID(id_global);
									directCorrection = sizes.split("///")[2];
									launchShortAnswerQuestionActivity(questionShortAnswer, directCorrection);
									LTApplication.qmcActivityState = null;
									LTApplication.currentTestActivitySingleton = null;
								}
							}
						}
					} else if (sizes.split("///")[0].split(":")[0].contentEquals("EVAL")) {
						DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(sizes.split("///")[2],sizes.split("///")[1], lastAnswer);
					} else if (sizes.split("///")[0].split(":")[0].contentEquals("UPDEV")) {
						DbTableIndividualQuestionForResult.setEvalForQuestion(Double.valueOf(sizes.split("///")[1]),sizes.split("///")[2]);
					} else if (sizes.split("///")[0].split(":")[0].contentEquals("CORR")) {
						Intent mIntent = new Intent(mContextWifCom, CorrectedQuestionActivity.class);
						Bundle bun = new Bundle();
						bun.putString("questionID", sizes.split("///")[1]);
						mIntent.putExtras(bun);
						mContextWifCom.startActivity(mIntent);
					} else if (sizes.split("///")[0].split(":")[0].contentEquals("TEST")) {
						//2000001///test1///2000005;;;2000006:::EVALUATION<60|||2000006;;;2000007:::EVALUATION<60|||2000007|||///objectives///TESTMODE///
						//first, fetch the size we'll have to read
						Integer textBytesSize = 0;
						try {
							textBytesSize = Integer.valueOf(sizes.split("///")[0].split(":")[1]);
						} catch (NumberFormatException e) {
							textBytesSize = 0;
							Log.e("WifiCommunication", "error reading size when receiving TEST");
						}

						byte[] wholeDataBuffer = new byte[textBytesSize];
						current = 0;
						do {
							try {
								//Log.v("read input stream", "second");

								bytes_read = mInputStream.read(wholeDataBuffer, current, (textBytesSize - current));
								Log.v("WifiCommunication", "number of bytes read:" + Integer.toString(bytes_read));
							} catch (IOException e) {
								e.printStackTrace();
								able_to_read = false;
							}
							if (bytes_read >= 0) {
								current += bytes_read;
								if (able_to_read == false) {
									bytes_read = -1;
									able_to_read = true;
								}
							}
						} while (bytes_read > 0);
						bytes_read = 1;

						//chop the first bytes
						//byte[] testDataBuffer = Arrays.copyOfRange(wholeDataBuffer, 80, wholeDataBuffer.length);
						String testString = "";
						try {
							testString = new String(wholeDataBuffer, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}

						Test newTest = new Test();
						newTest.setIdGlobal(Long.valueOf(testString.split("///")[0]));
						newTest.setTestName(testString.split("///")[1]);

						//read objectives
						try {
							String[] objectives = testString.split("///")[3].split("\\|\\|\\|");
							for (String objective : objectives) {
								DbTableRelationTestObjective.insertRelationTestObjective(String.valueOf(newTest.getIdGlobal()), objective);
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							Log.e("WifiCommunication", "ArrayOutOfBound when parsing objectives from: " + testString);
							e.printStackTrace();
						}

						String[] questionRelation = testString.split("///")[2].split("\\|\\|\\|");
						for (String relation : questionRelation) {
							String[] relationSplit = relation.split(";;;");
							String questionId = relationSplit[0];
							newTest.getQuestionsIDs().add(questionId);
							for (int i = 1; i < relationSplit.length; i++) {
								try {
								    String[] array = relationSplit[i].split(":::");
									DbTableRelationQuestionQuestion.insertRelationQuestionQuestion(StringTools.stringToLongID(questionId),
											StringTools.stringToLongID(array[0]), newTest.getTestName(),
											array[1]);
								} catch (ArrayIndexOutOfBoundsException e) {
									//ERROR HERE
								    Log.e("WifiCommunication", "Array out of bound when inserting the condition for insertRelationQuestionQuestion");
									e.printStackTrace();
								}
							}
						}
						DbTableTest.insertTest(newTest);
					} else if (sizes.split(":")[0].contentEquals("OEVAL")) {
						if (sizes.split(":").length > 1) {
							//Read text data into array
							Integer textBytesSize = 0;
							try {
								textBytesSize = Integer.valueOf(sizes.split("///")[0].split(":")[1]);
							} catch (NumberFormatException e) {
								textBytesSize = 0;
								Log.e("WifiCommunication", "error reading size when receiving OEVAL");
							}

							byte[] wholeDataBuffer = new byte[textBytesSize];
							current = 0;
							do {
								try {
									bytes_read = mInputStream.read(wholeDataBuffer, current, (textBytesSize - current));
									Log.v("WifiCommunication", "number of bytes read:" + Integer.toString(bytes_read));
								} catch (IOException e) {
									e.printStackTrace();
									able_to_read = false;
								}
								if (bytes_read >= 0) {
									current += bytes_read;
									if (able_to_read == false) {
										bytes_read = -1;
										able_to_read = true;
									}
								}
							} while (bytes_read > 0);
							bytes_read = 1;

							//Convert data to string
							String testString = "";
							try {
								testString = new String(wholeDataBuffer, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}

							if (testString.split("///").length > 4) {
								String testID = testString.split("///")[0];
								String testName = testString.split("///")[1];
								String objectiveID = testString.split("///")[2];
								String objective = testString.split("///")[3];
								String evaluation = testString.split("///")[4];

								DbTableLearningObjective.addLearningObjective(objectiveID, objective, 0);
								DbTableRelationTestObjective.insertRelationTestObjective(testID, objectiveID);
								Test certificativeTest = new Test();
								certificativeTest.setTestName(testName);
								certificativeTest.setTestType("CERTIF");
								certificativeTest.setIdGlobal(Long.getLong(testID));
								DbTableTest.insertTest(certificativeTest);
								DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(objectiveID, evaluation, 2, testName);
								Log.d("INFO","received OEVAL");
							}
						}
					}
				}
			}
		}).start();
	}

	public void launchMultChoiceQuestionActivity(QuestionMultipleChoice question_to_display, String directCorrection) {
		Intent mIntent = new Intent(mContextWifCom, MultChoiceQuestionActivity.class);
		Bundle bun = new Bundle();
		bun.putString("question", question_to_display.getQUESTION());
		bun.putString("opt0", question_to_display.getOPT0());
		bun.putString("opt1", question_to_display.getOPT1());
		bun.putString("opt2", question_to_display.getOPT2());
		bun.putString("opt3", question_to_display.getOPT3());
		bun.putString("opt4", question_to_display.getOPT4());
		bun.putString("opt5", question_to_display.getOPT5());
		bun.putString("opt6", question_to_display.getOPT6());
                bun.putString("opt7", question_to_display.getOPT7());
		bun.putString("opt8", question_to_display.getOPT8());
		bun.putString("opt9", question_to_display.getOPT9());
		bun.putString("id", question_to_display.getID());
		bun.putString("image_name", question_to_display.getIMAGE());
		bun.putString("directCorrection", directCorrection);
		bun.putInt("nbCorrectAnswers",question_to_display.getNB_CORRECT_ANS());
		mIntent.putExtras(bun);
		mContextWifCom.startActivity(mIntent);
	}

	public void launchShortAnswerQuestionActivity(QuestionShortAnswer question_to_display, String directCorrection) {
		Intent mIntent = new Intent(mContextWifCom, ShortAnswerQuestionActivity.class);
		Bundle bun = new Bundle();
		bun.putString("question", question_to_display.getQUESTION());
		bun.putString("id", question_to_display.getID());
		bun.putString("image_name", question_to_display.getIMAGE());
		bun.putString("directCorrection", directCorrection);
		mIntent.putExtras(bun);
		mContextWifCom.startActivity(mIntent);
	}

	public void launchTestActivity(Long testID, String directCorrection) {
		Intent mIntent = new Intent(mContextWifCom, TestActivity.class);
		Bundle bun = new Bundle();
		bun.putLong("testID", testID);
		bun.putString("directCorrection", directCorrection);
		mIntent.putExtras(bun);
		mContextWifCom.startActivity(mIntent);
	}

	/**
	 * WORK IN PROGRESS
	 * method used to scan the wifi network to which the smartphone is connected
	 * and try to connect to the "interesting" ip addresses (e.g. 192.168.x.xx, 127.0.x.x)
	 */
	public void scanAndConnectToIP() {
		try {
			Enumeration nis = NetworkInterface.getNetworkInterfaces();
			while(nis.hasMoreElements())
			{
				NetworkInterface ni = (NetworkInterface) nis.nextElement();
				Enumeration ias = ni.getInetAddresses();
				while (ias.hasMoreElements())
				{
					InetAddress ia = (InetAddress) ias.nextElement();
					Log.d("address found:",ia.getHostAddress());
				}

			}
		} catch (SocketException ex) {
		}
	}
}