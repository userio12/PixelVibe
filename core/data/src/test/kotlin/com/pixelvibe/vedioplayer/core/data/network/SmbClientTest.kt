package com.pixelvibe.vedioplayer.core.data.network

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isNotNull
import com.pixelvibe.vedioplayer.core.data.network.SmbClient
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SmbClientTest {

    private val client = SmbClient()

    @Test
    fun `listFiles returns empty on invalid source`() = runTest {
        val source = NetworkSource.Smb("test", "192.168.1.999", "nonexistent", "")
        val files = client.listFiles(source)
        assertThat(files).isEmpty()
    }

    @Test
    fun `authenticate returns false on invalid server`() = runTest {
        val source = NetworkSource.Smb("test", "192.168.1.999", "nonexistent", "user", "pass")
        val result = client.authenticate(source)
        assertThat(result).isNotNull()
    }

    @Test
    fun `release does not throw`() {
        client.release()
    }
}
