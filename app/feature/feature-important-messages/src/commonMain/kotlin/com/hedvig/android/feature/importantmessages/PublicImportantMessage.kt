package com.hedvig.android.feature.importantmessages

internal data class PublicImportantMessage(
  val id: String,
  val message: String,
  val severity: Severity,
  val link: Link?,
) {
  enum class Severity {
    Info,
    Warning,
    Error,
  }

  data class Link(val url: String, val buttonText: String)
}
