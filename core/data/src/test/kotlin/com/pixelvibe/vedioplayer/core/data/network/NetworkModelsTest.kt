package com.pixelvibe.vedioplayer.core.data.network

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class NetworkModelsTest {

    @Test
    fun `network file has correct defaults`() {
        val source = NetworkSource.Smb("test", "localhost", "share")
        val file = NetworkFile("video.mp4", "/share/video.mp4", false, 1024, source = source)
        assertThat(file.name).isEqualTo("video.mp4")
        assertThat(file.isDirectory).isFalse()
        assertThat(file.size).isEqualTo(1024)
    }

    @Test
    fun `network directory has isDirectory true`() {
        val source = NetworkSource.Ftp("test", "localhost")
        val dir = NetworkFile("videos", "/videos", true, source = source)
        assertThat(dir.isDirectory).isTrue()
    }

    @Test
    fun `network source smb has correct defaults`() {
        val smb = NetworkSource.Smb("home", "192.168.1.100", "media")
        assertThat(smb.name).isEqualTo("home")
        assertThat(smb.host).isEqualTo("192.168.1.100")
        assertThat(smb.share).isEqualTo("media")
        assertThat(smb.username).isEqualTo("guest")
    }

    @Test
    fun `network source ftp has correct defaults`() {
        val ftp = NetworkSource.Ftp("server", "10.0.0.1")
        assertThat(ftp.port).isEqualTo(21)
        assertThat(ftp.username).isEqualTo("anonymous")
    }

    @Test
    fun `network source webdav has correct defaults`() {
        val dav = NetworkSource.WebDav("cloud", "dav.example.com", "https://dav.example.com")
        assertThat(dav.path).isEqualTo("")
        assertThat(dav.username).isEqualTo("")
    }

    @Test
    fun `network source dlna has correct fields`() {
        val dlna = NetworkSource.Dlna("TV", "192.168.1.50", "uuid:1234", "http://192.168.1.50/desc.xml")
        assertThat(dlna.udn).isEqualTo("uuid:1234")
        assertThat(dlna.locationUrl).isEqualTo("http://192.168.1.50/desc.xml")
    }

    @Test
    fun `network result has loading state`() {
        val result = NetworkResult.Loading
        assertThat(result).isEqualTo(NetworkResult.Loading)
    }
}
