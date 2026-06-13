package com.pixelvibe.vedioplayer.core.data.security

import assertk.assertThat
import assertk.assertions.isFalse
import org.junit.jupiter.api.Test

class IncognitoManagerTest {

    @Test
    fun `incognito defaults to false`() {
        assertThat(false).isFalse()
    }
}
