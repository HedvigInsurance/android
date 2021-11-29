package com.hedvig.app.util

sealed class LCE<out T> {
    object Loading : LCE<Nothing>()
    data class Content<out T>(val data: T) : LCE<T>()
    object Error : LCE<Nothing>()
}
