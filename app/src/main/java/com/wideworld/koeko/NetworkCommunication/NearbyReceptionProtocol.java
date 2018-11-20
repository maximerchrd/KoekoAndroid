package com.wideworld.koeko.NetworkCommunication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.wideworld.koeko.Activities.CorrectedQuestionActivity;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.Test;
import com.wideworld.koeko.Tools.FileHandler;
import com.wideworld.koeko.database_management.DbTableIndividualQuestionForResult;
import com.wideworld.koeko.database_management.DbTableQuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableQuestionShortAnswer;
import com.wideworld.koeko.database_management.DbTableTest;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class NearbyReceptionProtocol {
    private Context receptionContext;
    private DataConversion dataConversion;
    private NearbyCommunication nearbyCommunication;

    String TAG = "NearbyReceptionProtocol";

    public NearbyReceptionProtocol(Context context, NearbyCommunication nearbyCommunication) {
        this.receptionContext = context;
        this.dataConversion = new DataConversion(context);
        this.nearbyCommunication = nearbyCommunication;
    }

    public void receivedData(byte[] bytesReceived) {
        try {
            byte[] prefix = new byte[DataConversion.prefixSize];
            for (int i = 0; i < bytesReceived.length && i < DataConversion.prefixSize; i++) {
                prefix[i] = bytesReceived[i];
            }

            String stringPrefix = new String(prefix, "UTF-8");
            DataPrefix dataPrefix = new DataPrefix();
            dataPrefix.stringToPrefix(stringPrefix);

            Log.d(TAG, "receivedData: " + stringPrefix);

            switch (dataPrefix.getDataType()) {
                case DataPref.multq:
                    ReceptionProtocol.receivedQuestionData(dataConversion, bytesReceived);
                    break;
                case DataPref.shrta:
                    ReceptionProtocol.receivedQuestionData(dataConversion, bytesReceived);
                    break;
                case DataPref.subObj:
                    ReceptionProtocol.receivedSubObj(dataConversion, bytesReceived);
                    break;
                case "QID":
                    receivedQID(stringPrefix);
                    break;
                case "SYNCIDS":
                    receivedSYNCIDS(bytesReceived);
                    break;
                case "EVAL":
                    DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(stringPrefix.split("///")[2],
                            stringPrefix.split("///")[1], Koeko.networkCommunicationSingleton.getLastAnswer());
                    break;
                case "UPDEV":
                    DbTableIndividualQuestionForResult.setEvalForQuestion(Double.valueOf(stringPrefix.split("///")[1]),
                            stringPrefix.split("///")[2]);
                    break;
                case "CORR":
                    Intent mIntent = new Intent(receptionContext, CorrectedQuestionActivity.class);
                    Bundle bun = new Bundle();
                    bun.putString("questionID", stringPrefix.split("///")[1]);
                    mIntent.putExtras(bun);
                    receptionContext.startActivity(mIntent);
                    break;
                case "TEST":
                    if (bytesReceived.length > 80) {
                        byte[] testBytes = Arrays.copyOfRange(bytesReceived, 80, bytesReceived.length);
                        Test newTest = dataConversion.byteToTest(testBytes);
                        DbTableTest.insertTest(newTest);
                    }
                    break;
                case "OEVAL":
                    //TODO: implement OEVAL
                    break;
                case DataPref.file:
                    receivedFILE(stringPrefix, bytesReceived);
                    break;
                case "OK":
                    Log.d(TAG, "forwarding OK");
                    Koeko.networkCommunicationSingleton.sendStringToServer(new String(bytesReceived));
                    break;
                case "ANSW0":
                    Log.d(TAG, "forwarding ANSW0");
                    Koeko.networkCommunicationSingleton.sendStringToServer(new String(bytesReceived));
                    break;
                case "ANSW1":
                    Log.d(TAG, "forwarding ANSW1");
                    Koeko.networkCommunicationSingleton.sendStringToServer(new String(bytesReceived));
                    break;
                case "CONN":
                    Log.d(TAG, "forwarding CONN");
                    Koeko.networkCommunicationSingleton.sendStringToServer(new String(bytesReceived));
                    break;
                case "DISC":
                    Log.d(TAG, "forwarding DISC");
                    Koeko.networkCommunicationSingleton.sendStringToServer(new String(bytesReceived));
                    break;
                case "HOTSPOTIP":
                    Log.d(TAG, "forwarding HOTSPOTIP");
                    Koeko.networkCommunicationSingleton.sendStringToServer(new String(bytesReceived));
                    break;
                case "SUCCESS":
                    Log.d(TAG, "forwarding SUCCESS");
                    Koeko.networkCommunicationSingleton.sendStringToServer(new String(bytesReceived));
                    break;
                default:
                    Log.e(TAG, "Prefix not supported");
                    break;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void receivedSYNCIDS(byte[] bytesReceived) {
        byte[] idsBytes = Arrays.copyOfRange(bytesReceived, 80, bytesReceived.length);
        Koeko.networkCommunicationSingleton.idsToSync.addAll(dataConversion.bytesToIdsList(idsBytes));
    }

    private void receivedQID(String stringPrefix) {
        String id_global = stringPrefix.split("///")[1];

        //reinitializing all types of displays
        Koeko.currentTestActivitySingleton = null;
        Koeko.shrtaqActivityState = null;
        Koeko.qmcActivityState = null;

        if (Long.valueOf(id_global) < 0) {
            //setup test and show it
            Long testId = -(Long.valueOf(stringPrefix.split("///")[1]));
            Koeko.networkCommunicationSingleton.directCorrection = stringPrefix.split("///")[2];
            Koeko.networkCommunicationSingleton.launchTestActivity(testId, Koeko.networkCommunicationSingleton.directCorrection);
            Koeko.shrtaqActivityState = null;
            Koeko.qmcActivityState = null;
        } else {
            QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(id_global);
            if (questionMultipleChoice.getQuestion().length() > 0) {
                questionMultipleChoice.setId(id_global);
                Koeko.networkCommunicationSingleton.directCorrection = stringPrefix.split("///")[2];
                Koeko.networkCommunicationSingleton.launchMultChoiceQuestionActivity(questionMultipleChoice, Koeko.networkCommunicationSingleton.directCorrection);
                Koeko.shrtaqActivityState = null;
                Koeko.currentTestActivitySingleton = null;
            } else {
                QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(id_global);
                questionShortAnswer.setId(id_global);
                Koeko.networkCommunicationSingleton.directCorrection = stringPrefix.split("///")[2];
                Koeko.networkCommunicationSingleton.launchShortAnswerQuestionActivity(questionShortAnswer, Koeko.networkCommunicationSingleton.directCorrection);
                Koeko.qmcActivityState = null;
                Koeko.currentTestActivitySingleton = null;
            }
        }
    }

    private void receivedFILE(String stringPrefix, byte[] bytesReceived) {
        if (stringPrefix.split("///").length >= 3) {
            try {
                int dataSize = Integer.valueOf(stringPrefix.split("///")[2]);
                byte[] dataBuffer = Arrays.copyOfRange(bytesReceived, 80, bytesReceived.length);
                //check if we got all the data
                if (dataSize == dataBuffer.length) {
                    FileHandler.saveMediaFile(dataBuffer, stringPrefix.split("///")[1], receptionContext);
                    String response = "OK:" + NetworkCommunication.deviceIdentifier + "///" + stringPrefix.split("///")[1] + "///";
                    Koeko.networkCommunicationSingleton.sendStringToServer(response);
                } else {
                    System.err.println("the expected file size and the size actually read don't match");
                }
            } catch (NumberFormatException e) {
                System.err.println("Error in FILE prefix: unable to read file size");
            }
        } else {
            System.err.println("Error in FILE prefix: array too short");
        }
    }
}
