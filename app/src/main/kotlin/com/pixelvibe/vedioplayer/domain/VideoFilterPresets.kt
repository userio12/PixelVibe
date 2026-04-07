package com.pixelvibe.vedioplayer.domain

/**
 * Represents a video filter preset with brightness, saturation, contrast, gamma, hue, sharpness.
 * Values range from -5 to +5 (0 = neutral).
 */
data class VideoFilterPreset(
    val name: String,
    val brightness: Int = 0,
    val saturation: Int = 0,
    val contrast: Int = 0,
    val gamma: Int = 0,
    val hue: Int = 0,
    val sharpness: Int = 0
) {
    /**
     * Convert to video engine vf (video filter) string.
     */
    fun toMpvVfString(): String {
        val filters = mutableListOf<String>()

        if (brightness != 0) filters.add("brightness=$brightness")
        if (saturation != 0) filters.add("saturation=${100 + saturation * 10}")
        if (contrast != 0) filters.add("contrast=${100 + contrast * 10}")
        if (gamma != 0) filters.add("gamma=${100 + gamma * 10}")
        if (hue != 0) filters.add("hue=$hue")
        if (sharpness != 0) filters.add("sharpen=${sharpness * 0.1}")

        return if (filters.isEmpty()) "" else "lavfi=[eq=${filters.joinToString(":")}]"
    }
}

/**
 * 13 built-in video filter presets.
 */
object VideoFilterPresets {
    val None = VideoFilterPreset("None")

    val Vivid = VideoFilterPreset(
        name = "Vivid",
        saturation = 3,
        contrast = 1
    )

    val WarmTone = VideoFilterPreset(
        name = "Warm Tone",
        brightness = 1,
        saturation = 2,
        gamma = 1
    )

    val CoolTone = VideoFilterPreset(
        name = "Cool Tone",
        brightness = 1,
        hue = -2,
        gamma = 1
    )

    val SoftPastel = VideoFilterPreset(
        name = "Soft Pastel",
        brightness = 2,
        saturation = -1,
        contrast = -1
    )

    val Cinematic = VideoFilterPreset(
        name = "Cinematic",
        contrast = 2,
        saturation = -1,
        sharpness = 1
    )

    val Dramatic = VideoFilterPreset(
        name = "Dramatic",
        contrast = 3,
        saturation = -2,
        brightness = -1
    )

    val NightMode = VideoFilterPreset(
        name = "Night Mode",
        brightness = -2,
        gamma = -1
    )

    val Nostalgic = VideoFilterPreset(
        name = "Nostalgic",
        saturation = -2,
        gamma = 2,
        brightness = 1
    )

    val GhibliStyle = VideoFilterPreset(
        name = "Ghibli Style",
        brightness = 2,
        saturation = 2,
        contrast = -1
    )

    val NeonPop = VideoFilterPreset(
        name = "Neon Pop",
        saturation = 4,
        contrast = 2,
        brightness = 1
    )

    val DeepBlack = VideoFilterPreset(
        name = "Deep Black",
        brightness = -3,
        contrast = 3,
        gamma = -1
    )

    /**
     * All available presets.
     */
    val all = listOf(
        None, Vivid, WarmTone, CoolTone, SoftPastel,
        Cinematic, Dramatic, NightMode, Nostalgic,
        GhibliStyle, NeonPop, DeepBlack
    )

    /**
     * Get preset by name.
     */
    fun byName(name: String): VideoFilterPreset {
        return all.find { it.name == name } ?: None
    }
}
