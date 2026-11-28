@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.data.importantmessages

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
data class PublicImportantMessage(
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
