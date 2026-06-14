package com.pixelvibe.vedioplayer.core.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface

data class SsdpDevice(
    val usn: String,
    val location: String,
    val server: String,
    val st: String
)

open class SsdpDiscovery {

    private val ssdpAddr = InetAddress.getByName("239.255.255.250")
    private val ssdpPort = 1900

    open fun discover(timeoutMs: Long = 3000): Flow<SsdpDevice> = flow {
        val socket = MulticastSocket()
        socket.soTimeout = timeoutMs.toInt()
        socket.timeToLive = 4

        val discoverMsg = (
            "M-SEARCH * HTTP/1.1\r\n" +
            "HOST: 239.255.255.250:1900\r\n" +
            "MAN: \"ssdp:discover\"\r\n" +
            "MX: 3\r\n" +
            "ST: ssdp:all\r\n" +
            "\r\n"
        ).toByteArray()

        try {
            NetworkInterface.getNetworkInterfaces()?.iterator()?.forEach { netif ->
                try {
                    socket.joinGroup(InetSocketAddress(ssdpAddr, ssdpPort), netif)
                } catch (_: Exception) {}
            }

            val sendPacket = DatagramPacket(discoverMsg, discoverMsg.size, ssdpAddr, ssdpPort)
            socket.send(sendPacket)

            val buffer = ByteArray(4096)
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < timeoutMs) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val data = String(packet.data, 0, packet.length)
                    parseSsdpResponse(data)?.let { emit(it) }
                } catch (_: Exception) {
                    break
                }
            }
        } finally {
            try {
                NetworkInterface.getNetworkInterfaces()?.iterator()?.forEach { netif ->
                    try { socket.leaveGroup(InetSocketAddress(ssdpAddr, ssdpPort), netif) } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
            socket.close()
        }
    }.flowOn(Dispatchers.IO)

    private fun parseSsdpResponse(data: String): SsdpDevice? {
        if (!data.contains("200 OK") && !data.contains("NOTIFY")) return null
        val lines = data.lines()
        var location = ""
        var usn = ""
        var server = ""
        var st = ""

        lines.forEach { line ->
            when {
                line.startsWith("LOCATION:", ignoreCase = true) -> location = line.substringAfter(":").trim()
                line.startsWith("USN:", ignoreCase = true) -> usn = line.substringAfter(":").trim()
                line.startsWith("SERVER:", ignoreCase = true) -> server = line.substringAfter(":").trim()
                line.startsWith("ST:", ignoreCase = true) -> st = line.substringAfter(":").trim()
            }
        }
        return if (location.isNotEmpty() && usn.isNotEmpty()) {
            SsdpDevice(usn = usn, location = location, server = server, st = st)
        } else null
    }
}
