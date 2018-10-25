package com.wideworld.koeko.NetworkCommunication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.wideworld.koeko.Activities.CorrectedQuestionActivity;
import com.wideworld.koeko.Activities.MultChoiceQuestionActivity;
import com.wideworld.koeko.Activities.ShortAnswerQuestionActivity;
import com.wideworld.koeko.Activities.TestActivity;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.Test;
import com.wideworld.koeko.Tools.FileHandler;
import com.wideworld.koeko.Tools.StringTools;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableIndividualQuestionForResult;
import com.wideworld.koeko.database_management.DbTableLearningObjective;
import com.wideworld.koeko.database_management.DbTableQuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableQuestionShortAnswer;
import com.wideworld.koeko.database_management.DbTableRelationQuestionQuestion;
import com.wideworld.koeko.database_management.DbTableRelationTestObjective;
import com.wideworld.koeko.database_management.DbTableSettings;
import com.wideworld.koeko.database_management.DbTableTest;
import com.google.android.gms.common.util.ArrayUtils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

public class WifiCommunication {
    final private int PORTNUMBER = 9090;
    public Integer connectionSuccess = 0;
    public String directCorrection = "0";
    List<android.net.wifi.ScanResult> mScanResults = new ArrayList<android.net.wifi.ScanResult>();
    BroadcastReceiver scanningreceiver;
    private WifiManager mWifi;
    private Context mContextWifCom;
    private Application mApplication;
    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;
    private int current = 0;
    private int bytes_read = 0;
    private String ip_address = "no IP";
    private String lastAnswer = "";
    private TextView logView = null;
    private DatagramSocket socket;
    public NetworkCommunication mNetworkCommunication;

    private String TAG = "WifiCommunication";

