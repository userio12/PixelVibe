package com.pixelvibe.vedioplayer.core.player

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.pixelvibe.vedioplayer.core.player.audio.AudioEffectManager
import com.pixelvibe.vedioplayer.core.player.audio.AudioEffectState
import com.pixelvibe.vedioplayer.core.player.audio.EqualizerPreset
import org.junit.jupiter.api.Test

class AudioEffectManagerTest {

    @Test
    fun `initial state has correct defaults`() {
        val state = AudioEffectState()
        assertThat(state.isEqualizerAvailable).isFalse()
        assertThat(state.isEnabled).isFalse()
        assertThat(state.bassBoostLevel).isEqualTo(0)
        assertThat(state.virtualizerStrength).isEqualTo(0)
        assertThat(state.loudnessGain).isEqualTo(0)
    }

    @Test
    fun `equalizer preset enum has 10 entries`() {
        assertThat(EqualizerPreset.entries.size).isEqualTo(10)
    }

    @Test
    fun `equalizer preset labels are non-empty`() {
        EqualizerPreset.entries.forEach { preset ->
            assertThat(preset.label.isNotEmpty()).isTrue()
        }
    }

    @Test
    fun `rock preset has correct levels`() {
        val rock = EqualizerPreset.ROCK
        assertThat(rock.levels).isNotNull()
        assertThat(rock.levels!!.size).isEqualTo(10)
        assertThat(rock.levels[0]).isEqualTo(6)
    }

    @Test
    fun `flat preset has all zeros`() {
        val flat = EqualizerPreset.FLAT
        assertThat(flat.levels).isNotNull()
        flat.levels!!.forEach { level ->
            assertThat(level).isEqualTo(0)
        }
    }

    @Test
    fun `normal preset has null levels`() {
        assertThat(EqualizerPreset.NORMAL.levels).isNull()
    }
}
