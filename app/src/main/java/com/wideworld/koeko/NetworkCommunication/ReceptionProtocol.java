package com.wideworld.koeko.NetworkCommunication;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wideworld.koeko.Activities.CorrectedQuestionActivity;
import com.wideworld.koeko.Activities.InteractiveModeActivity;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.NetworkCommunication.HotspotServer.HotspotServer;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.ClientToServerTransferable;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.CtoSPrefix;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.Evaluation;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.EvaluationTypes;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.QuestionIdentifier;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.ShortCommand;
import com.wideworld.koeko.NetworkCommunication.OtherTransferables.ShortCommands;
import com.wideworld.koeko.QuestionsManagement.GameView;
import com.wideworld.koeko.QuestionsManagement.ObjectiveTransferable;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.QuestionView;
import com.wideworld.koeko.QuestionsManagement.SubjectTransferable;
import com.wideworld.koeko.QuestionsManagement.SubjectsAndObjectivesForQuestion;
import com.wideworld.koeko.QuestionsManagement.Test;
import com.wideworld.koeko.QuestionsManagement.TransferPrefix;
import com.wideworld.koeko.Tools.FileHandler;
import com.wideworld.koeko.database_management.DbTableIndividualQuestionForResult;
import com.wideworld.koeko.database_management.DbTableLearningObjective;
import com.wideworld.koeko.database_management.DbTableQuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableQuestionShortAnswer;
import com.wideworld.koeko.database_management.DbTableRelationTestObjective;
import com.wideworld.koeko.database_management.DbTableSubject;
import com.wideworld.koeko.database_management.DbTableTest;

import java.io.IOException;
import java.util.Arrays;

import static android.content.Context.WIFI_SERVICE;

public class ReceptionProtocol {
    private static ObjectMapper objectMapper = null;

