package com.pixelvibe.vedioplayer.ui.player.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class TrackInfo(
    val id: Int,
    val title: String,
    val language: String? = null,
    val codec: String? = null,
    val isExternal: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleTracksSheet(
    primaryTracks: List<TrackInfo>,
    secondaryTracks: List<TrackInfo>,
    selectedPrimary: Int,
    selectedSecondary: Int,
    onPrimarySelected: (Int) -> Unit,
    onSecondarySelected: (Int) -> Unit,
    onAddExternal: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Text(
            text = "Subtitle Tracks",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Primary subtitles
            Text(
                text = "Primary",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            TrackRow(
                trackId = -1,
                title = "Off",
                isSelected = selectedPrimary == -1,
                onClick = { onPrimarySelected(-1) }
            )

            primaryTracks.forEach { track ->
                TrackRow(
                    trackId = track.id,
                    title = buildTrackTitle(track),
                    isSelected = selectedPrimary == track.id,
                    onClick = { onPrimarySelected(track.id) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Secondary subtitles
            Text(
                text = "Secondary",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            TrackRow(
                trackId = -1,
                title = "Off",
                isSelected = selectedSecondary == -1,
                onClick = { onSecondarySelected(-1) }
            )

            secondaryTracks.forEach { track ->
                TrackRow(
                    trackId = track.id,
                    title = buildTrackTitle(track),
                    isSelected = selectedSecondary == track.id,
                    onClick = { onSecondarySelected(track.id) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddExternal)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add external subtitles", style = MaterialTheme.typography.bodyLarge)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSettings)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Subtitle settings", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private fun buildTrackTitle(track: TrackInfo): String {
    val base = "#${track.id}: ${track.title}"
    return if (track.language != null) {
        "$base (${track.language})"
    } else {
        base
    }
}

@Composable
private fun TrackRow(
    trackId: Int,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() }
        )
        Text(
            text = title,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
