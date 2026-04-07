package com.pixelvibe.vedioplayer.ui.player.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SubtitleSettingsPanel(
    fontName: String,
    fontSize: Int,
    isBold: Boolean,
    isItalic: Boolean,
    borderColor: Int,
    textColor: Int,
    backgroundColor: Int,
    borderSize: Int,
    shadowOffset: Int,
    scale: Float,
    position: Int,
    isAssOverride: Boolean,
    isScaleByWindow: Boolean,
    onFontChange: (String) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onBoldChange: (Boolean) -> Unit,
    onTextColorChange: (Int) -> Unit,
    onBorderColorChange: (Int) -> Unit,
    onBackgroundColorChange: (Int) -> Unit,
    onBorderSizeChange: (Int) -> Unit,
    onShadowOffsetChange: (Int) -> Unit,
    onScaleChange: (Float) -> Unit,
    onPositionChange: (Int) -> Unit,
    onAssOverrideChange: (Boolean) -> Unit,
    onScaleByWindowChange: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subtitle Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp)
        ) {
            // Typography Card
            SettingsCard("Typography") {
                SettingsSlider(
                    label = "Font Size",
                    value = fontSize.toFloat(),
                    valueRange = 10f..60f,
                    onValueChange = { onFontSizeChange(it.toInt()) }
                )
                SettingsSlider(
                    label = "Border Size",
                    value = borderSize.toFloat(),
                    valueRange = 0f..10f,
                    onValueChange = { onBorderSizeChange(it.toInt()) }
                )
                SettingsSlider(
                    label = "Shadow Offset",
                    value = shadowOffset.toFloat(),
                    valueRange = 0f..5f,
                    onValueChange = { onShadowOffsetChange(it.toInt()) }
                )
                SettingsSlider(
                    label = "Scale",
                    value = scale,
                    valueRange = 0.5f..2f,
                    onValueChange = { onScaleChange(it) }
                )
                SettingsSlider(
                    label = "Position",
                    value = position.toFloat(),
                    valueRange = (-100f)..100f,
                    onValueChange = { onPositionChange(it.toInt()) }
                )
                SettingsSwitch(
                    label = "Bold",
                    checked = isBold,
                    onCheckedChange = onBoldChange
                )
                SettingsSwitch(
                    label = "Override ASS/SSA",
                    checked = isAssOverride,
                    onCheckedChange = onAssOverrideChange
                )
                SettingsSwitch(
                    label = "Scale by window",
                    checked = isScaleByWindow,
                    onCheckedChange = onScaleByWindowChange
                )
            }

            Spacer(Modifier.height(8.dp))

            // Colors Card
            SettingsCard("Colors") {
                ColorPickerRow("Text", textColor) { onTextColorChange(it) }
                ColorPickerRow("Border", borderColor) { onBorderColorChange(it) }
                ColorPickerRow("Background", backgroundColor) { onBackgroundColorChange(it) }
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (valueRange.endInclusive <= 10f) value.toInt().toString()
                else String.format("%.1f", value),
                style = MaterialTheme.typography.labelMedium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
private fun SettingsSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ColorPickerRow(label: String, color: Int, onColorChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(color), MaterialTheme.shapes.small)
                .clickable { /* Phase 5: Open color picker */ }
        )
    }
}
