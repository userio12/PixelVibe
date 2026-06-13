package com.pixelvibe.vedioplayer.core.data.security

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themePrefs by preferencesDataStore(name = "theme_prefs")

open class ThemePreferences(private val context: Context) {

    private val AMOLED_KEY = booleanPreferencesKey("amoled_theme")

    open val isAmoledTheme: Flow<Boolean> = context.themePrefs.data.map { prefs ->
        prefs[AMOLED_KEY] ?: false
    }

    open suspend fun setAmoledTheme(enabled: Boolean) {
        context.themePrefs.edit { it[AMOLED_KEY] = enabled }
    }
}
