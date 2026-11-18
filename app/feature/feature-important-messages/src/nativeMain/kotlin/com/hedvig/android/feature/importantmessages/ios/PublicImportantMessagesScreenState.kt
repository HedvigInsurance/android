package com.hedvig.android.feature.importantmessages.ios

class PublicImportantMessagesScreenState(val messages: List<PublicImportantMessageItem>)

class PublicImportantMessageItem(
  val id: String,
  val message: String,
  val severity: PublicImportantMessageSeverity,
  val linkUrl: String?,
  val linkButtonText: String?,
)

enum class PublicImportantMessageSeverity {
  INFO,
  WARNING,
  ERROR,
  ;

  /** Calls the closure for this entry, so a Swift call site stops compiling when an entry is added. */
  fun handle(onInfo: () -> Unit, onWarning: () -> Unit, onError: () -> Unit) {
    when (this) {
      INFO -> onInfo()
      WARNING -> onWarning()
      ERROR -> onError()
    }
  }
}
