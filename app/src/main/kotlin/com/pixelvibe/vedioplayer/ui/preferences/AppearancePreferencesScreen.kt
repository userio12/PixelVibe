package com.pixelvibe.vedioplayer.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.preferences.collectAsState
import com.pixelvibe.vedioplayer.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearancePreferencesScreen(
    preferences: PlayerPreferences,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appTheme by preferences.appTheme.collectAsState()
    val amoled by preferences.amoledMode.collectAsState()
    val materialYou by preferences.materialYou.collectAsState()
    val showNewLabel by preferences.showNewVideoLabel.collectAsState()
    val newLabelDays by preferences.newVideoLabelDays.collectAsState()
    val autoScroll by preferences.autoScroll.collectAsState()
    val watchedThreshold by preferences.watchedThreshold.collectAsState()
    val fullNames by preferences.fullNames.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
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
            // Theme Picker
            Text("App Theme", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AppTheme.all) { theme ->
                    ThemePreviewCard(
                        theme = theme,
                        isSelected = theme.id == appTheme,
                        onClick = { /* preferences.appTheme.set(theme.id) */ }
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))

            // AMOLED Mode
            PrefSwitchRow(
                title = "AMOLED Black Mode",
                summary = "Use pure black background for dark themes",
                checked = amoled,
                onCheckedChange = { /* preferences.amoledMode.set(it) */ }
            )

            // Material You
            PrefSwitchRow(
                title = "Material You",
                summary = "Enable dynamic colors (Android 12+)",
                checked = materialYou,
                onCheckedChange = { /* preferences.materialYou.set(it) */ }
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            Text("File Browser", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))

            PrefSwitchRow(
                title = "Show 'New' label",
                summary = "Mark recently added unplayed videos",
                checked = showNewLabel,
                onCheckedChange = { /* preferences.showNewVideoLabel.set(it) */ }
            )

            if (showNewLabel) {
                PrefSliderRow(
                    title = "Days threshold",
                    value = newLabelDays.toFloat(),
                    valueRange = 1f..30f,
                    onValueChange = { /* preferences.newVideoLabelDays.set(it.toInt()) */ }
                )
            }

            PrefSwitchRow(
                title = "Auto-scroll to last played",
                checked = autoScroll,
                onCheckedChange = { /* preferences.autoScroll.set(it) */ }
            )

            PrefSliderRow(
                title = "Watched threshold",
                summary = "Mark as watched at %",
                value = watchedThreshold.toFloat(),
                valueRange = 50f..100f,
                onValueChange = { /* preferences.watchedThreshold.set(it.toInt()) */ }
            )

            PrefSwitchRow(
                title = "Show full names",
                summary = "Don't truncate folder and video names",
                checked = fullNames,
                onCheckedChange = { /* preferences.fullNames.set(it) */ }
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Color preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}
