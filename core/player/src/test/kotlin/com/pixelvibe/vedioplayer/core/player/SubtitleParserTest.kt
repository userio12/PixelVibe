package com.pixelvibe.vedioplayer.core.player

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleParser
import org.junit.jupiter.api.Test

class SubtitleParserTest {

    @Test
    fun `parse valid SRT content`() {
        val srt = """
            1
            00:00:01,000 --> 00:00:04,000
            Hello world

            2
            00:00:05,000 --> 00:00:08,500
            This is a test subtitle
        """.trimIndent()

        val cues = SubtitleParser.parseSrt(srt)
        assertThat(cues).isNotEmpty()
        assertThat(cues.size).isEqualTo(2)
        assertThat(cues[0].text).isEqualTo("Hello world")
        assertThat(cues[0].startMs).isEqualTo(1000)
        assertThat(cues[0].endMs).isEqualTo(4000)
    }

    @Test
    fun `parse empty SRT returns empty list`() {
        val cues = SubtitleParser.parseSrt("")
        assertThat(cues.size).isEqualTo(0)
    }
}
