package com.wideworld.koeko.QuestionsManagement


class ObjectiveTransferable: TransferableObject(TransferPrefix.resource) {
    var _objectiveName = ""
    var _objectiveLevel = -1
    var questionId = ""
}