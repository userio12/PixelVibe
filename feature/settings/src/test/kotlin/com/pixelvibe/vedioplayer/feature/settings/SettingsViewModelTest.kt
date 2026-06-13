package com.pixelvibe.vedioplayer.feature.settings

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.pixelvibe.vedioplayer.core.data.security.AppLockManager
import com.pixelvibe.vedioplayer.core.data.security.BackupData
import com.pixelvibe.vedioplayer.core.data.security.IncognitoManager
import com.pixelvibe.vedioplayer.core.data.security.SettingsBackupManager
import com.pixelvibe.vedioplayer.core.data.security.ThemePreferences
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleStyle
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleStylePreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val incognitoFlow = MutableStateFlow(false)
    private val amoledFlow = MutableStateFlow(false)
    private var incognitoSet = false
    private var amoledSet = false
    private var pinSet: String? = null
    private var backupExported = false
    private var backupImported = false

    private val fakeIncognitoManager = object : IncognitoManager(null!!) {
        override val isIncognito: Flow<Boolean> = incognitoFlow
        override suspend fun setIncognito(enabled: Boolean) { incognitoSet = enabled }
    }

    private val fakeAmoledPrefs = object : ThemePreferences(null!!) {
        override val isAmoledTheme: Flow<Boolean> = amoledFlow
        override suspend fun setAmoledTheme(enabled: Boolean) { amoledSet = enabled }
    }

    private val fakeAppLock = object : AppLockManager(null!!) {
        override fun setPin(pin: String) { pinSet = pin }
    }

    private val fakeBackupManager = object : SettingsBackupManager(null!!) {
        override fun exportBackup(
            appLockEnabled: Boolean,
            incognitoMode: Boolean,
            amoledTheme: Boolean,
            subtitleFontSize: Int,
            subtitleFontColor: String,
            playbackSpeed: Float,
            bassBoostLevel: Int,
            virtualizerStrength: Int,
            equalizerPreset: String
        ): String {
            backupExported = true
            return """{"version":1}"""
        }
        override fun saveBackupToFile(filename: String, data: BackupData): File? {
            return if (backupExported) File("/tmp/test_backup.json") else null
        }
        override fun loadBackupFromFile(filename: String): BackupData? {
            backupImported = true
            return BackupData(incognitoMode = true, amoledTheme = true)
        }
    }

    private val fakeSubtitlePrefs = object : SubtitleStylePreferences(null!!) {
        override val style: Flow<SubtitleStyle> = flowOf(SubtitleStyle())
        override suspend fun updateFontSize(size: Int) {}
        override suspend fun updateFontColor(color: String) {}
    }

    @Test
    fun `initial state has defaults`() {
        val vm = createViewModel()
        assertThat(vm.state.value.incognitoEnabled).isFalse()
        assertThat(vm.state.value.amoledTheme).isFalse()
        assertThat(vm.state.value.showPinDialog).isFalse()
        assertThat(vm.state.value.isExporting).isFalse()
        assertThat(vm.state.value.isImporting).isFalse()
        assertThat(vm.state.value.message).isNull()
    }

    @Test
    fun `toggle incognito updates state and manager`() = runTest(UnconfinedTestDispatcher()) {
        val vm = createViewModel()
        incognitoFlow.value = true
        assertThat(vm.state.value.incognitoEnabled).isTrue()
    }

    @Test
    fun `toggle incognito action calls manager`() = runTest(UnconfinedTestDispatcher()) {
        val vm = createViewModel()
        incognitoSet = false
        vm.onAction(SettingsAction.OnToggleIncognito)
        assertThat(incognitoSet).isTrue()
    }

    @Test
    fun `toggle amoled updates state and manager`() = runTest(UnconfinedTestDispatcher()) {
        val vm = createViewModel()
        amoledFlow.value = true
        assertThat(vm.state.value.amoledTheme).isTrue()
    }

    @Test
    fun `toggle amoled action calls manager`() = runTest(UnconfinedTestDispatcher()) {
        val vm = createViewModel()
        amoledSet = false
        vm.onAction(SettingsAction.OnToggleAmoled)
        assertThat(amoledSet).isTrue()
    }

    @Test
    fun `show and dismiss pin dialog`() {
        val vm = createViewModel()
        vm.onAction(SettingsAction.OnShowPinDialog)
        assertThat(vm.state.value.showPinDialog).isTrue()
        vm.onAction(SettingsAction.OnDismissPinDialog)
        assertThat(vm.state.value.showPinDialog).isFalse()
    }

    @Test
    fun `set pin stores pin and dismisses dialog`() {
        val vm = createViewModel()
        vm.onAction(SettingsAction.OnShowPinDialog)
        vm.onAction(SettingsAction.OnSetPin("1234"))
        assertThat(pinSet).isEqualTo("1234")
        assertThat(vm.state.value.showPinDialog).isFalse()
    }

    @Test
    fun `export backup emits message`() = runTest(UnconfinedTestDispatcher()) {
        val vm = createViewModel()
        vm.events.test {
            vm.onAction(SettingsAction.OnExportBackup)
            val event = awaitItem()
            assertThat(event is SettingsEvent.ShowMessage).isTrue()
            assertThat((event as SettingsEvent.ShowMessage).message.toString().contains("Backup")).isTrue()
        }
    }

    @Test
    fun `import backup emits success message`() = runTest(UnconfinedTestDispatcher()) {
        val vm = createViewModel()
        vm.events.test {
            vm.onAction(SettingsAction.OnImportBackup)
            val event = awaitItem()
            assertThat(event is SettingsEvent.ShowMessage).isTrue()
            assertThat((event as SettingsEvent.ShowMessage).message.toString().contains("restored")).isTrue()
        }
    }

    @Test
    fun `import backup with no file emits error`() = runTest(UnconfinedTestDispatcher()) {
        val noFileManager = object : SettingsBackupManager(null!!) {
            override fun loadBackupFromFile(filename: String): BackupData? = null
        }
        val vm = SettingsViewModel(
            incognitoManager = fakeIncognitoManager,
            appLockManager = fakeAppLock,
            backupManager = noFileManager,
            subtitleStylePrefs = fakeSubtitlePrefs,
            themePreferences = fakeAmoledPrefs
        )
        vm.events.test {
            vm.onAction(SettingsAction.OnImportBackup)
            val event = awaitItem()
            assertThat(event is SettingsEvent.ShowMessage).isTrue()
            assertThat((event as SettingsEvent.ShowMessage).message.toString().contains("No backup")).isTrue()
        }
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            incognitoManager = fakeIncognitoManager,
            appLockManager = fakeAppLock,
            backupManager = fakeBackupManager,
            subtitleStylePrefs = fakeSubtitlePrefs,
            themePreferences = fakeAmoledPrefs
        )
    }
}
