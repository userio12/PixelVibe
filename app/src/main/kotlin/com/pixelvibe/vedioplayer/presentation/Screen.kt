package com.pixelvibe.vedioplayer.presentation

import androidx.compose.runtime.Composable

/**
 * Screen interface for navigation.
 * Each screen provides its own @Composable Content().
 */
interface Screen {
    val title: String
    @Composable
    fun Content(navigateTo: (Screen) -> Unit, onBack: () -> Unit)
}
