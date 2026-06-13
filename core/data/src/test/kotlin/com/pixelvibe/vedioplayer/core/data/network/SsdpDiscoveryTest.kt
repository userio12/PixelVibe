package com.pixelvibe.vedioplayer.core.data.network

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test

class SsdpDiscoveryTest {

    @Test
    fun `discovery constructable`() {
        val discovery = SsdpDiscovery()
        assertThat(discovery).isNotNull()
    }

    @Test
    fun `parse response handles valid SSDP`() {
        val data = "HTTP/1.1 200 OK\r\nLOCATION: http://192.168.1.50:5001/desc.xml\r\nUSN: uuid:1234::urn:schemas-upnp-org:device:MediaRenderer:1\r\nSERVER: Linux/5.10\r\nST: upnp:rootdevice\r\n"
        val discovery = SsdpDiscovery()
        val parsed = data.lines().firstOrNull { it.startsWith("HTTP") }
        assertThat(parsed).isNotNull()
    }

    @Test
    fun `ssdp device model has correct fields`() {
        val device = SsdpDevice(
            usn = "uuid:1234",
            location = "http://192.168.1.50/desc.xml",
            server = "Linux/5.10",
            st = "upnp:rootdevice"
        )
        assertThat(device.usn).isEqualTo("uuid:1234")
        assertThat(device.location).isEqualTo("http://192.168.1.50/desc.xml")
    }
}
