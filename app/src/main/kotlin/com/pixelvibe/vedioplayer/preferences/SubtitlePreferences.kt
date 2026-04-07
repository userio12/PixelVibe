package com.pixelvibe.vedioplayer.preferences

/**
 * Typed preference class for subtitle-related settings.
 * Complements PlayerPreferences with dedicated subtitle grouping.
 */
class SubtitlePreferences(private val store: PreferenceStore) {
    val onlineSearch = store.booleanPreference("subtitles_online_search", true)
    val preferredLanguages = store.stringPreference("subtitles_preferred_languages", "")
    val fontsDirectory = store.stringPreference("subtitles_fonts_dir", "")
    val autoLoad = store.booleanPreference("subtitles_auto_load", true)
    val saveLocation = store.stringPreference("subtitles_save_location", "")
    val fontSize = store.intPreference("subtitles_font_size", 0)
    val fontName = store.stringPreference("subtitles_font_name", "")
    val bold = store.booleanPreference("subtitles_bold", false)
    val textColor = store.intPreference("subtitles_text_color", -1)
    val borderColor = store.intPreference("subtitles_border_color", -1)
    val backgroundColor = store.intPreference("subtitles_bg_color", 0)
    val borderSize = store.intPreference("subtitles_border_size", 0)
    val scale = store.floatPreference("subtitles_scale", 1.0f)
    val position = store.intPreference("subtitles_position", 0)
    val overrideAss = store.booleanPreference("subtitles_override_ass", false)
    val scaleByWindow = store.booleanPreference("subtitles_scale_by_window", false)
    val primaryDelay = store.floatPreference("subtitles_primary_delay", 0f)
    val secondaryDelay = store.floatPreference("subtitles_secondary_delay", 0f)
}
