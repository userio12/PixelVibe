package com.pixelvibe.vedioplayer.core.player

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import com.pixelvibe.vedioplayer.core.player.engine.PlaybackState
import org.junit.jupiter.api.Test

class PlaybackEngineTest {

    @Test
    fun `initial state has correct defaults`() {
        val state = PlaybackState()
        assertThat(state.isPlaying).isFalse()
    }
}
