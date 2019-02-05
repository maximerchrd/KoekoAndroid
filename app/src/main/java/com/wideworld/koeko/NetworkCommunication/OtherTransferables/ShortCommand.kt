package com.wideworld.koeko.NetworkCommunication.OtherTransferables

import com.wideworld.koeko.QuestionsManagement.TransferPrefix
import com.wideworld.koeko.QuestionsManagement.TransferableObject

class ShortCommand : TransferableObject(groupPrefix = TransferPrefix.stateUpdate) {
    var command = -1
    var optionalArgument1 = ""
    var optionalArgument2 = ""
    var optionalArgument3 = ""
}

object ShortCommands {
    const val correction = 0
    const val advertiser = 1
    const val discoverer = 2
    const val thirdlayer = 3
    const val connected = 4
    const val disconnected = 5
    const val reconnected = 6
    const val gameScore = 7
}