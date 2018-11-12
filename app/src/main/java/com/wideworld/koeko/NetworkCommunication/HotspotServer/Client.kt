package com.wideworld.koeko.NetworkCommunication.HotspotServer

import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.util.*

class Client(inetAddress: InetAddress, outputStream: OutputStream, inputStream: InputStream) {
    var inetAddres: InetAddress = inetAddress
    var outputStream: OutputStream = outputStream
    var inputStream: InputStream = inputStream
    var uuid: String = ""
    var connected: Boolean = true
}