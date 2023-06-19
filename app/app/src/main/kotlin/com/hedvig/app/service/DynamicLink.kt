package com.hedvig.app.service

sealed class DynamicLink(
  val type: String,
) {
  object DirectDebit : DynamicLink("direct-debit")

  object None : DynamicLink("none")
  object Unknown : DynamicLink("unknown")
}
