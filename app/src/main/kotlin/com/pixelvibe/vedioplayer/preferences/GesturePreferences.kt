package com.pixelvibe.vedioplayer.preferences

/**
 * Typed preference class for gesture-related settings.
 */
class GesturePreferences(private val store: PreferenceStore) {
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
    val horizontalSwipeToSeek = store.booleanPreference("player_horizontal_swipe_to_seek", true)
    val horizontalSwipeSensitivity = store.floatPreference("player_swipe_sensitivity", 1.0f)
}
