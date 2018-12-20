package com.wideworld.koeko.NetworkCommunication.HotspotServer

import android.util.Log
import java.net.InetAddress

class ClientsGroup {
    val clients = mutableListOf<Client>()
    val TAG = "ClientsGroup"

    fun addClientCheckingInetAddress(client: Client) {
        var inetAddress: String = client.inetAddres.toString()
        for (clientInGroup: Client in clients) {
            if (clientInGroup.inetAddres.toString() == inetAddress) {
                //update streams and return
                clientInGroup.inputStream = client.inputStream
                clientInGroup.outputStream = client.outputStream
                return
            }
        }
        clients += client
        Log.v(TAG, "adding: ${client.inetAddres}")
    }
}