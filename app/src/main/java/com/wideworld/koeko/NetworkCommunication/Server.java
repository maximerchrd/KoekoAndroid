package com.wideworld.koeko.NetworkCommunication;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

public class Server {
    private String hotspotName;
    private String hotspotPassword;
    private Context context;

    public Server(String hotspotName, String hotspotPassword, Context context) {
        this.hotspotName = hotspotName;
        this.hotspotPassword = hotspotPassword;
        this.context = context;
    }

    //check whether wifi hotspot on or off
    public  boolean isApOn() {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        }
        catch (Throwable ignored) {}
        return false;
    }

    // toggle wifi hotspot on or off
    public boolean configApState() {
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
            if(wifimanager.isWifiEnabled()) {
                wifimanager.setWifiEnabled(false);
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, !isApOn());
            Boolean apOn = isApOn();
            return apOn;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
