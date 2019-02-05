package com.wideworld.koeko.NetworkCommunication.OtherTransferables

import com.wideworld.koeko.QuestionsManagement.TransferPrefix
import com.wideworld.koeko.QuestionsManagement.TransferableObject

class SyncedIds : TransferableObject(TransferPrefix.stateUpdate) {
    var ids = ArrayList<String>()
}