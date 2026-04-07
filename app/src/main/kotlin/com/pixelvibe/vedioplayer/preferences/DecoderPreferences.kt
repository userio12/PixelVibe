package com.pixelvibe.vedioplayer.preferences

/**
 * Typed preference class for decoder-related settings.
 */
class DecoderPreferences(private val store: PreferenceStore) {
    val mpvProfile = store.stringPreference("decoder_profile", "gpu")
    val hwDecoding = store.booleanPreference("decoder_hwdec", true)
    val gpuNext = store.booleanPreference("decoder_gpu_next", false)
    val vulkan = store.booleanPreference("decoder_vulkan", false)
    val yuv420p = store.booleanPreference("decoder_yuv420p", false)
    val debanding = store.stringPreference("decoder_deband", "none")
}
