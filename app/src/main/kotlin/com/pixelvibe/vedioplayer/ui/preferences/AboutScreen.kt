package com.pixelvibe.vedioplayer.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class OssLibrary(
    val name: String,
    val license: String,
    val url: String
)

val OssLibraries = listOf(
    OssLibrary("mpv-android", "LGPL-2.1", "https://github.com/mpv-android/mpv-android"),
    OssLibrary("Koin", "Apache-2.0", "https://insert-koin.io/"),
    OssLibrary("Room", "Apache-2.0", "https://developer.android.com/jetpack/androidx/releases/room"),
    OssLibrary("Jetpack Compose", "Apache-2.0", "https://developer.android.com/jetpack/compose"),
    OssLibrary("OkHttp", "Apache-2.0", "https://square.github.io/okhttp/"),
    OssLibrary("SMBJ", "Apache-2.0", "https://github.com/hierynomus/smbj"),
    OssLibrary("NanoHTTPD", "BSD-3", "https://github.com/NanoHttpd/nanohttpd"),
    OssLibrary("Coil", "Apache-2.0", "https://coil-kt.github.io/coil/"),
    OssLibrary("kotlinx.serialization", "Apache-2.0", "https://github.com/Kotlin/kotlinx.serialization"),
    OssLibrary("Reorderable", "Apache-2.0", "https://github.com/Calvin-LL/Reorderable"),
    OssLibrary("compose.prefs", "Apache-2.0", "https://github.com/zhanghai/ComposePreference"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                // App info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PixelVibe",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Version 1.0 (build 1)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "A feature-rich video player built with Jetpack Compose",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    text = "Open Source Libraries",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(OssLibraries) { lib ->
                LibraryRow(lib = lib)
            }
        }
    }
}

@Composable
private fun LibraryRow(lib: OssLibrary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable { /* Open URL */ },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(lib.name, style = MaterialTheme.typography.bodyLarge)
                Text(lib.license, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.OpenInNew, contentDescription = "Open URL", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
