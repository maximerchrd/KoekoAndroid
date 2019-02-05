package com.wideworld.koeko.NetworkCommunication.OtherTransferables

import com.wideworld.koeko.QuestionsManagement.TransferPrefix
import com.wideworld.koeko.QuestionsManagement.TransferableObject

class QuestionIdentifier : TransferableObject(TransferPrefix.stateUpdate) {
    var identifier = ""
    var correctionMode = -1
}