package com.wideworld.koeko.NetworkCommunication;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.QuestionsManagement.QuestionView;
import com.wideworld.koeko.QuestionsManagement.SubjectsAndObjectivesForQuestion;
import com.wideworld.koeko.database_management.DbTableLearningObjective;
import com.wideworld.koeko.database_management.DbTableQuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableSubject;

import java.util.Arrays;

public class ReceptionProtocol {
    static public void receivedQuestionData(DataConversion dataConversion, byte[] bytesReceived) {
        if (bytesReceived.length > 80) {
            byte[] questionBytes = Arrays.copyOfRange(bytesReceived, 80, bytesReceived.length);
            //Convert data and save question
            QuestionView questionView = dataConversion.bytearrayToQuestionView(questionBytes);
            DbTableQuestionMultipleChoice.addQuestionFromView(questionView);

            Koeko.networkCommunicationSingleton.sendStringToServer("OK:" + NetworkCommunication.deviceIdentifier + "///" + questionView.getID() + "///");

            if (NearbyCommunication.deviceRole == NearbyCommunication.ADVERTISER_ROLE) {
                Koeko.networkCommunicationSingleton.sendDataToClient(bytesReceived);
            }

            if (questionView.getQUESTION().contains("7492qJfzdDSB")) {
                Koeko.networkCommunicationSingleton.sendStringToServer("ACCUSERECEPTION");
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

    static public void receivedTest(DataConversion dataConversion, byte[] bytesReceived) {

    }
}
