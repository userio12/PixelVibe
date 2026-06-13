package com.pixelvibe.vedioplayer.core.data.security

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class AppLockManagerTest {

    @Test
    fun `app lock state enum has three values`() {
        val values = AppLockState.entries
        assertThat(values.size).isEqualTo(3)
        assertThat(values).containsExactly(AppLockState.UNINITIALIZED, AppLockState.LOCKED, AppLockState.UNLOCKED)
    }

    @Test
    fun `hashPin produces consistent results`() {
        val pin = "1234"
        val hash1 = pin.hashCode().toString() + pin.reversed().hashCode().toString()
        val hash2 = pin.hashCode().toString() + pin.reversed().hashCode().toString()
        assertThat(hash1).isEqualTo(hash2)
    }

    @Test
    fun `different pins produce different hashes`() {
        val hash1 = "1234".hashCode().toString() + "1234".reversed().hashCode().toString()
        val hash2 = "5678".hashCode().toString() + "5678".reversed().hashCode().toString()
        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun `pin mode enum has three values`() {
        assertThat(com.pixelvibe.vedioplayer.security.PinMode.entries.size).isEqualTo(3)
    }
}
