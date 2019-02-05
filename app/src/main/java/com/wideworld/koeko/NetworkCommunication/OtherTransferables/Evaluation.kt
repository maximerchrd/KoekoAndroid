package com.wideworld.koeko.NetworkCommunication.OtherTransferables

import com.wideworld.koeko.QuestionsManagement.TransferPrefix
import com.wideworld.koeko.QuestionsManagement.TransferableObject

class Evaluation : TransferableObject(groupPrefix = TransferPrefix.stateUpdate) {
    var evaluationType = 0
    var identifier = ""
    var name = ""
    var evaluation = 0.0
    var evalUpdate = false
    var testIdentifier = ""
    var testName = ""
}

object EvaluationTypes {
    const val questionEvaluation = 0
    const val objectiveEvaluation = 1
}