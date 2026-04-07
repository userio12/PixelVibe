package com.pixelvibe.vedioplayer.domain

import android.content.Context
import is.xyz.mpv.MPVLib
import java.io.File

/**
 * Anime4K upscaling modes/presets.
 */
enum class Anime4KMode(val id: String, val displayName: String) {
    OFF("off", "Off"),
    A("a", "A"),
    B("b", "B"),
    C("c", "C"),
    A_PLUS("a+", "A+"),
    B_PLUS("b+", "B+"),
    C_PLUS("c+", "C+")
}

/**
 * Anime4K quality levels.
 */
enum class Anime4KQuality(val id: String, val displayName: String) {
    FAST("fast", "Fast"),
    BALANCED("balanced", "Balanced"),
    HIGH("high", "High")
}

/**
 * Manages Anime4K upscaling filter configuration.
 * Requires legacy GPU backend (not gpu-next on OpenGL).
 */
class Anime4KManager(
    private val context: Context
) {
    private val shaderDir = "shaders"
    private var currentMode = Anime4KMode.OFF
    private var currentQuality = Anime4KQuality.BALANCED
    private var isActive = false

    /**
     * Get the list of available shader files from assets.
     */
    private fun getShaderFiles(): List<String> {
        return try {
            context.assets.list(shaderDir)?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get shader files for the current mode and quality.
     */
    private fun getShadersForModeAndQuality(): List<String> {
        if (currentMode == Anime4KMode.OFF) return emptyList()

        val qualitySuffix = when (currentQuality) {
            Anime4KQuality.FAST -> "_S"
            Anime4KQuality.BALANCED -> "_M"
            Anime4KQuality.HIGH -> "_L"
        }

        val shaders = mutableListOf<String>()

        when (currentMode) {
            Anime4KMode.A -> {
                shaders.add("Anime4K_Clamp_Highlights.glsl")
                shaders.add("Anime4K_Upscale_CNN_x2${qualitySuffix}.glsl")
            }
            Anime4KMode.B -> {
                shaders.add("Anime4K_Clamp_Highlights.glsl")
                shaders.add("Anime4K_Upscale_Denoise_CNN_x2${qualitySuffix}.glsl")
            }
            Anime4KMode.C -> {
                shaders.add("Anime4K_Clamp_Highlights.glsl")
                shaders.add("Anime4K_Upscale_CNN_x2${qualitySuffix}.glsl")
                shaders.add("Anime4K_Upscale_Denoise_CNN_x2${qualitySuffix}.glsl")
            }
            Anime4KMode.A_PLUS -> {
                shaders.add("Anime4K_Clamp_Highlights.glsl")
                shaders.add("Anime4K_Upscale_CNN_x2${qualitySuffix}.glsl")
                shaders.add("Anime4K_Restore_CNN_Soft_${qualitySuffix.replace("_", "")}.glsl")
            }
            Anime4KMode.B_PLUS -> {
                shaders.add("Anime4K_Clamp_Highlights.glsl")
                shaders.add("Anime4K_Upscale_Denoise_CNN_x2${qualitySuffix}.glsl")
                shaders.add("Anime4K_Restore_CNN_Soft_${qualitySuffix.replace("_", "")}.glsl")
            }
            Anime4KMode.C_PLUS -> {
                shaders.add("Anime4K_Clamp_Highlights.glsl")
                shaders.add("Anime4K_Upscale_CNN_x2${qualitySuffix}.glsl")
                shaders.add("Anime4K_Upscale_Denoise_CNN_x2${qualitySuffix}.glsl")
                shaders.add("Anime4K_Restore_CNN_Soft_${qualitySuffix.replace("_", "")}.glsl")
            }
            else -> {}
        }

        return shaders
    }

    /**
     * Apply Anime4K shaders to MPV.
     * Must be called with legacy GPU backend (not gpu-next).
     */
    fun apply() {
        if (currentMode == Anime4KMode.OFF) {
            disable()
            return
        }

        val shaderFiles = getShadersForModeAndQuality()
        if (shaderFiles.isEmpty()) return

        // Build glsl-shaders string
        val shaderPath = "~~~/${shaderDir}"
        val shaders = shaderFiles.joinToString(";") { "$shaderPath/$it" }

        MPVLib.setPropertyString("glsl-shaders", shaders)
        isActive = true
    }

    /**
     * Disable Anime4K shaders.
     */
    fun disable() {
        MPVLib.setPropertyString("glsl-shaders", "")
        isActive = false
    }

    /**
     * Set the current mode and apply.
     */
    fun setMode(mode: Anime4KMode) {
        currentMode = mode
        apply()
    }

    /**
     * Set the current quality and apply.
     */
    fun setQuality(quality: Anime4KQuality) {
        currentQuality = quality
        if (isActive) apply()
    }

    /**
     * Check if Anime4K is currently active.
     */
    fun isAnime4KActive(): Boolean = isActive

    /**
     * Get current mode.
     */
    fun getCurrentMode(): Anime4KMode = currentMode

    /**
     * Get current quality.
     */
    fun getCurrentQuality(): Anime4KQuality = currentQuality

    /**
     * Check if Anime4K is compatible with current MPV configuration.
     * Requires: legacy GPU backend (not gpu-next) + OpenGL context.
     */
    fun isCompatible(gpuNextEnabled: Boolean): Boolean {
        // Anime4K requires legacy GPU path for shader compatibility
        return !gpuNextEnabled
    }
}
