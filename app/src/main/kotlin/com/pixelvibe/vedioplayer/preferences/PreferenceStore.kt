package com.pixelvibe.vedioplayer.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.reflect.KProperty

class PreferenceStore(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "pixelvibe_preferences",
        Context.MODE_PRIVATE
    )

    private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    fun <T> preference(
        key: String,
        defaultValue: T,
        serializer: (T) -> String? = { it?.toString() },
        deserializer: (String?) -> T = { defaultValue }
    ): Preference<T> {
        return Preference(
            key = key,
            defaultValue = defaultValue,
            sharedPreferences = sharedPreferences,
            serializer = serializer,
            deserializer = deserializer
        )
    }

    fun <T> Flow<T>.asPreference(preference: Preference<T>): Preference<T> {
        return preference
    }

    operator fun plusAssign(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        listeners.add(listener)
    }

    operator fun minusAssign(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        listeners.remove(listener)
    }
}

class Preference<T>(
    private val key: String,
    private val defaultValue: T,
    private val sharedPreferences: SharedPreferences,
    private val serializer: (T) -> String?,
    private val deserializer: (String?) -> T
) {
    @Suppress("UNCHECKED_CAST")
    fun get(): T {
        return when (defaultValue) {
            is String -> sharedPreferences.getString(key, defaultValue) as T
            is Int -> sharedPreferences.getInt(key, defaultValue) as T
            is Long -> sharedPreferences.getLong(key, defaultValue) as T
            is Float -> sharedPreferences.getFloat(key, defaultValue) as T
            is Boolean -> sharedPreferences.getBoolean(key, defaultValue) as T
            else -> deserializer(sharedPreferences.getString(key, null))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun set(value: T) {
        sharedPreferences.edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Boolean -> putBoolean(key, value)
                else -> serializer(value)?.let { putString(key, it) }
            }
        }
    }

    fun changes(): Flow<T> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                trySend(get())
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(get())
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Preference<*>) return false
        return key == other.key
    }

    override fun hashCode(): Int = key.hashCode()
}

// Typed convenience functions
fun PreferenceStore.stringPreference(key: String, defaultValue: String = ""): Preference<String> =
    preference(key, defaultValue)

fun PreferenceStore.intPreference(key: String, defaultValue: Int = 0): Preference<Int> =
    preference(key, defaultValue)

fun PreferenceStore.longPreference(key: String, defaultValue: Long = 0L): Preference<Long> =
    preference(key, defaultValue)

fun PreferenceStore.floatPreference(key: String, defaultValue: Float = 0f): Preference<Float> =
    preference(key, defaultValue)

fun PreferenceStore.booleanPreference(key: String, defaultValue: Boolean = false): Preference<Boolean> =
    preference(key, defaultValue)