    static public void receivedQuestionData(DataConversion dataConversion, byte[] bytesReceived) {
        if (bytesReceived.length > 80) {
            byte[] questionBytes = Arrays.copyOfRange(bytesReceived, 80, bytesReceived.length);
            //Convert data and save question
            QuestionView questionView = dataConversion.bytearrayToQuestionView(questionBytes);
            DbTableQuestionMultipleChoice.addQuestionFromView(questionView);

            sendOK(questionView.getID());

            if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
                Koeko.networkCommunicationSingleton.sendDataToClient(bytesReceived);
            }

            if (questionView.getQUESTION().contains("7492qJfzdDSB")) {
                Koeko.networkCommunicationSingleton.sendBytesToServer(new ClientToServerTransferable(CtoSPrefix.accuserReceptionPrefix).getTransferableBytes());
            }

            if (NetworkCommunication.network_solution == 1) {
                Koeko.networkCommunicationSingleton.idsToSync.add(questionView.getID());
            }
        }

    }

    static public void receivedSubObj(DataConversion dataConversion, byte[] bytesReceived) {
        if (bytesReceived.length > 80) {
            byte[] subObjBytes = Arrays.copyOfRange(bytesReceived, 80, bytesReceived.length);
            SubjectsAndObjectivesForQuestion subObj = dataConversion.bytearrayvectorToSubjectsNObjectives(subObjBytes);
            if (subObj != null) {
                String[] subjects = subObj.getSubjects();
                for (int i = 0; i < subjects.length; i++) {
                    DbTableSubject.addSubject(subjects[i], subObj.getQuestionId());
                }
                String[] objectives = subObj.getObjectives();
                for (int i = 0; i < objectives.length; i++) {
                    DbTableLearningObjective.addLearningObjective(objectives[i], -1, subObj.getQuestionId());
                }

                if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
                    Koeko.networkCommunicationSingleton.sendDataToClient(bytesReceived);
                }
            }
        }
    }

    public static void receivedStateUpdate(String sizesPrefix, byte[] stateUpdBytes, byte[] allBytesReceived,
                                           Context context) throws IOException {
        String receivedClass = sizesPrefix.split(TransferPrefix.delimiter)[1];

        switch (getClassName(receivedClass)) {
            case "QuestionIdentifier":
                receivedQuestionIdentifier(stateUpdBytes, allBytesReceived);
                break;
            case "Evaluation":
                receivedEvaluation(stateUpdBytes);
                break;
            case "ShortCommand":
                receivedShortCommand(stateUpdBytes, allBytesReceived, context);
                break;
            default:
                System.err.println("State Update Object Unknown");
        }
    }

    private static void receivedShortCommand(byte[] stateUpdBytes, byte[] allBytesReceived, Context context) throws IOException {
        ShortCommand shortCommand = getObjectMapper().readValue(new String(stateUpdBytes), ShortCommand.class);
        switch (shortCommand.getCommand()) {
            case ShortCommands.correction:
                Intent mIntent = new Intent(context, CorrectedQuestionActivity.class);
                Bundle bun = new Bundle();
                bun.putString("questionID", shortCommand.getOptionalArgument1());
                mIntent.putExtras(bun);
                context.startActivity(mIntent);
                break;
            case ShortCommands.connected:
                Koeko.wifiCommunicationSingleton.connectionSuccess = 1;
                Koeko.networkCommunicationSingleton.mInteractiveModeActivity.showConnected();
                if (Koeko.wifiCommunicationSingleton.ServerWifiSSID.length() == 0) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    Koeko.wifiCommunicationSingleton.ServerWifiSSID = wifiInfo.getSSID();
                }
                break;
            case ShortCommands.advertiser:
                Koeko.networkCommunicationSingleton.getmNearbyCom().startAdvertising();
                break;
            case ShortCommands.discoverer:
                HotspotServer hotspotServerHotspot = new HotspotServer(shortCommand.getOptionalArgument1(),
                        shortCommand.getOptionalArgument2(), context);
                Koeko.networkCommunicationSingleton.setHotspotServerHotspot(hotspotServerHotspot);
                Koeko.networkCommunicationSingleton.getmNearbyCom().startDiscovery();
                System.out.println("Tried to start discovery");
                break;
            case ShortCommands.thirdlayer:
                //THIS POSSIBILITY IS NOT USED FOR NOW
                Koeko.wifiCommunicationSingleton.secondLayerMasterIp = shortCommand.getOptionalArgument3();
                Koeko.wifiCommunicationSingleton.tryToJoinWifi(shortCommand.getOptionalArgument1(),
                        shortCommand.getOptionalArgument2());
                System.out.println("Try to join third layer");
                break;
            case ShortCommands.gameScore:
                Double scoreTeamOne = Double.valueOf(shortCommand.getOptionalArgument1());
                Double scoreTeamTwo = Double.valueOf(shortCommand.getOptionalArgument2());
                if (Koeko.currentGameFragment != null) {
                    Koeko.currentGameFragment.changeScore(scoreTeamOne, scoreTeamTwo);
                }
                break;
            default:
                System.err.println("Short Command Unknown");
        }
    }

    private static void receivedEvaluation(byte[] stateUpdBytes) throws IOException {
        Evaluation evaluation = getObjectMapper().readValue(new String(stateUpdBytes), Evaluation.class);
        if (evaluation.getEvaluationType() == EvaluationTypes.questionEvaluation) {
            if (!evaluation.getEvalUpdate()) {
                DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(evaluation.getIdentifier(),
                        String.valueOf(evaluation.getEvaluation()), Koeko.networkCommunicationSingleton.getLastAnswer());
            } else {
                DbTableIndividualQuestionForResult.setEvalForQuestion(evaluation.getEvaluation(),
                        evaluation.getIdentifier());
            }
        } else if (evaluation.getEvaluationType() == EvaluationTypes.objectiveEvaluation) {
            DbTableLearningObjective.addLearningObjective(evaluation.getIdentifier(), evaluation.getName(), 0);
            DbTableRelationTestObjective.insertRelationTestObjective(evaluation.getTestIdentifier(), evaluation.getIdentifier());
            Test certificativeTest = new Test();
            certificativeTest.setTestName(evaluation.getTestName());
            certificativeTest.setTestType("CERTIF");
            certificativeTest.setIdGlobal(Long.getLong(evaluation.getTestIdentifier()));
            DbTableTest.insertTest(certificativeTest);
            DbTableIndividualQuestionForResult.addIndividualQuestionForStudentResult(evaluation.getIdentifier(),
                    String.valueOf(evaluation.getEvaluation()), 2, evaluation.getTestName());
            Log.d("INFO", "received OEVAL");
        }
    }

    private static void receivedQuestionIdentifier(byte[] stateUpdBytes, byte[] allBytesReceived) throws IOException {
        // stop and hide chronometer
        Koeko.networkCommunicationSingleton.mInteractiveModeActivity.runOnUiThread(() -> {
            if (Koeko.currentTestFragmentSingleton != null && Koeko.currentTestFragmentSingleton.testChronometer != null) {
                Koeko.currentTestFragmentSingleton.testChronometer.stop();
                Koeko.currentTestFragmentSingleton.testChronometer.reset();
            }
        });
        //reinitializing all types of displays
        Koeko.currentTestFragmentSingleton = null;
        Koeko.shrtaqActivityState = null;
        Koeko.qmcActivityState = null;
        if (InteractiveModeActivity.getCurrentTopFragment(Koeko.networkCommunicationSingleton.mInteractiveModeActivity
                .getSupportFragmentManager()) != null && !InteractiveModeActivity.getCurrentTopFragment(Koeko.networkCommunicationSingleton.mInteractiveModeActivity
                .getSupportFragmentManager()).getClass().getName().contains("GameFragment")) {
            Koeko.gameState = null;
        }

        QuestionIdentifier questionIdentifier = getObjectMapper().readValue(new String(stateUpdBytes), QuestionIdentifier.class);

        if (Long.valueOf(questionIdentifier.getIdentifier()) < 0) {
            //setup test and show it
            Long testId = -(Long.valueOf(questionIdentifier.getIdentifier()));
            Koeko.networkCommunicationSingleton.directCorrection = String.valueOf(questionIdentifier.getCorrectionMode());
            Koeko.networkCommunicationSingleton.launchTestActivity(testId, Koeko.networkCommunicationSingleton.directCorrection);
            Koeko.shrtaqActivityState = null;
            Koeko.qmcActivityState = null;
        } else {
            QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(questionIdentifier.getIdentifier());
            if (questionMultipleChoice.getQuestion().length() > 0) {
                questionMultipleChoice.setId(questionIdentifier.getIdentifier());
                Koeko.networkCommunicationSingleton.directCorrection = String.valueOf(questionIdentifier.getCorrectionMode());
                Koeko.networkCommunicationSingleton.launchMultChoiceQuestionActivity(questionMultipleChoice, Koeko.networkCommunicationSingleton.directCorrection);
                Koeko.shrtaqActivityState = null;
                Koeko.currentTestFragmentSingleton = null;
            } else {
                QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(questionIdentifier.getIdentifier());
                questionShortAnswer.setId(questionIdentifier.getIdentifier());
                Koeko.networkCommunicationSingleton.directCorrection = String.valueOf(questionIdentifier.getCorrectionMode());
                Koeko.networkCommunicationSingleton.launchShortAnswerQuestionActivity(questionShortAnswer, Koeko.networkCommunicationSingleton.directCorrection);
                Koeko.qmcActivityState = null;
                Koeko.currentTestFragmentSingleton = null;
            }
        }

        if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
            Koeko.networkCommunicationSingleton.sendDataToClient(allBytesReceived);
        }
    }

    public static void receivedResource(String sizesPrefix, byte[] questionBytes, byte[] allBytesReceived) throws IOException {
        String receivedClass = sizesPrefix.split(TransferPrefix.delimiter)[1];

        switch (getClassName(receivedClass)) {
            case "QuestionView":
                receivedQuestionView(questionBytes, allBytesReceived);
                break;
            case "SubjectTransferable":
                receivedSubject(questionBytes, allBytesReceived);
                break;
            case "ObjectiveTransferable":
                receivedObjective(questionBytes, allBytesReceived);
                break;
            case "GameView":
                receivedGameView(questionBytes);
                break;
            case "TestView":
                receivedTestView(questionBytes, allBytesReceived);
                break;
            default:
                System.err.println("Resource Object Unknown");
        }
    }

    private static void receivedTestView(byte[] testBytes, byte[] allBytesReceived) {
        Test newTest = Koeko.wifiCommunicationSingleton.dataConversion.byteToTest(testBytes);
        DbTableTest.insertTest(newTest);

        if (NetworkCommunication.network_solution == 1) {
            Koeko.networkCommunicationSingleton.idsToSync.add(String.valueOf(newTest.getIdGlobal()));
            if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
                Koeko.networkCommunicationSingleton.getmNearbyCom().sendBytes(allBytesReceived);
            }
        }
    }

    private static void receivedGameView(byte[] questionBytes) throws IOException {
        GameView gameView = getObjectMapper().readValue(new String(questionBytes), GameView.class);
        //check if we got all the data
        Koeko.networkCommunicationSingleton.launchGameActivity(gameView, gameView.getTeam());
        Koeko.shrtaqActivityState = null;
        Koeko.qmcActivityState = null;
        Koeko.currentTestFragmentSingleton = null;
    }

    private static void receivedObjective(byte[] objectiveBytes, byte[] allBytesReceived) throws IOException {
        ObjectiveTransferable objectiveTransferable = getObjectMapper().readValue(new String(objectiveBytes), ObjectiveTransferable.class);
        DbTableLearningObjective.addLearningObjective(objectiveTransferable.get_objectiveName(),
                objectiveTransferable.get_objectiveLevel(), objectiveTransferable.getQuestionId());

        if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
            Koeko.networkCommunicationSingleton.sendDataToClient(allBytesReceived);
        }
    }

    private static void receivedSubject(byte[] subjectBytes, byte[] allBytesReceived) throws IOException {
        SubjectTransferable subject = getObjectMapper().readValue(new String(subjectBytes), SubjectTransferable.class);
        DbTableSubject.addSubject(subject.get_subjectName(), subject.getQuestionId());

        if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
            Koeko.networkCommunicationSingleton.sendDataToClient(allBytesReceived);
        }
    }

    private static void receivedQuestionView(byte[] questionBytes, byte[] allBytesReceived) throws IOException {
        QuestionView questionView = getObjectMapper().readValue(new String(questionBytes), QuestionView.class);
        DbTableQuestionMultipleChoice.addQuestionFromView(questionView);

        sendOK(questionView.getID());

        if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
            Koeko.networkCommunicationSingleton.sendDataToClient(allBytesReceived);
        }

        if (questionView.getQUESTION().contains("7492qJfzdDSB")) {
            Koeko.networkCommunicationSingleton.sendBytesToServer(new ClientToServerTransferable(CtoSPrefix.accuserReceptionPrefix).getTransferableBytes());
        }

        if (NetworkCommunication.network_solution == 1) {
            Koeko.networkCommunicationSingleton.idsToSync.add(questionView.getID());
        }
    }

    private static String getClassName(String wholeName) {
        String[] nameArray = wholeName.split("\\.");
        int lastIndex = nameArray.length - 1;
        if (lastIndex < 0) {
            lastIndex = 0;
        }
        return nameArray[lastIndex];
    }

    protected static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return objectMapper;
    }

    public static void receivedFile(String sizesPrefix, byte[] fileBytes, byte[] allBytesReceived,
                                    Context context) {
        String fileName = sizesPrefix.split(TransferPrefix.delimiter)[1];

        //check if we got all the data
        if (Integer.valueOf(sizesPrefix.split(TransferPrefix.delimiter)[2]) == fileBytes.length) {
            FileHandler.saveMediaFile(fileBytes, fileName, context);
            sendOK(fileName);

            if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
                Koeko.networkCommunicationSingleton.sendDataToClient(allBytesReceived);
            }
        } else {
            System.err.println("the expected file size and the size actually read don't match");
        }
    }

    private static void sendOK(String globalId) {
        ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.okPrefix);
        transferable.setOptionalArgument1(NetworkCommunication.deviceIdentifier);
        transferable.setOptionalArgument2(globalId);
        Koeko.networkCommunicationSingleton.sendBytesToServer(transferable.getTransferableBytes());
    }
}
