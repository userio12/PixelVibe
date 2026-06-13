package com.pixelvibe.vedioplayer.core.player

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isNotNull
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleSearchClient
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SubtitleSearchClientTest {

    private val client = SubtitleSearchClient()

    @Test
    fun `search returns empty list on network error`() = runTest {
        val results = client.search("")
        assertThat(results).isEmpty()
    }

    @Test
    fun `searchByHash returns empty list on network error`() = runTest {
        val results = client.searchByHash("invalid", 0)
        assertThat(results).isEmpty()
    }

    @Test
    fun `client is not null`() {
        assertThat(client).isNotNull()
    }
}
