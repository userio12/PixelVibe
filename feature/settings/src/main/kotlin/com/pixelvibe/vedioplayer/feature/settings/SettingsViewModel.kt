package com.pixelvibe.vedioplayer.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvibe.vedioplayer.core.common.util.UiText
import com.pixelvibe.vedioplayer.core.data.security.AppLockManager
import com.pixelvibe.vedioplayer.core.data.security.BackupData
import com.pixelvibe.vedioplayer.core.data.security.IncognitoManager
import com.pixelvibe.vedioplayer.core.data.security.SettingsBackupManager
import com.pixelvibe.vedioplayer.core.data.security.ThemePreferences
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleStylePreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsState(
    val incognitoEnabled: Boolean = false,
    val amoledTheme: Boolean = false,
    val showPinDialog: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: UiText? = null
)

sealed interface SettingsAction {
    data object OnToggleIncognito : SettingsAction
    data object OnToggleAmoled : SettingsAction
    data object OnExportBackup : SettingsAction
    data object OnImportBackup : SettingsAction
    data object OnShowPinDialog : SettingsAction
    data object OnDismissPinDialog : SettingsAction
    data class OnSetPin(val pin: String) : SettingsAction
}

sealed interface SettingsEvent {
    data class ShowMessage(val message: UiText) : SettingsEvent
}

class SettingsViewModel(
    private val incognitoManager: IncognitoManager,
    private val appLockManager: AppLockManager,
    private val backupManager: SettingsBackupManager,
    private val subtitleStylePrefs: SubtitleStylePreferences,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            incognitoManager.isIncognito.collect { value ->
                _state.value = _state.value.copy(incognitoEnabled = value)
            }
        }
        viewModelScope.launch {
            themePreferences.isAmoledTheme.collect { value ->
                _state.value = _state.value.copy(amoledTheme = value)
            }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OnToggleIncognito -> {
                val newValue = !_state.value.incognitoEnabled
                viewModelScope.launch {
                    incognitoManager.setIncognito(newValue)
                }
            }
            SettingsAction.OnToggleAmoled -> {
                val newValue = !_state.value.amoledTheme
                viewModelScope.launch {
                    themePreferences.setAmoledTheme(newValue)
                }
            }
            SettingsAction.OnExportBackup -> exportBackup()
            SettingsAction.OnImportBackup -> importBackup()
            SettingsAction.OnShowPinDialog -> {
                _state.value = _state.value.copy(showPinDialog = true)
            }
            SettingsAction.OnDismissPinDialog -> {
                _state.value = _state.value.copy(showPinDialog = false)
            }
            is SettingsAction.OnSetPin -> {
                appLockManager.setPin(action.pin)
                _state.value = _state.value.copy(showPinDialog = false)
            }
        }
    }

    private fun exportBackup() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true)
            val incognito = incognitoManager.isIncognito.first()
            val style = subtitleStylePrefs.style.first()
            val amoled = themePreferences.isAmoledTheme.first()
            val json = backupManager.exportBackup(
                incognitoMode = incognito,
                amoledTheme = amoled,
                subtitleFontSize = style.fontSize,
                subtitleFontColor = style.fontColor
            )
            val file = backupManager.saveBackupToFile(data = BackupData(
                incognitoMode = incognito,
                amoledTheme = amoled,
                subtitleFontSize = style.fontSize,
                subtitleFontColor = style.fontColor
            ))
            _state.value = _state.value.copy(isExporting = false)
            val msg = if (file != null) "Backup saved to ${file.absolutePath}"
            else "Failed to export backup"
            _events.tryEmit(SettingsEvent.ShowMessage(UiText.DynamicString(msg)))
        }
    }

    private fun importBackup() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isImporting = true)
            val data = backupManager.loadBackupFromFile()
            if (data != null) {
                incognitoManager.setIncognito(data.incognitoMode)
                subtitleStylePrefs.updateFontSize(data.subtitleFontSize)
                subtitleStylePrefs.updateFontColor(data.subtitleFontColor)
                themePreferences.setAmoledTheme(data.amoledTheme)
                _state.value = _state.value.copy(isImporting = false)
                _events.tryEmit(SettingsEvent.ShowMessage(UiText.DynamicString("Backup restored")))
            } else {
                _state.value = _state.value.copy(isImporting = false)
                _events.tryEmit(SettingsEvent.ShowMessage(UiText.DynamicString("No backup found or invalid format")))
            }
        }
    }
}
