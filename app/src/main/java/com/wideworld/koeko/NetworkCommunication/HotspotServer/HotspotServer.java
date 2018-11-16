package com.wideworld.koeko.NetworkCommunication.HotspotServer;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.wideworld.koeko.Koeko;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class HotspotServer {
    static public Boolean serverON = false;
    static private int PORTNUMBER = 9090;
    private String hotspotName;
    private String hotspotPassword;
    private String serverIpAddress = "";
    private Context context;
    private ClientsGroup clientsGroup;

    private String TAG = "HotspotServer";

    public HotspotServer(String hotspotName, String hotspotPassword, Context context) {
        this.hotspotName = hotspotName;
        this.hotspotPassword = hotspotPassword;
        this.context = context;
        clientsGroup = new ClientsGroup();
    }

    //check whether wifi hotspot on or off
    public boolean isHotspotOn() {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    // toggle wifi hotspot on or off
    public boolean configHotspotState() {
        System.out.println("Trying to start hotspot");
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = new WifiConfiguration();
        wificonfiguration.SSID = hotspotName;
        wificonfiguration.preSharedKey = hotspotPassword;
        wificonfiguration.hiddenSSID = false;
        wificonfiguration.status = WifiConfiguration.Status.ENABLED;
        wificonfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wificonfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wificonfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wificonfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wificonfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wificonfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wificonfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        try {
            // if WiFi is on, turn it off
            if (wifimanager.isWifiEnabled()) {
                wifimanager.setWifiEnabled(false);
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            if (isHotspotOn()) {
                method.invoke(wifimanager, wificonfiguration, !isHotspotOn());
                Thread.sleep(600);
            }
            method.invoke(wifimanager, wificonfiguration, !isHotspotOn());
            Boolean apOn = false;
            for (int i = 0; i < 7 && !apOn; i++) {
                Thread.sleep(3000);
                apOn = isHotspotOn();
            }
            if (apOn) {
                for (int i = 0; i < 10 && (serverIpAddress == null || serverIpAddress.length() == 0); i++) {
                    serverIpAddress = getLocalIpAddress();
                    Thread.sleep(500);
                }
                System.out.println("Local ip was read to be: " + serverIpAddress);
            } else {
                System.out.println("Didn't manage to launch hotspot (fast enough?)");
            }
            return apOn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "configHotspotState: something bad happened, returning false");
        return false;
    }

    public void startHotspotServer() {
        try {
            Log.d(TAG, "startHotspotServer: ipAddress=" + serverIpAddress);

            HotspotServer.serverON = true;
            // we create a server socket and bind it to port 9090.
            ServerSocket myServerSocket = new ServerSocket(PORTNUMBER);

            String ipAddressMessage = "HOTSPOTIP///" + serverIpAddress + "///" + hotspotPassword + "///";
            Koeko.networkCommunicationSingleton.getmNearbyCom().sendBytes(ipAddressMessage.getBytes());

            //Wait for client connection
            System.out.println("HotspotServer Started. Waiting for clients to connect...");
            Thread connectionthread = new Thread(() -> {
                while (true) {
                    try {
                        //listening to client connection and accept it
                        Socket skt = myServerSocket.accept();
                        Client client = new Client(skt.getInetAddress(), skt.getOutputStream(), skt.getInputStream());
                        System.out.println("Student with address: " + client.getInetAddres() + " accepted. Waiting for next client to connect");

                        //register student
                        clientsGroup.addClientCheckingInetAddress(client);

                        //start a new thread for listening to each student
                        listenForClient(client);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            connectionthread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForClient(final Client client) {
        Log.v(TAG, "listening to: " + client.getInetAddres());
        Thread listeningthread = new Thread() {
            public void run() {
                int bytesread = 0;
                Boolean ableToRead = true;
                while (bytesread >= 0 && ableToRead) {
                    try {
                        byte[] bytesData = new byte[1000];
                        bytesread = client.getInputStream().read(bytesData);
                        if (bytesread >= 1000) {
                            System.out.println("Answer too large for bytearray: " + bytesread + " bytes read");
                        }
                        if (bytesread >= 0) {
                            String answerString = new String(bytesData, 0, bytesread, "UTF-8");
                            System.out.println(client.getInetAddres().toString() + " message:" + answerString);
                            if (answerString.split("///")[0].contentEquals("CONN")) {
                                Koeko.networkCommunicationSingleton.getmNearbyCom().sendBytes(bytesData);

                                //if RESIDS were merged with CONN
                                if (answerString.contains("RESIDS")) {
                                    //TODO: implement syncing of clients
                                }
                            } else if (answerString.split("///")[0].contains("RESIDS")) {
                                //TODO: implement syncing of clients
                            } else if (answerString.contains("ENDTRSM")) {
                                //TODO: implement syncing of clients
                            } else {
                                Koeko.networkCommunicationSingleton.getmNearbyCom().sendBytes(bytesData);
                            }
                        } else {
                            System.out.println("Communication over?");
                        }
                    } catch (SocketException sockex) {
                        if (sockex.toString().contains("timed out")) {
                            System.out.println("Socket exception: read timed out");
                        } else if (sockex.toString().contains("Connection reset")) {
                            System.out.println("Socket exception: connection reset");
                        } else {
                            System.out.println("Other Socket exception");
                        }
                        bytesread = -1;
                    } catch (IOException e1) {
                        System.out.println("Some other IOException occured");
                        if (e1.toString().contains("Connection reset")) {
                            bytesread = -1;
                        }
                    }
                }
            }
        };
        listeningthread.start();
    }

    public void sendDataToClients(Client client, byte[] data) {
        if (client == null) {
            for (Client singleClient : clientsGroup.getClients()) {
                try {
                    System.out.println("Sending: " + data.length + " bytes");
                    synchronized (singleClient.getOutputStream()) {
                        singleClient.getOutputStream().write(data, 0, data.length);
                        singleClient.getOutputStream().flush();
                    }
                } catch (SocketException sockex) {
                    System.out.println("SocketException (socket closed by client?)");
                } catch (IOException ex2) {
                    if (ex2.toString().contains("Broken pipe")) {
                        System.out.println("Broken pipe with a student (student was null)");
                    } else {
                        System.out.println("Other IOException occured");
                        ex2.printStackTrace();
                    }
                } catch (NullPointerException nulex) {
                    System.out.println("NullPointerException in a thread with null output stream (closed by another thread)");
                }
            }
        }
    }

    static private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
