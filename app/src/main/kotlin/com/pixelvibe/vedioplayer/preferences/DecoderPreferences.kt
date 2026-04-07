package com.pixelvibe.vedioplayer.preferences

/**
 * Decoder-related preferences.
 */
class DecoderPreferences(private val store: PreferenceStore) {
    val hwDecoding = store.booleanPreference("decoder_hwdec", true)
    val gpuNext = store.booleanPreference("decoder_gpu_next", false)
    val vulkan = store.booleanPreference("decoder_vulkan", false)
    val yuv420p = store.booleanPreference("decoder_yuv420p", false)
    val debanding = store.stringPreference("decoder_deband", "none")
}
