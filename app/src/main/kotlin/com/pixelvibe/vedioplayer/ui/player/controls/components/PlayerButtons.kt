package com.pixelvibe.vedioplayer.ui.player.controls.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.ui.player.PlayerButtonType

@Composable
fun PlayerButton(
    buttonType: PlayerButtonType,
    onClick: () -> Unit,
    showBackground: Boolean = true,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (showBackground) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    } else {
        androidx.compose.ui.graphics.Color.Transparent
    }

    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            imageVector = buttonType.icon,
            contentDescription = buttonType.contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ButtonSlot(
    buttons: List<PlayerButtonType>,
    onButtonClick: (PlayerButtonType) -> Unit,
    showBackground: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        buttons.forEach { type ->
            PlayerButton(
                buttonType = type,
                onClick = { onButtonClick(type) },
                showBackground = showBackground
            )
        }
    }
}
