package com.pixelvibe.vedioplayer.feature.network

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import androidx.lifecycle.SavedStateHandle
import com.pixelvibe.vedioplayer.core.data.network.Breadcrumb
import com.pixelvibe.vedioplayer.core.data.network.ChromecastClient
import com.pixelvibe.vedioplayer.core.data.network.FtpClient
import com.pixelvibe.vedioplayer.core.data.network.NetworkFile
import com.pixelvibe.vedioplayer.core.data.network.NetworkResult
import com.pixelvibe.vedioplayer.core.data.network.NetworkSource
import com.pixelvibe.vedioplayer.core.data.network.SmbClient
import com.pixelvibe.vedioplayer.core.data.network.SsdpDevice
import com.pixelvibe.vedioplayer.core.data.network.SsdpDiscovery
import com.pixelvibe.vedioplayer.core.data.network.WebDavClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private class FakeSmbClient : SmbClient() {
    var authenticateResult = true
    var lastSource: NetworkSource.Smb? = null
    override suspend fun authenticate(source: NetworkSource.Smb): Boolean {
        lastSource = source
        return authenticateResult
    }
    override suspend fun listFiles(source: NetworkSource.Smb): List<NetworkFile> = emptyList()
}

private class FakeFtpClient : FtpClient() {
    var authenticateResult = true
    var lastSource: NetworkSource.Ftp? = null
    override suspend fun authenticate(source: NetworkSource.Ftp): Boolean {
        lastSource = source
        return authenticateResult
    }
    override suspend fun listFiles(source: NetworkSource.Ftp): List<NetworkFile> = emptyList()
}

private class FakeWebDavClient : WebDavClient() {
    var authenticateResult = true
    var lastSource: NetworkSource.WebDav? = null
    override suspend fun authenticate(source: NetworkSource.WebDav): Boolean {
        lastSource = source
        return authenticateResult
    }
    override suspend fun listFiles(source: NetworkSource.WebDav): List<NetworkFile> = emptyList()
}

