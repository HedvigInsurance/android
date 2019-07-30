package com.hedvig.app.util

sealed class Optional<out T> {
    class Some<out T>(val element: T) : Optional<T>()
    object None : Optional<Nothing>()

    fun getOrNull(): T? {
        return when (this) {
            is Some -> this.element
            is None -> null
        }
    }
}
