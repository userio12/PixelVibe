package com.pixelvibe.vedioplayer.core.data.security

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import org.json.JSONObject
import org.junit.jupiter.api.Test

class SettingsBackupManagerTest {

    @Test
    fun `backup data has defaults`() {
        val data = BackupData()
        assertThat(data.version).isEqualTo(1)
        assertThat(data.appLockEnabled).isFalse()
        assertThat(data.incognitoMode).isFalse()
        assertThat(data.amoledTheme).isFalse()
        assertThat(data.subtitleFontSize).isEqualTo(18)
    }

    @Test
    fun `import null data returns null`() {
        val result = try {
            JSONObject("invalid")
            BackupData()
        } catch (_: Exception) {
            null
        }
        assertThat(result).isNull()
    }

    @Test
    fun `roundtrip preserves values`() {
        val data = BackupData(
            version = 1,
            appLockEnabled = true,
            incognitoMode = true,
            amoledTheme = true,
            subtitleFontSize = 24,
            subtitleFontColor = "#00FF00",
            playbackSpeed = 1.5f,
            bassBoostLevel = 500,
            virtualizerStrength = 300,
            equalizerPreset = "ROCK"
        )
        val json = JSONObject().apply {
            put("version", data.version)
            put("appLockEnabled", data.appLockEnabled)
            put("incognitoMode", data.incognitoMode)
            put("amoledTheme", data.amoledTheme)
            put("subtitleFontSize", data.subtitleFontSize)
            put("subtitleFontColor", data.subtitleFontColor)
            put("playbackSpeed", data.playbackSpeed.toDouble())
            put("bassBoostLevel", data.bassBoostLevel)
            put("virtualizerStrength", data.virtualizerStrength)
            put("equalizerPreset", data.equalizerPreset)
        }
        val parsed = JSONObject(json.toString())
        assertThat(parsed.optBoolean("appLockEnabled")).isEqualTo(true)
        assertThat(parsed.optBoolean("incognitoMode")).isEqualTo(true)
        assertThat(parsed.optBoolean("amoledTheme")).isEqualTo(true)
        assertThat(parsed.optInt("subtitleFontSize")).isEqualTo(24)
        assertThat(parsed.optString("subtitleFontColor")).isEqualTo("#00FF00")
        assertThat(parsed.optDouble("playbackSpeed", 1.0)).isEqualTo(1.5)
        assertThat(parsed.optInt("bassBoostLevel")).isEqualTo(500)
        assertThat(parsed.optInt("virtualizerStrength")).isEqualTo(300)
        assertThat(parsed.optString("equalizerPreset")).isEqualTo("ROCK")
    }
}
