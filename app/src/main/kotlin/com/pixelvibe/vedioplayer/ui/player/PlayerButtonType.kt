package com.pixelvibe.vedioplayer.ui.player

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enum of all 22 available player button types.
 */
enum class PlayerButtonType(
    val icon: ImageVector,
    val contentDescription: String
) {
    BACK_ARROW(Icons.AutoMirrored.Filled.ArrowBack, "Back"),
    VIDEO_TITLE(Icons.Default.Info, "Video title"),
    CHAPTERS(Icons.Default.Bookmarks, "Chapters"),
    SPEED(Icons.Default.Speed, "Playback speed"),
    DECODER(Icons.Default.Memory, "Decoder"),
    ROTATION(Icons.Default.ScreenRotation, "Rotation"),
    FRAME_NAV(Icons.Default.SkipNext, "Frame navigation"),
    ZOOM(Icons.Default.ZoomInMap, "Zoom"),
    PIP(Icons.Default.PictureInPictureAlt, "Picture-in-Picture"),
    ASPECT_RATIO(Icons.Default.AspectRatio, "Aspect ratio"),
    LOCK(Icons.Default.Lock, "Lock controls"),
    AUDIO(Icons.Default.VolumeUp, "Audio tracks"),
    SUBTITLES(Icons.Default.ClosedCaption, "Subtitles"),
    MORE(Icons.Default.MoreVert, "More options"),
    CURRENT_CHAPTER(Icons.Default.Bookmark, "Current chapter"),
    REPEAT(Icons.Default.Repeat, "Repeat mode"),
    SHUFFLE(Icons.Default.Shuffle, "Shuffle"),
    MIRROR(Icons.Default.Flip, "Mirror"),
    VERTICAL_FLIP(Icons.Default.Flip, "Vertical flip"),
    A_B_LOOP(Icons.Default.Loop, "A-B loop"),
    CUSTOM_SKIP(Icons.Default.Forward, "Custom skip"),
    BACKGROUND_PLAYBACK(Icons.Default.Headphones, "Background playback")
}
