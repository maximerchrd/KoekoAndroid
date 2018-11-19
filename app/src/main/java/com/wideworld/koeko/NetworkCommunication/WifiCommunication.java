package com.wideworld.koeko.NetworkCommunication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wideworld.koeko.Activities.CorrectedQuestionActivity;
import com.wideworld.koeko.NetworkCommunication.HotspotServer.HotspotServer;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.Test;
import com.wideworld.koeko.Tools.FileHandler;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableIndividualQuestionForResult;
import com.wideworld.koeko.database_management.DbTableLearningObjective;
import com.wideworld.koeko.database_management.DbTableQuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableQuestionShortAnswer;
import com.wideworld.koeko.database_management.DbTableRelationTestObjective;
import com.wideworld.koeko.database_management.DbTableSettings;
import com.wideworld.koeko.database_management.DbTableTest;
import com.google.android.gms.common.util.ArrayUtils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.WIFI_SERVICE;

public class WifiCommunication {
    final private int PORTNUMBER = 9090;
    public Integer connectionSuccess = 0;
    private Context mContextWifCom;
    private Application mApplication;
    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;

    private String ip_address = "no IP";
    private TextView logView = null;
    private DatagramSocket socket;
    private DataConversion dataConversion;

    private String ServerWifiSSID = "";
    private String secondLayerMasterIp = "";

    private String TAG = "WifiCommunication";

