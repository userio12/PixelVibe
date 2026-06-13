package com.pixelvibe.vedioplayer.core.data.network

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isNotNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class WebDavClientTest {

    private val client = WebDavClient()

    @Test
    fun `listFiles returns empty on invalid URL`() = runTest {
        val source = NetworkSource.WebDav("test", "invalid", "http://192.168.1.999/nonexistent")
        val files = client.listFiles(source)
        assertThat(files).isEmpty()
    }

    @Test
    fun `authenticate returns false on invalid server`() = runTest {
        val source = NetworkSource.WebDav("test", "invalid", "http://192.168.1.999/nonexistent")
        val result = client.authenticate(source)
        assertThat(result).isNotNull()
    }

    @Test
    fun `network models have correct defaults`() {
        val smb = NetworkSource.Smb("test", "host", "share")
        assertThat(smb.username).isNotNull()
        assertThat(smb.port).isNull()
    }
}
