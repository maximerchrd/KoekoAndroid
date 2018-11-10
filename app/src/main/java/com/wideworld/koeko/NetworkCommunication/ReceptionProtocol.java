package com.wideworld.koeko.NetworkCommunication;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.QuestionsManagement.QuestionView;
import com.wideworld.koeko.database_management.DbTableQuestionMultipleChoice;

import java.util.Arrays;

public class ReceptionProtocol {
    static public void receivedMULTQ(DataConversion dataConversion, byte[] bytesReceived) {
        if (bytesReceived.length > 80) {
            byte[] questionBytes = Arrays.copyOfRange(bytesReceived, 80, bytesReceived.length);
            //Convert data and save question
            QuestionView questionView = dataConversion.bytearrayToQuestionView(questionBytes);
            DbTableQuestionMultipleChoice.addQuestionFromView(questionView);

            Koeko.networkCommunicationSingleton.sendStringToServer("OK///" + questionView.getID() + "///");

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
}
