package com.pixelvibe.vedioplayer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Color picker with preset color swatches and RGB sliders.
 */
@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val presetColors = listOf(
        Color.Red, Color.Green, Color.Blue, Color.Yellow,
        Color.Cyan, Color.Magenta, Color.White, Color.Black,
        Color(0xFFFF5722), Color(0xFF4CAF50), Color(0xFF2196F3),
        Color(0xFFFFC107), Color(0xFF9C27B0), Color(0xFF00BCD4)
    )

    Column(modifier = modifier) {
        // Preset swatches
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presetColors) { color ->
                ColorSwatch(
                    color = color,
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }

        // RGB sliders
        var red by remember { mutableFloatStateOf(selectedColor.red) }
        var green by remember { mutableFloatStateOf(selectedColor.green) }
        var blue by remember { mutableFloatStateOf(selectedColor.blue) }

        Text("Red: ${(red * 255).toInt()}", style = MaterialTheme.typography.labelMedium)
        Slider(value = red, onValueChange = { red = it; onColorSelected(Color(red, green, blue)) })

        Text("Green: ${(green * 255).toInt()}", style = MaterialTheme.typography.labelMedium)
        Slider(value = green, onValueChange = { green = it; onColorSelected(Color(red, green, blue)) })

        Text("Blue: ${(blue * 255).toInt()}", style = MaterialTheme.typography.labelMedium)
        Slider(value = blue, onValueChange = { blue = it; onColorSelected(Color(red, green, blue)) })

        // Preview
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.small)
                .background(Color(red, green, blue))
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
        )
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.small)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = MaterialTheme.shapes.small
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.small
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = if ((color.red * 0.299 + color.green * 0.587 + color.blue * 0.114) > 0.5f) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
