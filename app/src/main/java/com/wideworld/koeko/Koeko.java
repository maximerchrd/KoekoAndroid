package com.wideworld.koeko;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.wideworld.koeko.Activities.GameFragment;
import com.wideworld.koeko.Activities.TestFragment;
import com.wideworld.koeko.NetworkCommunication.NetworkCommunication;
import com.wideworld.koeko.NetworkCommunication.WifiCommunication;
import com.wideworld.koeko.QuestionsManagement.States.GameState;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.States.QuestionMultipleChoiceState;
import com.wideworld.koeko.QuestionsManagement.States.QuestionShortAnswerState;

/**
 * Created by maximerichard on 17/02/17.
 */
public class Koeko extends MultiDexApplication {
    public static int testConnectivity = 0;
    public static GameFragment currentGameFragment = null;
    public static TestFragment currentTestFragmentSingleton = null;
    public static QuestionMultipleChoiceState qmcActivityState = null;
    public static QuestionMultipleChoice currentQuestionMultipleChoiceSingleton = null;
    public static QuestionShortAnswerState shrtaqActivityState = null;
    public static QuestionShortAnswer currentQuestionShortAnswerSingleton = null;
    public static GameState gameState = null;
    public static Long activeTestStartTime = 0L;
    public static String qrCode = "";


    public static WifiCommunication wifiCommunicationSingleton;
    public static NetworkCommunication networkCommunicationSingleton;
    private NetworkCommunication appNetwork = null;
    private Integer quitApp = 0;
    static public long MAX_ACTIVITY_TRANSITION_TIME_MS = Koeko.SHORT_TRANSITION_TIME;
    static public long SHORT_TRANSITION_TIME = 200;
    static public long MEDIUM_TRANSITION_TIME = 800;
    static public long LONG_TRANSITION_TIME = 2000;

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
}
