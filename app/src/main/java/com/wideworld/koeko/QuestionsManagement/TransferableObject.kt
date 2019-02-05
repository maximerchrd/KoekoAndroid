package com.wideworld.koeko.QuestionsManagement

import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.charset.Charset

open class TransferableObject(groupPrefix: String) {
    var objectId: String? = null
    var prefix = groupPrefix
    var files = ArrayList<String>()
    var fileBytes = ByteArray(0)

    fun objectToByteArray(): ByteArray {
        var transferableObjectBytes: ByteArray
        var wholePrefix: String
        if (prefix.contentEquals(TransferPrefix.file)) {
            transferableObjectBytes = fileBytes
            wholePrefix = prefix + TransferPrefix.delimiter + objectId + TransferPrefix.delimiter + transferableObjectBytes.size + TransferPrefix.delimiter
        } else {
            var objectMapper = ObjectMapper()
            var tranferableObjectJson = objectMapper.writeValueAsString(this)
            transferableObjectBytes = tranferableObjectJson.toByteArray()
            wholePrefix = prefix + TransferPrefix.delimiter + this.javaClass.name + TransferPrefix.delimiter + transferableObjectBytes.size + TransferPrefix.delimiter
        }
        var prefixBytes = ByteArray(TransferPrefix.prefixSize)
        val prefixToBytes = wholePrefix.toByteArray(Charset.forName("UTF-8"))
        for (i in 0..(prefixToBytes.size - 1)) {
            prefixBytes[i] = prefixToBytes[i]
        }
        return prefixBytes + transferableObjectBytes
    }
}

object TransferPrefix {
    const val resource = "RESOURCE"
    const val stateUpdate = "STATEUPD"
    const val file = "FILE"
    const val delimiter = "/"
    const val prefixSize = 80

    fun getSize(prefix: String): Int {
        if (prefix.split(delimiter).size >= 3) {
            var size = -1
            try {
                size = prefix.split(delimiter)[2].toInt()
            } catch (e: NumberFormatException) {

            }
            return size
        } else {
            return -1
        }
    }

    fun getObjectName(prefix: String): String {
        if (prefix.split(delimiter).size >= 2) {
            return prefix.split(delimiter)[1]
        } else {
            return ""
        }
    }

    fun isResource(prefix: String): Boolean {
        return prefix.split(delimiter)[0].contentEquals(resource)
    }

    fun isStateUpdate(prefix: String): Boolean {
        return prefix.split(delimiter)[0].contentEquals(stateUpdate)
    }

    fun isFile(prefix: String): Boolean {
        return prefix.split(delimiter)[0].contentEquals(file)
    }
}