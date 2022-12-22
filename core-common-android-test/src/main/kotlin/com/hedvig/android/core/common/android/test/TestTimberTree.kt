package com.hedvig.android.core.common.android.test

import timber.log.Timber

@Suppress("unused") // Used temporarily in tests to more easily reason about what's happening.
class TestTimberTree : Timber.Tree() {
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    println("${priority.toReadableText()} [$tag]: $message")
  }
}

private fun Int.toReadableText(): String {
  return when (this) {
    2 -> "VERBOSE"
    3 -> "DEBUG"
    4 -> "INFO"
    5 -> "WARN"
    6 -> "ERROR"
    7 -> "ASSERT"
    else -> "Unknown Priority"
  }
}