    public WifiCommunication(Context arg_context, Application arg_application, TextView logView, NetworkCommunication networkCommunication) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mApplication = arg_application;
        ((Koeko) mApplication).setAppWifi(this);
        mContextWifCom = arg_context;
        mWifi = (WifiManager) mContextWifCom.getSystemService(Context.WIFI_SERVICE);
        mNetworkCommunication = networkCommunication;
        this.logView = logView;
    }


    public void connectToServer(String connectionString, String deviceIdentifier) {
        try {
            //Automatic connection
            Integer automaticConnection = DbTableSettings.getAutomaticConnection();
            if (automaticConnection == 1) {
                listenForIPThroughUDP();
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!ip_address.contentEquals("no IP")) {
                        break;
                    }
                }

                if (ip_address.contentEquals("no IP")) {
                    ip_address = DbTableSettings.getMaster();
                    connectionSuccess = -2;
                }
            } else {
                ip_address = DbTableSettings.getMaster();
            }

            Log.v("connectToServer", "beginning");
            Socket s = new Socket(ip_address, PORTNUMBER);
            connectionSuccess = 1;
            //outgoing stream redirect to socket
            mOutputStream = s.getOutputStream();
            mInputStream = s.getInputStream();

            NetworkCommunication.connected = true;

            byte[] conBuffer = connectionString.getBytes();
            try {
                mOutputStream.write(conBuffer, 0, conBuffer.length);
                mOutputStream.flush();
            } catch (IOException e) {
                String msg = "In connectToServer() and an exception occurred during write: " + e.getMessage();
                Log.e("Fatal Error", msg);
            }

            //send resource ids present on the device
            String idsOnDevice = DbTableQuestionMultipleChoice.getAllQuestionMultipleChoiceIds() + "|" +
                    DbTableQuestionShortAnswer.getAllShortAnswerIds() + "|"
                    + FileHandler.getMediaFilesList(mContextWifCom);
            String[] arrayIds = idsOnDevice.split("\\|");
            String stringToSend = "RESIDS///" + deviceIdentifier + "///";
            for (int i = 0; i < arrayIds.length; i++) {
                if (arrayIds[i].length() > 0) {
                    stringToSend += arrayIds[i] + "|";
                    if (stringToSend.getBytes().length >= 900) {
                        stringToSend += "///";
                        sendStringToServer(stringToSend);
                        stringToSend = "RESIDS///" + deviceIdentifier + "///";
                    }
                }
            }
            sendStringToServer(stringToSend + "///ENDTRSM///");


            listenForQuestions();


        } catch (UnknownHostException e) {
            Log.v("connection to server", ": failure, unknown host");

            if (connectionSuccess != -2) {
                connectionSuccess = -1;
            }
            e.printStackTrace();
        } catch (IOException e) {
            Log.v("connection to server", ": failure, i/o exception");

            if (connectionSuccess != -2) {
                connectionSuccess = -1;
            }
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

    public void sendStringToServer(String message) {
        byte[] sigBuffer = message.getBytes();
        try {
            if (mOutputStream != null) {
                mOutputStream.write(sigBuffer, 0, sigBuffer.length);
                mOutputStream.flush();
            } else {
                Log.v("SendStringToServer", "tries to send signal to null output stream");
            }
        } catch (IOException e) {
            String msg = "In sendStringToServer() and an IOexception occurred during write: " + e.getMessage();
            Log.e("Fatal Error", msg);
        }
    }

    public void listenForQuestions() {
        final WifiCommunication selfWifiCommunication = this;
        new Thread(new Runnable() {
            public void run() {
                try {
                    Boolean able_to_read = true;
                    while (able_to_read && mInputStream != null) {
                        current = 0;
                        byte[] prefix_buffer = readDataIntoArray(80, able_to_read);
                        String sizesPrefix = null;
                        sizesPrefix = new String(prefix_buffer, "UTF-8");
                        Log.v("WifiCommunication", "received string: " + sizesPrefix);
                        if (sizesPrefix.split("///")[0].split(":")[0].contentEquals("MULTQ")) {
                            //read question data
                            int size_of_image = Integer.parseInt(sizesPrefix.split(":")[1]);
                            int size_of_text = Integer.parseInt(sizesPrefix.split(":")[2].replaceAll("\\D+", ""));
                            byte[] question_buffer = readDataIntoArray(size_of_image + size_of_text, able_to_read);
                            byte[] whole_question_buffer = ArrayUtils.concatByteArrays(prefix_buffer, question_buffer);

                            //Convert data and save question
                            DataConversion convert_question = new DataConversion(mContextWifCom);
                            QuestionMultipleChoice multquestion_to_save = convert_question.bytearrayvectorToMultChoiceQuestion(whole_question_buffer);
                            DbTableQuestionMultipleChoice.addMultipleChoiceQuestion(multquestion_to_save);

                            sendStringToServer("OK///" + multquestion_to_save.getID() + "///");

                            if (multquestion_to_save.getQUESTION().contains("7492qJfzdDSB")) {
                                sendStringToServer("ACCUSERECEPTION");
                            }
                        } else if (sizesPrefix.split("///")[0].split(":")[0].contentEquals("SHRTA")) {
                            //read question data
                            int size_of_image = Integer.parseInt(sizesPrefix.split(":")[1]);
                            int size_of_text = Integer.parseInt(sizesPrefix.split(":")[2].replaceAll("\\D+", ""));
                            byte[] question_buffer = readDataIntoArray(size_of_image + size_of_text, able_to_read);
                            byte[] whole_question_buffer = ArrayUtils.concatByteArrays(prefix_buffer, question_buffer);

                            DataConversion convert_question = new DataConversion(mContextWifCom);
                            QuestionShortAnswer shrtquestion_to_save = convert_question.bytearrayvectorToShortAnswerQuestion(whole_question_buffer);
                            DbTableQuestionShortAnswer.addShortAnswerQuestion(shrtquestion_to_save);

                            sendStringToServer("OK///" + shrtquestion_to_save.getID() + "///");
                        } else if (sizesPrefix.split("///")[0].split(":")[0].contentEquals("QID")) {
                            if (sizesPrefix.split(":")[1].contains("MLT")) {
                                String id_global = sizesPrefix.split("///")[1];

                                //reinitializing all types of displays
                                Koeko.currentTestActivitySingleton = null;
                                Koeko.shrtaqActivityState = null;
                                Koeko.qmcActivityState = null;

                                if (Long.valueOf(id_global) < 0) {
                                    //setup test and show it
                                    Long testId = -(Long.valueOf(sizesPrefix.split("///")[1]));
                                    directCorrection = sizesPrefix.split("///")[2];
                                    launchTestActivity(testId, directCorrection);
                                    Koeko.shrtaqActivityState = null;
                                    Koeko.qmcActivityState = null;
                                } else {
                                    QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(id_global);
                                    if (questionMultipleChoice.getQUESTION().length() > 0) {
                                        questionMultipleChoice.setID(id_global);
                                        directCorrection = sizesPrefix.split("///")[2];
                                        launchMultChoiceQuestionActivity(questionMultipleChoice, directCorrection);
                                        Koeko.shrtaqActivityState = null;
                                        Koeko.currentTestActivitySingleton = null;
                                    } else {
                                        QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(id_global);
                                        questionShortAnswer.setID(id_global);
                                        directCorrection = sizesPrefix.split("///")[2];
                                        launchShortAnswerQuestionActivity(questionShortAnswer, directCorrection);
                                        Koeko.qmcActivityState = null;
                                        Koeko.currentTestActivitySingleton = null;
                                    }
                                }
                            }
                        } else if (sizesPrefix.split("///")[0].split(":")[0].contentEquals("EVAL")) {
                            DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(sizesPrefix.split("///")[2], sizesPrefix.split("///")[1], lastAnswer);
                        } else if (sizesPrefix.split("///")[0].split(":")[0].contentEquals("UPDEV")) {
                            DbTableIndividualQuestionForResult.setEvalForQuestion(Double.valueOf(sizesPrefix.split("///")[1]), sizesPrefix.split("///")[2]);
                        } else if (sizesPrefix.split("///")[0].split(":")[0].contentEquals("CORR")) {
                            Intent mIntent = new Intent(mContextWifCom, CorrectedQuestionActivity.class);
                            Bundle bun = new Bundle();
                            bun.putString("questionID", sizesPrefix.split("///")[1]);
                            mIntent.putExtras(bun);
                            mContextWifCom.startActivity(mIntent);
                        } else if (sizesPrefix.split("///")[0].split(":")[0].contentEquals("TEST")) {
                            //2000001///test1///2000005;;;2000006:::EVALUATION<60|||2000006;;;2000007:::EVALUATION<60|||2000007|||///objectives///TESTMODE///
                            //first, fetch the size we'll have to read
                            Integer textBytesSize = 0;
                            textBytesSize = Integer.valueOf(sizesPrefix.split("///")[0].split(":")[1]);

                            byte[] wholeDataBuffer = readDataIntoArray(textBytesSize, able_to_read);

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
                            newTest.setMedalsInstructionsString(testString.split("///")[5]);
                            if (testString.split("///").length > 6) {
                                newTest.setMediaFileName(testString.split("///")[6]);
                            }
                            DbTableTest.insertTest(newTest);
                        } else if (sizesPrefix.split(":")[0].contentEquals("OEVAL")) {
                            if (sizesPrefix.split(":").length > 1) {
                                //Read text data into array
                                Integer textBytesSize = 0;
                                textBytesSize = Integer.valueOf(sizesPrefix.split("///")[0].split(":")[1]);

                                byte[] wholeDataBuffer = readDataIntoArray(textBytesSize, able_to_read);

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
                                    Log.d("INFO", "received OEVAL");
                                }
                            }
                        } else if (sizesPrefix.split("///")[0].contentEquals("FILE")) {
                            if (sizesPrefix.split("///").length >= 3) {
                                try {
                                    int dataSize = Integer.valueOf(sizesPrefix.split("///")[2]);
                                    byte[] dataBuffer = readDataIntoArray(dataSize, able_to_read);

                                    //check if we got all the data
                                    if (dataSize == dataBuffer.length) {
                                        FileHandler.saveMediaFile(dataBuffer, sizesPrefix.split("///")[1], mContextWifCom);
                                        sendStringToServer("OK///" + sizesPrefix.split("///")[1] + "///");
                                    } else {
                                        System.err.println("the expected file size and the size actually read don't match");
                                    }
                                } catch (NumberFormatException e) {
                                    System.err.println("Error in FILE prefix: unable to read file size");
                                }
                            } else {
                                System.err.println("Error in FILE prefix: array too short");
                            }
                        } else {
                            mNetworkCommunication.sendDisconnectionSignal();
                            Log.d(TAG, "no byte read or prefix not supported");
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.e("ListenToServer", "not able to read prefix:" + e.getMessage());
                } catch (NumberFormatException e) {
                    Log.e("ListenToServer", "not able to read sizes from prefix:" + e.getMessage());
                }
            }
        }).start();
    }

    private byte[] readDataIntoArray(int expectedSize, Boolean able_to_read) {
        byte[] arrayToReadInto = new byte[expectedSize];
        int bytesReadAlready = 0;
        int totalBytesRead = 0;
        do {
            try {
                bytesReadAlready = mInputStream.read(arrayToReadInto, totalBytesRead, expectedSize - totalBytesRead);
                Log.v(TAG, "number of bytes read:" + Integer.toString(bytesReadAlready));
            } catch (IOException e) {
                able_to_read = false;
                NetworkCommunication.connected = false;
                if (e.toString().contains("Socket closed")) {
                    Log.d(TAG, "Reading data stream: input stream was closed");
                } else {
                    e.printStackTrace();
                }
            }
            if (bytesReadAlready >= 0) {
                totalBytesRead += bytesReadAlready;
                if (able_to_read == false) {
                    bytesReadAlready = -1;
                    able_to_read = true;
                }
            }
        } while (bytesReadAlready > 0);    //shall be sizeRead > -1, because .read returns -1 when finished reading, but outstream not closed on server side

        return arrayToReadInto;
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
        bun.putInt("nbCorrectAnswers", question_to_display.getNB_CORRECT_ANS());
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

    //Get the IP address of the server through UDP listening
    private void listenForIPThroughUDP() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    System.out.println("Open socket");
                    if (socket == null) {
                        try {
                            socket = new DatagramSocket(9346);
                            DatagramPacket packet = new DatagramPacket(new byte[100], 100);
                            socket.receive(packet);
                            socket.close();
                            System.out.println("Close socket");


                            byte[] data = packet.getData();
                            String message = new String(data);
                            System.out.println(message);

                            if (message.split("///")[0].contentEquals("IPADDRESS")) {
                                ip_address = message.split("///")[1];
                                DbTableSettings.addMaster(message.split("///")[1]);
                            }
                        } catch (BindException ex) {
                            System.err.println("Encountered BindException. Address is probably already in use");
                        }
                    } else {
                        socket.close();
                        socket = null;
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void closeConnection() {
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }
            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }

            //close udp socket
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}