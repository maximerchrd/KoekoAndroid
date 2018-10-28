package com.wideworld.koeko.NetworkCommunication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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

    public NearbyReceptionProtocol(Context context, NearbyCommunication nearbyCommunication) {
        this.receptionContext = context;
        this.dataConversion = new DataConversion(context);
        this.nearbyCommunication = nearbyCommunication;
    }

    public void receivedData(byte[] bytesReceived) {
        try {
            if (bytesReceived.length >= DataConversion.prefixSize) {
                byte[] prefix = new byte[DataConversion.prefixSize];
                for (int i = 0; i < 80; i++) {
                    prefix[i] = bytesReceived[i];
                }

                String stringPrefix = new String(prefix, "UTF-8");

                if (stringPrefix.split("///")[0].split(":")[0].contentEquals("MULTQ")) {
                    receivedMULTQ(bytesReceived);
                } else if (stringPrefix.split("///")[0].split(":")[0].contentEquals("SHRTA")) {
                    receivedSHRTA(bytesReceived);
                } else if (stringPrefix.split("///")[0].split(":")[0].contentEquals("QID")) {
                    receivedQID(stringPrefix);
                } else if (stringPrefix.split("///")[0].split(":")[0].contentEquals("SYNCIDS")) {
                    receivedSYNCIDS(bytesReceived);
                } else if (stringPrefix.split("///")[0].split(":")[0].contentEquals("EVAL")) {
                    DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(stringPrefix.split("///")[2],
                            stringPrefix.split("///")[1], Koeko.networkCommunicationSingleton.getLastAnswer());
                } else if (stringPrefix.split("///")[0].split(":")[0].contentEquals("UPDEV")) {
                    DbTableIndividualQuestionForResult.setEvalForQuestion(Double.valueOf(stringPrefix.split("///")[1]), stringPrefix.split("///")[2]);
                } else if (stringPrefix.split("///")[0].split(":")[0].contentEquals("CORR")) {
                    Intent mIntent = new Intent(receptionContext, CorrectedQuestionActivity.class);
                    Bundle bun = new Bundle();
                    bun.putString("questionID", stringPrefix.split("///")[1]);
                    mIntent.putExtras(bun);
                    receptionContext.startActivity(mIntent);
                } else if (stringPrefix.split("///")[0].split(":")[0].contentEquals("TEST")) {
                    Test newTest = dataConversion.byteToTest(bytesReceived);
                    DbTableTest.insertTest(newTest);
                } else if (stringPrefix.split("///")[0].split(":")[0].contentEquals("OEVAL")) {
                    //TODO: implement OEVAL
                } else if (stringPrefix.split("///")[0].split(":")[0].contentEquals("FILE")) {
                    receivedFILE(stringPrefix, bytesReceived);
                } else {
                    System.err.println("Prefix not supported");
                }
            } else {
                System.err.println("NEARBY: Problem with prefix, bytesReceived too short!");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void receivedSYNCIDS(byte[] bytesReceived) {
        byte[] idsBytes = Arrays.copyOfRange(bytesReceived, 80, bytesReceived.length);
        Koeko.networkCommunicationSingleton.idsToSync.addAll(dataConversion.bytesToIdsList(idsBytes));
    }

    private void receivedMULTQ(byte[] bytesReceived) {
        QuestionMultipleChoice questionMultipleChoice = dataConversion.bytearrayvectorToMultChoiceQuestion(bytesReceived);
        DbTableQuestionMultipleChoice.addMultipleChoiceQuestion(questionMultipleChoice);
        String response = "OK///" + questionMultipleChoice.getId() + "///";
        nearbyCommunication.sendBytes(response.getBytes());
    }

    private void receivedSHRTA(byte[] bytesReceived) {
        QuestionShortAnswer questionShortAnswer = dataConversion.bytearrayvectorToShortAnswerQuestion(bytesReceived);
        DbTableQuestionShortAnswer.addShortAnswerQuestion(questionShortAnswer);
        String response = "OK///" + questionShortAnswer.getId() + "///";
        nearbyCommunication.sendBytes(response.getBytes());
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
                    String response = "OK///" + stringPrefix.split("///")[1] + "///";
                    Koeko.networkCommunicationSingleton.sendDataToClient(response.getBytes());
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
