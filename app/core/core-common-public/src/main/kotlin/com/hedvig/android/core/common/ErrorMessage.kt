package com.hedvig.android.core.common

interface ErrorMessage {
  val message: String?
  val throwable: Throwable?
}

fun ErrorMessage(message: String? = null, throwable: Throwable? = null): ErrorMessage = object : ErrorMessage {
  override val message = message
  override val throwable = throwable

  override fun toString(): String {
    return "ErrorMessage(message=$message, throwable=$throwable)"
  }
}
