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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTracksSheet(
    tracks: List<TrackInfo>,
    selectedTrack: Int,
    onTrackSelected: (Int) -> Unit,
    onAddExternal: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Text(
            text = "Audio Tracks",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TrackRow(
                trackId = -1,
                title = "Off",
                isSelected = selectedTrack == -1,
                onClick = { onTrackSelected(-1) }
            )

            tracks.forEach { track ->
                TrackRow(
                    trackId = track.id,
                    title = buildTrackTitle(track),
                    isSelected = selectedTrack == track.id,
                    onClick = { onTrackSelected(track.id) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddExternal)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add external audio tracks", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun TrackRow(trackId: Int, title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onClick() })
        Text(
            text = title,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun buildTrackTitle(track: TrackInfo): String {
    val base = "#${track.id}: ${track.title}"
    return if (track.language != null) "$base (${track.language})" else base
}
