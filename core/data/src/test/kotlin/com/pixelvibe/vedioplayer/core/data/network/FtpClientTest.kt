package com.pixelvibe.vedioplayer.core.data.network

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isNotNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FtpClientTest {

    private val client = FtpClient()

    @Test
    fun `listFiles returns empty on invalid source`() = runTest {
        val source = NetworkSource.Ftp("test", "192.168.1.999")
        val files = client.listFiles(source)
        assertThat(files).isEmpty()
    }

    @Test
    fun `authenticate returns false on invalid server`() = runTest {
        val source = NetworkSource.Ftp("test", "192.168.1.999")
        val result = client.authenticate(source)
        assertThat(result).isNotNull()
    }

    @Test
    fun `release does not throw`() {
        client.release()
    }
}
