package com.LearningTracker.LearningTrackerApp;

import android.app.Application;
import android.util.Log;

import com.LearningTracker.LearningTrackerApp.NetworkCommunication.NetworkCommunication;
import com.LearningTracker.LearningTrackerApp.NetworkCommunication.WifiCommunication;

/**
 * Created by maximerichard on 17/02/17.
 */
public class LTApplication extends Application {
    private static WifiCommunication wifiCommunicationSingleton;
    private NetworkCommunication appNetwork;
    private Integer quitApp = 0;
    public final long MAX_ACTIVITY_TRANSITION_TIME_MS = 700;

    public WifiCommunication getAppWifi() {return wifiCommunicationSingleton;}
    public NetworkCommunication getAppNetwork() {
        return appNetwork;
    }


    public void setAppWifi(WifiCommunication appWifi) {
        this.wifiCommunicationSingleton = appWifi;
    }

    public void setAppNetwork(NetworkCommunication appNetwork) {
        this.appNetwork = appNetwork;
    }

    public void resetQuitApp() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(MAX_ACTIVITY_TRANSITION_TIME_MS);
                    quitApp = 0;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void startActivityTransitionTimer() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    quitApp++;
                    sleep(MAX_ACTIVITY_TRANSITION_TIME_MS);
                    if (quitApp > 0) {
                        Log.v("WARNING: ", "user left application");
                        appNetwork.sendDisconnectionSignal();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void stopActivityTransitionTimer() {
        quitApp--;
    }

}
