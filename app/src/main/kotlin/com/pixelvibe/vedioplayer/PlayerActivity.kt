package com.pixelvibe.vedioplayer

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.pixelvibe.vedioplayer.database.repositories.PlaybackHistoryRepository
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.ui.player.MPVView
import com.pixelvibe.vedioplayer.ui.player.PlayerHost
import com.pixelvibe.vedioplayer.ui.player.PlayerObserver
import com.pixelvibe.vedioplayer.ui.player.PlayerViewModel
import com.pixelvibe.vedioplayer.ui.player.controls.PlayerControls
import com.pixelvibe.vedioplayer.ui.theme.PixelVibeTheme
import is.xyz.mpv.MPVLib
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PlayerActivity : ComponentActivity(), PlayerHost {

    private val preferences: PlayerPreferences by inject()
    private val playbackHistoryRepository: PlaybackHistoryRepository by inject()

    private lateinit var mpvView: MPVView
    private lateinit var composeView: ComposeView
    private lateinit var playerObserver: PlayerObserver

    private val viewModel: PlayerViewModel by viewModels(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlayerViewModel(preferences, playbackHistoryRepository) as T
                }
            }
        }
    )

    override val context = this
    override val windowManager = windowManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize MPV view
        mpvView = MPVView(this).apply {
            id = View.generateViewId()
            initOptions()
        }

        // Compose overlay
        composeView = ComposeView(this).apply {
            id = View.generateViewId()
        }

        // Set content with ConstraintLayout-like structure
        val root = FrameLayout(this).apply {
            addView(mpvView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
            addView(composeView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
        }

        setContentView(root)

        // Initialize MPV after surface is ready
        mpvView.initialize({
            mpvView.postInitOptions()
            mpvView.observeProperties()

            // Set up observer
            playerObserver = PlayerObserver(
                onEvent = { event -> viewModel.onEvent(event) },
                onPropertyChange = { property, value -> viewModel.onPropertyChange(property, value) }
            )
            MPVLib.addObserver(playerObserver)

            // Load intent data
            handleIntent(intent)

            // Restore playback state
            val title = viewModel.state.value.mediaTitle
            if (title.isNotBlank()) {
                viewModel.restorePlaybackState(title)
            }
        })

        // Set Compose content
        composeView.setContent {
            val state by viewModel.state.observeAsState()
            val currentState = state ?: return@setContent

            PixelVibeTheme {
                PlayerControls(
                    state = currentState,
                    onBackClick = { finish() },
                    onPlayPauseClick = { viewModel.togglePlayback() },
                    onSeek = { pos -> viewModel.seek(pos, precise = false) },
                    onNextClick = { viewModel.nextVideo() },
                    onPrevClick = { viewModel.prevVideo() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val uri = intent.data
                val filePath = uri?.toString() ?: return

                val title = uri.lastPathSegment ?: "Unknown"

                // Update ViewModel
                lifecycleScope.launch {
                    // We need to update the state - in production use a proper method
                    handleFileLoad(filePath, title)
                }

                // Check for headers/referer
                val headers = intent.getStringExtra("headers")
                val referer = intent.getStringExtra("referer")

                // Load into MPV
                val options = mutableMapOf<String, String>()
                headers?.let { options["http-header-fields"] = it }
                referer?.let { options["referrer"] = it }

                mpvView.loadFile(filePath, "replace", options)
            }
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
                uri?.let {
                    handleFileLoad(it.toString(), uri.lastPathSegment ?: "Unknown")
                    mpvView.loadFile(it.toString())
                }
            }
        }
    }

    private fun handleFileLoad(filePath: String, title: String) {
        // In production, this should update the ViewModel properly
        // For now, MPV will handle loading
    }

    override fun onPause() {
        super.onPause()
        viewModel.savePlaybackState()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerObserver.cleanup()
        mpvView.destroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Handle orientation changes
    }

    // ── PlayerHost Implementation ──

    override fun setKeepScreenOn(keepOn: Boolean) {
        if (keepOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun setBrightness(brightness: Float) {
        if (brightness < 0) return
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
    }

    override fun getBrightness(): Float {
        val brightness = window.attributes.screenBrightness
        return if (brightness < 0) {
            // Get system brightness
            try {
                val resolver = contentResolver
                val systemBrightness = android.provider.Settings.System.getInt(
                    resolver,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS
                )
                systemBrightness / 255f
            } catch (e: Exception) {
                0.5f
            }
        } else {
            brightness
        }
    }

    override fun setVolume(volume: Float) {
        MPVLib.setPropertyDouble("volume", (volume * 100).toDouble())
    }

    override fun getVolume(): Float {
        return (MPVLib.getPropertyDouble("volume") ?: 100.0).toFloat() / 100f
    }

    override fun getMaxVolume(): Int = 100

    override fun requestAudioFocus() {
        // Handle audio focus
    }

    override fun abandonAudioFocus() {
        // Abandon audio focus
    }

    override fun setSecureFlags(secure: Boolean) {
        if (secure) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun hideSystemUi() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
    }

    override fun showSystemUi() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}
