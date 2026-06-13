package com.pixelvibe.vedioplayer.core.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

enum class AppLockState {
    UNINITIALIZED, LOCKED, UNLOCKED
}

open class AppLockManager(private val context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "app_lock_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _state = MutableStateFlow(AppLockState.UNINITIALIZED)
    val state: StateFlow<AppLockState> = _state.asStateFlow()

    val isEnabled: Boolean get() = prefs.contains("pin_hash")

    fun isPinSet(): Boolean = prefs.contains("pin_hash")

    open fun setPin(pin: String) {
        prefs.edit().putString("pin_hash", hashPin(pin)).apply()
        _state.value = AppLockState.UNLOCKED
    }

    open fun verifyPin(pin: String): Boolean {
        if (!prefs.contains("pin_salt")) return false
        val stored = prefs.getString("pin_hash", null) ?: return false
        val valid = stored == hashPin(pin)
        if (valid) _state.value = AppLockState.UNLOCKED
        return valid
    }

    open fun lock() {
        _state.value = AppLockState.LOCKED
    }

    open fun unlock() {
        _state.value = AppLockState.UNLOCKED
    }

    open fun disable() {
        prefs.edit().remove("pin_hash").apply()
        _state.value = AppLockState.UNINITIALIZED
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        if (!verifyPin(oldPin)) return false
        setPin(newPin)
        return true
    }

    private fun hashPin(pin: String): String {
        val salt = prefs.getString("pin_salt", null)?.let {
            Base64.decode(it, Base64.DEFAULT)
        } ?: ByteArray(16).also { SecureRandom().nextBytes(it) }
        val spec = PBEKeySpec(pin.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        if (!prefs.contains("pin_salt")) {
            prefs.edit().putString("pin_salt", Base64.encodeToString(salt, Base64.DEFAULT)).apply()
        }
        return Base64.encodeToString(hash, Base64.DEFAULT)
    }
}
