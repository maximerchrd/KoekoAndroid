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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wideworld.koeko.Activities.CorrectedQuestionActivity;
import com.wideworld.koeko.NetworkCommunication.HotspotServer.HotspotServer;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.ClientToServerTransferable;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.CtoSPrefix;
import com.wideworld.koeko.QuestionsManagement.GameView;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.Result;
import com.wideworld.koeko.QuestionsManagement.Test;
import com.wideworld.koeko.QuestionsManagement.TransferPrefix;
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
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

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
    public DataConversion dataConversion;

    public String ServerWifiSSID = "";
    public String secondLayerMasterIp = "";

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
     * @param connectionBytes
     * @param deviceIdentifier
     * @param reconnection/    0: no reconnection; 1: reconnection; 3: connect to 2nd layer server; 4: reconnect after fail: must send FAIL before CONN
     *                    reconnection (continuing)
     */
    public void connectToServer(byte[] connectionBytes, String deviceIdentifier, int reconnection) {
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
                //try to avoid having e.g. 2 listening threads with inputStream
                closeConnection();

                Socket s = new Socket(ip_address, PORTNUMBER);
                connectionSuccess = 1;
                //outgoing stream redirect to socket
                mOutputStream = s.getOutputStream();
                mInputStream = s.getInputStream();

                NetworkCommunication.connected = 1;

                byte[] conBuffer = connectionBytes;
                try {
                    if (reconnection == 4) {
                        System.out.println("Reconnection code 4 failed ");
                        ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.failPrefix);
                        transferable.setOptionalArgument1(NetworkCommunication.deviceIdentifier);
                        mOutputStream.write(transferable.getTransferableBytes(), 0, transferable.getTransferableBytes().length);
                        mOutputStream.flush();
                    }
                    mOutputStream.write(conBuffer, 0, conBuffer.length);
                    mOutputStream.flush();
                } catch (IOException e) {
                    String msg = "In connectToServer() and an exception occurred during write: " + e.getMessage();
                    Log.e(TAG, msg);
                    NetworkCommunication.connected = 0;
                } catch (NullPointerException e) {
                    String msg = "In connectToServer() and an exception occurred during write: " + e.getMessage();
                    Log.e(TAG, msg);
                    NetworkCommunication.connected = 0;
                }

                //send resource ids present on the device
                ArrayList<String> idsOnDevice = DbTableQuestionMultipleChoice.getAllQuestionMultipleChoiceIdsAndHashCode();
                idsOnDevice.addAll(DbTableQuestionShortAnswer.getAllShortAnswerIdsAndHashCode());
                idsOnDevice.addAll(FileHandler.getMediaFilesList(mContextWifCom));
                ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.resourceIdsPrefix);
                transferable.setOptionalArgument1(deviceIdentifier);
                transferable.setFileBytes(ReceptionProtocol.getObjectMapper().writeValueAsString(idsOnDevice).getBytes());
                sendBytes(transferable.getTransferableBytes());

                sendHomeworkResults();

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
            NetworkCommunication.connected = 0;
        } catch (SocketException e) {
            if (e.toString().contains("Network is unreachable")) {
                Log.d(TAG, "connectToServer: network is unreachable");
            } else {
                e.printStackTrace();
            }
            if (connectionSuccess != -2) {
                connectionSuccess = -1;
            }
            NetworkCommunication.connected = 0;
        } catch (UnknownHostException e) {
            Log.v("connection to server", ": failure, unknown host");
            if (connectionSuccess != -2) {
                connectionSuccess = -1;
            }
            NetworkCommunication.connected = 0;
            e.printStackTrace();
        } catch (IOException e) {
            Log.v("connection to server", ": failure, i/o exception");
            if (connectionSuccess != -2) {
                connectionSuccess = -1;
            }
            NetworkCommunication.connected = 0;
            e.printStackTrace();
        }
    }

    private void sendHomeworkResults() throws JsonProcessingException {
        ArrayList<Result> results = DbTableIndividualQuestionForResult.getUnsyncedHomeworks();
        String resultsAsString = ReceptionProtocol.getObjectMapper().writeValueAsString(results);
        ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.homeworkResultPrefix);
        transferable.setFileBytes(resultsAsString.getBytes());
        if (sendBytes(transferable.getTransferableBytes())) {
            DbTableIndividualQuestionForResult.setAllHomeworkSynced();
        }
    }

    public void sendAnswerToServer(String answer) {
        byte[] ansBuffer = answer.getBytes();
        sendBytes(ansBuffer);
    }

    protected Boolean sendBytes(byte[] bytesToSend) {
        Boolean success = true;
        try {
            if (mOutputStream != null) {
                mOutputStream.write(bytesToSend, 0, bytesToSend.length);
                Log.d("answer buffer length: ", String.valueOf(bytesToSend.length));
                mOutputStream.flush();
            } else {
                Log.d(TAG, "sendAnswerToServer: ERROR, outputStream is null");
            }
        } catch (IOException e) {
            success = false;
            String msg = "In sendAnswerToServer() and an exception occurred during write: " + e.getMessage();
            Log.e("IOException", msg);
        } catch (NullPointerException e) {
            success = false;
            Log.e(TAG, e.getMessage());
            Log.w(TAG, "Probably User is playing with the Connection/Disconnection button");
        }
        return success;
    }

    public void listenForQuestions() {
        new Thread(() -> {
            try {
                Boolean ableToRead = true;
                while (ableToRead && mInputStream != null) {
                    byte[] prefix_buffer = readDataIntoArray(80, ableToRead);
                    String sizesPrefix = null;
                    sizesPrefix = new String(prefix_buffer, "UTF-8");
                    DataPrefix prefix = new DataPrefix();
                    prefix.stringToPrefix(sizesPrefix);
                    Log.v("WifiCommunication", "received string: " + sizesPrefix);
                    if (TransferPrefix.INSTANCE.isResource(sizesPrefix)) {
                        byte[] question_buffer = readDataIntoArray(TransferPrefix.INSTANCE.getSize(sizesPrefix), ableToRead);
                        byte[] allBytesReceived = ArrayUtils.concatByteArrays(prefix_buffer, question_buffer);
                        ReceptionProtocol.receivedResource(sizesPrefix, question_buffer, allBytesReceived);
                    } else if (TransferPrefix.INSTANCE.isStateUpdate(sizesPrefix)) {
                        byte[] question_buffer = readDataIntoArray(TransferPrefix.INSTANCE.getSize(sizesPrefix), ableToRead);
                        byte[] allBytesReceived = ArrayUtils.concatByteArrays(prefix_buffer, question_buffer);
                        ReceptionProtocol.receivedStateUpdate(sizesPrefix, question_buffer, allBytesReceived,
                                mContextWifCom);
                    } else if (TransferPrefix.INSTANCE.isFile(sizesPrefix)) {
                        byte[] question_buffer = readDataIntoArray(TransferPrefix.INSTANCE.getSize(sizesPrefix), ableToRead);
                        byte[] allBytesReceived = ArrayUtils.concatByteArrays(prefix_buffer, question_buffer);
                        ReceptionProtocol.receivedFile(sizesPrefix, question_buffer, allBytesReceived, mContextWifCom);
                    } else if (sizesPrefix.contentEquals("RECONNECTION")) {
                        System.out.println("We were reconnected. Quit this reading loop, because" +
                                " an other one should be active");
                        ableToRead = false;
                    } else {
                        if (NearbyCommunication.deviceRole != NearbyCommunication.DISCOVERER_ROLE) {
                            Koeko.networkCommunicationSingleton.sendDisconnectionSignal("close-connection");
                            Koeko.networkCommunicationSingleton.closeConnection();
                            Koeko.networkCommunicationSingleton.mInteractiveModeActivity.showDisconnected();
                            Log.d(TAG, "no byte read or prefix not supported");
                        } else {
                            ableToRead = false;
                            Log.d(TAG, "listenForQuestions: closing wifi reading loop");
                        }
                    }
                }
            } catch (UnsupportedEncodingException e) {
                Log.e("ListenToServer", "not able to read prefix:" + e.getMessage());
            } catch (NumberFormatException e) {
                Log.e("ListenToServer", "not able to read sizes from prefix:" + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private byte[] readDataIntoArray(int expectedSize, Boolean ableToRead) {
        byte[] arrayToReadInto = new byte[expectedSize];
        int bytesReadAlready = 0;
        int totalBytesRead = 0;
        do {
            try {
                bytesReadAlready = mInputStream.read(arrayToReadInto, totalBytesRead, expectedSize - totalBytesRead);
                Log.v(TAG, "number of bytes read:" + Integer.toString(bytesReadAlready));
            } catch (IOException e) {
                ableToRead = false;
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
            } catch (NullPointerException e) {
                ableToRead = false;
                NetworkCommunication.connected = 0;
                Log.e(TAG, e.getMessage());
                Log.w(TAG, "Probably User is playing with the Connection/Disconnection button");
            } catch (ArrayIndexOutOfBoundsException e) {
                ableToRead = false;
                NetworkCommunication.connected = 0;
                Log.e(TAG, e.getStackTrace().toString() + "\ntotalBytesRead: " + totalBytesRead
                        + "\nexpectedSize: " + expectedSize);
            }
            if (bytesReadAlready >= 0) {
                totalBytesRead += bytesReadAlready;
                if (ableToRead == false) {
                    bytesReadAlready = -1;
                    ableToRead = true;
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
        Log.v(TAG, "Showing toast trying to reconnect");
        Koeko.networkCommunicationSingleton.mInteractiveModeActivity.showShortToast("SocketException: trying to reconnect");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        Log.v(TAG, "NetworkCommunication.connected:" + NetworkCommunication.connected);
        for (int i = 0; i < 30 && NetworkCommunication.connected == 0; i++) {
            long waitingTime = 2000;
            if (NetworkCommunication.network_solution == 0 || (Koeko.networkCommunicationSingleton.getHotspotServerHotspot() != null && !Koeko.networkCommunicationSingleton.getHotspotServerHotspot().isHotspotOn())) {
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
        if (NetworkCommunication.connected == 0) {
            Koeko.networkCommunicationSingleton.mInteractiveModeActivity.showMessage("We lost the connection :-( \n" +
                    "Try to reconnect when you are on the Wifi.");
        } else {
            ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.reconnectedPrefix);
            transferable.setOptionalArgument1(DbTableSettings.getName());
            sendBytes(transferable.getTransferableBytes());
        }
    }

    public void tryToJoinWifi(String networkSSID, String password) {
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