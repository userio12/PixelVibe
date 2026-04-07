package com.pixelvibe.vedioplayer.preferences

import com.pixelvibe.vedioplayer.preferences.PreferenceStore

/**
 * Typed preference class for audio-related settings.
 * Most audio settings are in PlayerPreferences, but this provides
 * a dedicated access point for audio-specific preference grouping.
 */
class AudioPreferences(private val store: PreferenceStore) {
    val pitchCorrection = store.booleanPreference("audio_pitch_correction", true)
    val volumeNormalization = store.booleanPreference("audio_volume_normalization", false)
    val audioChannels = store.stringPreference("audio_channels", "auto")
    val preferredAudioLanguage = store.stringPreference("audio_preferred_language", "")
    val volumeBoostCap = store.intPreference("audio_volume_boost_cap", 100)
    val audioDelay = store.floatPreference("audio_delay", 0f)
}
