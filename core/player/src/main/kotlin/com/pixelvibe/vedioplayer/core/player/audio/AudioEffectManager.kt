package com.pixelvibe.vedioplayer.core.player.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class AudioEffectState(
    val isEqualizerAvailable: Boolean = false,
    val bandLevels: List<Short> = emptyList(),
    val bandFrequencies: List<Int> = emptyList(),
    val numberOfBands: Int = 0,
    val bandLevelRange: IntRange = 0..0,
    val bassBoostLevel: Short = 0,
    val virtualizerStrength: Short = 0,
    val loudnessGain: Int = 0,
    val isEnabled: Boolean = false,
    val currentPreset: EqualizerPreset = EqualizerPreset.NORMAL,
    val isHeadphoneConnected: Boolean = false
)

enum class EqualizerPreset(val label: String, val levels: List<Short>? = null) {
    NORMAL("Normal", null),
    CLASSICAL("Classical", listOf(-4, -4, -4, -2, 0, 2, 2, 4, 4, 4)),
    DANCE("Dance", listOf(6, 4, 2, 0, 0, 0, 2, 4, 6, 6)),
    FLAT("Flat", listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)),
    FOLK("Folk", listOf(-2, 0, 2, 4, 4, 4, 2, 0, -2, -4)),
    HEAVY_METAL("Heavy Metal", listOf(6, 4, 0, -2, -4, -4, -2, 0, 4, 6)),
    HIP_HOP("Hip Hop", listOf(6, 4, 2, 0, -2, -2, 0, 2, 4, 6)),
    JAZZ("Jazz", listOf(4, 2, 0, 2, 4, 4, 2, 0, 2, 4)),
    POP("Pop", listOf(-2, 0, 2, 4, 4, 4, 2, 0, -2, -4)),
    ROCK("Rock", listOf(6, 4, 2, -2, -4, -4, -2, 2, 4, 6))
}

class AudioEffectManager(private val context: Context) {

    private val _state = MutableStateFlow(AudioEffectState())
    val state: StateFlow<AudioEffectState> = _state.asStateFlow()

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var audioSessionId: Int = 0
    private var currentPresetLevels: List<Short>? = null

    init {
        runCatching { detectHeadphone() }
    }

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>?) {
            detectHeadphone()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>?) {
            detectHeadphone()
        }
    }

    private fun detectHeadphone() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val devices = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS).orEmpty()
        val isHeadphone = devices.any { device ->
            device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
            device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        }
        _state.value = _state.value.copy(isHeadphoneConnected = isHeadphone)
    }

    suspend fun init(sessionId: Int) = withContext(Dispatchers.IO) {
        audioSessionId = sessionId
        try {
            equalizer?.release()
            equalizer = Equalizer(0, sessionId).apply {
                enabled = false
                val bands = numberOfBands.toInt()
                val levels = (0 until bands).map { getBandLevel(it.toShort()) }
                val freqs = (0 until bands).map { getCenterFreq(it.toShort()).toInt() }
                val range = bandLevelRange
                _state.value = AudioEffectState(
                    isEqualizerAvailable = true,
                    bandLevels = levels,
                    bandFrequencies = freqs,
                    numberOfBands = bands,
                    bandLevelRange = range[0].toInt()..range[1].toInt()
                )
            }
        } catch (_: Exception) {
            _state.value = AudioEffectState(isEqualizerAvailable = false)
        }
    }

    fun setBandLevel(band: Int, level: Short) {
        equalizer?.setBandLevel(band.toShort(), level)
        currentPresetLevels = null
        val current = _state.value
        val newLevels = current.bandLevels.toMutableList()
        if (band in newLevels.indices) {
            newLevels[band] = level
            _state.value = current.copy(
                bandLevels = newLevels,
                currentPreset = EqualizerPreset.NORMAL
            )
        }
    }

    fun setPreset(preset: EqualizerPreset) {
        val levels = preset.levels ?: return
        val currentBands = _state.value.numberOfBands
        levels.take(currentBands).forEachIndexed { index, level ->
            equalizer?.setBandLevel(index.toShort(), level)
        }
        currentPresetLevels = levels
        _state.value = _state.value.copy(
            bandLevels = levels.take(currentBands).map { it.toShort() },
            currentPreset = preset
        )
    }

    fun setBassBoost(level: Short) {
        if (bassBoost == null) {
            bassBoost = BassBoost(0, audioSessionId)
        }
        bassBoost?.setStrength(level)
        bassBoost?.enabled = level > 0
        _state.value = _state.value.copy(bassBoostLevel = level)
    }

    fun setVirtualizer(strength: Short) {
        if (virtualizer == null) {
            virtualizer = Virtualizer(0, audioSessionId)
        }
        virtualizer?.setStrength(strength)
        virtualizer?.enabled = strength > 0
        _state.value = _state.value.copy(virtualizerStrength = strength)
    }

    fun setLoudnessGain(gain: Int) {
        if (loudnessEnhancer == null) {
            loudnessEnhancer = LoudnessEnhancer(audioSessionId)
        }
        loudnessEnhancer?.setTargetGain(gain)
        loudnessEnhancer?.enabled = gain > 0
        _state.value = _state.value.copy(loudnessGain = gain)
    }

    fun toggleEnabled() {
        val newEnabled = !_state.value.isEnabled
        equalizer?.enabled = newEnabled
        bassBoost?.enabled = newEnabled && _state.value.bassBoostLevel > 0
        virtualizer?.enabled = newEnabled && _state.value.virtualizerStrength > 0
        loudnessEnhancer?.enabled = newEnabled && _state.value.loudnessGain > 0
        _state.value = _state.value.copy(isEnabled = newEnabled)
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        loudnessEnhancer?.release()
    }
}
