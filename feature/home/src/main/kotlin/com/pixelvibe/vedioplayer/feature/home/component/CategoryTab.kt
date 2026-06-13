package com.pixelvibe.vedioplayer.feature.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.feature.home.HomeTab

@Composable
fun CategoryTabRow(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = HomeTab.entries
    TabRow(
        selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.displayName,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

private val HomeTab.displayName: String
    get() = when (this) {
        HomeTab.ALL -> "All"
        HomeTab.FOLDERS -> "Folders"
        HomeTab.FAVORITES -> "Favorites"
        HomeTab.IPTV -> "IPTV"
    }
