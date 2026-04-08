package com.pixelvibe.vedioplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pixelvibe.vedioplayer.ui.player.controls.PlayerControls
import com.pixelvibe.vedioplayer.ui.player.PlayerState
import com.pixelvibe.vedioplayer.ui.theme.PixelVibeTheme

/**
 * Player Activity with Compose-only UI.
 * No video engine dependency — pure UI shell ready for any video engine.
 */
class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PixelVibeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlayerControls(
                        state = PlayerState(
                            mediaTitle = intent.data?.lastPathSegment ?: "PixelVibe"
                        ),
                        onBackClick = { finish() },
                        onPlayPauseClick = {},
                        onSeek = {}
                    )
                }
            }
        }
    }
}
