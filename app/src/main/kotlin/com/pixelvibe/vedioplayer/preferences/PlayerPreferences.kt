package com.pixelvibe.vedioplayer.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

class PlayerPreferences(
    private val store: PreferenceStore
) {
    // Player
    val orientation = store.stringPreference("player_orientation", "free")
    val savePositionOnQuit = store.booleanPreference("player_save_position", true)
    val closeAfterEof = store.booleanPreference("player_close_after_eof", false)
    val rememberBrightness = store.booleanPreference("player_remember_brightness", false)
    val brightness = store.floatPreference("player_brightness", -1f)
    val autoplayNext = store.booleanPreference("player_autoplay_next", true)
    val autoPip = store.booleanPreference("player_auto_pip", false)
    val preciseSeeking = store.booleanPreference("player_precise_seeking", false)
    val doubleTapSeekDuration = store.intPreference("player_double_tap_seek", 10)
    val customSkipDuration = store.intPreference("player_custom_skip_duration", 90)
    val horizontalSwipeSensitivity = store.floatPreference("player_swipe_sensitivity", 1.0f)
    val horizontalSwipeToSeek = store.booleanPreference("player_horizontal_swipe_to_seek", true)

    // Gestures
    val doubleTapLeft = store.stringPreference("gesture_double_tap_left", "seek")
    val doubleTapCenter = store.stringPreference("gesture_double_tap_center", "play_pause")
    val doubleTapRight = store.stringPreference("gesture_double_tap_right", "seek")
    val singleTapCenter = store.booleanPreference("gesture_single_tap_center", false)
    val gestureBrightness = store.booleanPreference("gesture_brightness", true)
    val gestureVolume = store.booleanPreference("gesture_volume", true)
    val gesturePinchToZoom = store.booleanPreference("gesture_pinch_to_zoom", true)
    val doubleTapSeekAreaWidth = store.intPreference("gesture_double_tap_area_width", 33)
    val mediaControlSideSwap = store.booleanPreference("gesture_media_side_swap", false)
    val dynamicSpeedOverlay = store.booleanPreference("gesture_dynamic_speed_overlay", true)

    // Controls
    val seekbarStyle = store.stringPreference("controls_seekbar_style", "standard")
    val wavySeekbar = store.booleanPreference("controls_wavy_seekbar", true)
    val showLoadingCircle = store.booleanPreference("controls_loading_circle", true)
    val displayVolumeAsPercentage = store.booleanPreference("controls_volume_percentage", true)
    val allowGesturesInPanels = store.booleanPreference("controls_gestures_in_panels", true)
    val showStatusBarWithControls = store.booleanPreference("controls_show_status_bar", false)
    val panelOpacity = store.floatPreference("controls_panel_opacity", 0.8f)

    // Decoder
    val mpvProfile = store.stringPreference("decoder_profile", "gpu")
    val hwDecoding = store.booleanPreference("decoder_hwdec", true)
    val gpuNext = store.booleanPreference("decoder_gpu_next", false)
    val vulkan = store.booleanPreference("decoder_vulkan", false)
    val yuv420p = store.booleanPreference("decoder_yuv420p", false)
    val debanding = store.stringPreference("decoder_deband", "none")

    // Subtitles
    val onlineSubtitleSearch = store.booleanPreference("subtitles_online_search", true)
    val preferredSubtitleLanguages = store.stringPreference("subtitles_preferred_languages", "")
    val autoLoadSubtitles = store.booleanPreference("subtitles_auto_load", true)
    val subtitleSaveLocation = store.stringPreference("subtitles_save_location", "")
    val subtitleFontSize = store.intPreference("subtitles_font_size", 0)
    val subtitleFontName = store.stringPreference("subtitles_font_name", "")
    val subtitleBold = store.booleanPreference("subtitles_bold", false)
    val subtitleTextColor = store.intPreference("subtitles_text_color", -1)
    val subtitleBorderColor = store.intPreference("subtitles_border_color", -1)
    val subtitleBackgroundColor = store.intPreference("subtitles_bg_color", 0)
    val subtitleBorderSize = store.intPreference("subtitles_border_size", 0)
    val subtitleScale = store.floatPreference("subtitles_scale", 1.0f)
    val subtitlePosition = store.intPreference("subtitles_position", 0)
    val subtitleOverrideAss = store.booleanPreference("subtitles_override_ass", false)
    val subtitleScaleByWindow = store.booleanPreference("subtitles_scale_by_window", false)

    // Audio
    val pitchCorrection = store.booleanPreference("audio_pitch_correction", true)
    val volumeNormalization = store.booleanPreference("audio_volume_normalization", false)
    val audioChannels = store.stringPreference("audio_channels", "auto")
    val preferredAudioLanguage = store.stringPreference("audio_preferred_language", "")
    val volumeBoostCap = store.intPreference("audio_volume_boost_cap", 100)

    // Appearance
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

    // Folders
    val folderViewMode = store.stringPreference("folders_view_mode", "tree")
    val showHiddenFiles = store.booleanPreference("folders_show_hidden", false)
    val scanDirectories = store.stringPreference("folders_scan_dirs", "")

    // Layout
    val playerLayout = store.stringPreference("player_layout", "default")
    val topRightButtons = store.stringPreference("layout_top_right", "speed,subtitles,audio")
    val bottomLeftButtons = store.stringPreference("layout_bottom_left", "chapters,aspect_ratio,pip")
    val bottomRightButtons = store.stringPreference("layout_bottom_right", "more,zoom")
    val portraitBottomButtons = store.stringPreference("layout_portrait_bottom", "speed,subtitles,audio,more")
}

// Compose helper for collecting preference as State
@Composable
fun <T> Preference<T>.collectAsState(): State<T> {
    return remember(this) { changes() }.collectAsState(initial = get())
}
