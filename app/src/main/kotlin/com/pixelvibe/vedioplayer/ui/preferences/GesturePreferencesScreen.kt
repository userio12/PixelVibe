package com.pixelvibe.vedioplayer.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.preferences.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GesturePreferencesScreen(
    preferences: PlayerPreferences,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val doubleTapLeft by preferences.doubleTapLeft.collectAsState()
    val doubleTapCenter by preferences.doubleTapCenter.collectAsState()
    val doubleTapRight by preferences.doubleTapRight.collectAsState()
    val singleTapCenter by preferences.singleTapCenter.collectAsState()
    val sideSwap by preferences.mediaControlSideSwap.collectAsState()
    val areaWidth by preferences.doubleTapSeekAreaWidth.collectAsState()

    val tapActions = listOf(
        "seek" to "Seek",
        "play_pause" to "Play/Pause",
        "custom" to "Custom",
        "none" to "None"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Gestures") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Double-tap Actions",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            GestureActionRow("Double-tap (left)", doubleTapLeft, tapActions) {
                // preferences.doubleTapLeft.set(it)
            }
            GestureActionRow("Double-tap (center)", doubleTapCenter, tapActions) {
                // preferences.doubleTapCenter.set(it)
            }
            GestureActionRow("Double-tap (right)", doubleTapRight, tapActions) {
                // preferences.doubleTapRight.set(it)
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))

            PrefSwitchRow(
                "Use single tap for center",
                "Single tap instead of double tap for center gesture",
                singleTapCenter
            ) { /* preferences.singleTapCenter.set(it) */ }

            PrefSwitchRow(
                "Media controls side swap",
                "Swap brightness/volume sides",
                sideSwap
            ) { /* preferences.mediaControlSideSwap.set(it) */ }

            PrefSliderRow(
                "Double-tap seek area width",
                "Current: ${areaWidth}%",
                areaWidth.toFloat(),
                10f..50f
            ) { /* preferences.doubleTapSeekAreaWidth.set(it.toInt()) */ }
        }
    }
}

@Composable
private fun GestureActionRow(
    label: String,
    currentValue: String,
    actions: List<Pair<String, String>>,
    onValueChange: (String) -> Unit
) {
    Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.forEach { (value, text) ->
            FilterChip(
                selected = currentValue == value,
                onClick = { onValueChange(value) },
                label = { Text(text) }
            )
        }
    }
    androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
}
