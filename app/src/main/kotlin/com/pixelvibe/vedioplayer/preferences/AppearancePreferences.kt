package com.pixelvibe.vedioplayer.preferences

/**
 * Typed preference class for appearance-related settings.
 * Complements PlayerPreferences with dedicated appearance grouping.
 */
class AppearancePreferences(private val store: PreferenceStore) {
    val appTheme = store.stringPreference("appearance_theme", "dynamic")
    val amoledMode = store.booleanPreference("appearance_amoled", false)
    val materialYou = store.booleanPreference("appearance_material_you", true)
    val showNewVideoLabel = store.booleanPreference("appearance_new_video_label", true)
    val newVideoLabelDays = store.intPreference("appearance_new_video_days", 7)
    val showNetworkThumbnails = store.booleanPreference("appearance_network_thumbnails", false)
    val autoScroll = store.booleanPreference("appearance_auto_scroll", true)
    val watchedThreshold = store.intPreference("appearance_watched_threshold", 90)
    val hideButtonBackgrounds = store.booleanPreference("appearance_hide_button_bg", false)
    val fullNames = store.booleanPreference("appearance_full_names", false)
}
