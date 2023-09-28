package com.hedvig.android.feature.demomode

fun interface Provider<T> {
  fun provide(): T
}
