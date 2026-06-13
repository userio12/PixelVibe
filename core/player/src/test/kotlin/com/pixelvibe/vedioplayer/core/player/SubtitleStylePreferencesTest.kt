package com.pixelvibe.vedioplayer.core.player

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitlePosition
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleStyle
import org.junit.jupiter.api.Test

class SubtitleStylePreferencesTest {

    @Test
    fun `default subtitle style has expected values`() {
        val style = SubtitleStyle()
        assertThat(style.fontSize).isEqualTo(18)
        assertThat(style.fontColor).isEqualTo("#FFFFFF")
        assertThat(style.position).isEqualTo(SubtitlePosition.BOTTOM)
        assertThat(style.isBilingual).isEqualTo(false)
    }

    @Test
    fun `subtitle position enum has three values`() {
        val values = SubtitlePosition.values()
        assertThat(values.size).isEqualTo(3)
    }
}
