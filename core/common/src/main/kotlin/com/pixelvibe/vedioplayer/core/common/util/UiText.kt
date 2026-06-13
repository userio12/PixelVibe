package com.pixelvibe.vedioplayer.core.common.util

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(val id: Int, val args: Array<Any> = emptyArray()) : UiText
}
