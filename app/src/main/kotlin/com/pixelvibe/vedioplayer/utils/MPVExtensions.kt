package com.pixelvibe.vedioplayer.utils

import is.xyz.mpv.MPVLib
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Flow-based property observers for MPVLib.
 * Extends the static MPVLib methods with Kotlin Flow support.
 */

/**
 * Observe an MPV boolean property as a Flow.
 */
fun MPVLib.Companion.propBoolean(property: String, initial: Boolean = false): Flow<Boolean> = callbackFlow {
    val observer = object : MPVLib.EventObserver {
        override fun eventProperty(prop: String, value: Boolean) {
            if (prop == property) trySend(value)
        }
        override fun eventProperty(property: String) {}
        override fun eventProperty(property: String, value: Long) {}
        override fun eventProperty(property: String, value: Double) {}
        override fun eventProperty(property: String, value: String) {}
        override fun event(event: MPVLib.Event) {}
    }
    MPVLib.addObserver(observer)
    trySend(initial)
    awaitClose { MPVLib.removeObserver(observer) }
}

/**
 * Observe an MPV integer property as a Flow.
 */
fun MPVLib.Companion.propInt(property: String, initial: Int = 0): Flow<Int> = callbackFlow {
    val observer = object : MPVLib.EventObserver {
        override fun eventProperty(prop: String, value: Long) {
            if (prop == property) trySend(value.toInt())
        }
        override fun eventProperty(property: String) {}
        override fun eventProperty(property: String, value: Boolean) {}
        override fun eventProperty(property: String, value: Double) {}
        override fun eventProperty(property: String, value: String) {}
        override fun event(event: MPVLib.Event) {}
    }
    MPVLib.addObserver(observer)
    trySend(initial)
    awaitClose { MPVLib.removeObserver(observer) }
}

/**
 * Observe an MPV double/float property as a Flow.
 */
fun MPVLib.Companion.propFloat(property: String, initial: Float = 0f): Flow<Float> = callbackFlow {
    val observer = object : MPVLib.EventObserver {
        override fun eventProperty(prop: String, value: Double) {
            if (prop == property) trySend(value.toFloat())
        }
        override fun eventProperty(property: String) {}
        override fun eventProperty(property: String, value: Boolean) {}
        override fun eventProperty(property: String, value: Long) {}
        override fun eventProperty(property: String, value: String) {}
        override fun event(event: MPVLib.Event) {}
    }
    MPVLib.addObserver(observer)
    trySend(initial)
    awaitClose { MPVLib.removeObserver(observer) }
}

/**
 * Observe an MPV string property as a Flow.
 */
fun MPVLib.Companion.propString(property: String): Flow<String> = callbackFlow {
    val observer = object : MPVLib.EventObserver {
        override fun eventProperty(prop: String, value: String) {
            if (prop == property) trySend(value)
        }
        override fun eventProperty(property: String) {}
        override fun eventProperty(property: String, value: Boolean) {}
        override fun eventProperty(property: String, value: Long) {}
        override fun eventProperty(property: String, value: Double) {}
        override fun event(event: MPVLib.Event) {}
    }
    MPVLib.addObserver(observer)
    awaitClose { MPVLib.removeObserver(observer) }
}
