package com.hedvig.android.core.demomode

fun interface Provider<T> {
  suspend fun provide(): T
}
