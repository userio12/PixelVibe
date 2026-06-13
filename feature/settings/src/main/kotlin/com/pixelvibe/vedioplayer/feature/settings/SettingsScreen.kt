package com.pixelvibe.vedioplayer.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.core.common.util.UiText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pixelvibe.vedioplayer.core.ui.component.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SettingsEvent.ShowMessage -> {
                val text = when (val msg = event.message) {
                    is UiText.DynamicString -> msg.value
                    is UiText.StringResource -> "Error"
                }
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (state.showPinDialog) {
        PinEntryDialog(
            onConfirm = { viewModel.onAction(SettingsAction.OnSetPin(it)) },
            onDismiss = { viewModel.onAction(SettingsAction.OnDismissPinDialog) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        SectionHeader("Security & Privacy")
        SettingToggle(
            label = "Incognito Mode",
            subtitle = "Don't save watch history or resume positions",
            checked = state.incognitoEnabled,
            onCheckedChange = { viewModel.onAction(SettingsAction.OnToggleIncognito) }
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.onAction(SettingsAction.OnShowPinDialog) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Set App Lock PIN") }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        SectionHeader("Appearance")
        SettingToggle(
            label = "AMOLED Dark Mode",
            subtitle = "Pure black background for OLED screens",
            checked = state.amoledTheme,
            onCheckedChange = { viewModel.onAction(SettingsAction.OnToggleAmoled) }
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        SectionHeader("Audio")
        SettingItem("Equalizer", "Open from player controls")
        SettingItem("Bass Boost", "Open from player controls")

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        SectionHeader("Playback")
        SettingItem("Sleep Timer", "Available in player controls")

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        SectionHeader("Subtitle")
        SettingItem("Style", "Font, size, color — open from player")
        SettingItem("Search", "Online subtitle download — open from player")

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        SectionHeader("Backup & Restore")
        if (state.isExporting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            OutlinedButton(
                onClick = { viewModel.onAction(SettingsAction.OnExportBackup) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Export Settings") }
        }
        Spacer(Modifier.height(8.dp))
        if (state.isImporting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            OutlinedButton(
                onClick = { viewModel.onAction(SettingsAction.OnImportBackup) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Import Settings") }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        SectionHeader("About")
        SettingItem("Version", "1.0.0")
        SettingItem("Build", "PixelVibe 2025")
        SettingItem("Package", "com.pixelvibe.vedioplayer")
        SettingItem("Min SDK", "28 / Target: 34")
    }
}

@Composable
private fun PinEntryDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set App Lock PIN") },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 6) pin = it },
                label = { Text("Enter 4-6 digit PIN") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (pin.length >= 4) onConfirm(pin) },
                enabled = pin.length >= 4
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingToggle(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
