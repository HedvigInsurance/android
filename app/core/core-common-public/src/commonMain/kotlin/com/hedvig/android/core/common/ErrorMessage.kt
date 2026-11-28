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

/**
 * An [ErrorMessage] whose [message] the backend wrote for the member to read, such as a `userError.message`, so an app
 * can show it as it is. The [message] of any other [ErrorMessage] can be a debug string. A shared read for iOS raises
 * this to have the iOS app show the text.
 */
class MemberFacingErrorMessage(override val message: String) : ErrorMessage {
  override val throwable: Throwable? get() = null

  override fun toString(): String = "MemberFacingErrorMessage(message=$message)"
}