    public WifiCommunication(Context arg_context, Application arg_application, TextView logView) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mApplication = arg_application;
        ((Koeko) mApplication).setAppWifi(this);
        mContextWifCom = arg_context;
        this.logView = logView;
        this.dataConversion = new DataConversion(arg_context);
    }

    /**
     * @param connectionString
     * @param deviceIdentifier
     * @param reconnection/    0: no reconnection; 1: reconnection; 3: connect to 2nd layer server; 4: reconnect after fail: must send FAIL before CONN
     *                    reconnection (continuing)
     */
    public void connectToServer(String connectionString, String deviceIdentifier, int reconnection) {
        try {
            NetworkCommunication.connected = 2;
            ip_address = "no IP"; 
            //test specific to Nearby Connections
            if (NearbyCommunication.NEARBY_TESTING == 1) {
                WifiManager wifimanager = (WifiManager) mContextWifCom.getSystemService(WIFI_SERVICE);
                if (!wifimanager.isWifiEnabled()) {
                    Log.d(TAG, "connectToServer: discoverer");
                    Koeko.networkCommunicationSingleton.getmNearbyCom().startDiscovery();
                } else {
                    Log.d(TAG, "connectToServer: advertiser");
                    Koeko.networkCommunicationSingleton.getmNearbyCom().startAdvertising();
                }
            } else {
                //Reset the networking solution to 0
                NetworkCommunication.network_solution = 0;
            }

            if (Koeko.networkCommunicationSingleton.getHotspotServerHotspot() != null && Koeko.networkCommunicationSingleton.getHotspotServerHotspot().isHotspotOn()) {
                NetworkCommunication.network_solution = 1;
                NearbyCommunication.deviceRole = NearbyCommunication.DISCOVERER_ROLE;
            }

            //Don't try to connect through wifi if we are discoverer
            if (NetworkCommunication.network_solution == 0 || NearbyCommunication.deviceRole != NearbyCommunication.DISCOVERER_ROLE) {
                //Automatic connection
                Integer automaticConnection = DbTableSettings.getAutomaticConnection();
                if (automaticConnection == 1 && (reconnection == 0 || reconnection == 2)) {
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
                } else if (reconnection == 3) {
                    ip_address = secondLayerMasterIp;
                } else {
                    ip_address = DbTableSettings.getMaster();
                }

                Log.v("connectToServer", "beginning");
                Socket s = new Socket(ip_address, PORTNUMBER);
                connectionSuccess = 1;
                //outgoing stream redirect to socket
                mOutputStream = s.getOutputStream();
                mInputStream = s.getInputStream();

                NetworkCommunication.connected = 1;

                byte[] conBuffer = connectionString.getBytes();
                try {
                    if (reconnection == 4) {
                        System.out.println("Reconnection code 4 failed ");
                        String failString = "FAIL///" + NetworkCommunication.deviceIdentifier + "///";
                        mOutputStream.write(failString.getBytes(), 0, conBuffer.length);
                        mOutputStream.flush();
                    }
                    mOutputStream.write(conBuffer, 0, conBuffer.length);
                    mOutputStream.flush();
                } catch (IOException e) {
                    String msg = "In connectToServer() and an exception occurred during write: " + e.getMessage();
                    Log.e("Fatal Error", msg);
                }

                //send resource ids present on the device
                String idsOnDevice = DbTableQuestionMultipleChoice.getAllQuestionMultipleChoiceIdsAndHashCode() + "|" +
                        DbTableQuestionShortAnswer.getAllShortAnswerIdsAndHashCode() + "|"
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
            } else {
                Koeko.networkCommunicationSingleton.getmNearbyCom().startDiscovery();
            }
        } catch (ConnectException e) {
            //TODO: warn student that he is maybe not connected to the right wifi
            Log.d(TAG, "connectToServer: warn student that he is maybe not connected to the right wifi");
            if (connectionSuccess != -2) {
                connectionSuccess = -1;
            }
        } catch (SocketException e) {
            if (e.toString().contains("Network is unreachable")) {
                Log.d(TAG, "connectToServer: network is unreachable");
            } else {
                e.printStackTrace();
            }
            if (connectionSuccess != -2) {
                connectionSuccess = -1;
            }
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
        byte[] ansBuffer = answer.getBytes();
        try {
            if (mOutputStream != null) {
                mOutputStream.write(ansBuffer, 0, ansBuffer.length);
                Log.d("answer buffer length: ", String.valueOf(ansBuffer.length));
                mOutputStream.flush();
            } else {
                Log.d(TAG, "sendAnswerToServer: ERROR, outputStream is null");
            }
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
        new Thread(() -> {
            try {
                Boolean able_to_read = true;
                while (able_to_read && mInputStream != null) {
                    byte[] prefix_buffer = readDataIntoArray(80, able_to_read);
                    String sizesPrefix = null;
                    sizesPrefix = new String(prefix_buffer, "UTF-8");
                    DataPrefix prefix = new DataPrefix();
                    prefix.stringToPrefix(sizesPrefix);
                    Log.v("WifiCommunication", "received string: " + sizesPrefix);
                    if (prefix.getDataType().contentEquals(DataPref.multq)) {
                        //read question data
                        byte[] question_buffer = readDataIntoArray(Integer.valueOf(prefix.getDataLength()), able_to_read);
                        byte[] allBytesReceived = ArrayUtils.concatByteArrays(prefix_buffer, question_buffer);
                        ReceptionProtocol.receivedQuestionData(dataConversion, allBytesReceived);
                    } else if (prefix.getDataType().contentEquals(DataPref.shrta)) {
                        //read question data
                        byte[] question_buffer = readDataIntoArray(Integer.valueOf(prefix.getDataLength()), able_to_read);
                        byte[] allBytesReceived = ArrayUtils.concatByteArrays(prefix_buffer, question_buffer);
                        ReceptionProtocol.receivedQuestionData(dataConversion, allBytesReceived);
                    } else if (prefix.getDataType().contentEquals(DataPref.subObj)) {
                        byte[] data = readDataIntoArray(Integer.valueOf(prefix.getDataLength()), able_to_read);
                        byte[] allBytesReceived = ArrayUtils.concatByteArrays(prefix_buffer, data);
                        ReceptionProtocol.receivedSubObj(dataConversion, allBytesReceived);
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
                                Koeko.networkCommunicationSingleton.directCorrection = sizesPrefix.split("///")[2];
                                Koeko.networkCommunicationSingleton.launchTestActivity(testId, Koeko.networkCommunicationSingleton.directCorrection);
                                Koeko.shrtaqActivityState = null;
                                Koeko.qmcActivityState = null;
                            } else {
                                QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(id_global);
                                if (questionMultipleChoice.getQuestion().length() > 0) {
                                    questionMultipleChoice.setId(id_global);
                                    Koeko.networkCommunicationSingleton.directCorrection = sizesPrefix.split("///")[2];
                                    Koeko.networkCommunicationSingleton.launchMultChoiceQuestionActivity(questionMultipleChoice, Koeko.networkCommunicationSingleton.directCorrection);
                                    Koeko.shrtaqActivityState = null;
                                    Koeko.currentTestActivitySingleton = null;
                                } else {
                                    QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(id_global);
                                    questionShortAnswer.setId(id_global);
                                    Koeko.networkCommunicationSingleton.directCorrection = sizesPrefix.split("///")[2];
                                    Koeko.networkCommunicationSingleton.launchShortAnswerQuestionActivity(questionShortAnswer, Koeko.networkCommunicationSingleton.directCorrection);
                                    Koeko.qmcActivityState = null;
                                    Koeko.currentTestActivitySingleton = null;
                                }
                            }
                        }

                        if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
                            Koeko.networkCommunicationSingleton.sendDataToClient(prefix_buffer);
                        }
                    } else if (sizesPrefix.split("///")[0].split(":")[0].contentEquals("SYNCIDS")) {
                        if (sizesPrefix.split("///").length >= 2) {
                            Integer sizeToread = Integer.valueOf(sizesPrefix.split("///")[1]);
                            byte[] idsBytes = readDataIntoArray(sizeToread, able_to_read);
                            Koeko.networkCommunicationSingleton.idsToSync.addAll(dataConversion.bytesToIdsList(idsBytes));
                            for (String id : Koeko.networkCommunicationSingleton.idsToSync) {
                                System.out.println(id);
                            }
                            System.out.println("________________");
                        }
                    } else if (sizesPrefix.split("///")[0].split(":")[0].contentEquals("EVAL")) {
                        DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(sizesPrefix.split("///")[2],
                                sizesPrefix.split("///")[1], Koeko.networkCommunicationSingleton.getLastAnswer());
                    } else if (sizesPrefix.split("///")[0].split(":")[0].contentEquals("UPDEV")) {
                        DbTableIndividualQuestionForResult.setEvalForQuestion(Double.valueOf(sizesPrefix.split("///")[1]),
                                sizesPrefix.split("///")[2]);
                    } else if (sizesPrefix.split("///")[0].split(":")[0].contentEquals("CORR")) {
                        Intent mIntent = new Intent(mContextWifCom, CorrectedQuestionActivity.class);
                        Bundle bun = new Bundle();
                        bun.putString("questionID", sizesPrefix.split("///")[1]);
                        mIntent.putExtras(bun);
                        mContextWifCom.startActivity(mIntent);
                    } else if (sizesPrefix.split("///")[0].contentEquals("TEST")) {
                        //2000001///test1///2000005;;;2000006:::EVALUATION<60|||2000006;;;2000007:::EVALUATION<60|||2000007|||///objectives///TESTMODE///
                        //first, fetch the size we'll have to read
                        Integer textBytesSize = 0;
                        textBytesSize = Integer.valueOf(sizesPrefix.split("///")[1]);

                        byte[] testDataBuffer = readDataIntoArray(textBytesSize, able_to_read);

                        Test newTest = dataConversion.byteToTest(testDataBuffer);

                        DbTableTest.insertTest(newTest);

                        if (NetworkCommunication.network_solution == 1) {
                            Koeko.networkCommunicationSingleton.idsToSync.add(String.valueOf(newTest.getIdGlobal()));
                            if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
                                byte[] allBytesReceived = ArrayUtils.concatByteArrays(prefix_buffer, testDataBuffer);
                                Koeko.networkCommunicationSingleton.getmNearbyCom().sendBytes(allBytesReceived);
                            }
                        }
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
                                    sendStringToServer("OK:" + NetworkCommunication.deviceIdentifier + "///" + sizesPrefix.split("///")[1] + "///");


                                    if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
                                        byte[] allBytesReceived = ArrayUtils.concatByteArrays(prefix_buffer, dataBuffer);
                                        Koeko.networkCommunicationSingleton.sendDataToClient(allBytesReceived);
                                    }
                                } else {
                                    System.err.println("the expected file size and the size actually read don't match");
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Error in FILE prefix: unable to read file size");
                            }
                        } else {
                            System.err.println("Error in FILE prefix: array too short");
                        }
                    } else if (sizesPrefix.split("///")[0].contentEquals("ADVER")) {
                        Koeko.networkCommunicationSingleton.getmNearbyCom().startAdvertising();
                    } else if (sizesPrefix.split("///")[0].contentEquals("DISCOV")) {
                        HotspotServer hotspotServerHotspot = new HotspotServer(sizesPrefix.split("///")[1], sizesPrefix.split("///")[2], mContextWifCom);
                        Koeko.networkCommunicationSingleton.setHotspotServerHotspot(hotspotServerHotspot);
                        Koeko.networkCommunicationSingleton.getmNearbyCom().startDiscovery();
                        System.out.println("Tried to start discovery");
                    } else if (sizesPrefix.split("///")[0].contentEquals("THIRDLAY")) {
                        secondLayerMasterIp = sizesPrefix.split("///")[3];
                        tryToJoinWifi(sizesPrefix.split("///")[1], sizesPrefix.split("///")[2]);
                        System.out.println("Try to join third layer");
                    } else if (sizesPrefix.split("///")[0].contentEquals("CONNECTED")) {
                        connectionSuccess = 1;
                        Koeko.networkCommunicationSingleton.mInteractiveModeActivity.showConnected();
                        if (ServerWifiSSID.length() == 0) {
                            WifiManager wifiManager = (WifiManager) mContextWifCom.getSystemService(WIFI_SERVICE);
                            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                            ServerWifiSSID = wifiInfo.getSSID();
                        }
                    } else if (sizesPrefix.contentEquals("RECONNECTION")) {
                        System.out.println("We were reconnected. Quit this reading loop, because" +
                                " an other one should be active");
                        able_to_read = false;
                    } else {
                        Koeko.networkCommunicationSingleton.sendDisconnectionSignal();
                        Koeko.networkCommunicationSingleton.closeConnection();
                        Koeko.networkCommunicationSingleton.mInteractiveModeActivity.showDisconnected();
                        Log.d(TAG, "no byte read or prefix not supported");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                Log.e("ListenToServer", "not able to read prefix:" + e.getMessage());
            } catch (NumberFormatException e) {
                Log.e("ListenToServer", "not able to read sizes from prefix:" + e.getMessage());
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
                NetworkCommunication.connected = 0;
                if (e.toString().contains("Socket closed")) {
                    Log.d(TAG, "Reading data stream: input stream was closed");
                } else {
                    e.printStackTrace();
                    if (e.toString().contains("ETIMEDOUT")) {
                        Log.d(TAG, "readDataIntoArray: SocketException: ETIMEDOUT, trying to reconnect");
                        wifiReconnectionTrial();
                        //prevent disconnection by signaling that we were trying to reconnect to the reading loop
                        arrayToReadInto = "RECONNECTION".getBytes();
                        bytesReadAlready = 0;
                    }
                }
            }
            if (bytesReadAlready >= 0) {
                totalBytesRead += bytesReadAlready;
                if (able_to_read == false) {
                    bytesReadAlready = -1;
                    able_to_read = true;
                }
            }
        }
        while (bytesReadAlready > 0);    //shall be sizeRead > -1, because .read returns -1 when finished reading, but outstream not closed on server side

        return arrayToReadInto;
    }


    //Get the IP address of the server through UDP listening
    private void listenForIPThroughUDP() {
        new Thread(() -> {
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

    private void wifiReconnectionTrial() {
        Koeko.networkCommunicationSingleton.mInteractiveModeActivity.showShortToast("SocketException: trying to reconnect");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        for (int i = 0; i < 30 && NetworkCommunication.connected == 0; i++) {
            long waitingTime = 2000;
            if (Koeko.networkCommunicationSingleton.getHotspotServerHotspot() != null && !Koeko.networkCommunicationSingleton.getHotspotServerHotspot().isHotspotOn()) {
                Log.d(TAG, "readDataIntoArray: reconnection, trial: " + i);
                Koeko.networkCommunicationSingleton.mInteractiveModeActivity.showShortToast("Reconnection trial: " + (i + 1));
                closeConnection();


                if (i < 15) {
                    Koeko.networkCommunicationSingleton.connectToMaster(1);
                } else {
                    Koeko.networkCommunicationSingleton.connectToMaster(2);
                    waitingTime = 5000;
                }
            }
            try {
                Thread.sleep(waitingTime);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        if (NetworkCommunication.connected == 1) {
            System.out.println("Display lost connection message");
            Koeko.networkCommunicationSingleton.mInteractiveModeActivity.showMessage("We lost the connection :-( \n" +
                    "Try to reconnect when you are on the Wifi.");
        } else {
            sendStringToServer("RECONNECTED///" + DbTableSettings.getName() + "///");
        }
    }

    private void tryToJoinWifi(String networkSSID, String password) {
        closeConnection();
        connectToWifiWPA(networkSSID, password);
        new Thread(() -> {
            try {
                String joinedWifi = checkIfJoinedWifi(10);
                System.out.println("Did we manage to connect to wifi: " + joinedWifi);
                if (joinedWifi.contentEquals(networkSSID)) {
                    //Connect to hotspot server
                    Thread.sleep(4000);
                    Koeko.networkCommunicationSingleton.connectToMaster(3);
                    return;
                } else if (joinedWifi.contentEquals(ServerWifiSSID.replace("\"", ""))) {
                    Thread.sleep(4000);
                    Koeko.networkCommunicationSingleton.connectToMaster(4);
                    return;
                }

                reconnectToWifiWPA(ServerWifiSSID);
                new Thread(() -> {
                    try {
                        String joinedWifi1 = checkIfJoinedWifi(15);
                        System.out.println("Did we manage to reconnect to original wifi: " + joinedWifi1);
                        if (joinedWifi1.contentEquals(ServerWifiSSID)) {
                            //TODO: loop and try to connect faster
                            Thread.sleep(4000);
                            Koeko.networkCommunicationSingleton.connectToMaster(4);
                        } else {
                            System.err.println("Unable to reconnect to Master");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String checkIfJoinedWifi(int nbSeconds) {
        String joinedWifi = "";
        nbSeconds = nbSeconds / 2;
        for (int i = 0; i < nbSeconds; i++) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WifiManager wifiManager = (WifiManager) mContextWifCom.getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            Log.d(TAG, "checkIfJoinedWifi: " + wifiInfo.getSSID());
            if (wifiInfo.getNetworkId() >= 0) {
                joinedWifi = wifiInfo.getSSID();
                return joinedWifi.replace("\"", "");
            }
        }
        return joinedWifi;
    }

    private Boolean connectToWifiWPA(String networkSSID, String password) {
        try {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain SSID in quotes

            conf.preSharedKey = "\"" + password + "\"";

            conf.status = WifiConfiguration.Status.ENABLED;
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            Log.d("connecting", conf.SSID + " " + conf.preSharedKey);

            WifiManager wifiManager = (WifiManager) mContextWifCom.getSystemService(Context.WIFI_SERVICE);
            wifiManager.addNetwork(conf);

            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                wifiManager.disableNetwork(i.networkId);
            }
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    //wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    break;
                }
            }


            //WiFi Connection success, return true
            return true;
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            return false;
        }
    }

    private Boolean reconnectToWifiWPA(String networkSSID) {
        try {
            WifiManager wifiManager = (WifiManager) mContextWifCom.getSystemService(Context.WIFI_SERVICE);

            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                wifiManager.disableNetwork(i.networkId);
            }
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals(networkSSID)) {
                    //wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    Log.d("re connecting", i.SSID);

                    break;
                }
            }

            //WiFi Connection success, return true
            return true;
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            return false;
        }
    }
}