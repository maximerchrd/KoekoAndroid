package com.wideworld.koeko.NetworkCommunication

class DataPrefix {
    var dataType = ""
    var dataLength = ""
    var directCorrection = ""
    var dataName = ""

    constructor() {
    }

    constructor(dataType: String, dataLength: String = "", directCorrection: String = "", dataName: String = "") {
        this.dataType = dataType
        this.dataLength = dataLength
        this.directCorrection = directCorrection
        this.dataName = dataName
    }

    fun parseToString(): String {
        var parsedString = ""
        when (dataType) {
            DataPref.multq -> parsedString = "$dataType///$dataLength///"
            DataPref.shrta -> parsedString = "$dataType///$dataLength///"
            DataPref.qid -> parsedString = "$dataType///$dataName///$directCorrection///"
            DataPref.file -> parsedString = "$dataType///$dataName///$dataLength///"
            DataPref.subObj -> parsedString = "$dataType///$dataLength///"
        }
        return parsedString
    }

    fun stringToPrefix(stringPrefix: String) {
        val length = stringPrefix.split("///").size
        dataType = stringPrefix.split("///")[0]
        when(dataType) {
            DataPref.multq -> if (length >= 2) {
                    dataLength = stringPrefix.split("///")[1]
            }
            DataPref.shrta -> if (length >= 2) {
                dataLength = stringPrefix.split("///")[1]
            }
            DataPref.qid -> if (length >=3) {
                dataName = stringPrefix.split("///")[1]
                directCorrection = stringPrefix.split("///")[2]
            }
            DataPref.file -> if (length >= 3) {
                dataName = stringPrefix.split("///")[1]
                dataLength = stringPrefix.split("///")[2]
            }
            DataPref.subObj -> if (length >= 2) {
                dataLength = stringPrefix.split("///")[1]
            }
        }
    }
}

object DataPref {
    const val multq = "MULTQ"
    const val shrta = "SHRTA"
    const val qid = "QID"
    const val file = "FILE"
    const val subObj = "SUBOBJ"
}