private class FakeSsdpDiscovery : SsdpDiscovery() {
    override fun discover(): Flow<SsdpDevice> = emptyFlow()
}

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val smbClient = FakeSmbClient()
    private val ftpClient = FakeFtpClient()
    private val webDavClient = FakeWebDavClient()
    private val ssdpDiscovery = FakeSsdpDiscovery()
    private val chromecastClient = ChromecastClient()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has SMB tab selected`() {
        val vm = createViewModel()
        assertThat(vm.state.value.selectedTab).isEqualTo(NetworkTab.SMB)
    }

    @Test
    fun `tab selection updates state`() {
        val vm = createViewModel()
        vm.onAction(NetworkAction.OnTabSelected(NetworkTab.FTP))
        assertThat(vm.state.value.selectedTab).isEqualTo(NetworkTab.FTP)
    }

    @Test
    fun `smb field changes update state`() {
        val vm = createViewModel()
        vm.onAction(NetworkAction.OnSmbHostChange("192.168.1.1"))
        vm.onAction(NetworkAction.OnSmbShareChange("videos"))
        vm.onAction(NetworkAction.OnSmbUsernameChange("admin"))
        vm.onAction(NetworkAction.OnSmbPasswordChange("pass"))
        assertThat(vm.state.value.smbHost).isEqualTo("192.168.1.1")
        assertThat(vm.state.value.smbShare).isEqualTo("videos")
        assertThat(vm.state.value.smbUsername).isEqualTo("admin")
        assertThat(vm.state.value.smbPassword).isEqualTo("pass")
    }

    @Test
    fun `ftp field changes update state`() {
        val vm = createViewModel()
        vm.onAction(NetworkAction.OnFtpHostChange("10.0.0.1"))
        vm.onAction(NetworkAction.OnFtpPortChange("2121"))
        vm.onAction(NetworkAction.OnFtpUsernameChange("user"))
        vm.onAction(NetworkAction.OnFtpPasswordChange("1234"))
        assertThat(vm.state.value.ftpHost).isEqualTo("10.0.0.1")
        assertThat(vm.state.value.ftpPort).isEqualTo("2121")
        assertThat(vm.state.value.ftpUsername).isEqualTo("user")
        assertThat(vm.state.value.ftpPassword).isEqualTo("1234")
    }

    @Test
    fun `webdav field changes update state`() {
        val vm = createViewModel()
        vm.onAction(NetworkAction.OnWebDavUrlChange("https://example.com/dav"))
        vm.onAction(NetworkAction.OnWebDavUsernameChange("user"))
        vm.onAction(NetworkAction.OnWebDavPasswordChange("pass"))
        assertThat(vm.state.value.webdavUrl).isEqualTo("https://example.com/dav")
        assertThat(vm.state.value.webdavUsername).isEqualTo("user")
        assertThat(vm.state.value.webdavPassword).isEqualTo("pass")
    }

    @Test
    fun `smb connect reads fields from state`() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.onAction(NetworkAction.OnSmbHostChange("10.0.0.1"))
        vm.onAction(NetworkAction.OnSmbShareChange("share"))
        vm.onAction(NetworkAction.OnSmbUsernameChange("user"))
        vm.onAction(NetworkAction.OnSmbPasswordChange("pwd"))
        vm.onAction(NetworkAction.OnSmbConnect)
        val source = smbClient.lastSource
        assertThat(source?.host).isEqualTo("10.0.0.1")
        assertThat(source?.share).isEqualTo("share")
        assertThat(source?.username).isEqualTo("user")
        assertThat(source?.password).isEqualTo("pwd")
    }

    @Test
    fun `ftp connect reads fields from state`() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.onAction(NetworkAction.OnFtpHostChange("10.0.0.1"))
        vm.onAction(NetworkAction.OnFtpPortChange("2121"))
        vm.onAction(NetworkAction.OnFtpUsernameChange("user"))
        vm.onAction(NetworkAction.OnFtpPasswordChange("pwd"))
        vm.onAction(NetworkAction.OnFtpConnect)
        val source = ftpClient.lastSource
        assertThat(source?.host).isEqualTo("10.0.0.1")
        assertThat(source?.port).isEqualTo(2121)
        assertThat(source?.username).isEqualTo("user")
        assertThat(source?.password).isEqualTo("pwd")
    }

    @Test
    fun `webdav connect reads fields from state`() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.onAction(NetworkAction.OnWebDavUrlChange("https://example.com/dav"))
        vm.onAction(NetworkAction.OnWebDavUsernameChange("user"))
        vm.onAction(NetworkAction.OnWebDavPasswordChange("pwd"))
        vm.onAction(NetworkAction.OnWebDavConnect)
        val source = webDavClient.lastSource
        assertThat(source?.baseUrl).isEqualTo("https://example.com/dav")
        assertThat(source?.username).isEqualTo("user")
        assertThat(source?.password).isEqualTo("pwd")
    }

    @Test
    fun `back when connected clears current source`() {
        val vm = createViewModel()
        vm.onAction(NetworkAction.OnSmbHostChange("host"))
        vm.onAction(NetworkAction.OnSmbConnect)
        assertThat(vm.state.value.currentSource).isNotNull()
        vm.onAction(NetworkAction.OnBack)
        assertThat(vm.state.value.currentSource).isNull()
    }

    @Test
    fun `video click emits play event`() = runTest(testDispatcher) {
        val vm = createViewModel()
        val file = NetworkFile("video.mp4", "/path/video.mp4", false, source = NetworkSource.Smb("", ""))
        vm.events.test {
            vm.onAction(NetworkAction.OnVideoClick(file))
            val event = awaitItem()
            assertThat(event).isEqualTo(NetworkEvent.PlayVideo("/path/video.mp4"))
        }
    }

    @Test
    fun `savedStateHandle restores selected tab`() {
        val handle = SavedStateHandle().apply { set("selectedTab", NetworkTab.WEBDAV.name) }
        val vm = NetworkViewModel(handle, smbClient, ftpClient, webDavClient, ssdpDiscovery, chromecastClient)
        assertThat(vm.state.value.selectedTab).isEqualTo(NetworkTab.WEBDAV)
    }

    private fun createViewModel(): NetworkViewModel {
        return NetworkViewModel(
            savedStateHandle = SavedStateHandle(),
            smbClient = smbClient,
            ftpClient = ftpClient,
            webDavClient = webDavClient,
            ssdpDiscovery = ssdpDiscovery,
            chromecastClient = chromecastClient
        )
    }
}
