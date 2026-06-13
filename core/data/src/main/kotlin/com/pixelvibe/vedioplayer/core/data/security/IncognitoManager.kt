package com.pixelvibe.vedioplayer.core.data.security

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.incognitoPrefs by preferencesDataStore(name = "incognito_prefs")

open class IncognitoManager(private val context: Context) {

    private val INCOGNITO_KEY = booleanPreferencesKey("incognito_mode")

    open val isIncognito: Flow<Boolean> by lazy {
        context.incognitoPrefs.data.map { prefs ->
            prefs[INCOGNITO_KEY] ?: false
        }
    }

    open suspend fun setIncognito(enabled: Boolean) {
        context.incognitoPrefs.edit { it[INCOGNITO_KEY] = enabled }
    }
}
