package com.pixelvibe.vedioplayer.feature.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.core.common.util.formatMillis
import com.pixelvibe.vedioplayer.core.data.db.entity.VideoEntity
import com.pixelvibe.vedioplayer.core.ui.component.VideoCard
import com.pixelvibe.vedioplayer.core.ui.component.VideoCardData

@Composable
fun VideoGrid(
    videos: List<VideoEntity>,
    onVideoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(videos, key = { it.id }) { video ->
            VideoCard(
                data = VideoCardData(
                    id = video.id,
                    title = video.title,
                    durationText = video.durationMs.formatMillis(),
                    thumbnailUri = video.uri
                ),
                onClick = { onVideoClick(video.id) }
            )
        }
    }
}

