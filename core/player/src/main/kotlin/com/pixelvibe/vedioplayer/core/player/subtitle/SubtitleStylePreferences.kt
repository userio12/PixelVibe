package com.pixelvibe.vedioplayer.core.player.subtitle

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.subtitlePrefs by preferencesDataStore(name = "subtitle_style")

data class SubtitleStyle(
    val fontSize: Int = 18,
    val fontColor: String = "#FFFFFF",
    val outlineColor: String = "#000000",
    val outlineWidth: Float = 1f,
    val backgroundColor: String = "#80000000",
    val position: SubtitlePosition = SubtitlePosition.BOTTOM,
    val isBilingual: Boolean = false
)

enum class SubtitlePosition { TOP, MIDDLE, BOTTOM }

open class SubtitleStylePreferences(private val context: Context) {

    private val FONT_SIZE = intPreferencesKey("font_size")
    private val FONT_COLOR = stringPreferencesKey("font_color")
    private val OUTLINE_COLOR = stringPreferencesKey("outline_color")
    private val OUTLINE_WIDTH = floatPreferencesKey("outline_width")
    private val BACKGROUND_COLOR = stringPreferencesKey("background_color")
    private val POSITION = stringPreferencesKey("position")
    private val IS_BILINGUAL = stringPreferencesKey("is_bilingual")

    open val style: Flow<SubtitleStyle> = context.subtitlePrefs.data.map { prefs ->
        SubtitleStyle(
            fontSize = prefs[FONT_SIZE] ?: 18,
            fontColor = prefs[FONT_COLOR] ?: "#FFFFFF",
            outlineColor = prefs[OUTLINE_COLOR] ?: "#000000",
            outlineWidth = prefs[OUTLINE_WIDTH] ?: 1f,
            backgroundColor = prefs[BACKGROUND_COLOR] ?: "#80000000",
            position = try {
                SubtitlePosition.valueOf(prefs[POSITION] ?: "BOTTOM")
            } catch (_: Exception) { SubtitlePosition.BOTTOM },
            isBilingual = prefs[IS_BILINGUAL]?.toBooleanStrictOrNull() ?: false
        )
    }

    open suspend fun updateStyle(style: SubtitleStyle) {
        context.subtitlePrefs.edit { prefs ->
            prefs[FONT_SIZE] = style.fontSize
            prefs[FONT_COLOR] = style.fontColor
            prefs[OUTLINE_COLOR] = style.outlineColor
            prefs[OUTLINE_WIDTH] = style.outlineWidth
            prefs[BACKGROUND_COLOR] = style.backgroundColor
            prefs[POSITION] = style.position.name
            prefs[IS_BILINGUAL] = style.isBilingual.toString()
        }
    }

    open suspend fun updateFontSize(size: Int) {
        context.subtitlePrefs.edit { it[FONT_SIZE] = size }
    }

    open suspend fun updateFontColor(color: String) {
        context.subtitlePrefs.edit { it[FONT_COLOR] = color }
    }

    open suspend fun updateBilingual(enabled: Boolean) {
        context.subtitlePrefs.edit { it[IS_BILINGUAL] = enabled.toString() }
    }
}
